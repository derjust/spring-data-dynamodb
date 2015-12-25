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

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;

/**
 * @author Michael Lavelle
 */
public class QueryExpressionCountQuery<T> extends AbstractSingleEntityQuery<Long> {

	private DynamoDBQueryExpression<T> queryExpression;

	private Class<T> domainClass;


	public QueryExpressionCountQuery(DynamoDBOperations dynamoDBOperations, Class<T> clazz,
			DynamoDBQueryExpression<T> queryExpression) {
		super(dynamoDBOperations, Long.class);
		this.queryExpression = queryExpression;
		this.domainClass = clazz;
	}

	@Override
	public Long getSingleResult() {
		return new Long(dynamoDBOperations.count(domainClass, queryExpression));
	}

}
