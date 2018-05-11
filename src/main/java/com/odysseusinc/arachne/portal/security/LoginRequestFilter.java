package com.odysseusinc.arachne.portal.security;

import java.io.IOException;
import java.util.Objects;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

public class LoginRequestFilter extends OncePerRequestFilter {

    private static final String USER_REQ_PARAM = "user-req";

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {

        String username = httpServletRequest.getParameter(USER_REQ_PARAM);
        if (Objects.nonNull(username)) {
            LoginRequestContext.setUserName(username);
        }
        filterChain.doFilter(httpServletRequest, httpServletResponse);
        LoginRequestContext.clear();
    }
}
