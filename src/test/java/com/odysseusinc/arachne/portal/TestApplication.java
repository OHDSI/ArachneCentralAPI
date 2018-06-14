/*
 *
 * Copyright 2018 Observational Health Data Sciences and Informatics
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Company: Odysseus Data Services, Inc.
 * Product Owner/Architecture: Gregory Klebanov
 * Authors: Pavel Grafkin, Alexandr Ryabokon, Vitaly Koulakov, Anton Gackovka, Maria Pozhidaeva, Mikhail Mironov
 * Created: May 25, 2017
 *
 */

package com.odysseusinc.arachne.portal;

import com.cosium.spring.data.jpa.entity.graph.repository.support.EntityGraphJpaRepositoryFactoryBean;
import com.odysseusinc.arachne.portal.config.WebSecurityConfig;
import com.odysseusinc.arachne.portal.exception.SecurityConfigException;
import com.odysseusinc.arachne.portal.security.AuthenticationTokenFilter;
import com.odysseusinc.arachne.portal.security.DataNodeAuthenticationProvider;
import com.odysseusinc.arachne.portal.security.EntryPointUnauthorizedHandler;
import com.odysseusinc.arachne.portal.security.Roles;
import edu.vt.middleware.password.LengthRule;
import edu.vt.middleware.password.MessageResolver;
import edu.vt.middleware.password.PasswordValidator;
import edu.vt.middleware.password.QwertySequenceRule;
import edu.vt.middleware.password.RepeatCharacterRegexRule;
import edu.vt.middleware.password.Rule;
import edu.vt.middleware.password.WhitespaceRule;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@SpringBootApplication
@ComponentScan(basePackages = {"com.odysseusinc.arachne.portal", "com.odysseusinc.arachne.storage"},
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = PortalStarter.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = WebSecurityConfig.class)
        })
@EnableJpaRepositories(
        repositoryFactoryBeanClass = EntityGraphJpaRepositoryFactoryBean.class,
        basePackages = {"com.odysseusinc.arachne.*"}
)
@EntityScan(basePackages = {"com.odysseusinc.arachne.*"})
@EnableAspectJAutoProxy
public class TestApplication {

    @TestConfiguration
    static class Config {
        @Bean
        public PasswordValidator passwordValidator() throws IOException {

            LengthRule lengthRule = new LengthRule(8, 16);
            WhitespaceRule whitespaceRule = new WhitespaceRule();
            QwertySequenceRule qwertySeqRule = new QwertySequenceRule();
            RepeatCharacterRegexRule repeatRule = new RepeatCharacterRegexRule(4);
            List<Rule> ruleList = new ArrayList<>();
            ruleList.add(lengthRule);
            ruleList.add(whitespaceRule);
            ruleList.add(qwertySeqRule);
            ruleList.add(repeatRule);
            Properties props = new Properties();

            props.load(new ClassPathResource("password_messages.properties").getInputStream());
            MessageResolver resolver = new MessageResolver(props);
            return new PasswordValidator(resolver, ruleList);
        }
    }

    @Configuration
    @Order(111)
    public static class WebSecurityConfig extends WebSecurityConfigurerAdapter {

        @Autowired
        private EntryPointUnauthorizedHandler unauthorizedHandler;
        @Autowired
        private UserDetailsService userDetailsService;

        @Override
        protected void configure(AuthenticationManagerBuilder builder) throws Exception {

            builder.authenticationProvider(dataNodeAuthenticationProvider());
        }

        @Bean
        public DataNodeAuthenticationProvider dataNodeAuthenticationProvider() {

            return new DataNodeAuthenticationProvider();
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {

            http
                    .csrf()
                    .disable()
                    .exceptionHandling()
                    .authenticationEntryPoint(this.unauthorizedHandler)
                    .and()
                    .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                    .and()
                    .authorizeRequests()
                    .antMatchers(HttpMethod.OPTIONS, "*//**").permitAll()
                    .antMatchers("/api/v1/auth/logout**").permitAll()
                    .antMatchers("/api/v1/auth/login**").permitAll()
                    .antMatchers("/api/v1/auth/logout**").permitAll()
                    .antMatchers("/api/v1/auth/login*//**").permitAll()
                    .antMatchers("/api/v1/auth/registration*//**").permitAll()
                    .antMatchers("/api/v1/auth/resend-activation-email*//**").permitAll()
                    .antMatchers("/api/v1/auth/remind-password**").permitAll()
                    .antMatchers("/api/v1/auth/reset-password**").permitAll()
                    .antMatchers("/api/v1/auth/method**").permitAll()
                    .antMatchers("/api/v1/user-management/activation*//**").permitAll()
                    .antMatchers("/api/v1/user-management/datanodes*//**").hasRole(Roles.ROLE_DATA_NODE)
                    .antMatchers("/api/v1/user-management/professional-types**").permitAll()
                    .antMatchers("/api/v1/user-management/users/changepassword").authenticated()
                    .antMatchers("/api/v1/user-management/users*//**").permitAll()
                    .antMatchers("/api/v1/user-management/users/avatar").hasRole(Roles.ROLE_USER)
                    .antMatchers("/api/v1/build-number*//**").permitAll()
                    .antMatchers("/api/v1/auth/status*//*").permitAll()
                    .antMatchers("/api/v1/data-nodes*//**//*check-health*//**").permitAll()
                    .antMatchers("/api/v1/analysis-management/submissions*//**//*status*//**").permitAll()
                    .antMatchers("/api/v1/analysis-management/submissions*//**//*files**").permitAll()
                    .antMatchers("/api/v1/analysis-management/submissions/result/upload**").permitAll()
                    .antMatchers("/api/v1/user-management/users/invitations/mail**").permitAll()
                    .antMatchers("/api/v1/achilles/datanode/datasource/**").permitAll()
                    .antMatchers("/api**").authenticated()
                    .antMatchers("/api*//**").authenticated()
                    .anyRequest().permitAll();
            http
                    .addFilterBefore(authenticationTokenFilterBean(), UsernamePasswordAuthenticationFilter.class);

        }

        @Bean
        public AuthenticationTokenFilter authenticationTokenFilterBean() throws Exception {

            AuthenticationTokenFilter authenticationTokenFilter = new AuthenticationTokenFilter();
            authenticationTokenFilter.setAuthenticationManager(authenticationManagerBean());
            return authenticationTokenFilter;
        }

        @Autowired
        public void configureAuthentication(
                AuthenticationManagerBuilder authenticationManagerBuilder
        ) throws SecurityConfigException {

            try {
                authenticationManagerBuilder
                        .authenticationProvider(dataNodeAuthenticationProvider())
                        .userDetailsService(userDetailsService)
                        .passwordEncoder(passwordEncoder());
            } catch (Exception ex) {
                throw new SecurityConfigException(ex.getMessage());
            }
        }

        @Bean
        public PasswordEncoder passwordEncoder() {

            return new BCryptPasswordEncoder();
        }

    }
}
