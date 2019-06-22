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
package org.socialsignin.spring.data.dynamodb.core;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper.FailedBatch;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperTableModel;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.KeyPair;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedScanList;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;

import java.util.List;
import java.util.Map;

/**
 * Interface to DynmaoDB - as seen from the Spring-Data world
 */
public interface DynamoDBOperations {

	<T> int count(Class<T> domainClass, DynamoDBQueryExpression<T> queryExpression);
	<T> int count(Class<T> domainClass, DynamoDBScanExpression scanExpression);
	<T> int count(Class<T> clazz, QueryRequest mutableQueryRequest);

	<T> PaginatedQueryList<T> query(Class<T> clazz, QueryRequest queryRequest);
	<T> PaginatedQueryList<T> query(Class<T> domainClass, DynamoDBQueryExpression<T> queryExpression);
	<T> PaginatedScanList<T> scan(Class<T> domainClass, DynamoDBScanExpression scanExpression);

	<T> T load(Class<T> domainClass, Object hashKey, Object rangeKey);
	<T> T load(Class<T> domainClass, Object hashKey);
	<T> List<T> batchLoad(Map<Class<?>, List<KeyPair>> itemsToGet);

	<T> T save(T entity);
	List<FailedBatch> batchSave(Iterable<?> entities);

	<T> T delete(T entity);
	List<FailedBatch> batchDelete(Iterable<?> entities);

	<T> String getOverriddenTableName(Class<T> domainClass, String tableName);

	/**
	 * Provides access to the DynamoDB mapper table model of the underlying domain
	 * type.
	 *
	 * @param <T>
	 *            The type of the domain type itself
	 * @param domainClass
	 *            A domain type
	 * @return Corresponding DynamoDB table model
	 */
	<T> DynamoDBMapperTableModel<T> getTableModel(Class<T> domainClass);
}
