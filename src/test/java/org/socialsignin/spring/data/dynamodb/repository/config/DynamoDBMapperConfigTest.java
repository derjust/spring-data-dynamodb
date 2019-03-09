/**
 * Copyright © 2018 spring-data-dynamodb (https://github.com/derjust/spring-data-dynamodb)
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
package org.socialsignin.spring.data.dynamodb.repository.config;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test Issue #233 fix
 */
public class DynamoDBMapperConfigTest {

	static class BaseTestConfig {
		@Bean
		public AmazonDynamoDB amazonDynamoDB() {
			return Mockito.mock(AmazonDynamoDB.class);
		}

		@Bean
		public DynamoDBMapper dynamoDBMapper() {
			return Mockito.mock(DynamoDBMapper.class);
		}
	}

	@Configuration
	@EnableDynamoDBRepositories(dynamoDBMapperConfigRef = "customDynamoDBMapperConfig")
	static class TestConfigWithDynamoDBMapperConfigRef extends BaseTestConfig {
		@Bean("customDynamoDBMapperConfig")
		public DynamoDBMapperConfig dynamoDBMapperConfig() {
			return DynamoDBMapperConfig.DEFAULT;
		}
	}

	@Configuration
	@EnableDynamoDBRepositories
	static class TestConfigWithoutDynamoDBMapperConfigRef extends TestConfigWithDynamoDBMapperConfigRef {
	}

	@Configuration
	@EnableDynamoDBRepositories
	static class TestConfigWithoutDynamoDBMapperConfigBean extends BaseTestConfig {
	}

	@Test
	public void testConfigWithDynamoDBMapperConfigRef() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(
				TestConfigWithDynamoDBMapperConfigRef.class);

		boolean containsBean = ctx.containsBean("customDynamoDBMapperConfig");
		assertTrue(containsBean);
	}

	@Test(expected = UnsatisfiedDependencyException.class)
	public void testConfigWithoutDynamoDBMapperConfigRef() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(
				TestConfigWithoutDynamoDBMapperConfigRef.class);

		boolean containsBean = ctx.containsBean("customDynamoDBMapperConfig");
		assertTrue(containsBean);
	}

	@Test
	public void testConfigWithoutDynamoDBMapperConfigBean() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(
				TestConfigWithoutDynamoDBMapperConfigBean.class);

		boolean containsBean = ctx.containsBean("customDynamoDBMapperConfig");
		assertFalse(containsBean);

		containsBean = ctx.containsBean("dynamoDB-DynamoDBMapperConfig");
		assertTrue(containsBean);
	}
}
