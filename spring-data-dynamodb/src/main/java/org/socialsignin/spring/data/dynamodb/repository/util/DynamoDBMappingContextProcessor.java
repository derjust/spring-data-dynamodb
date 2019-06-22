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

import org.socialsignin.spring.data.dynamodb.mapping.DynamoDBMappingContext;
import org.socialsignin.spring.data.dynamodb.repository.support.DynamoDBEntityInformation;
import org.springframework.data.repository.core.support.RepositoryProxyPostProcessor;

public class DynamoDBMappingContextProcessor<T, ID> extends EntityInformationProxyPostProcessor<T, ID>
		implements
			RepositoryProxyPostProcessor {

	private final DynamoDBMappingContext context;

	public DynamoDBMappingContextProcessor(DynamoDBMappingContext context) {
		this.context = context;
	}

	@Override
	protected void registeredEntity(DynamoDBEntityInformation<T, ID> entityInformation) {
		// register entities
		context.getPersistentEntity(entityInformation.getJavaType());
	}

}
