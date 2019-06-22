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

/**
 * {@link org.socialsignin.spring.data.dynamodb.mapping.DynamoDBPersistentProperty}
 * implementation
 *
 * @author Michael Lavelle
 * @author Sebastian Just
 */
public abstract class AbstractQuery<T> implements Query<T> {

	protected boolean scanEnabled = false;
	protected boolean scanCountEnabled = false;

	@Override
	public boolean isScanCountEnabled() {
		return scanCountEnabled;
	}

	@Override
	public void setScanCountEnabled(boolean scanCountEnabled) {
		this.scanCountEnabled = scanCountEnabled;
	}

	@Override
	public void setScanEnabled(boolean scanEnabled) {
		this.scanEnabled = scanEnabled;
	}

	@Override
	public boolean isScanEnabled() {
		return scanEnabled;
	}

}
