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
package org.socialsignin.spring.data.dynamodb.domain.sample;

import org.junit.Before;
import org.junit.Rule;

import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories;
import org.socialsignin.spring.data.dynamodb.utils.DynamoDBLocalResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {DynamoDBLocalResource.class, CRUDOperationsIT.TestAppConfig.class})
@TestPropertySource(properties = {"spring.data.dynamodb.entity2ddl.auto=create"})
public class CRUDOperationsIT {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Configuration
	@EnableDynamoDBRepositories(basePackages = "org.socialsignin.spring.data.dynamodb.domain.sample")
	public static class TestAppConfig {
	}

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private UserPaginationRepository userPaginationRepository;
	@Autowired
	private PlaylistRepository playlistRepository;

	@Before
	public void setUp() {
		userRepository.deleteAll();
		userPaginationRepository.deleteAll();
		playlistRepository.deleteAll();
	}

	@Test
	public void testProjection() {
		String postCode = "postCode";
		String user1 = "projection1" + ThreadLocalRandom.current().nextLong();
		String user2 = "projection2" + ThreadLocalRandom.current().nextLong();
		String user3 = "projection2" + ThreadLocalRandom.current().nextLong();

		User u1 = new User();
		u1.setId("Id1" + ThreadLocalRandom.current().nextLong());
		u1.setName(user1);
		u1.setLeaveDate(Instant.now());
		u1.setPostCode(postCode);
		u1.setNumberOfPlaylists(1);

		User u3 = new User();
		u3.setId("Id3" + ThreadLocalRandom.current().nextLong());
		u3.setName(user3);
		u3.setLeaveDate(Instant.now());
		u3.setPostCode(postCode);
		u3.setNumberOfPlaylists(1);

		User u2 = new User();
		u2.setId("Id2" + ThreadLocalRandom.current().nextLong());
		u2.setName(user2);
		u2.setLeaveDate(Instant.now());
		u2.setPostCode(postCode + postCode);
		u2.setNumberOfPlaylists(2);

		userRepository.save(u1);
		userRepository.save(u2);
		userRepository.save(u3);

		List<User> actualList = new ArrayList<>();
		userRepository.findAll().forEach(actualList::add);

		List<User> projectedActuals = userRepository.findByPostCode(postCode);
		// 2 matches but should be limited to 1 by @Query
		assertEquals(1, projectedActuals.size());
		User projectedActual = projectedActuals.get(0);
		assertNull("Attribute not projected", projectedActual.getName());
		assertNull("Attribute not projected", projectedActual.getPostCode());
		assertNull("Attribute not projected", projectedActual.getNumberOfPlaylists());
		assertNull("Key not projected", projectedActual.getId());
		assertNotNull("LeaveDate is projected", projectedActual.getLeaveDate());

		List<User> fullActuals = userRepository.findByNameIn(Arrays.asList(user1, user2, user3));
		assertEquals(3, fullActuals.size());
		User fullActual = fullActuals.get(0);
		assertThat(Arrays.asList(user1, user2, user3), hasItems(fullActual.getName()));
		assertThat(Arrays.asList(user1, user2, user3), hasItems(fullActuals.get(1).getName()));
		assertNotNull(fullActual.getPostCode());
		assertNotNull(fullActual.getNumberOfPlaylists());
		assertNotNull(fullActual.getId());
		assertNotNull(fullActual.getLeaveDate());
	}

	@Test
	public void testEmptyResult() throws InterruptedException, ExecutionException {

		Future<User> actual1 = userRepository.findByNameAndPostCode("does not", "exist");
		assertNull(actual1.get());

		User actual2 = userRepository.findByNameAndLeaveDate("does not exist", Instant.now());
		assertNull(actual2);
	}

