package org.socialsignin.spring.data.dynamodb.repository.cdi;

import static org.junit.Assert.fail;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Set;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.socialsignin.spring.data.dynamodb.core.DynamoDBOperations;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;

@RunWith(MockitoJUnitRunner.class)
public class DynamoDBRepositoryBeanTest {
    public interface RepositoryType {

    }
    @Mock
    BeanManager beanManager;
    @Mock
    Bean<AmazonDynamoDB> amazonDynamoDBBean;
    @Mock
    Bean<DynamoDBMapperConfig> dynamoDBMapperConfigBean;
    @Mock
    Bean<DynamoDBOperations> dynamoDBOperationsBean;
    Set<Annotation> qualifiers = Collections.emptySet();
    Class<RepositoryType> repositoryType = RepositoryType.class;

    @Before
    public void setUp() {
    }

    @Test
    public void testConstructor() {
        DynamoDBRepositoryBean<RepositoryType> underTest;

        try {
            underTest = new DynamoDBRepositoryBean<>(beanManager, null, dynamoDBMapperConfigBean, dynamoDBOperationsBean, qualifiers, repositoryType);
            fail("Should have failed");
        } catch (IllegalArgumentException ignored) {
        }

        try {
            underTest = new DynamoDBRepositoryBean<>(beanManager, amazonDynamoDBBean, null, dynamoDBOperationsBean, qualifiers, repositoryType);
            fail("Should have failed");
        } catch (IllegalArgumentException ignored) {
        }

        underTest = new DynamoDBRepositoryBean<>(beanManager, amazonDynamoDBBean, dynamoDBMapperConfigBean, null, qualifiers, repositoryType);
        underTest = new DynamoDBRepositoryBean<>(beanManager, null, null, dynamoDBOperationsBean, qualifiers, repositoryType);

        try {
            underTest = new DynamoDBRepositoryBean<>(beanManager, null, dynamoDBMapperConfigBean, null, qualifiers, repositoryType);
            fail("Should have failed");
        } catch (IllegalArgumentException ignored) {
        }
    }
}
