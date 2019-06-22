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
package org.socialsignin.spring.data.dynamodb.query;

import org.socialsignin.spring.data.dynamodb.core.DynamoDBOperations;

/**
 * @author Michael Lavelle
 * @author Sebastian Just
 */
public class CountByHashKeyQuery<T> extends AbstractSingleEntityQuery<Long> implements Query<Long> {

	private Object hashKey;
	private Class<T> entityClass;

	public CountByHashKeyQuery(DynamoDBOperations dynamoDBOperations, Class<T> clazz, Object hashKey) {
		super(dynamoDBOperations, Long.class);
		this.hashKey = hashKey;
		this.entityClass = clazz;
	}

	@Override
	public Long getSingleResult() {
		return dynamoDBOperations.load(entityClass, hashKey) == null ? 0l : 1l;
	}

}
