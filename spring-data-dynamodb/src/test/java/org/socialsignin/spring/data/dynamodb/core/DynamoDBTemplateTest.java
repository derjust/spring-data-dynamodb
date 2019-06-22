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

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.socialsignin.spring.data.dynamodb.domain.sample.Playlist;
import org.socialsignin.spring.data.dynamodb.domain.sample.User;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class DynamoDBTemplateTest {
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	@Mock
	private DynamoDBMapper dynamoDBMapper;
	@Mock
	private DynamoDBMapperConfig dynamoDBMapperConfig;
	@Mock
	private AmazonDynamoDB dynamoDB;
	@Mock
	private ApplicationContext applicationContext;
	@Mock
	private DynamoDBQueryExpression<User> countUserQuery;

	private DynamoDBTemplate dynamoDBTemplate;

	@Before
	public void setUp() {
		this.dynamoDBTemplate = new DynamoDBTemplate(dynamoDB, dynamoDBMapper, dynamoDBMapperConfig);
		this.dynamoDBTemplate.setApplicationContext(applicationContext);

		// check that the defaults are properly initialized - #108
		String userTableName = dynamoDBTemplate.getOverriddenTableName(User.class, "UserTable");
		assertEquals("UserTable", userTableName);
	}

	@Test
	public void testConstructorAllNull() {
		try {
			dynamoDBTemplate = new DynamoDBTemplate(null, null, null);
			fail("AmazonDynamoDB must not be null!");
		} catch (IllegalArgumentException iae) {
			// ignored
		}

		try {
			dynamoDBTemplate = new DynamoDBTemplate(dynamoDB, null, null);
			fail("DynamoDBMapper must not be null!");
		} catch (IllegalArgumentException iae) {
			// ignored
		}
		try {
			dynamoDBTemplate = new DynamoDBTemplate(dynamoDB, dynamoDBMapper, null);
			fail("DynamoDBMapperConfig must not be null!");
		} catch (IllegalArgumentException iae) {
			// ignored
		}
		assertTrue(true);
	}

	// TODO remove and replace with postprocessor test
	@Test
	public void testConstructorOptionalPreconfiguredDynamoDBMapper() {
		// Introduced constructor via #91 should not fail its assert
		this.dynamoDBTemplate = new DynamoDBTemplate(dynamoDB, dynamoDBMapper, dynamoDBMapperConfig);

		assertTrue("The constructor should not fail with an assert error", true);
	}

	@Test
	public void testDelete() {
		User user = new User();
		dynamoDBTemplate.delete(user);

		verify(dynamoDBMapper).delete(user);
	}

	@Test
	public void testBatchDelete_CallsCorrectDynamoDBMapperMethod() {
		List<User> users = new ArrayList<>();
		dynamoDBTemplate.batchDelete(users);
		verify(dynamoDBMapper).batchDelete(anyList());
	}

	@Test
	public void testSave() {
		User user = new User();
		dynamoDBTemplate.save(user);

		verify(dynamoDBMapper).save(user);
	}

	@Test
	public void testBatchSave_CallsCorrectDynamoDBMapperMethod() {
		List<User> users = new ArrayList<>();
		dynamoDBTemplate.batchSave(users);

		verify(dynamoDBMapper).batchSave(eq(users));
	}

	@Test
	public void testCountQuery() {
		DynamoDBQueryExpression<User> query = countUserQuery;
		dynamoDBTemplate.count(User.class, query);

		verify(dynamoDBMapper).count(User.class, query);
	}

	@Test
	public void testCountScan() {
		DynamoDBScanExpression scan = mock(DynamoDBScanExpression.class);
		int actual = dynamoDBTemplate.count(User.class, scan);

		assertEquals(0, actual);
		verify(dynamoDBMapper).count(User.class, scan);
	}

	@Test
	public void testLoadByHashKey_WhenDynamoDBMapperReturnsNull() {
		User user = dynamoDBTemplate.load(User.class, "someHashKey");
		Assert.assertNull(user);
	}

	@Test
	public void testLoadByHashKeyAndRangeKey_WhenDynamoDBMapperReturnsNull() {
		Playlist playlist = dynamoDBTemplate.load(Playlist.class, "someHashKey", "someRangeKey");
		Assert.assertNull(playlist);
	}

}
