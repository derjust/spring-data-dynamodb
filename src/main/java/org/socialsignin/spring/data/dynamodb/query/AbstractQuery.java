/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.socialsignin.spring.data.dynamodb.query;

import org.socialsignin.spring.data.dynamodb.core.DynamoDBOperations;

/**
 * {@link org.socialsignin.spring.data.dynamodb.mapping.DynamoDBPersistentProperty} implementation
 *
 * @author Michael Lavelle
 */
public abstract class AbstractQuery<T> implements Query<T> {

	//protected DynamoDBMapper dynamoDBMapper;
	protected DynamoDBOperations dynamoDBOperations;
	protected Class<T> clazz;
	protected boolean scanEnabled = false;
	protected boolean scanCountEnabled = false;



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

	public boolean isScanEnabled() {
		return scanEnabled;
	}

	public AbstractQuery(DynamoDBOperations dynamoDBOperations, Class<T> clazz) {
		//this.dynamoDBMapper = dynamoDBMapper;
		this.dynamoDBOperations = dynamoDBOperations;
		this.clazz = clazz;
	}

}
