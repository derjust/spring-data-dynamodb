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

import org.socialsignin.spring.data.dynamodb.core.DynamoDBOperations;
import org.socialsignin.spring.data.dynamodb.mapping.DynamoDBMappingContext;
import org.socialsignin.spring.data.dynamodb.repository.util.DynamoDBMappingContextProcessor;
import org.socialsignin.spring.data.dynamodb.repository.util.Entity2DynamoDBTableSynchronizer;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

import java.io.Serializable;

/**
 * Special adapter for Springs
 * {@link org.springframework.beans.factory.FactoryBean} interface to allow easy
 * setup of repository factories via Spring configuration.
 * 
 * @author Michael Lavelle
 * @author Sebastian Just
 * @param <T>
 *            the type of the repository
 */
public class DynamoDBRepositoryFactoryBean<T extends Repository<S, ID>, S, ID extends Serializable>
		extends
			RepositoryFactoryBeanSupport<T, S, ID> {

	private DynamoDBOperations dynamoDBOperations;
	private Entity2DynamoDBTableSynchronizer<S, ID> tableSynchronizer;
	private DynamoDBMappingContextProcessor<S, ID> dynamoDBMappingContextProcessor;

	public DynamoDBRepositoryFactoryBean(Class<? extends T> repositoryInterface) {
		super(repositoryInterface);
	}

	@Override
	protected RepositoryFactorySupport createRepositoryFactory() {
		assert dynamoDBOperations != null;
		assert tableSynchronizer != null;
		assert dynamoDBMappingContextProcessor != null;
		DynamoDBRepositoryFactory dynamoDBRepositoryFactory = new DynamoDBRepositoryFactory(dynamoDBOperations);
		dynamoDBRepositoryFactory.addRepositoryProxyPostProcessor(tableSynchronizer);
		dynamoDBRepositoryFactory.addRepositoryProxyPostProcessor(dynamoDBMappingContextProcessor);
		return dynamoDBRepositoryFactory;
	}

	@Required
	public void setDynamoDBMappingContextProcessor(
			DynamoDBMappingContextProcessor<S, ID> dynamoDBMappingContextProcessor) {
		this.dynamoDBMappingContextProcessor = dynamoDBMappingContextProcessor;
	}

	@Required
	public void setEntity2DynamoDBTableSynchronizer(Entity2DynamoDBTableSynchronizer<S, ID> tableSynchronizer) {
		this.tableSynchronizer = tableSynchronizer;
	}

	@Required
	public void setDynamoDBOperations(DynamoDBOperations dynamoDBOperations) {
		this.dynamoDBOperations = dynamoDBOperations;
	}

	@Required
	public void setDynamoDBMappingContext(DynamoDBMappingContext dynamoDBMappingContext) {
		setMappingContext(dynamoDBMappingContext);
	}
}
