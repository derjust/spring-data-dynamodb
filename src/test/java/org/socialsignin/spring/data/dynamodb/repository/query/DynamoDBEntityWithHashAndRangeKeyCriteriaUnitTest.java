package org.socialsignin.spring.data.dynamodb.repository.query;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.socialsignin.spring.data.dynamodb.domain.sample.Playlist;
import org.socialsignin.spring.data.dynamodb.repository.support.DynamoDBIdIsHashAndRangeKeyEntityInformation;

@RunWith(MockitoJUnitRunner.class)
public class DynamoDBEntityWithHashAndRangeKeyCriteriaUnitTest extends AbstractDynamoDBQueryCriteriaUnitTests<DynamoDBEntityWithHashAndRangeKeyCriteria<Playlist,String>> {
	
	@Mock
	private DynamoDBIdIsHashAndRangeKeyEntityInformation<Playlist,String> entityInformation;
	
	
	@Before
	public void setUp()
	{
		Mockito.when(entityInformation.getHashKeyPropertyName()).thenReturn("userName");
		Mockito.when(entityInformation.getRangeKeyPropertyName()).thenReturn("playlistName");
		criteria = new DynamoDBEntityWithHashAndRangeKeyCriteria<Playlist,String>(entityInformation);
	}
	
	@Test
	public void testHasIndexHashKeyEqualConditionAnd_WhenConditionCriteriaIsEqualityOnAPropertyWhichIsAnIndexHashKeyButNotAHashKeyOrRangeKey()
	{
		Mockito.when(entityInformation.isGlobalIndexHashKeyProperty("displayName")).thenReturn(true);
		criteria.withPropertyEquals("displayName", "some display name", String.class);
		boolean hasIndexHashKeyEqualCondition = criteria.hasIndexHashKeyEqualCondition();
		Assert.assertTrue(hasIndexHashKeyEqualCondition);
	}
	
	@Test
	public void testHasIndexHashKeyEqualCondition_WhenConditionCriteriaIsEqualityOnAPropertyWhichIsNotAnIndexHashKeyButIsAHashKey()
	{
		Mockito.when(entityInformation.isGlobalIndexHashKeyProperty("userName")).thenReturn(false);
		criteria.withPropertyEquals("userName", "some user name", String.class);
		boolean hasIndexHashKeyEqualCondition = criteria.hasIndexHashKeyEqualCondition();
		Assert.assertFalse(hasIndexHashKeyEqualCondition);
	}
	
	@Test
	public void testHasIndexHashKeyEqualCondition_WhenConditionCriteriaIsEqualityOnAPropertyWhichIsNotAnIndexHashKeyButIsARangeKey()
	{
		Mockito.when(entityInformation.isGlobalIndexHashKeyProperty("playlistName")).thenReturn(false);
		criteria.withPropertyEquals("playlistName", "some playlist name", String.class);
		boolean hasIndexHashKeyEqualCondition = criteria.hasIndexHashKeyEqualCondition();
		Assert.assertFalse(hasIndexHashKeyEqualCondition);
	}
	
	@Test
	public void testHasIndexHashKeyEqualCondition_WhenConditionCriteriaIsEqualityOnAPropertyWhichIsBothAnIndexHashKeyAndAHashKey()
	{
		Mockito.when(entityInformation.isGlobalIndexHashKeyProperty("userName")).thenReturn(true);
		criteria.withPropertyEquals("userName", "some user name", String.class);
		boolean hasIndexHashKeyEqualCondition = criteria.hasIndexHashKeyEqualCondition();
		Assert.assertTrue(hasIndexHashKeyEqualCondition);
	}
	
	@Test
	public void testHasIndexHashKeyEqualCondition_WhenConditionCriteriaIsEqualityOnAPropertyWhichIsBothAnIndexHashKeyAndARangeKey()
	{
		Mockito.when(entityInformation.isGlobalIndexHashKeyProperty("playlistName")).thenReturn(true);
		criteria.withPropertyEquals("playlistName", "some playlist name", String.class);
		boolean hasIndexHashKeyEqualCondition = criteria.hasIndexHashKeyEqualCondition();
		Assert.assertTrue(hasIndexHashKeyEqualCondition);
	}
	
	
	@Test
	public void testHasIndexHashKeyEqualCondition_WhenConditionCriteriaIsEqualityOnAPropertyWhichIsNeitherAnIndexHashKeyOrAHashKeyOrRangeKey()
	{
		Mockito.when(entityInformation.isGlobalIndexHashKeyProperty("displayName")).thenReturn(false);
		criteria.withPropertyEquals("displayName", "some display name", String.class);
		boolean hasIndexHashKeyEqualCondition = criteria.hasIndexHashKeyEqualCondition();
		Assert.assertFalse(hasIndexHashKeyEqualCondition);
	}
	
