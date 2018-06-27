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

package com.odysseusinc.arachne.portal.model.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collection;
import java.util.Date;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class ArachneUser implements UserDetails {

    private Long id;
    private Long activeTenantId;
    private String username;
    private String password;
    private String email;
    private Date lastPasswordReset;
    private Collection<? extends GrantedAuthority> authorities;
    private Boolean accountNonExpired = true;
    private Boolean accountNonLocked = true;
    private Boolean credentialsNonExpired = true;
    private Boolean enabled = true;

    public ArachneUser() {

        super();
    }

    public ArachneUser(Long id) {

        this.id = id;
    }

    public ArachneUser(Long id, Long activeTenantId, String username, String password, String email, Date lastPasswordReset,
                       Collection<? extends GrantedAuthority> authorities) {

        this.setId(id);
        this.setActiveTenantId(activeTenantId);
        this.setUsername(username);
        this.setPassword(password);
        this.setEmail(email);
        this.setLastPasswordReset(lastPasswordReset);
        this.setAuthorities(authorities);
    }

    public Long getId() {

        return this.id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    public Long getActiveTenantId() {

        return activeTenantId;
    }

    public void setActiveTenantId(Long activeTenantId) {

        this.activeTenantId = activeTenantId;
    }

    public String getUsername() {

        return this.username;
    }

    public void setUsername(String username) {

        this.username = username;
    }

    @JsonIgnore
    public String getPassword() {

        return this.password;
    }

    public void setPassword(String password) {

        this.password = password;
    }

    public String getEmail() {

        return this.email;
    }

    public void setEmail(String email) {

        this.email = email;
    }

    @JsonIgnore
    public Date getLastPasswordReset() {

        return this.lastPasswordReset;
    }

    public void setLastPasswordReset(Date lastPasswordReset) {

        this.lastPasswordReset = lastPasswordReset;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        return this.authorities;
    }

    public void setAuthorities(Collection<? extends GrantedAuthority> authorities) {

        this.authorities = authorities;
    }

    @JsonIgnore
    public Boolean getAccountNonExpired() {

        return this.accountNonExpired;
    }

    public void setAccountNonExpired(Boolean accountNonExpired) {

        this.accountNonExpired = accountNonExpired;
    }

    @Override
    public boolean isAccountNonExpired() {

        return this.getAccountNonExpired();
    }

    @JsonIgnore
    public Boolean getAccountNonLocked() {

        return this.accountNonLocked;
    }

    public void setAccountNonLocked(Boolean accountNonLocked) {

        this.accountNonLocked = accountNonLocked;
    }

    @Override
    public boolean isAccountNonLocked() {

        return this.getAccountNonLocked();
    }

    @JsonIgnore
    public Boolean getCredentialsNonExpired() {

        return this.credentialsNonExpired;
    }

    public void setCredentialsNonExpired(Boolean credentialsNonExpired) {

        this.credentialsNonExpired = credentialsNonExpired;
    }

    @Override
    public boolean isCredentialsNonExpired() {

        return this.getCredentialsNonExpired();
    }

    @JsonIgnore
    public Boolean getEnabled() {

        return this.enabled;
    }

    public void setEnabled(Boolean enabled) {

        this.enabled = enabled;
    }

    @Override
    public boolean isEnabled() {

        return this.getEnabled();
    }

}