	@Test
	public void testDelete() {
		// Prepare
		User u1 = new User();
		String name1 = "name1" + ThreadLocalRandom.current().nextLong();
		u1.setName(name1);
		u1.setId("u1");

		User u2 = new User();
		String name2 = "name1" + ThreadLocalRandom.current().nextLong();
		u2.setId("u2");
		u2.setName(name2);

		User u3 = new User();
		String name3 = "name1" + ThreadLocalRandom.current().nextLong();
		u3.setId("u3");
		u3.setName(name3);

		userRepository.save(u1);
		userRepository.save(u2);
		userRepository.save(u3);

		List<User> actualList = new ArrayList<>();
		userRepository.findAll().forEach(actualList::add);
		assertEquals("Unexpected List: " + actualList, 3, actualList.size());
		actualList.clear();

		userRepository.findByNameIn(Arrays.asList(name1, name2)).forEach(actualList::add);
		assertEquals("Unexpected List: " + actualList, 2, actualList.size());
		actualList.clear();

		// Delete specific
		userRepository.deleteById("u2");
		userRepository.findAll().forEach(actualList::add);
		assertEquals("u1", actualList.get(0).getId());
		assertEquals("u3", actualList.get(1).getId());

		// Delete conditional
		userRepository.deleteByIdAndName("u1", name1);
		Optional<User> actualUser = userRepository.findById("u1");
		assertFalse("User should have been deleted!", actualUser.isPresent());
	}

	@Test
	public void testDeleteNonExistent() {

		expectedException.expect(EmptyResultDataAccessException.class);
		// Delete specific
		userRepository.deleteById("non-existent");
	}

	@Test
	public void testDeleteHashRangeKey() {
		// setup
		long rnd = ThreadLocalRandom.current().nextLong();
		Playlist p = new Playlist();
		p.setPlaylistName("playlistName-" + rnd);
		p.setUserName("userName-" + rnd);

		playlistRepository.save(p);

		PlaylistId id = new PlaylistId("userName-" + rnd, "playlistName-" + rnd);
		assertTrue("Entity with id not found: " + id, playlistRepository.findById(id).isPresent());

		playlistRepository.deleteById(id);
		assertFalse("Entity with id not deleted: " + id, playlistRepository.findById(id).isPresent());
	}

	@Test
	public void testFilterAndPagination() {

		Supplier<User> userSupplier = () -> {
			User u = new User();
			u.setName("test");
			return u;
		};

		for (int i = 0; i < 22; i++) {
			User u = userSupplier.get();
			userPaginationRepository.save(u);
		}
		User u = userSupplier.get();
		u.setName("not-test");
		userPaginationRepository.save(u);

		List<User> allUsers = userPaginationRepository.findAll();
		assertEquals(23, allUsers.size());

		List<User> allTestUsers = userPaginationRepository.findAllByName("test");
		assertEquals(22, allTestUsers.size());

		Pageable firstPage = PageRequest.of(0, 10);
		Page<User> firstResults = userPaginationRepository.findAllByName("test", firstPage);
		assertEquals(10, firstResults.getNumberOfElements());

		Pageable secondPage = PageRequest.of(1, 10);
		Page<User> secondResults = userPaginationRepository.findAllByName("test", secondPage);
		assertEquals(10, secondResults.getNumberOfElements());

		Pageable thirdPage = PageRequest.of(2, 10);
		Page<User> thirdResults = userPaginationRepository.findAllByName("test", thirdPage);
		assertEquals(2, thirdResults.getNumberOfElements());

		Pageable wholePage = Pageable.unpaged();
		Page<User> wholeResults = userPaginationRepository.findAllByName("test", wholePage);
		assertEquals(22, wholeResults.getNumberOfElements());
	}

	@Test
	public void testDeleteNonExistentIdWithCondition() {
		// Delete conditional
		userRepository.deleteByIdAndName("non-existent", "non-existent");
	}

	@Test
	public void testDeleteNonExistingGsiWithConditoin() {
		// Delete via GSI
		userRepository.deleteByPostCodeAndNumberOfPlaylists("non-existing", 23);

	}
}
