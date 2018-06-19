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
 * Created: October 19, 2016
 *
 */

package com.odysseusinc.arachne.portal.security;

import com.odysseusinc.arachne.portal.model.security.ArachneUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class TokenUtils {

    public static final String EX_CONCURRENT_LOGIN = "User %s token invalidated due to concurrent login";
    private final Logger log = Logger.getLogger(getClass());
    private final Object monitor = new Object();
    @Value("${arachne.token.header}")
    private String tokenHeader;
    @Value("${arachne.token.secret}")
    private String secret;
    @Value("${arachne.token.expiration}")
    private Long expiration;
    private ConcurrentHashMap<String, Date> invalidatedTokens = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String, String> usernameTokenMap = new ConcurrentHashMap<>();

    public List<String> getAuthToken(HttpServletRequest request) {

        List<String> tokens = new ArrayList<>();

        // Get token from header
        String headerToken = request.getHeader(tokenHeader);
        if (headerToken != null) {
            tokens.add(headerToken);
        }

        // Get token from cookie
        if (request.getCookies() != null) {
            Arrays.stream(request.getCookies())
                    .filter(cookie -> cookie.getName().equalsIgnoreCase(tokenHeader) && cookie.getValue() != null)
                    .findFirst()
                    .ifPresent(cookie -> tokens.add(cookie.getValue()));
        }

        return tokens;
    }

    public boolean isExpired(String token) {

        boolean expired;
        try {
            Claims claims = getClaimsFromToken(token);
            expired = claims.getExpiration().getTime() * 1000 < new Date().getTime();
        } catch (Exception ex) {
            log.debug(ex.getMessage(), ex);
            expired = true;
        }
        return expired;
    }


    public String getUsernameFromToken(String token) {

        String username;
        try {
            final Claims claims = getClaimsFromToken(token);
            username = claims.getSubject();
        } catch (Exception ex) {
            log.debug(ex.getMessage(), ex);
            username = null;
        }
        return username;
    }

    public String getUUIDFromToken(String token) {

        String uuid;
        try {
            final Claims claims = getClaimsFromToken(token);
            uuid = claims.get("uuid", String.class);
        } catch (Exception ex) {
            log.debug(ex.getMessage(), ex);
            uuid = null;
        }
        return uuid;
    }

    public Date getCreatedDateFromToken(String token) {

        Date created;
        try {
            final Claims claims = getClaimsFromToken(token);
            created = new Date((Long) claims.get("created"));
        } catch (Exception ex) {
            log.debug(ex.getMessage(), ex);
            created = null;
        }
        return created;
    }

    public Date getExpirationDateFromToken(String token) {

        Date expiration;
        try {
            final Claims claims = getClaimsFromToken(token);
            expiration = claims.getExpiration();
        } catch (Exception ex) {
            log.debug(ex.getMessage(), ex);
            expiration = null;
        }
        return expiration;
    }


    private Claims getClaimsFromToken(String token) {

        Claims claims;
        try {
            claims = Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception ex) {
            log.debug(ex.getMessage(), ex);
            claims = null;
        }
        return claims;
    }

    private Date generateCurrentDate() {

        return new Date(System.currentTimeMillis());
    }

    private Date generateExpirationDate() {

        return new Date(System.currentTimeMillis() + expiration * 1000);
    }

    private Boolean isTokenExpired(String token) {

        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(generateCurrentDate());
    }

    private Boolean isCreatedBeforeLastPasswordReset(Date created, Date lastPasswordReset) {

        return (lastPasswordReset != null && created.before(lastPasswordReset));
    }


    public String generateToken(String username) {

        Map<String, Object> claims = new HashMap<String, Object>();
        claims.put("sub", username);
        claims.put("created", generateCurrentDate());
        claims.put("uuid", UUID.randomUUID().toString());

        return checkTokenConcurrency(username, generateToken(claims));
    }

    private String checkTokenConcurrency(final String username, final String token) {

        synchronized (monitor) {
            String oldToken = usernameTokenMap.getOrDefault(username, null);
            if (!Objects.equals(token, oldToken) && (Objects.nonNull(oldToken) && !isExpired(oldToken))) {
                log.info(String.format(EX_CONCURRENT_LOGIN, username));
                addInvalidateToken(oldToken);
            }
            usernameTokenMap.put(username, token);
        }
        return token;
    }

    private String generateToken(Map<String, Object> claims) {

        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(generateExpirationDate())
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    public Boolean canTokenBeRefreshed(String token, Date lastPasswordReset) {

        final Date created = getCreatedDateFromToken(token);
        String uuid = getUUIDFromToken(token);
        return (!(invalidatedTokens.containsKey(uuid))
                && !(isCreatedBeforeLastPasswordReset(created, lastPasswordReset))
                && (!(isTokenExpired(token))));
    }

    public String refreshToken(String token) {

        String refreshedToken;
        try {
            final Claims claims = getClaimsFromToken(token);
            claims.put("created", generateCurrentDate());
            refreshedToken = generateToken(claims);
        } catch (Exception ex) {
            log.debug(ex.getMessage(), ex);
            refreshedToken = null;
        }
        return refreshedToken;
    }

    public Boolean validateToken(String token, UserDetails userDetails) {

        String uuid = getUUIDFromToken(token);
        boolean result = false;
        if (!invalidatedTokens.containsKey(uuid)) {
            ArachneUser user = (ArachneUser) userDetails;
            final String username = getUsernameFromToken(token);
            final Date created = getCreatedDateFromToken(token);
            result = (username.equals(user.getUsername())
                    && !(isTokenExpired(token))
                    && !(isCreatedBeforeLastPasswordReset(created, user.getLastPasswordReset())));
        }
        return result;
    }

    public void addInvalidateToken(String token) {

        String uuid = getUUIDFromToken(token);
        Date expirationDate = getExpirationDateFromToken(token);
        Date now = new Date();
        if ((expirationDate == null || expirationDate.after(now)) && uuid != null) {
            invalidatedTokens.put(uuid, expirationDate);
        }
        //remove old tokens
        for (Iterator<Map.Entry<String, Date>> iterator = invalidatedTokens.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<String, Date> stringDateEntry = iterator.next();
            if (stringDateEntry.getValue() != null && now.after(stringDateEntry.getValue())) {
                iterator.remove();
            }
        }
    }
}
