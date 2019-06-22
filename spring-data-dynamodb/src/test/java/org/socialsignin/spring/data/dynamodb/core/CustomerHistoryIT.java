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
package org.socialsignin.spring.data.dynamodb.core;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.socialsignin.spring.data.dynamodb.domain.sample.CustomerHistory;
import org.socialsignin.spring.data.dynamodb.domain.sample.CustomerHistoryRepository;
import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories;
import org.socialsignin.spring.data.dynamodb.utils.DynamoDBLocalResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.MethodMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
@ContextConfiguration(classes = {CustomerHistoryIT.TestAppConfig.class, DynamoDBLocalResource.class})
@TestPropertySource(properties = {"spring.data.dynamodb.entity2ddl.auto=create"})
public class CustomerHistoryIT {

	@Configuration
	@EnableDynamoDBRepositories(basePackages = "org.socialsignin.spring.data.dynamodb.domain.sample")
	public static class TestAppConfig {
	}

	@Autowired
	CustomerHistoryRepository customerHistoryRepository;

	@Test
	public void saveAndGSITest() {

		CustomerHistory expected = new CustomerHistory();
		expected.setId("customerId");
		expected.setCreateDt("createDTt");
		expected.setTag("2342");

		customerHistoryRepository.save(expected);

		CustomerHistory actual = customerHistoryRepository.findByTag(expected.getTag());

		assertEquals(expected.getId(), actual.getId());
		assertEquals(expected.getCreateDt(), actual.getCreateDt());
		assertEquals(expected.getTag(), actual.getTag());
	}
}
