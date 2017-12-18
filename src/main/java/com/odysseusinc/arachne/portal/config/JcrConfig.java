package com.odysseusinc.arachne.portal.config;

import com.odysseusinc.arachne.portal.config.interceptors.RequestInterceptor;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import org.apache.jackrabbit.commons.cnd.CndImporter;
import org.apache.jackrabbit.commons.cnd.ParseException;
import org.apache.jackrabbit.core.RepositoryImpl;
import org.apache.jackrabbit.core.config.RepositoryConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.springmodules.jcr.JcrTemplate;
import org.springmodules.jcr.jackrabbit.JackrabbitSessionFactory;
import org.xml.sax.InputSource;

@Configuration
public class JcrConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestInterceptor.class);

    private static final String REPOSITORY_CONFIG_FILE = "jackrabbit-repository.xml";
    private static final String CND_DIR = "jcr-cnd";
    private static final String REP_HOME_KEY = "rep.home";

    @Value("${files.store.jcr-path}")
    private String homeDir;

    @Bean
    public Repository repository(ConfigurableEnvironment environment) throws RepositoryException, IOException {

        ClassPathResource repositoryConfigResource = new ClassPathResource(REPOSITORY_CONFIG_FILE);
        Repository repository;

        checkRepoHomeExists();

        try (InputStream repositoryConfigStream = repositoryConfigResource.getInputStream()) {
            RepositoryConfig config = RepositoryConfig.create(new InputSource(repositoryConfigStream), new Properties() {

                public String getProperty(String key) {

                    return key.equals(REP_HOME_KEY) ? homeDir : environment.getProperty(key);
                }

            });
            repository = RepositoryImpl.create(config);
        }

        return repository;
    }

    @Bean
    public JackrabbitSessionFactory jackrabbitSessionFactory(Repository repository) {

        JackrabbitSessionFactory sessionFactory = new JackrabbitSessionFactory() {

            protected void registerNodeTypes() throws Exception {

                Session session = this.getSession();
                File cndDir = new ClassPathResource(CND_DIR).getFile();
                for (File cndConfFile : cndDir.listFiles()) {
                    try {
                        CndImporter.registerNodeTypes(new FileReader(cndConfFile), session, true);
                        session.logout();
                    } catch (ParseException e) {
                        LOGGER.error("Cannot register JCR node types from " + cndConfFile.getName());
                    }
                }
            }

        };
        sessionFactory.setRepository(repository);
        sessionFactory.setCredentials(new SimpleCredentials("superuser", "".toCharArray()));
        return sessionFactory;
    }

    @Bean
    public JcrTemplate jcrTemplate(JackrabbitSessionFactory sessionFactory) {

        JcrTemplate jcrTemplate = new JcrTemplate();
        jcrTemplate.setAllowCreate(true);
        jcrTemplate.setSessionFactory(sessionFactory);

        return jcrTemplate;
    }

    private void checkRepoHomeExists() {

        File jcrHomeDir = new File(this.homeDir);
        if (!jcrHomeDir.isDirectory()) {
            jcrHomeDir.mkdirs();
        }
    }
}