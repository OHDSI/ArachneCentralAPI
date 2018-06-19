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
import com.odysseusinc.arachne.portal.security.LoginRequestFilter;
import com.odysseusinc.arachne.portal.security.Roles;
import com.odysseusinc.arachne.portal.security.passwordvalidator.ArachnePasswordValidator;
import com.odysseusinc.arachne.portal.security.passwordvalidator.PasswordValidatorBuilder;
import com.odysseusinc.arachne.portal.service.BaseDataNodeService;
import java.io.IOException;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private static final Logger log = LoggerFactory.getLogger(WebSecurityConfig.class);
    public static final ThreadLocal<String> portalUrl = new ThreadLocal<>();
    private static String defaultPortalURI;

    @Value("#{'${portal.urlWhiteList}'.toLowerCase().split(',')}")
    private List<String> portalUrlWhiteList;

    @Value("${arachne.passwordBlacklist}")
    private String[] passwordBlacklist;

    private static Map<String, URI> urls = new LinkedHashMap<>();

    public static String getDefaultPortalURI() {

        return defaultPortalURI;
    }

    @PostConstruct
    public void initialize() {

        final LinkedHashMap<String, URI> urlsLocal = urlToHostUrlMapConverter(portalUrlWhiteList);
        urls.putAll(urlsLocal);
        if (urls.keySet().isEmpty()) {
            throw new BeanInitializationException("At least one portalUrlWhiteList entry must be specified");
        }
        final String hostString = urls.keySet().stream().collect(Collectors.joining(","));
        log.info("host white list: {}", hostString);
        urls.values().stream().findFirst().ifPresent(url -> {
            final String urlString = url.toString();
            log.info("default Portal URL: {}", urlString);
            defaultPortalURI = urlString;
        });
    }

    public static LinkedHashMap<String, URI> urlToHostUrlMapConverter(List<String> portalUrlWhiteList) {

        final Pattern urlPattern = Pattern.compile("(https?://)([^:^/]*)(:\\\\d*)?(.*)?");
        final Predicate<String> urlFilterPredicate = s -> {
            final Matcher matcher = urlPattern.matcher(s);
            return matcher.matches();
        };
        return portalUrlWhiteList.stream()
                .filter(urlFilterPredicate)
                .map(URI::create)
                .collect(Collectors.toMap(URI::getHost, s -> s, (host1, host2) -> host1, LinkedHashMap::new));
    }

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
    public ArachnePasswordValidator passwordValidator() throws IOException {

        return PasswordValidatorBuilder.create()
                .withComplexRules()
                .withUppercaseCharacter(1)
                .withLowercaseCharacter(1)
                .withDigitCharacter(1)
                .withNonAlphanumericCharacter(1)
                .withNumberOfCharacteristics(3)
                .done()
                .withLength(10, 128)
                .withWhitespace()
                .withQuerty()
                .withUsername(true, true)
                .withUsersNames()
                .withBlacklist(passwordBlacklist)
                .withRepeatChars(3)
                .withAlphabeticalChars(2)
                .withIllegalChars(new char[]{'‘', '\"', '&', ' '})
                .withMessages(new ClassPathResource("password_messages.properties"))
                .build();
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
    public LoginRequestFilter loginRequestFilter() {

        return new LoginRequestFilter();
    }

    @Bean
    public HostFilter hostFilter() {

        return new HostFilter(urls);
    }

    public static class HostFilter extends OncePerRequestFilter {

        protected Map<String, URI> portalUrlWhiteList;

        @Value("${server.ssl.enabled}")
        private Boolean httpsEnabled;

        public HostFilter(Map<String, URI> portalUrlWhiteList) {

            this.portalUrlWhiteList = portalUrlWhiteList;
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

            final String host = request.getHeader("Host").split(":")[0];
            if (!portalUrlWhiteList.containsKey(StringUtils.lowerCase(host))) {
                throw new HostNameIsNotInServiceException(host);
            }
            portalUrl.set(portalUrlWhiteList.get(host).toString());
            filterChain.doFilter(request, response);
            // portalHost.remove();
        }
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry reg = http
                .csrf()
                .disable()
                .exceptionHandling()
                .authenticationEntryPoint(this.unauthorizedHandler)
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests();

        reg.antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .antMatchers("/api/v1/auth/logout**").permitAll()
                .antMatchers("/api/v1/auth/login**").permitAll()
                .antMatchers("/api/v1/auth/password-policies**").permitAll()
                .antMatchers("/api/v1/auth/logout**").permitAll()
                .antMatchers("/api/v1/auth/login/**").permitAll()
                .antMatchers("/api/v1/auth/registration/**").permitAll()
                .antMatchers("/api/v1/auth/resend-activation-email/**").permitAll()
                .antMatchers("/api/v1/auth/remind-password**").permitAll()
                .antMatchers("/api/v1/auth/reset-password**").permitAll()
                .antMatchers("/api/v1/auth/method**").permitAll()
                .antMatchers("/api/v1/user-management/activation/**").permitAll()
                .antMatchers("/api/v1/data-sources/dbms-types").permitAll()
                .antMatchers("/api/v1/user-management/datanodes/**").hasRole(Roles.ROLE_DATA_NODE)
                .antMatchers("/api/v1/user-management/professional-types**").permitAll()
                .antMatchers("/api/v1/user-management/users/changepassword").authenticated()
                .antMatchers("/api/v1/user-management/organizations/**").authenticated()
                .antMatchers("/api/v1/user-management/users/**").permitAll()
                .antMatchers("/api/v1/user-management/users/avatar").hasRole(Roles.ROLE_USER)
                .antMatchers("/api/v1/build-number**").permitAll()
                .antMatchers("/api/v1/auth/status/*").permitAll()
                .antMatchers("/api/v1/data-nodes/**/check-health/**").hasRole(Roles.ROLE_DATA_NODE)
                .antMatchers("/api/v1/data-nodes/manual").authenticated()
                .antMatchers("/api/v1/data-nodes").authenticated()
                .antMatchers("/api/v1/analysis-management/submissions/**/status/**").permitAll()
                .antMatchers("/api/v1/user-management/users/invitations/mail**").permitAll()
                .antMatchers("/api/v1/achilles/datanode/datasource/**").permitAll()
                .antMatchers("/api/v1/data-nodes/submissions/**").hasRole(Roles.ROLE_DATA_NODE)
                .antMatchers("/api/v1/data-nodes/cohorts**").hasRole(Roles.ROLE_DATA_NODE)
                .antMatchers("/api/v1/data-sources/byuuid/**").hasRole(Roles.ROLE_DATA_NODE)

                // Next 2 are used by Data node (authed by query param, manually)
                .antMatchers("/api/v1/analysis-management/submissions/**/files**").permitAll()
                .antMatchers("/api/v1/analysis-management/submissions/result/upload**").permitAll()
                .antMatchers("/insights-library/insights/**").permitAll();

        extendHttpSecurity(reg);
        reg.antMatchers("/api**").authenticated()
                .antMatchers("/api/**").authenticated()
                .anyRequest().permitAll();

        // Custom JWT based authentication
        http.addFilterBefore(loginRequestFilter(), UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(authenticationTokenFilterBean(), LoginRequestFilter.class);
        // DataNode authentication
        http.addFilterBefore(authenticationSystemTokenFilter(), AuthenticationTokenFilter.class);
        http.addFilterBefore(hostfilter, AuthenticationSystemTokenFilter.class);
    }

    protected void extendHttpSecurity(ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry  registry) {

    }

}