	@Test
	public void testHasIndexHashKeyEqualCondition_WhenConditionCriteriaIsEqualityOnAPropertyWhichIsNeitherAnIndexHashKeyOrAHashKeyButIsRangeKey()
	{
		Mockito.when(entityInformation.isGlobalIndexHashKeyProperty("playlistName")).thenReturn(false);
		criteria.withPropertyEquals("playlistName", "some playlist name", String.class);
		boolean hasIndexHashKeyEqualCondition = criteria.hasIndexHashKeyEqualCondition();
		Assert.assertFalse(hasIndexHashKeyEqualCondition);
	}
	
	@Test
	public void testHasIndexRangeKeyCondition_WhenConditionCriteriaIsEqualityOnAPropertyWhichIsAnIndexRangeKeyButNotAHashKeyOrRangeKey()
	{
		Mockito.when(entityInformation.isGlobalIndexRangeKeyProperty("displayName")).thenReturn(true);
		criteria.withPropertyEquals("displayName", "some display name", String.class);
		boolean hasIndexRangeKeyCondition = criteria.hasIndexRangeKeyCondition();
		Assert.assertTrue(hasIndexRangeKeyCondition);
	}
	
	@Test
	public void testHasIndexRangeKeyCondition_WhenConditionCriteriaIsEqualityOnAPropertyWhichIsAnIndexRangeKeyButNotAHashKeyAndIsARangeKey()
	{
		Mockito.when(entityInformation.isGlobalIndexRangeKeyProperty("playlistName")).thenReturn(true);
		criteria.withPropertyEquals("playlistName", "some playlist name", String.class);
		boolean hasIndexRangeKeyCondition = criteria.hasIndexRangeKeyCondition();
		Assert.assertTrue(hasIndexRangeKeyCondition);
	}
	
	@Test
	public void testHasIndexRangeKeyCondition_WhenConditionCriteriaIsEqualityOnAPropertyWhichIsNotAnIndexRangeKeyButIsAHashKey()
	{
		Mockito.when(entityInformation.isGlobalIndexRangeKeyProperty("userName")).thenReturn(false);
		criteria.withPropertyEquals("userName", "some user name", String.class);
		boolean hasIndexRangeKeyCondition = criteria.hasIndexRangeKeyCondition();
		Assert.assertFalse(hasIndexRangeKeyCondition);
	}
	
	@Test
	public void testHasIndexRangeKeyCondition_WhenConditionCriteriaIsEqualityOnAPropertyWhichIsNotAnIndexRangeKeyButIsARangeKey()
	{
		Mockito.when(entityInformation.isGlobalIndexRangeKeyProperty("playlistName")).thenReturn(false);
		criteria.withPropertyEquals("playlist name", "some playlist name", String.class);
		boolean hasIndexRangeKeyCondition = criteria.hasIndexRangeKeyCondition();
		Assert.assertFalse(hasIndexRangeKeyCondition);
	}
	
	@Test
	public void testHasIndexRangeKeyCondition_WhenConditionCriteriaIsEqualityOnAPropertyWhichIsBothAnIndexRangeKeyAndAHashKey()
	{
		Mockito.when(entityInformation.isGlobalIndexRangeKeyProperty("userName")).thenReturn(true);
		criteria.withPropertyEquals("userName", "some user name", String.class);
		boolean hasIndexRangeKeyCondition = criteria.hasIndexRangeKeyCondition();
		Assert.assertTrue(hasIndexRangeKeyCondition);
	}
	
