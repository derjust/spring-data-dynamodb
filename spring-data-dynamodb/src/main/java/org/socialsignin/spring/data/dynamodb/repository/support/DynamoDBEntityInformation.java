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

import org.springframework.data.repository.core.EntityInformation;

import java.util.Optional;

/**
 * Encapsulates minimal information needed to load DynamoDB entities.
 * 
 * As a minimum, provides access to hash-key related metadata.
 * 
 * Implementing classes can elect to be either range-key aware or not. If a
 * subclass is not range-key aware it should return null from getRangeKey(ID id)
 * method, and return false from isRangeKeyAware and
 * isCompositeHashAndRangeKeyProperty methods
 * 
 * @author Michael Lavelle
 * @author Sebastian Just
 */
public interface DynamoDBEntityInformation<T, ID>
		extends
			EntityInformation<T, ID>,
			DynamoDBHashKeyExtractingEntityMetadata<T> {

	default boolean isRangeKeyAware() {
		return false;
	}

	boolean isCompositeHashAndRangeKeyProperty(String propertyName);

	Object getHashKey(ID id);

	default Object getRangeKey(ID id) {
		return null;
	}

	Optional<String> getProjection();

	Optional<Integer> getLimit();
}
