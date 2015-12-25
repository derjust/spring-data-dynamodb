package org.socialsignin.spring.data.dynamodb.repository.query;

import java.util.Date;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.socialsignin.spring.data.dynamodb.domain.sample.User;
import org.socialsignin.spring.data.dynamodb.repository.support.DynamoDBEntityInformation;

@RunWith(MockitoJUnitRunner.class)
public class DynamoDBEntityWithHashKeyOnlyCriteriaUnitTest extends AbstractDynamoDBQueryCriteriaUnitTests<DynamoDBEntityWithHashKeyOnlyCriteria<User,String>> {
	
	@Mock
	private DynamoDBEntityInformation<User,String> entityInformation;
	
	
	@Before
	public void setUp()
	{
		Mockito.when(entityInformation.getHashKeyPropertyName()).thenReturn("id");
		criteria = new DynamoDBEntityWithHashKeyOnlyCriteria<User,String>(entityInformation);
	}
	
	@Test
	public void testHasIndexHashKeyEqualConditionAnd_WhenConditionCriteriaIsEqualityOnAPropertyWhichIsAnIndexHashKeyButNotAHashKey()
	{
		Mockito.when(entityInformation.isGlobalIndexHashKeyProperty("name")).thenReturn(true);
		criteria.withPropertyEquals("name", "some name", String.class);
		boolean hasIndexHashKeyEqualCondition = criteria.hasIndexHashKeyEqualCondition();
		Assert.assertTrue(hasIndexHashKeyEqualCondition);
	}
	
	@Test
	public void testHasIndexHashKeyEqualCondition_WhenConditionCriteriaIsEqualityOnAPropertyWhichIsNotAnIndexHashKeyButIsAHashKey()
	{
		Mockito.when(entityInformation.isGlobalIndexHashKeyProperty("id")).thenReturn(false);
		criteria.withPropertyEquals("id", "some id", String.class);
		boolean hasIndexHashKeyEqualCondition = criteria.hasIndexHashKeyEqualCondition();
		Assert.assertFalse(hasIndexHashKeyEqualCondition);
	}
	
	@Test
	public void testHasIndexHashKeyEqualCondition_WhenConditionCriteriaIsEqualityOnAPropertyWhichIsBothAnIndexHashKeyAndAHashKey()
	{
		Mockito.when(entityInformation.isGlobalIndexHashKeyProperty("id")).thenReturn(true);
		criteria.withPropertyEquals("id", "some id", String.class);
		boolean hasIndexHashKeyEqualCondition = criteria.hasIndexHashKeyEqualCondition();
		Assert.assertTrue(hasIndexHashKeyEqualCondition);
	}
	
	@Test
	public void testHasIndexHashKeyEqualCondition_WhenConditionCriteriaIsEqualityOnAPropertyWhichIsNeitherAnIndexHashKeyOrAHashKey()
	{
		Mockito.when(entityInformation.isGlobalIndexHashKeyProperty("joinDate")).thenReturn(false);
		criteria.withPropertyEquals("joinDate", new Date(), Date.class);
		boolean hasIndexHashKeyEqualCondition = criteria.hasIndexHashKeyEqualCondition();
		Assert.assertFalse(hasIndexHashKeyEqualCondition);
	}
	
	@Test
	public void testHasIndexRangeKeyCondition_WhenConditionCriteriaIsEqualityOnAPropertyWhichIsAnIndexRangeKeyButNotAHashKey()
	{
		Mockito.when(entityInformation.isGlobalIndexRangeKeyProperty("name")).thenReturn(true);
		criteria.withPropertyEquals("name", "some name", String.class);
		boolean hasIndexRangeKeyCondition = criteria.hasIndexRangeKeyCondition();
		Assert.assertTrue(hasIndexRangeKeyCondition);
	}
	
	@Test
	public void testHasIndexRangeKeyCondition_WhenConditionCriteriaIsEqualityOnAPropertyWhichIsNotAnIndexRangeKeyButIsAHashKey()
	{
		Mockito.when(entityInformation.isGlobalIndexRangeKeyProperty("id")).thenReturn(false);
		criteria.withPropertyEquals("id", "some id", String.class);
		boolean hasIndexRangeKeyCondition = criteria.hasIndexRangeKeyCondition();
		Assert.assertFalse(hasIndexRangeKeyCondition);
	}
	
	@Test
	public void testHasIndexRangeKeyCondition_WhenConditionCriteriaIsEqualityOnAPropertyWhichIsBothAnIndexRangeKeyAndAHashKey()
	{
		Mockito.when(entityInformation.isGlobalIndexRangeKeyProperty("id")).thenReturn(true);
		criteria.withPropertyEquals("id", "some id", String.class);
		boolean hasIndexRangeKeyCondition = criteria.hasIndexRangeKeyCondition();
		Assert.assertTrue(hasIndexRangeKeyCondition);
	}
	
