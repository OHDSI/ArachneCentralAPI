package com.odysseusinc.arachne.portal.service;

import com.odysseusinc.arachne.portal.model.ExternalLogin;
import com.odysseusinc.arachne.portal.model.IUser;
import com.odysseusinc.arachne.portal.repository.UserRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.function.Supplier;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private UserRepository userRepo;

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

        return existingLogin.orElseGet(() -> {
            ExternalLogin login = new ExternalLogin();
            login.setCreated(Instant.now());
            login.setProvider(provider);
            login.setSub(sub);
            login.setEmail(email);
            login.setUser(linkOrCreateUser(sub, email, createUser));
            em.persist(login);
            return login;
        });
    }

    private <U extends IUser> IUser linkOrCreateUser(String sub, String email, Supplier<U> importUser) {
        if (autolink) {
            if (email == null) {
                log.info("Autolink on [{}] skipped, missing email", sub);
            } else {
                log.info("Autolink [{}] via [{}]", sub, email);
                IUser user = userRepo.findByEmailAndEnabledTrue(email);
                log.info("Autolink [{}] result: [{}]", sub, user);
                return user;
            }
        }
        U user = importUser.get();
        log.info("For remote login [{}], created account [{}]", sub, user.getId());
        return user;
    }


}
