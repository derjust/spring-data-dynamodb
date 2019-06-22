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

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper.FailedBatch;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.KeyPair;
import org.socialsignin.spring.data.dynamodb.core.DynamoDBOperations;
import org.socialsignin.spring.data.dynamodb.exception.BatchWriteException;
import org.socialsignin.spring.data.dynamodb.repository.DynamoDBCrudRepository;
import org.socialsignin.spring.data.dynamodb.utils.ExceptionHandler;
import org.socialsignin.spring.data.dynamodb.utils.SortHandler;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Default implementation of the
 * {@link org.springframework.data.repository.CrudRepository} interface.
 * 
 * @param <T>
 *            the type of the entity to handle
 * @param <ID>
 *            the type of the entity's identifier
 */
public class SimpleDynamoDBCrudRepository<T, ID>
		implements
			DynamoDBCrudRepository<T, ID>,
			SortHandler,
			ExceptionHandler {

	protected DynamoDBEntityInformation<T, ID> entityInformation;

	protected Class<T> domainType;

	protected EnableScanPermissions enableScanPermissions;

	protected DynamoDBOperations dynamoDBOperations;

	public SimpleDynamoDBCrudRepository(DynamoDBEntityInformation<T, ID> entityInformation,
			DynamoDBOperations dynamoDBOperations, EnableScanPermissions enableScanPermissions) {
		Assert.notNull(entityInformation, "entityInformation must not be null");
		Assert.notNull(dynamoDBOperations, "dynamoDBOperations must not be null");

		this.entityInformation = entityInformation;
		this.dynamoDBOperations = dynamoDBOperations;
		this.domainType = entityInformation.getJavaType();
		this.enableScanPermissions = enableScanPermissions;
	}

	@Override
	public Optional<T> findById(ID id) {

		Assert.notNull(id, "The given id must not be null!");

		T result;
		if (entityInformation.isRangeKeyAware()) {
			result = dynamoDBOperations.load(domainType, entityInformation.getHashKey(id),
					entityInformation.getRangeKey(id));
		} else {
			result = dynamoDBOperations.load(domainType, entityInformation.getHashKey(id));
		}

		return Optional.ofNullable(result);
	}

	@Override
	public List<T> findAllById(Iterable<ID> ids) {

		Assert.notNull(ids, "The given ids must not be null!");

		// Works only with non-parallel streams!
		AtomicInteger idx = new AtomicInteger();
		List<KeyPair> keyPairs = StreamSupport.stream(ids.spliterator(), false).map(id -> {

			Assert.notNull(id, "The given id at position " + idx.getAndIncrement() + " must not be null!");

			if (entityInformation.isRangeKeyAware()) {
				return new KeyPair().withHashKey(entityInformation.getHashKey(id))
						.withRangeKey(entityInformation.getRangeKey(id));
			} else {
				return new KeyPair().withHashKey(id);
			}
		}).collect(Collectors.toList());

		Map<Class<?>, List<KeyPair>> keyPairsMap = Collections.<Class<?>, List<KeyPair>>singletonMap(domainType,
				keyPairs);
		return dynamoDBOperations.batchLoad(keyPairsMap);
	}

	@Override
	public <S extends T> S save(S entity) {

		dynamoDBOperations.save(entity);
		return entity;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws BatchWriteException
	 *             in case of an error during saving
	 */
	@Override
	public <S extends T> Iterable<S> saveAll(Iterable<S> entities)
			throws BatchWriteException, IllegalArgumentException {

		Assert.notNull(entities, "The given Iterable of entities not be null!");
		List<FailedBatch> failedBatches = dynamoDBOperations.batchSave(entities);

		if (failedBatches.isEmpty()) {
			// Happy path
			return entities;
		} else {
			// Error handling:
			throw repackageToException(failedBatches, BatchWriteException.class);
		}
	}

	@Override
	public boolean existsById(ID id) {

		Assert.notNull(id, "The given id must not be null!");
		return findById(id).isPresent();
	}

	void assertScanEnabled(boolean scanEnabled, String methodName) {
		Assert.isTrue(scanEnabled, "Scanning for unpaginated " + methodName + "() queries is not enabled.  "
				+ "To enable, re-implement the " + methodName
				+ "() method in your repository interface and annotate with @EnableScan, or "
				+ "enable scanning for all repository methods by annotating your repository interface with @EnableScan");
	}

	@Override
	public List<T> findAll() {

		assertScanEnabled(enableScanPermissions.isFindAllUnpaginatedScanEnabled(), "findAll");
		DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
		return dynamoDBOperations.scan(domainType, scanExpression);
	}

	@Override
	public long count() {
		assertScanEnabled(enableScanPermissions.isCountUnpaginatedScanEnabled(), "count");
		final DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
		return dynamoDBOperations.count(domainType, scanExpression);
	}

	@Override
	public void deleteById(ID id) {

		Assert.notNull(id, "The given id must not be null!");

		Optional<T> entity = findById(id);

		if (entity.isPresent()) {
			dynamoDBOperations.delete(entity.get());

		} else {
			throw new EmptyResultDataAccessException(String.format("No %s entity with id %s exists!", domainType, id),
					1);
		}
	}

	@Override
	public void delete(T entity) {
		Assert.notNull(entity, "The entity must not be null!");
		dynamoDBOperations.delete(entity);
	}

	@Override
	public void deleteAll(Iterable<? extends T> entities) {

		Assert.notNull(entities, "The given Iterable of entities not be null!");
		dynamoDBOperations.batchDelete(entities);
	}

	@Override
	public void deleteAll() {

		assertScanEnabled(enableScanPermissions.isDeleteAllUnpaginatedScanEnabled(), "deleteAll");
		dynamoDBOperations.batchDelete(findAll());
	}

	@NonNull
	public DynamoDBEntityInformation<T, ID> getEntityInformation() {
		return this.entityInformation;
	}
}
