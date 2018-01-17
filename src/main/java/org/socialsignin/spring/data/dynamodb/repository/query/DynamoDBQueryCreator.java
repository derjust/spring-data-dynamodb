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
package org.socialsignin.spring.data.dynamodb.repository.query;

import org.socialsignin.spring.data.dynamodb.core.DynamoDBOperations;
import org.socialsignin.spring.data.dynamodb.query.Query;
import org.socialsignin.spring.data.dynamodb.repository.support.DynamoDBEntityInformation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.ParameterAccessor;
import org.springframework.data.repository.query.parser.PartTree;

public class DynamoDBQueryCreator<T,ID> extends AbstractDynamoDBQueryCreator<T, ID,T> {

	private final Pageable pageable;

	public DynamoDBQueryCreator(PartTree tree,
			DynamoDBEntityInformation<T, ID> entityMetadata,
			DynamoDBOperations dynamoDBOperations) {
		super(tree, entityMetadata, dynamoDBOperations);
		pageable = Pageable.unpaged();
	}

	public DynamoDBQueryCreator(PartTree tree,
			ParameterAccessor parameterAccessor,
			DynamoDBEntityInformation<T, ID> entityMetadata,
			DynamoDBOperations dynamoDBOperations) {
		super(tree, parameterAccessor, entityMetadata, dynamoDBOperations);
		pageable = parameterAccessor.getPageable();
	}

	@Override
	protected Query<T> complete(DynamoDBQueryCriteria<T, ID> criteria, Sort sort) {
		if (sort != null) {
			criteria.withSort(sort);
		}

		criteria.withPageable(pageable);

		return criteria.buildQuery(dynamoDBOperations);

	}

}