	@Test
	public void testHasIndexRangeKeyCondition_WhenConditionCriteriaIsEqualityOnAPropertyWhichIsNeitherAnIndexRangeKeyOrAHashKey()
	{
		Mockito.when(entityInformation.isGlobalIndexRangeKeyProperty("joinDate")).thenReturn(false);
		criteria.withPropertyEquals("joinDate", new Date(), Date.class);
		boolean hasIndexRangeKeyCondition = criteria.hasIndexRangeKeyCondition();
		Assert.assertFalse(hasIndexRangeKeyCondition);
	}
	
	// repeat
	
	@Test
	public void testHasIndexHashKeyEqualConditionAnd_WhenConditionCriteriaIsNonEqualityConditionOnAPropertyWhichIsAnIndexHashKeyButNotAHashKey()
	{
		Mockito.when(entityInformation.isGlobalIndexHashKeyProperty("name")).thenReturn(true);
		criteria.withPropertyBetween("name", "some name","some other name", String.class);
		boolean hasIndexHashKeyEqualCondition = criteria.hasIndexHashKeyEqualCondition();
		Assert.assertFalse(hasIndexHashKeyEqualCondition);
	}
	
	@Test
	public void testHasIndexHashKeyEqualCondition_WhenConditionCriteriaIsNonEqualityConditionOnAPropertyWhichIsNotAnIndexHashKeyButIsAHashKey()
	{
		Mockito.when(entityInformation.isGlobalIndexHashKeyProperty("name")).thenReturn(false);
		criteria.withPropertyBetween("name", "some name","some other name", String.class);
		boolean hasIndexHashKeyEqualCondition = criteria.hasIndexHashKeyEqualCondition();
		Assert.assertFalse(hasIndexHashKeyEqualCondition);
	}
	
	@Test
	public void testHasIndexHashKeyEqualCondition_WhenConditionCriteriaIsNonEqualityConditionOnAPropertyWhichIsBothAnIndexHashKeyAndAHashKey()
	{
		Mockito.when(entityInformation.isGlobalIndexHashKeyProperty("id")).thenReturn(true);
		criteria.withPropertyBetween("id", "some id","some other id", String.class);
		boolean hasIndexHashKeyEqualCondition = criteria.hasIndexHashKeyEqualCondition();
		Assert.assertFalse(hasIndexHashKeyEqualCondition);
	}
	
	@Test
	public void testHasIndexHashKeyEqualCondition_WhenConditionCriteriaIsNonEqualityConditionOnAPropertyWhichIsNeitherAnIndexHashKeyOrAHashKey()
	{
		Mockito.when(entityInformation.isGlobalIndexHashKeyProperty("joinDate")).thenReturn(false);
		criteria.withPropertyBetween("joinDate", new Date(),new Date(), Date.class);
		boolean hasIndexHashKeyEqualCondition = criteria.hasIndexHashKeyEqualCondition();
		Assert.assertFalse(hasIndexHashKeyEqualCondition);
	}
	
	@Test
	public void testHasIndexRangeKeyCondition_WhenConditionCriteriaIsNonEqualityConditionOnAPropertyWhichIsAnIndexRangeKeyButNotAHashKey()
	{
		Mockito.when(entityInformation.isGlobalIndexRangeKeyProperty("name")).thenReturn(true);
		criteria.withPropertyBetween("name", "some name","some other name", String.class);
		boolean hasIndexRangeKeyCondition = criteria.hasIndexRangeKeyCondition();
		Assert.assertTrue(hasIndexRangeKeyCondition);
	}
	
	@Test
	public void testHasIndexRangeKeyCondition_WhenConditionCriteriaIsNonEqualityConditionOnAPropertyWhichIsNotAnIndexRangeKeyButIsAHashKey()
	{
		Mockito.when(entityInformation.isGlobalIndexRangeKeyProperty("id")).thenReturn(false);
		criteria.withPropertyBetween("id", "some id","some other id", String.class);
		boolean hasIndexRangeKeyCondition = criteria.hasIndexRangeKeyCondition();
		Assert.assertFalse(hasIndexRangeKeyCondition);
	}
	
	@Test
	public void testHasIndexRangeKeyCondition_WhenConditionCriteriaIsNonEqualityConditionOnAPropertyWhichIsBothAnIndexRangeKeyAndAHashKey()
	{
		Mockito.when(entityInformation.isGlobalIndexRangeKeyProperty("id")).thenReturn(true);
		criteria.withPropertyBetween("id", "some id","some other id", String.class);
		boolean hasIndexRangeKeyCondition = criteria.hasIndexRangeKeyCondition();
		Assert.assertTrue(hasIndexRangeKeyCondition);
	}
	
	@Test
	public void testHasIndexRangeKeyCondition_WhenConditionCriteriaIsNonEqualityConditionOnAPropertyWhichIsNeitherAnIndexRangeKeyOrAHashKey()
	{
		Mockito.when(entityInformation.isGlobalIndexRangeKeyProperty("joinDate")).thenReturn(false);
		criteria.withPropertyBetween("joinDate", new Date(),new Date(), Date.class);
		boolean hasIndexRangeKeyCondition = criteria.hasIndexRangeKeyCondition();
		Assert.assertFalse(hasIndexRangeKeyCondition);
	}
	
	
	
	
}