	@Test
	public void testHasIndexRangeKeyCondition_WhenConditionCriteriaIsEqualityOnAPropertyWhichIsNeitherAnIndexRangeKeyOrAHashKeyOrARangeKey()
	{
		Mockito.when(entityInformation.isGlobalIndexRangeKeyProperty("displayName")).thenReturn(false);
		criteria.withPropertyEquals("displayName", "some display name", String.class);
		boolean hasIndexRangeKeyCondition = criteria.hasIndexRangeKeyCondition();
		Assert.assertFalse(hasIndexRangeKeyCondition);
	}
	
	@Test
	public void testHasIndexRangeKeyCondition_WhenConditionCriteriaIsEqualityOnAPropertyWhichIsNeitherAnIndexRangeKeyOrAHashKeyButIsARangeKey()
	{
		Mockito.when(entityInformation.isGlobalIndexRangeKeyProperty("playlistName")).thenReturn(false);
		criteria.withPropertyEquals("playlistName", "some playlist name", String.class);
		boolean hasIndexRangeKeyCondition = criteria.hasIndexRangeKeyCondition();
		Assert.assertFalse(hasIndexRangeKeyCondition);
	}
	
	// repeat
	
	@Test
	public void testHasIndexHashKeyEqualConditionAnd_WhenConditionCriteriaIsNonEqualityConditionOnAPropertyWhichIsAnIndexHashKeyButNotAHashKeyOrRangeKey()
	{
		Mockito.when(entityInformation.isGlobalIndexHashKeyProperty("displayName")).thenReturn(true);
		criteria.withPropertyBetween("displayName", "some display name","some other display name", String.class);
		boolean hasIndexHashKeyEqualCondition = criteria.hasIndexHashKeyEqualCondition();
		Assert.assertFalse(hasIndexHashKeyEqualCondition);
	}
	
	@Test
	public void testHasIndexHashKeyEqualConditionAnd_WhenConditionCriteriaIsNonEqualityConditionOnAPropertyWhichIsAnIndexHashKeyButNotAHashKeyButIsARangeKey()
	{
		Mockito.when(entityInformation.isGlobalIndexHashKeyProperty("playlistName")).thenReturn(true);
		criteria.withPropertyBetween("playlistName", "some playlist name","some other playlist name", String.class);
		boolean hasIndexHashKeyEqualCondition = criteria.hasIndexHashKeyEqualCondition();
		Assert.assertFalse(hasIndexHashKeyEqualCondition);
	}
	
	@Test
	public void testHasIndexHashKeyEqualCondition_WhenConditionCriteriaIsNonEqualityConditionOnAPropertyWhichIsNotAnIndexHashKeyButIsAHashKey()
	{
		Mockito.when(entityInformation.isGlobalIndexHashKeyProperty("userName")).thenReturn(false);
		criteria.withPropertyBetween("userName", "some user name","some other user name", String.class);
		boolean hasIndexHashKeyEqualCondition = criteria.hasIndexHashKeyEqualCondition();
		Assert.assertFalse(hasIndexHashKeyEqualCondition);
	}
	
	@Test
	public void testHasIndexHashKeyEqualCondition_WhenConditionCriteriaIsNonEqualityConditionOnAPropertyWhichIsBothAnIndexHashKeyAndAHashKey()
	{
		Mockito.when(entityInformation.isGlobalIndexHashKeyProperty("userName")).thenReturn(true);
		criteria.withPropertyBetween("userName", "some user name","some other user name", String.class);
		boolean hasIndexHashKeyEqualCondition = criteria.hasIndexHashKeyEqualCondition();
		Assert.assertFalse(hasIndexHashKeyEqualCondition);
	}
	
	@Test
	public void testHasIndexHashKeyEqualCondition_WhenConditionCriteriaIsNonEqualityConditionOnAPropertyWhichIsNeitherAnIndexHashKeyOrAHashKeyOrARangeKey()
	{
		Mockito.when(entityInformation.isGlobalIndexHashKeyProperty("displayName")).thenReturn(false);
		criteria.withPropertyBetween("displayName", "some display name","some other display name", String.class);
		boolean hasIndexHashKeyEqualCondition = criteria.hasIndexHashKeyEqualCondition();
		Assert.assertFalse(hasIndexHashKeyEqualCondition);
	}
	
