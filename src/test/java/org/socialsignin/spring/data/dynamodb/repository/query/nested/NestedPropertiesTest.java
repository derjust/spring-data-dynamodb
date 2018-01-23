package org.socialsignin.spring.data.dynamodb.repository.query.nested;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories;
import org.socialsignin.spring.data.dynamodb.utils.DynamoDBLocalResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {DynamoDBLocalResource.class, NestedPropertiesTest.TestAppConfig.class})
public class NestedPropertiesTest {

    @Configuration
    @EnableDynamoDBRepositories(basePackages = "org.socialsignin.spring.data.dynamodb.query.nested")
    public static class TestAppConfig {
    }

    @Autowired
    private PersonRepository personRepository;

    @Test
    public void testNestedProperty() {

        Address usaAddress = new Address();
        usaAddress.setCity("New York");
        usaAddress.setCountry("USA");

        Address deAddress = new Address();
        deAddress.setCity("Frankfurt");
        deAddress.setCity("Germany");

        Person p1 = new Person();
        p1.setName("personName");
        p1.setPhone("phone");
        p1.setArea("area");
        p1.setAddress(usaAddress);

        Person p2 = new Person();
        p2.setName("otherName");
        p2.setPhone("42");
        p2.setArea("otherArea");
        p2.setAddress(deAddress);

///        personRepository.save(p1);

        List<Person> actual = personRepository.findByAddressCountry("USA");
        assertEquals(1, actual.size());

        actual = personRepository.findByPhone("42");
        assertEquals(1, actual.size());
    }
}
