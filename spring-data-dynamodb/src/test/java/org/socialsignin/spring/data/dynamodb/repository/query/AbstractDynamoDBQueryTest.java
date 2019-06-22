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

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.socialsignin.spring.data.dynamodb.core.DynamoDBOperations;
import org.socialsignin.spring.data.dynamodb.domain.sample.User;
import org.socialsignin.spring.data.dynamodb.query.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.core.RepositoryMetadata;

@RunWith(MockitoJUnitRunner.class)
public class AbstractDynamoDBQueryTest {

	public static interface UserRepository extends CrudRepository<User, String> {
		public Page<User> findByName(String name, Pageable pageable);
	}
	@Mock
	private Query<User> query;
	@Mock
	private Query<Long> countQuery;
	private boolean isSingleEntityResultsRestriction = false;
	private Integer resultsRestrictionIfApplicable = 0;
	private boolean isDeleteQuery = false;
	private boolean isExistsQuery = false;
	private boolean isCountQuery = false;

	public class TestAbstractDynamoDBQuery extends AbstractDynamoDBQuery<User, String> {
		public TestAbstractDynamoDBQuery(DynamoDBOperations dynamoDBOperations,
				DynamoDBQueryMethod<User, String> method) {
			super(dynamoDBOperations, method);
		}

		@Override
		protected Query<User> doCreateQuery(Object[] values) {
			return query;
		}

		@Override
		protected Query<Long> doCreateCountQuery(Object[] values, boolean pageQuery) {
			return countQuery;
		}

		@Override
		protected boolean isCountQuery() {
			return isCountQuery;
		}

		@Override
		protected boolean isExistsQuery() {
			return isExistsQuery;
		}

		@Override
		protected boolean isDeleteQuery() {
			return isDeleteQuery;
		}

		@Override
		protected Integer getResultsRestrictionIfApplicable() {
			return resultsRestrictionIfApplicable;
		}

		@Override
		protected boolean isSingleEntityResultsRestriction() {
			return isSingleEntityResultsRestriction;
		}

	}

	@Mock
	private DynamoDBOperations dynamoDBOperations;
	@Mock
	private RepositoryMetadata metadata;
	@Mock
	private ProjectionFactory factory;

	@Before
	public void setUp() {
		doReturn(UserRepository.class).when(metadata).getRepositoryInterface();
		doReturn(User.class).when(metadata).getReturnedDomainClass(any());
	}

	private List<User> generateContent(long count) {
		List<User> retValue = new ArrayList<>();
		for (long i = 0; i < count; i++) {
			User u = new User();
			u.setId("id");
			retValue.add(u);
		}
		return retValue;
	}

	@Test
	public void testPaged() throws NoSuchMethodException, SecurityException {
		long total = 1;
		resultsRestrictionIfApplicable = 1;
		List<User> content = generateContent(total);

		Method method = UserRepository.class.getMethod("findByName", String.class, Pageable.class);
		DynamoDBQueryMethod<User, String> dynamoDBQueryMethod = new DynamoDBQueryMethod<User, String>(method, metadata,
				factory);

		when(countQuery.getSingleResult()).thenReturn(total);
		when(query.getResultList()).thenReturn(content);

		TestAbstractDynamoDBQuery underTest = new TestAbstractDynamoDBQuery(dynamoDBOperations, dynamoDBQueryMethod);

		Object actual = underTest.execute(new Object[]{"testName", PageRequest.of(0, 10)});

		assertThat(actual, instanceOf(Page.class));
		Page<User> actualPage = (Page<User>) actual;

		assertEquals(1, actualPage.getTotalElements());
		assertThat(content, is(actualPage.getContent()));
	}

	@Test
	public void testUnpaged() throws NoSuchMethodException, SecurityException {
		long total = 1;
		List<User> content = spy(generateContent(total));

		Method method = UserRepository.class.getMethod("findByName", String.class, Pageable.class);
		DynamoDBQueryMethod<User, String> dynamoDBQueryMethod = new DynamoDBQueryMethod<User, String>(method, metadata,
				factory);

		when(countQuery.getSingleResult()).thenReturn(total);
		when(query.getResultList()).thenReturn(content);

		TestAbstractDynamoDBQuery underTest = new TestAbstractDynamoDBQuery(dynamoDBOperations, dynamoDBQueryMethod);

		Object actual = underTest.execute(new Object[]{"testName", Pageable.unpaged()});

		assertThat(actual, instanceOf(Page.class));
		Page<User> actualPage = (Page<User>) actual;

		assertEquals(1, actualPage.getTotalElements());
		assertThat(content, is(actualPage.getContent()));
		// There should be never an index access to the list - just iterator access to
		// ensure that lazy loading behavior of the AWS list implementation is kept
		// intact
		verify(content, never()).get(anyInt());
		verify(content).iterator();
	}

}
