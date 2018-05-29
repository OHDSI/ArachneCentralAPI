package com.odysseusinc.arachne.portal.security;

import java.io.IOException;
import java.util.Objects;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

public class LoginRequestFilter extends OncePerRequestFilter {

    @Autowired
    private TokenUtils tokenUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {

        String token = httpServletRequest.getParameter("token");
        if (Objects.nonNull(token)) {

            String userName = tokenUtils.getUsernameFromToken(token);
            LoginRequestContext.setUserName(userName);
            if (Objects.nonNull(SecurityContextHolder.getContext().getAuthentication())) {
                //force logout
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication instanceof UsernamePasswordAuthenticationToken) {
                    Object detailsObject = authentication.getDetails();
                    if (detailsObject instanceof UserDetails) {
                        UserDetails userDetails = (UserDetails) detailsObject;
                        if (!Objects.equals(userName, userDetails.getUsername())) {
                            tokenUtils.addInvalidateToken(token);
                        } else {
                            LoginRequestContext.clear();
                        }
                    }
                }
            }
        }
        filterChain.doFilter(httpServletRequest, httpServletResponse);
        LoginRequestContext.clear();
    }
}
