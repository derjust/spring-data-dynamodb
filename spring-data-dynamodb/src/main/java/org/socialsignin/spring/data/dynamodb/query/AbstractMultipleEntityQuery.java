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
import org.springframework.dao.IncorrectResultSizeDataAccessException;

import java.util.List;

/**
 * 
 * @author Michael Lavelle
 * @author Sebastian Just
 */
public abstract class AbstractMultipleEntityQuery<T> extends AbstractDynamicQuery<T> implements Query<T> {

	public AbstractMultipleEntityQuery(DynamoDBOperations dynamoDBOperations, Class<T> clazz) {
		super(dynamoDBOperations, clazz);
	}

	@Override
	public T getSingleResult() {
		List<T> results = getResultList();
		if (results.size() > 1) {
			throw new IncorrectResultSizeDataAccessException("result returns more than one elements", 1,
					results.size());
		}
		if (results.size() == 0) {
			// return null here as Spring will convert that to Optional if nessassary
			// https://jira.spring.io/browse/DATACMNS-483
			return null;
		} else {
			return results.get(0);
		}
	}
}
