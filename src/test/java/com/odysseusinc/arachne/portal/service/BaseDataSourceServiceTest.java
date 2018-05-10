package com.odysseusinc.arachne.portal.service;

import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import com.odysseusinc.arachne.portal.SingleContextTest;
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
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

@RunWith(Parameterized.class)
@DatabaseTearDown(value = "/data/empty.xml", type = DatabaseOperation.DELETE_ALL)
public class BaseDataSourceServiceTest extends SingleContextTest {

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();
    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();
    @Autowired
    BaseDataSourceService baseDataSourceService;

    @Parameters
    public static Collection<Object[]> data() {

        return Arrays.asList(new Object[]{"P", "p"});
    }

    @Parameter
    public String checkingString;

    @Test
    @DatabaseSetup("/data/users.xml")
    @DatabaseSetup("/data/published-datanode-with-datasources.xml")
    public void testSuggestionDataSourceDifferentCase() {
        //Arrange
        Sort sort = new Sort(Sort.Direction.ASC, "name");
        PageRequest pageRequest = new PageRequest(0, 10, sort);
        //Action
        Page pageWithMatchedDS = baseDataSourceService.suggestDataSource(checkingString, 0L, 2L, pageRequest);
        //Assert
        Assert.assertEquals(1, pageWithMatchedDS.getTotalElements());
    }
}
