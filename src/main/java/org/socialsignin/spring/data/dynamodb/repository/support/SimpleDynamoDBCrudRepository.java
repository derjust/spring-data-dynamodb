/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.socialsignin.spring.data.dynamodb.repository.support;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.socialsignin.spring.data.dynamodb.core.DynamoDBOperations;
import org.socialsignin.spring.data.dynamodb.repository.DynamoDBCrudRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.util.Assert;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.KeyPair;

/**
 * Default implementation of the
 * {@link org.springframework.data.repository.CrudRepository} interface.
 * 
 * @author Michael Lavelle
 * 
 * @param <T>
 *            the type of the entity to handle
 * @param <ID>
 *            the type of the entity's identifier
 */
public class SimpleDynamoDBCrudRepository<T, ID extends Serializable>
		implements DynamoDBCrudRepository<T, ID> {

	protected DynamoDBEntityInformation<T, ID> entityInformation;

	protected Class<T> domainType;

	protected EnableScanPermissions enableScanPermissions;
	
	protected DynamoDBOperations dynamoDBOperations;

	public SimpleDynamoDBCrudRepository(
			DynamoDBEntityInformation<T, ID> entityInformation,
			DynamoDBOperations dynamoDBOperations,
			EnableScanPermissions enableScanPermissions) {
		Assert.notNull(entityInformation);
		Assert.notNull(dynamoDBOperations);
		
		this.entityInformation = entityInformation;
		this.dynamoDBOperations = dynamoDBOperations;
		this.domainType = entityInformation.getJavaType();
		this.enableScanPermissions = enableScanPermissions;

	}

	@Override
	public T findOne(ID id) {
		if (entityInformation.isRangeKeyAware()) {
			return dynamoDBOperations.load(domainType,
					entityInformation.getHashKey(id),
					entityInformation.getRangeKey(id));
		} else {
			return dynamoDBOperations.load(domainType,
					entityInformation.getHashKey(id));
		}
	}

	@SuppressWarnings("unchecked")
	public List<T> findAll(Iterable<ID> ids) {
		Map<Class<?>, List<KeyPair>> keyPairsMap = new HashMap<Class<?>, List<KeyPair>>();
		List<KeyPair> keyPairs = new ArrayList<KeyPair>();
		for (ID id : ids) {
			if (entityInformation.isRangeKeyAware()) {
				keyPairs.add(new KeyPair().withHashKey(
						entityInformation.getHashKey(id)).withRangeKey(
						entityInformation.getRangeKey(id)));
			} else {
				keyPairs.add(new KeyPair().withHashKey(id));
			}
		}
		keyPairsMap.put(domainType, keyPairs);
		return (List<T>) dynamoDBOperations.batchLoad(keyPairsMap).get(dynamoDBOperations.getOverriddenTableName(entityInformation.getDynamoDBTableName()));
	}

	protected T load(ID id) {
		if (entityInformation.isRangeKeyAware()) {
			return dynamoDBOperations.load(domainType,
					entityInformation.getHashKey(id),
					entityInformation.getRangeKey(id));
		} else {
			return dynamoDBOperations.load(domainType,
					entityInformation.getHashKey(id));
		}
	}

	@SuppressWarnings("unchecked")
	protected List<T> loadBatch(Iterable<ID> ids) {
		Map<Class<?>, List<KeyPair>> keyPairsMap = new HashMap<Class<?>, List<KeyPair>>();
		List<KeyPair> keyPairs = new ArrayList<KeyPair>();
		for (ID id : ids) {
			if (entityInformation.isRangeKeyAware()) {
				keyPairs.add(new KeyPair().withHashKey(
						entityInformation.getHashKey(id)).withRangeKey(
						entityInformation.getRangeKey(id)));
			} else {
				keyPairs.add(new KeyPair().withHashKey(id));

			}
		}
		keyPairsMap.put(domainType, keyPairs);
		return (List<T>) dynamoDBOperations.batchLoad(keyPairsMap).get(domainType);
	}

	
	@Override
	public <S extends T> S save(S entity) {

		dynamoDBOperations.save(entity);
		return entity;
	}

	@Override
	public <S extends T> List<S> save(Iterable<S> entities) {
		final List<S> entityList = new ArrayList<S>();
		for (S entity : entities) {
			entityList.add(entity);
		}
		dynamoDBOperations.batchSave(entityList);
		return entityList;
	}

	@Override
	public boolean exists(ID id) {

		Assert.notNull(id, "The given id must not be null!");
		return findOne(id) != null;
	}

	public void assertScanEnabled(boolean scanEnabled, String methodName) {
		Assert.isTrue(
				scanEnabled,
				"Scanning for unpaginated "
						+ methodName
						+ "() queries is not enabled.  "
						+ "To enable, re-implement the "
						+ methodName
						+ "() method in your repository interface and annotate with @EnableScan, or "
						+ "enable scanning for all repository methods by annotating your repository interface with @EnableScan");
	}

	@Override
	public List<T> findAll() {

		assertScanEnabled(
				enableScanPermissions.isFindAllUnpaginatedScanEnabled(),
				"findAll");
		DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
		return dynamoDBOperations.scan(domainType, scanExpression);
	}

	@Override
	public long count() {
		assertScanEnabled(
				enableScanPermissions.isCountUnpaginatedScanEnabled(), "count");
		final DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
		return dynamoDBOperations.count(domainType, scanExpression);
	}

	@Override
	public void delete(ID id) {

		Assert.notNull(id, "The given id must not be null!");

		T entity = findOne(id);
		if (entity == null) {
			throw new EmptyResultDataAccessException(String.format(
					"No %s entity with id %s exists!", domainType, id), 1);
		}
		dynamoDBOperations.delete(entity);
	}

	@Override
	public void delete(T entity) {
		Assert.notNull(entity, "The entity must not be null!");
		dynamoDBOperations.delete(entity);
	}

	@Override
	public void delete(Iterable<? extends T> entities) {

		Assert.notNull(entities, "The given Iterable of entities not be null!");

		List<T> entityList = new ArrayList<T>();
		for (T entity : entities) {
			entityList.add(entity);
		}
		dynamoDBOperations.batchDelete(entityList);
	}

	@Override
	public void deleteAll() {

		assertScanEnabled(
				enableScanPermissions.isDeleteAllUnpaginatedScanEnabled(),
				"deleteAll");
		dynamoDBOperations.batchDelete(findAll());
	}

}
