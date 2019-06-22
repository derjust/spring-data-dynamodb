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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.socialsignin.spring.data.dynamodb.core.DynamoDBOperations;
import org.socialsignin.spring.data.dynamodb.domain.sample.User;

import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class AbstractDynamicQueryTest {

	private static class QueryTest<T> extends AbstractDynamicQuery<T> {
		public QueryTest(DynamoDBOperations dynamoDBOperations, Class<T> clazz) {
			super(dynamoDBOperations, clazz);
		}

		@Override
		public List<T> getResultList() {
			return null;
		}

		@Override
		public T getSingleResult() {
			return null;
		}
	}

	@Mock
	private DynamoDBOperations dynamoDBOperations;
	private AbstractQuery<User> underTest;

	@Before
	public void setUp() {
		underTest = new QueryTest<>(dynamoDBOperations, User.class);
	}

	@Test
	public void testSetter() {
		assertFalse(underTest.isScanCountEnabled());
		assertFalse(underTest.isScanEnabled());

		underTest.setScanCountEnabled(true);
		underTest.setScanEnabled(true);

		assertTrue(underTest.isScanCountEnabled());
		assertTrue(underTest.isScanEnabled());
	}

}
