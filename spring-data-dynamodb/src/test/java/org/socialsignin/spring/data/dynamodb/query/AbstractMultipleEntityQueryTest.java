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
package org.socialsignin.spring.data.dynamodb.query;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.socialsignin.spring.data.dynamodb.core.DynamoDBOperations;
import org.socialsignin.spring.data.dynamodb.domain.sample.User;
import org.springframework.dao.IncorrectResultSizeDataAccessException;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

@RunWith(MockitoJUnitRunner.class)
public class AbstractMultipleEntityQueryTest {

	private static class TestAbstractMultipleEntityQuery extends AbstractMultipleEntityQuery<User> {
		private final List<User> resultList;

		public TestAbstractMultipleEntityQuery(DynamoDBOperations dynamoDBOperations, User... resultEntities) {
			super(dynamoDBOperations, User.class);
			resultList = Arrays.asList(resultEntities);
		}

		@Override
		public List<User> getResultList() {
			return resultList;
		}
	}

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Mock
	private DynamoDBOperations dynamoDBOperations;
	@Mock
	private User entity;

	private AbstractMultipleEntityQuery<User> underTest;

	@Test
	public void testNullResult() {
		underTest = new TestAbstractMultipleEntityQuery(dynamoDBOperations);

		assertNull(underTest.getSingleResult());
	}

	@Test
	public void testSingleResult() {
		underTest = new TestAbstractMultipleEntityQuery(dynamoDBOperations, entity);

		assertSame(entity, underTest.getSingleResult());
	}

	@Test
	public void testMultiResult() {
		expectedException.expect(IncorrectResultSizeDataAccessException.class);
		underTest = new TestAbstractMultipleEntityQuery(dynamoDBOperations, entity, entity);

		underTest.getSingleResult();
	}
}
