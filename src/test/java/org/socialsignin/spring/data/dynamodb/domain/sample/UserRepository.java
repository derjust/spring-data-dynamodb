/**
 * Copyright © 2013 spring-data-dynamodb (https://github.com/derjust/spring-data-dynamodb)
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

import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.repository.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends Repository<User, String> {

	// CRUD method using Optional
	Optional<User> findById(String id);

	@EnableScan
	List<User> findByLeaveDate(Instant leaveDate);

	@EnableScan
	List<User> findByNameOrderByNameAsc(String name);

	@EnableScan
	List<User> findAll();

	@EnableScan
	Optional<User> findByName(String name);

	<T extends User> User save(T entity);
}
