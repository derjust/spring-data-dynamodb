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

import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.socialsignin.spring.data.dynamodb.core.DynamoDBOperations;

import java.util.concurrent.ThreadLocalRandom;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class QueryRequestCountQueryTest {
	@Mock
	private DynamoDBOperations dynamoDBOperations;
	@Mock
	private QueryRequest queryRequest;

	private QueryRequestCountQuery underTest;

	@Before
	public void setUp() {
		underTest = new QueryRequestCountQuery(dynamoDBOperations, queryRequest);
	}

	@Test
	public void testGetSingleResult() {
		int expected = ThreadLocalRandom.current().nextInt();
		when(dynamoDBOperations.count(Long.class, queryRequest)).thenReturn(expected);

		Long actual = underTest.getSingleResult();

		assertEquals(Long.valueOf(expected), actual);
	}
}