	@Test
	public void testHasIndexHashKeyEqualCondition_WhenConditionCriteriaIsNonEqualityConditionOnAPropertyWhichIsNeitherAnIndexHashKeyOrAHashKeyButIsARangeKey()
	{
		Mockito.when(entityInformation.isGlobalIndexHashKeyProperty("playlistName")).thenReturn(false);
		criteria.withPropertyBetween("playlistName", "some playlist name","some other playlist name", String.class);
		boolean hasIndexHashKeyEqualCondition = criteria.hasIndexHashKeyEqualCondition();
		Assert.assertFalse(hasIndexHashKeyEqualCondition);
	}
	
	@Test
	public void testHasIndexRangeKeyCondition_WhenConditionCriteriaIsNonEqualityConditionOnAPropertyWhichIsAnIndexRangeKeyButNotAHashKeyOrARangeKey()
	{
		Mockito.when(entityInformation.isGlobalIndexRangeKeyProperty("displayName")).thenReturn(true);
		criteria.withPropertyBetween("displayName", "some display name","some other display name", String.class);
		boolean hasIndexRangeKeyCondition = criteria.hasIndexRangeKeyCondition();
		Assert.assertTrue(hasIndexRangeKeyCondition);
	}
	
	@Test
	public void testHasIndexRangeKeyCondition_WhenConditionCriteriaIsNonEqualityConditionOnAPropertyWhichIsAnIndexRangeKeyButNotAHashKeyButIsARangeKey()
	{
		Mockito.when(entityInformation.isGlobalIndexRangeKeyProperty("playlistName")).thenReturn(true);
		criteria.withPropertyBetween("playlistName", "some playlist name","some other playlist name", String.class);
		boolean hasIndexRangeKeyCondition = criteria.hasIndexRangeKeyCondition();
		Assert.assertTrue(hasIndexRangeKeyCondition);
	}
	
	@Test
	public void testHasIndexRangeKeyCondition_WhenConditionCriteriaIsNonEqualityConditionOnAPropertyWhichIsNotAnIndexRangeKeyButIsAHashKey()
	{
		Mockito.when(entityInformation.isGlobalIndexRangeKeyProperty("userName")).thenReturn(false);
		criteria.withPropertyBetween("userName", "some user name","some other user name", String.class);
		boolean hasIndexRangeKeyCondition = criteria.hasIndexRangeKeyCondition();
		Assert.assertFalse(hasIndexRangeKeyCondition);
	}
	
	@Test
	public void testHasIndexRangeKeyCondition_WhenConditionCriteriaIsNonEqualityConditionOnAPropertyWhichIsBothAnIndexRangeKeyAndAHashKey()
	{
		Mockito.when(entityInformation.isGlobalIndexRangeKeyProperty("userName")).thenReturn(true);
		criteria.withPropertyBetween("userName", "some user name","some other user name", String.class);
		boolean hasIndexRangeKeyCondition = criteria.hasIndexRangeKeyCondition();
		Assert.assertTrue(hasIndexRangeKeyCondition);
	}
	
	@Test
	public void testHasIndexRangeKeyCondition_WhenConditionCriteriaIsNonEqualityConditionOnAPropertyWhichIsNeitherAnIndexRangeKeyOrAHashKeyOrARangeKey()
	{
		Mockito.when(entityInformation.isGlobalIndexRangeKeyProperty("displayName")).thenReturn(false);
		criteria.withPropertyBetween("displayName", "some display name","some other display name", String.class);
		boolean hasIndexRangeKeyCondition = criteria.hasIndexRangeKeyCondition();
		Assert.assertFalse(hasIndexRangeKeyCondition);
	}
	
	@Test
	public void testHasIndexRangeKeyCondition_WhenConditionCriteriaIsNonEqualityConditionOnAPropertyWhichIsNeitherAnIndexRangeKeyOrAHashKeyButIsARangeKey()
	{
		Mockito.when(entityInformation.isGlobalIndexRangeKeyProperty("playlistName")).thenReturn(false);
		criteria.withPropertyBetween("playlistName", "some playlist name","some other playlist name", String.class);
		boolean hasIndexRangeKeyCondition = criteria.hasIndexRangeKeyCondition();
		Assert.assertFalse(hasIndexRangeKeyCondition);
	}
	
	
	
	
}
