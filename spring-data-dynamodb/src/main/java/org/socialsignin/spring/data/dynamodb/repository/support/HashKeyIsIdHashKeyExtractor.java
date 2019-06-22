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

import org.springframework.util.Assert;

/**
 * @author Michael Lavelle
 * @author Sebastian Just
 */
public class HashKeyIsIdHashKeyExtractor<ID> implements HashKeyExtractor<ID, ID> {

	private Class<ID> idAndHashKeyType;

	public HashKeyIsIdHashKeyExtractor(Class<ID> idAndHashKeyType) {
		this.idAndHashKeyType = idAndHashKeyType;
	}

	@Override
	public ID getHashKey(ID id) {
		Assert.isAssignable(idAndHashKeyType, id.getClass(),
				"Expected ID type to be the same as the return type of the hash key method ( " + idAndHashKeyType
						+ " ) : ");
		return id;
	}

}
