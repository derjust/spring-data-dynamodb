/**
 * Copyright © 2013-2019 spring-data-dynamodb (https://github.com/derjust/spring-data-dynamodb/spring-data-dynamodb)
 *
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
 */
package org.socialsignin.spring.data.dynamodb.repository.cdi;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.socialsignin.spring.data.dynamodb.core.DynamoDBOperations;
import org.socialsignin.spring.data.dynamodb.domain.sample.User;
import org.springframework.data.repository.Repository;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DynamoDBRepositoryBeanTest {
	interface SampleRepository extends Repository<User, String> {
	}

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Mock
	private CreationalContext<AmazonDynamoDB> creationalContext;
	@Mock
	private CreationalContext<SampleRepository> repoCreationalContext;
	@Mock
	private BeanManager beanManager;
	@Mock
	private Bean<AmazonDynamoDB> amazonDynamoDBBean;
	@Mock
	private AmazonDynamoDB amazonDynamoDB;
	@Mock
	private javax.enterprise.inject.spi.Bean<DynamoDBMapperConfig> dynamoDBMapperConfigBean;
	@Mock
	private Bean<DynamoDBOperations> dynamoDBOperationsBean;

	private Set<Annotation> qualifiers = Collections.emptySet();
	private Class<SampleRepository> repositoryType = SampleRepository.class;

	@Before
	public void setUp() {
		when(beanManager.createCreationalContext(amazonDynamoDBBean)).thenReturn(creationalContext);
		when(beanManager.getReference(amazonDynamoDBBean, AmazonDynamoDB.class, creationalContext))
				.thenReturn(amazonDynamoDB);
	}

	@Test
	public void testNullOperationsOk() {
		DynamoDBRepositoryBean<SampleRepository> underTest = new DynamoDBRepositoryBean<>(beanManager,
				amazonDynamoDBBean, dynamoDBMapperConfigBean, null, qualifiers, repositoryType);

		assertNotNull(underTest);
	}

	@Test
	public void testNullOperationFail() {
		expectedException.expectMessage("amazonDynamoDBBean must not be null!");

		new DynamoDBRepositoryBean<>(beanManager, null, dynamoDBMapperConfigBean, null, qualifiers, repositoryType);
	}

	@Test
	public void testSetOperationOk1() {
		DynamoDBRepositoryBean<SampleRepository> underTest = new DynamoDBRepositoryBean<>(beanManager, null, null,
				dynamoDBOperationsBean, qualifiers, repositoryType);

		assertNotNull(underTest);
	}

	@Test
	public void testSetOperationFail1() {
		expectedException.expectMessage(
				"Cannot specify both dynamoDBMapperConfigBean bean and dynamoDBOperationsBean in repository configuration");

		new DynamoDBRepositoryBean<>(beanManager, null, dynamoDBMapperConfigBean, dynamoDBOperationsBean, qualifiers,
				repositoryType);
	}

	@Test
	public void testSetOperationFail2() {
		expectedException.expectMessage(
				"Cannot specify both amazonDynamoDB bean and dynamoDBOperationsBean in repository configuration");

		new DynamoDBRepositoryBean<>(beanManager, amazonDynamoDBBean, null, dynamoDBOperationsBean, qualifiers,
				repositoryType);
	}

	@Test
	public void testCreateRepostiory() {
		DynamoDBRepositoryBean<SampleRepository> underTest = new DynamoDBRepositoryBean<>(beanManager,
				amazonDynamoDBBean, dynamoDBMapperConfigBean, null, qualifiers, repositoryType);

		SampleRepository actual = underTest.create(repoCreationalContext, SampleRepository.class,
				Optional.<Object>empty());
		assertNotNull(actual);
	}
}
