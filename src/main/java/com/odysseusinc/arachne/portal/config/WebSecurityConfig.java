/*
 *
 * Copyright 2017 Observational Health Data Sciences and Informatics
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
 * Created: October 11, 2016
 *
 */

package com.odysseusinc.arachne.portal.config;

import com.odysseusinc.arachne.portal.exception.SecurityConfigException;
import com.odysseusinc.arachne.portal.security.AuthenticationSystemTokenFilter;
import com.odysseusinc.arachne.portal.security.AuthenticationTokenFilter;
import com.odysseusinc.arachne.portal.security.DataNodeAuthenticationProvider;
import com.odysseusinc.arachne.portal.security.EntryPointUnauthorizedHandler;
import com.odysseusinc.arachne.portal.security.HostNameIsNotInServiceException;
import com.odysseusinc.arachne.portal.security.Roles;
import com.odysseusinc.arachne.portal.service.BaseDataNodeService;
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
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private final static Logger log = LoggerFactory.getLogger(WebSecurityConfig.class);
    public static final ThreadLocal<String> portalHost = new ThreadLocal<>();

    @Value("#{'${portal.hostsWhiteList}'.split(',')}")
    private List<String> portalHostWhiteList;

    @Autowired
    protected BaseDataNodeService baseDataNodeService;

    @Autowired
    protected EntryPointUnauthorizedHandler unauthorizedHandler;

    @Autowired
    protected UserDetailsService userDetailsService;

    @Autowired
    public void configureAuthentication(
            AuthenticationManagerBuilder authenticationManagerBuilder
    ) throws SecurityConfigException {

        try {
            authenticationManagerBuilder
                    .authenticationProvider(dataNodeAuthenticationProvider())
                    .userDetailsService(this.userDetailsService)
                    .passwordEncoder(passwordEncoder());
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            throw new SecurityConfigException(ex.getMessage());
        }
    }

    @Autowired
    private HostFilter hostfilter;

    @Bean
    public DataNodeAuthenticationProvider dataNodeAuthenticationProvider() {

        return new DataNodeAuthenticationProvider();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }

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

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {

        return super.authenticationManagerBean();
    }


    @Bean
    public AuthenticationTokenFilter authenticationTokenFilterBean() throws Exception {

        AuthenticationTokenFilter authenticationTokenFilter = new AuthenticationTokenFilter();
        authenticationTokenFilter.setAuthenticationManager(authenticationManagerBean());
        return authenticationTokenFilter;
    }

    @Bean
    public AuthenticationSystemTokenFilter authenticationSystemTokenFilter() {

        return new AuthenticationSystemTokenFilter(baseDataNodeService);
    }

    @Bean
    public HostFilter hostFilter() {

        return new HostFilter(portalHostWhiteList);
    }

    public static class HostFilter extends OncePerRequestFilter {

        protected List<String> portalHostWhiteList;

        public HostFilter(List<String> portalHostWhiteList) {

            this.portalHostWhiteList = portalHostWhiteList;
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

            final String host = request.getHeader("Host");
            if (!portalHostWhiteList.contains(host.split(":")[0])) {
                throw new HostNameIsNotInServiceException(host);
            }
            portalHost.set("http://" + host);
            filterChain.doFilter(request, response);
            // portalHost.remove();
        }
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
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .antMatchers("/api/v1/auth/logout**").permitAll()
                .antMatchers("/api/v1/auth/login**").permitAll()
                .antMatchers("/api/v1/auth/logout**").permitAll()
                .antMatchers("/api/v1/auth/login/**").permitAll()
                .antMatchers("/api/v1/auth/registration/**").permitAll()
                .antMatchers("/api/v1/auth/resend-activation-email/**").permitAll()
                .antMatchers("/api/v1/auth/remind-password**").permitAll()
                .antMatchers("/api/v1/auth/reset-password**").permitAll()
                .antMatchers("/api/v1/auth/method**").permitAll()
                .antMatchers("/api/v1/user-management/activation/**").permitAll()
                .antMatchers("/api/v1/user-management/datanodes/**").hasRole(Roles.ROLE_DATA_NODE)
                .antMatchers("/api/v1/user-management/professional-types**").permitAll()
                .antMatchers("/api/v1/user-management/users/changepassword").authenticated()
                .antMatchers("/api/v1/user-management/users/**").permitAll()
                .antMatchers("/api/v1/user-management/users/avatar").hasRole(Roles.ROLE_USER)
                .antMatchers("/api/v1/build-number/**").permitAll()
                .antMatchers("/api/v1/auth/status/*").permitAll()
                .antMatchers("/api/v1/data-nodes/**/check-health/**").hasRole(Roles.ROLE_DATA_NODE)
                .antMatchers("/api/v1/analysis-management/submissions/**/status/**").permitAll()
                .antMatchers("/api/v1/user-management/users/invitations/mail**").permitAll()
                .antMatchers("/api/v1/achilles/datanode/datasource/**").hasRole(Roles.ROLE_DATA_NODE)
                .antMatchers("/api/v1/data-nodes/submissions/**").hasRole(Roles.ROLE_DATA_NODE)
                .antMatchers("/api/v1/data-nodes/cohorts**").hasRole(Roles.ROLE_DATA_NODE)
                .antMatchers("/api/v1/data-sources/byuuid/**").hasRole(Roles.ROLE_DATA_NODE)

                .antMatchers("/api/v1/admin/users", "/api/v1/admin/users/**").hasRole("ADMIN")
                // Next 2 are used by Data node (authed by query param, manually)
                .antMatchers("/api/v1/analysis-management/submissions/**/files**").permitAll()
                .antMatchers("/api/v1/analysis-management/submissions/result/upload**").permitAll()
                .antMatchers("/insights-library/insights/**").permitAll()

                .antMatchers("/api**").authenticated()
                .antMatchers("/api/**").authenticated()
                .anyRequest().permitAll();

        // Custom JWT based authentication
        http
                .addFilterBefore(authenticationTokenFilterBean(), UsernamePasswordAuthenticationFilter.class);
        // DataNode authentication
        http.addFilterBefore(authenticationSystemTokenFilter(), AuthenticationTokenFilter.class);
        http.addFilterBefore(hostfilter, AuthenticationSystemTokenFilter.class);
    }

}
