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
package org.socialsignin.spring.data.dynamodb.repository.util;

import org.socialsignin.spring.data.dynamodb.repository.support.DynamoDBEntityInformation;
import org.socialsignin.spring.data.dynamodb.repository.support.SimpleDynamoDBCrudRepository;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.support.RepositoryProxyPostProcessor;

public abstract class EntityInformationProxyPostProcessor<T, ID> implements RepositoryProxyPostProcessor {

	protected abstract void registeredEntity(DynamoDBEntityInformation<T, ID> entityInformation);

	@Override
	public final void postProcess(ProxyFactory factory, RepositoryInformation repositoryInformation) {
		try {
			TargetSource targetSource = factory.getTargetSource();
			// assert
			// targetSource.getTargetClass().equals(SimpleDynamoDBCrudRepository.class);

			@SuppressWarnings("unchecked")
			SimpleDynamoDBCrudRepository<T, ID> target = SimpleDynamoDBCrudRepository.class
					.cast(targetSource.getTarget());

			assert target != null;
			DynamoDBEntityInformation<T, ID> entityInformation = target.getEntityInformation();
			registeredEntity(entityInformation);

		} catch (Exception e) {
			throw new RuntimeException("Could not extract SimpleDynamoDBCrudRepository from " + factory, e);
		}
	}

}
