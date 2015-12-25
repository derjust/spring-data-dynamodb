package org.socialsignin.spring.data.dynamodb.repository.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.annotation.Annotation;

import org.junit.Before;
import org.junit.Test;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;

public class DynamoDBRepositoriesRegistrarTest {

    private DynamoDBRepositoriesRegistrar underTest;

    @Before
    public void setUp() {
        underTest = new DynamoDBRepositoriesRegistrar();
    }

    @Test
    public void testGetAnnotation() {
        Class<? extends Annotation> actual = underTest.getAnnotation();

        assertEquals(EnableDynamoDBRepositories.class, actual);
    }

    @Test
    public void testGetExtension() {
        RepositoryConfigurationExtension actual = underTest.getExtension();

        assertNotNull(actual);
        assertEquals(actual.getClass(), DynamoDBRepositoryConfigExtension.class);
    }
}
