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
package org.socialsignin.spring.data.dynamodb.repository.support;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.socialsignin.spring.data.dynamodb.core.DynamoDBOperations;
import org.socialsignin.spring.data.dynamodb.domain.sample.Playlist;
import org.socialsignin.spring.data.dynamodb.domain.sample.PlaylistId;
import org.socialsignin.spring.data.dynamodb.domain.sample.User;
import org.springframework.dao.EmptyResultDataAccessException;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DynamoDBSimpleIdRepository}.
 * 
 * @author Michael Lavelle
 * @author Sebastian Just
 */
@RunWith(MockitoJUnitRunner.class)
public class SimpleDynamoDBPagingAndSortingRepositoryUnitTest {

	SimpleDynamoDBPagingAndSortingRepository<User, Long> repoForEntityWithOnlyHashKey;

	SimpleDynamoDBPagingAndSortingRepository<Playlist, PlaylistId> repoForEntityWithHashAndRangeKey;

	@Mock
	DynamoDBOperations dynamoDBOperations;

	private User testUser;

	private Playlist testPlaylist;

	private PlaylistId testPlaylistId;

	@Mock
	EnableScanPermissions mockEnableScanPermissions;

	@Mock
	DynamoDBEntityInformation<User, Long> entityWithOnlyHashKeyInformation;

	@Mock
	DynamoDBEntityInformation<Playlist, PlaylistId> entityWithHashAndRangeKeyInformation;

	@Before
	public void setUp() {

		testUser = new User();

		testPlaylistId = new PlaylistId();
		testPlaylistId.setUserName("michael");
		testPlaylistId.setPlaylistName("playlist1");

		testPlaylist = new Playlist(testPlaylistId);

		when(entityWithOnlyHashKeyInformation.getJavaType()).thenReturn(User.class);
		when(entityWithOnlyHashKeyInformation.getHashKey(1l)).thenReturn(1l);

		when(entityWithHashAndRangeKeyInformation.getJavaType()).thenReturn(Playlist.class);
		when(entityWithHashAndRangeKeyInformation.getHashKey(testPlaylistId)).thenReturn("michael");
		when(entityWithHashAndRangeKeyInformation.getRangeKey(testPlaylistId)).thenReturn("playlist1");
		when(entityWithHashAndRangeKeyInformation.isRangeKeyAware()).thenReturn(true);

		repoForEntityWithOnlyHashKey = new SimpleDynamoDBPagingAndSortingRepository<>(entityWithOnlyHashKeyInformation,
				dynamoDBOperations, mockEnableScanPermissions);
		repoForEntityWithHashAndRangeKey = new SimpleDynamoDBPagingAndSortingRepository<>(
				entityWithHashAndRangeKeyInformation, dynamoDBOperations, mockEnableScanPermissions);

		when(dynamoDBOperations.load(User.class, 1l)).thenReturn(testUser);
		when(dynamoDBOperations.load(Playlist.class, "michael", "playlist1")).thenReturn(testPlaylist);

	}

	/**
	 * @see DATAJPA-177
	 */
	@Test(expected = EmptyResultDataAccessException.class)
	public void throwsExceptionIfEntityWithOnlyHashKeyToDeleteDoesNotExist() {

		repoForEntityWithOnlyHashKey.deleteById(4711L);
	}

	@Test
	public void findOneEntityWithOnlyHashKey() {
		Optional<User> user = repoForEntityWithOnlyHashKey.findById(1l);
		Mockito.verify(dynamoDBOperations).load(User.class, 1l);
		assertEquals(testUser, user.get());
	}

	@Test
	public void findOneEntityWithHashAndRangeKey() {
		Optional<Playlist> playlist = repoForEntityWithHashAndRangeKey.findById(testPlaylistId);
		assertEquals(testPlaylist, playlist.get());
	}

	/**
	 * @see DATAJPA-177
	 */
	@Test(expected = EmptyResultDataAccessException.class)
	public void throwsExceptionIfEntityWithHashAndRangeKeyToDeleteDoesNotExist() {

		PlaylistId playlistId = new PlaylistId();
		playlistId.setUserName("someUser");
		playlistId.setPlaylistName("somePlaylistName");

		repoForEntityWithHashAndRangeKey.deleteById(playlistId);
	}
}
