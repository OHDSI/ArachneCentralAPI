package com.odysseusinc.arachne.portal.service;

import com.odysseusinc.arachne.portal.model.ExternalLogin;
import com.odysseusinc.arachne.portal.model.IUser;
import com.odysseusinc.arachne.portal.model.RawUser;
import java.time.Instant;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class ExternalLoginService {
    private static final Logger log = LoggerFactory.getLogger(ExternalLoginService.class);

    /**
     * Specifies whether newly created external users can be linked to existing local users.
     */
    @Value("${user.external.autolink:false}")
    private boolean autolink;

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public <U extends IUser> ExternalLogin login(String provider, String sub, String email, Supplier<U> createUser) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ExternalLogin> q = cb.createQuery(ExternalLogin.class);
        Root<ExternalLogin> root = q.from(ExternalLogin.class);
        Optional<ExternalLogin> existingLogin = em.createQuery(
                q.where(
                        cb.equal(root.get("provider"), provider),
                        cb.equal(root.get("sub"), sub)
                )
        ).getResultStream().findFirst();

        ExternalLogin externalLogin = existingLogin.orElseGet(() -> {
            ExternalLogin login = new ExternalLogin();
            login.setCreated(Instant.now());
            login.setProvider(provider);
            login.setSub(sub);
            login.setEmail(email);
            em.persist(login);
            return login;
        });

        if (externalLogin.getUser() == null) {
            externalLogin.setUser(linkOrCreateUser(sub, email, createUser));
        }
        return externalLogin;
    }

    private <U extends IUser> IUser linkOrCreateUser(String sub, String email, Supplier<U> importUser) {
        return Optional.ofNullable(email).filter(__ -> autolink).<IUser>flatMap(address -> {
            log.info("Autolink [{}] via [{}]", sub, address);
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<RawUser> q = cb.createQuery(RawUser.class);
            Root<RawUser> root = q.from(RawUser.class);
            return em.createQuery(
                    q.where(
                            cb.equal(root.get("email"), address),
                            cb.equal(root.get("enabled"), true)
                    )
            ).getResultStream().findFirst().map(Function.identity());
        }).orElseGet(importUser);
    }


}
