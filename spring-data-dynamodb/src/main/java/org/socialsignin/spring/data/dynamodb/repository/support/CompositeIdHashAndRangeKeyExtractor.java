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

import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

/**
 * @author Michael Lavelle
 * @author Sebastian Just
 */
public class CompositeIdHashAndRangeKeyExtractor<ID, H> implements HashAndRangeKeyExtractor<ID, H> {

	private DynamoDBHashAndRangeKeyMethodExtractor<ID> hashAndRangeKeyMethodExtractor;

	public CompositeIdHashAndRangeKeyExtractor(Class<ID> idClass) {
		this.hashAndRangeKeyMethodExtractor = new DynamoDBHashAndRangeKeyMethodExtractorImpl<ID>(idClass);
	}

	@SuppressWarnings("unchecked")
	@Override
	public H getHashKey(ID id) {
		Method method = hashAndRangeKeyMethodExtractor.getHashKeyMethod();
		if (method != null) {
			return (H) ReflectionUtils.invokeMethod(method, id);
		} else {
			return (H) ReflectionUtils.getField(hashAndRangeKeyMethodExtractor.getHashKeyField(), id);
		}
	}

	@Override
	public Object getRangeKey(ID id) {
		Method method = hashAndRangeKeyMethodExtractor.getRangeKeyMethod();
		if (method != null) {
			return ReflectionUtils.invokeMethod(method, id);
		} else {
			return ReflectionUtils.getField(hashAndRangeKeyMethodExtractor.getRangeKeyField(), id);
		}
	}

}
