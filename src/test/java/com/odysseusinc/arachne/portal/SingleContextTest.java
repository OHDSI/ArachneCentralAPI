package com.odysseusinc.arachne.portal;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import com.odysseusinc.arachne.portal.config.tenancy.TenantContext;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

@SuppressWarnings(value = "unchecked")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application.properties")
@SpringBootTest(classes = PortalStarter.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        DbUnitTestExecutionListener.class,
        WithSecurityContextTestExecutionListener.class})
@DbUnitConfiguration(databaseConnection = {"primaryDataSource"})
public abstract class SingleContextTest {

    public SingleContextTest() {

        TenantContext.setCurrentTenant(1L);
    }
}
