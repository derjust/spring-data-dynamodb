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
package org.socialsignin.spring.data.dynamodb.repository.support;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.socialsignin.spring.data.dynamodb.core.DynamoDBOperations;
import org.socialsignin.spring.data.dynamodb.domain.sample.User;
import org.socialsignin.spring.data.dynamodb.mapping.DynamoDBMappingContext;
import org.socialsignin.spring.data.dynamodb.repository.util.DynamoDBMappingContextProcessor;
import org.socialsignin.spring.data.dynamodb.repository.util.Entity2DynamoDBTableSynchronizer;
import org.springframework.context.ApplicationContext;
import org.springframework.data.repository.Repository;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.spy;

@RunWith(MockitoJUnitRunner.class)
public class DynamoDBRepositoryFactoryBeanTest {

	@Mock
	private ApplicationContext applicationContext;
	@Mock
	private DynamoDBOperations dynamoDBOperations;
	@Mock
	private DynamoDBMapperConfig dynamoDBMapperConfig;
	@Mock
	private AmazonDynamoDB amazonDynamoDB;
	@Mock
	private Entity2DynamoDBTableSynchronizer<User, String> tableSynchronizer;
	@Mock
	private DynamoDBMappingContextProcessor<User, String> dynamoDBMappingContextProcessor;

	private DynamoDBMappingContext dynamoDBMappingContext = new DynamoDBMappingContext();

	private DynamoDBRepositoryFactoryBean<UserRepository, User, String> underTest;

	public interface UserRepository extends Repository<User, String> {

	}

	@Before
	public void setUp() {
		underTest = spy(new DynamoDBRepositoryFactoryBean<>(UserRepository.class));
		underTest.setDynamoDBMappingContext(dynamoDBMappingContext);
		underTest.setEntity2DynamoDBTableSynchronizer(tableSynchronizer);
		underTest.setDynamoDBMappingContextProcessor(dynamoDBMappingContextProcessor);
	}

	@Test
	public void testDynamoDBOperations() {
		try {
			underTest.getPersistentEntity();
			fail();
		} catch (NullPointerException /* IllegalStateException */ ise) {
			assertTrue(true);
		}

		underTest.setDynamoDBOperations(dynamoDBOperations);
		underTest.afterPropertiesSet();

		assertNotNull(underTest.getPersistentEntity());
	}

	@Test
	public void testAmazonDynamoDB() {
		try {
			underTest.getPersistentEntity();
			fail();
		} catch (NullPointerException /* IllegalStateException */ ise) {
			assertTrue(true);
		}

		underTest.setDynamoDBOperations(dynamoDBOperations);
		underTest.afterPropertiesSet();

		assertNotNull(underTest.getPersistentEntity());
	}

}
