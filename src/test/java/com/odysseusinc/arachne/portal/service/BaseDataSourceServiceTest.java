package com.odysseusinc.arachne.portal.service;

import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import com.odysseusinc.arachne.portal.SingleContextTest;
import com.odysseusinc.arachne.portal.TestContainersInitializer;
import com.odysseusinc.arachne.portal.model.security.ArachneUser;
import com.odysseusinc.arachne.portal.security.JWTAuthenticationToken;
import edu.emory.mathcs.backport.java.util.Arrays;
import java.util.Collection;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

@RunWith(Parameterized.class)
@DatabaseSetup("/data/users.xml")
@DatabaseSetup("/data/published-datanode-with-datasources.xml")
@DatabaseTearDown(value = "/data/empty.xml", type = DatabaseOperation.DELETE_ALL)
@ContextConfiguration(initializers = TestContainersInitializer.class)
public class BaseDataSourceServiceTest extends SingleContextTest {

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();
    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();
    @Autowired
    BaseDataSourceService baseDataSourceService;

    public BaseDataSourceServiceTest() {
        ArachneUser user = new ArachneUser();
        user.setActiveTenantId(1L);
        SecurityContextHolder.getContext().setAuthentication(new JWTAuthenticationToken("", user, null));
    }

    @Parameters
    public static Collection<Object[]> data() {

        return Arrays.asList(new Object[]{"P", "p"});
    }

    @Parameter
    public String checkingString;

    @Test
    public void testSuggestionDataSourceDifferentCase() {
        //Arrange
        Sort sort = Sort.by(Sort.Direction.ASC, "name");
        PageRequest pageRequest = PageRequest.of(0, 10, sort);
        //Action
        Page pageWithMatchedDS = baseDataSourceService.suggestDataSource(checkingString, 0L, 2L, pageRequest);
        //Assert
        Assert.assertEquals(1, pageWithMatchedDS.getTotalElements());
    }
}
