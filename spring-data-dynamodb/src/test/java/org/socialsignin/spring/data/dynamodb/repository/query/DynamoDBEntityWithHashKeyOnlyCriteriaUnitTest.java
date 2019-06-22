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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.socialsignin.spring.data.dynamodb.domain.sample.User;
import org.socialsignin.spring.data.dynamodb.repository.support.DynamoDBEntityInformation;

import java.util.Date;

@RunWith(MockitoJUnitRunner.class)
public class DynamoDBEntityWithHashKeyOnlyCriteriaUnitTest
		extends
			AbstractDynamoDBQueryCriteriaUnitTest<DynamoDBEntityWithHashKeyOnlyCriteria<User, String>> {

	@Mock
	private DynamoDBEntityInformation<User, String> entityInformation;

	@Before
	public void setUp() {
		Mockito.when(entityInformation.getHashKeyPropertyName()).thenReturn("id");
		criteria = new DynamoDBEntityWithHashKeyOnlyCriteria<>(entityInformation, null);
	}

	@Test
	public void testHasIndexHashKeyEqualConditionAnd_WhenConditionCriteriaIsEqualityOnAPropertyWhichIsAnIndexHashKeyButNotAHashKey() {
		Mockito.when(entityInformation.isGlobalIndexHashKeyProperty("name")).thenReturn(true);
		criteria.withPropertyEquals("name", "some name", String.class);
		boolean hasIndexHashKeyEqualCondition = criteria.hasIndexHashKeyEqualCondition();
		Assert.assertTrue(hasIndexHashKeyEqualCondition);
	}

	@Test
	public void testHasIndexHashKeyEqualCondition_WhenConditionCriteriaIsEqualityOnAPropertyWhichIsNotAnIndexHashKeyButIsAHashKey() {
		Mockito.when(entityInformation.isGlobalIndexHashKeyProperty("id")).thenReturn(false);
		criteria.withPropertyEquals("id", "some id", String.class);
		boolean hasIndexHashKeyEqualCondition = criteria.hasIndexHashKeyEqualCondition();
		Assert.assertFalse(hasIndexHashKeyEqualCondition);
	}

	@Test
	public void testHasIndexHashKeyEqualCondition_WhenConditionCriteriaIsEqualityOnAPropertyWhichIsBothAnIndexHashKeyAndAHashKey() {
		Mockito.when(entityInformation.isGlobalIndexHashKeyProperty("id")).thenReturn(true);
		criteria.withPropertyEquals("id", "some id", String.class);
		boolean hasIndexHashKeyEqualCondition = criteria.hasIndexHashKeyEqualCondition();
		Assert.assertTrue(hasIndexHashKeyEqualCondition);
	}

	@Test
	public void testHasIndexHashKeyEqualCondition_WhenConditionCriteriaIsEqualityOnAPropertyWhichIsNeitherAnIndexHashKeyOrAHashKey() {
		Mockito.when(entityInformation.isGlobalIndexHashKeyProperty("joinDate")).thenReturn(false);
		criteria.withPropertyEquals("joinDate", new Date(), Date.class);
		boolean hasIndexHashKeyEqualCondition = criteria.hasIndexHashKeyEqualCondition();
		Assert.assertFalse(hasIndexHashKeyEqualCondition);
	}

	@Test
	public void testHasIndexRangeKeyCondition_WhenConditionCriteriaIsEqualityOnAPropertyWhichIsAnIndexRangeKeyButNotAHashKey() {
		Mockito.when(entityInformation.isGlobalIndexRangeKeyProperty("name")).thenReturn(true);
		criteria.withPropertyEquals("name", "some name", String.class);
		boolean hasIndexRangeKeyCondition = criteria.hasIndexRangeKeyCondition();
		Assert.assertTrue(hasIndexRangeKeyCondition);
	}

	@Test
	public void testHasIndexRangeKeyCondition_WhenConditionCriteriaIsEqualityOnAPropertyWhichIsNotAnIndexRangeKeyButIsAHashKey() {
		Mockito.when(entityInformation.isGlobalIndexRangeKeyProperty("id")).thenReturn(false);
		criteria.withPropertyEquals("id", "some id", String.class);
		boolean hasIndexRangeKeyCondition = criteria.hasIndexRangeKeyCondition();
		Assert.assertFalse(hasIndexRangeKeyCondition);
	}

	@Test
	public void testHasIndexRangeKeyCondition_WhenConditionCriteriaIsEqualityOnAPropertyWhichIsBothAnIndexRangeKeyAndAHashKey() {
		Mockito.when(entityInformation.isGlobalIndexRangeKeyProperty("id")).thenReturn(true);
		criteria.withPropertyEquals("id", "some id", String.class);
		boolean hasIndexRangeKeyCondition = criteria.hasIndexRangeKeyCondition();
		Assert.assertTrue(hasIndexRangeKeyCondition);
	}

	@Test
	public void testHasIndexRangeKeyCondition_WhenConditionCriteriaIsEqualityOnAPropertyWhichIsNeitherAnIndexRangeKeyOrAHashKey() {
		Mockito.when(entityInformation.isGlobalIndexRangeKeyProperty("joinDate")).thenReturn(false);
		criteria.withPropertyEquals("joinDate", new Date(), Date.class);
		boolean hasIndexRangeKeyCondition = criteria.hasIndexRangeKeyCondition();
		Assert.assertFalse(hasIndexRangeKeyCondition);
	}

	// repeat

	@Test
	public void testHasIndexHashKeyEqualConditionAnd_WhenConditionCriteriaIsNonEqualityConditionOnAPropertyWhichIsAnIndexHashKeyButNotAHashKey() {
		Mockito.when(entityInformation.isGlobalIndexHashKeyProperty("name")).thenReturn(true);
		criteria.withPropertyBetween("name", "some name", "some other name", String.class);
		boolean hasIndexHashKeyEqualCondition = criteria.hasIndexHashKeyEqualCondition();
		Assert.assertFalse(hasIndexHashKeyEqualCondition);
	}

	@Test
	public void testHasIndexHashKeyEqualCondition_WhenConditionCriteriaIsNonEqualityConditionOnAPropertyWhichIsNotAnIndexHashKeyButIsAHashKey() {
		Mockito.when(entityInformation.isGlobalIndexHashKeyProperty("name")).thenReturn(false);
		criteria.withPropertyBetween("name", "some name", "some other name", String.class);
		boolean hasIndexHashKeyEqualCondition = criteria.hasIndexHashKeyEqualCondition();
		Assert.assertFalse(hasIndexHashKeyEqualCondition);
	}

	@Test
	public void testHasIndexHashKeyEqualCondition_WhenConditionCriteriaIsNonEqualityConditionOnAPropertyWhichIsBothAnIndexHashKeyAndAHashKey() {
		Mockito.when(entityInformation.isGlobalIndexHashKeyProperty("id")).thenReturn(true);
		criteria.withPropertyBetween("id", "some id", "some other id", String.class);
		boolean hasIndexHashKeyEqualCondition = criteria.hasIndexHashKeyEqualCondition();
		Assert.assertFalse(hasIndexHashKeyEqualCondition);
	}

	@Test
	public void testHasIndexHashKeyEqualCondition_WhenConditionCriteriaIsNonEqualityConditionOnAPropertyWhichIsNeitherAnIndexHashKeyOrAHashKey() {
		Mockito.when(entityInformation.isGlobalIndexHashKeyProperty("joinDate")).thenReturn(false);
		criteria.withPropertyBetween("joinDate", new Date(), new Date(), Date.class);
		boolean hasIndexHashKeyEqualCondition = criteria.hasIndexHashKeyEqualCondition();
		Assert.assertFalse(hasIndexHashKeyEqualCondition);
	}

	@Test
	public void testHasIndexRangeKeyCondition_WhenConditionCriteriaIsNonEqualityConditionOnAPropertyWhichIsAnIndexRangeKeyButNotAHashKey() {
		Mockito.when(entityInformation.isGlobalIndexRangeKeyProperty("name")).thenReturn(true);
		criteria.withPropertyBetween("name", "some name", "some other name", String.class);
		boolean hasIndexRangeKeyCondition = criteria.hasIndexRangeKeyCondition();
		Assert.assertTrue(hasIndexRangeKeyCondition);
	}

	@Test
	public void testHasIndexRangeKeyCondition_WhenConditionCriteriaIsNonEqualityConditionOnAPropertyWhichIsNotAnIndexRangeKeyButIsAHashKey() {
		Mockito.when(entityInformation.isGlobalIndexRangeKeyProperty("id")).thenReturn(false);
		criteria.withPropertyBetween("id", "some id", "some other id", String.class);
		boolean hasIndexRangeKeyCondition = criteria.hasIndexRangeKeyCondition();
		Assert.assertFalse(hasIndexRangeKeyCondition);
	}

	@Test
	public void testHasIndexRangeKeyCondition_WhenConditionCriteriaIsNonEqualityConditionOnAPropertyWhichIsBothAnIndexRangeKeyAndAHashKey() {
		Mockito.when(entityInformation.isGlobalIndexRangeKeyProperty("id")).thenReturn(true);
		criteria.withPropertyBetween("id", "some id", "some other id", String.class);
		boolean hasIndexRangeKeyCondition = criteria.hasIndexRangeKeyCondition();
		Assert.assertTrue(hasIndexRangeKeyCondition);
	}

	@Test
	public void testHasIndexRangeKeyCondition_WhenConditionCriteriaIsNonEqualityConditionOnAPropertyWhichIsNeitherAnIndexRangeKeyOrAHashKey() {
		Mockito.when(entityInformation.isGlobalIndexRangeKeyProperty("joinDate")).thenReturn(false);
		criteria.withPropertyBetween("joinDate", new Date(), new Date(), Date.class);
		boolean hasIndexRangeKeyCondition = criteria.hasIndexRangeKeyCondition();
		Assert.assertFalse(hasIndexRangeKeyCondition);
	}

}
