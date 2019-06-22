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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.time.LocalDateTime;
import java.util.Random;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.socialsignin.spring.data.dynamodb.domain.sample.Feed;
import org.socialsignin.spring.data.dynamodb.domain.sample.FeedPagingRepository;
import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories;
import org.socialsignin.spring.data.dynamodb.utils.DynamoDBLocalResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SortPageableIT.TestAppConfig.class, DynamoDBLocalResource.class})
@TestPropertySource(properties = {"spring.data.dynamodb.entity2ddl.auto=create"})
public class SortPageableIT {
	private final Random r = new Random();

	@Configuration
	@EnableDynamoDBRepositories(basePackages = "org.socialsignin.spring.data.dynamodb.domain.sample")
	public static class TestAppConfig {
	}

	@Autowired
	FeedPagingRepository feedPagingRepository;

	private Feed createFeed(String message) {
		Feed retValue = new Feed();
		retValue.setUserIdx(r.nextInt());
		retValue.setPaymentType(r.nextInt());
		retValue.setMessage(message);
		retValue.setRegDate(LocalDateTime.now());
		return retValue;
	}

	@Test
	public void feed_test() {
		feedPagingRepository.save(createFeed("not yet me"));
		feedPagingRepository.save(createFeed("me"));
		feedPagingRepository.save(createFeed("not me"));
		feedPagingRepository.save(createFeed("me"));
		feedPagingRepository.save(createFeed("also not me"));

		PageRequest pageable = PageRequest.of(0, 10);

		Page<Feed> actuals = feedPagingRepository.findAllByMessageOrderByRegDateDesc("me", pageable);
		assertEquals(2, actuals.getTotalElements());

		for (Feed actual : actuals) {
			assertNotEquals(0, actual.getPaymentType());
			assertNotEquals(0, actual.getUserIdx());
		}

	}
}
