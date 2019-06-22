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

import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import org.socialsignin.spring.data.dynamodb.core.DynamoDBOperations;

import java.util.List;

public class MultipleEntityQueryRequestQuery<T> extends AbstractMultipleEntityQuery<T> {

	private DynamoDBOperations dynamoDBOperations;
	private QueryRequest queryRequest;

	public MultipleEntityQueryRequestQuery(DynamoDBOperations dynamoDBOperations, Class<T> clazz,
			QueryRequest queryRequest) {
		super(null, clazz);
		this.queryRequest = queryRequest;
		this.dynamoDBOperations = dynamoDBOperations;
	}

	@Override
	public List<T> getResultList() {

		return dynamoDBOperations.query(clazz, queryRequest);
	}

}
