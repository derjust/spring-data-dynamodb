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
package org.socialsignin.spring.data.dynamodb.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;

import java.io.Serializable;

/**
 * Default implementation of a DynamoDB composite key, comprising of both a hash
 * and a range key.
 * 
 * @author Michael Lavelle
 * @author Sebastian Just
 */
public class DynamoDBHashAndRangeKey implements Serializable {

	public DynamoDBHashAndRangeKey() {
	}

	public DynamoDBHashAndRangeKey(Object hash, Object range) {
		this.rangeKey = range;
		this.hashKey = hash;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Object hashKey;
	private Object rangeKey;

	@DynamoDBHashKey
	public Object getHashKey() {
		return hashKey;
	}

	public void setHashKey(Object hashKey) {
		this.hashKey = hashKey;
	}

	@DynamoDBRangeKey
	public Object getRangeKey() {
		return rangeKey;
	}

	public void setRangeKey(Object rangeKey) {
		this.rangeKey = rangeKey;
	}

}
