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
package org.socialsignin.spring.data.dynamodb.repository.query;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.socialsignin.spring.data.dynamodb.core.DynamoDBOperations;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryLookupStrategy.Key;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

@RunWith(MockitoJUnitRunner.class)
public class DynamoDBQueryLookupStrategyTest {
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	@Mock
	private DynamoDBOperations dynamoDBOperations;

	@Test
	public void testCreate() {
		QueryLookupStrategy actual;
		actual = DynamoDBQueryLookupStrategy.create(dynamoDBOperations, Key.CREATE);
		assertNotNull(actual);

		actual = DynamoDBQueryLookupStrategy.create(dynamoDBOperations, Key.CREATE_IF_NOT_FOUND);
		assertNotNull(actual);
	}

	@Test
	public void testNull() {
		QueryLookupStrategy actualNull = DynamoDBQueryLookupStrategy.create(dynamoDBOperations, null);
		QueryLookupStrategy actualCreate = DynamoDBQueryLookupStrategy.create(dynamoDBOperations, Key.CREATE);

		assertSame(actualNull.getClass(), actualCreate.getClass());
	}

	@Test
	public void testDeclaredQuery() {
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("Unsupported query lookup strategy USE_DECLARED_QUERY!");

		DynamoDBQueryLookupStrategy.create(dynamoDBOperations, Key.USE_DECLARED_QUERY);
	}

}
