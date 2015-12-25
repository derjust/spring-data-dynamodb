/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.socialsignin.spring.data.dynamodb.repository.query;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.socialsignin.spring.data.dynamodb.core.DynamoDBOperations;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryLookupStrategy.Key;
import org.springframework.data.repository.query.RepositoryQuery;

/**
 * @author Michael Lavelle
 */
public class DynamoDBQueryLookupStrategy {

	/**
	 * Private constructor to prevent instantiation.
	 */
	private DynamoDBQueryLookupStrategy() {

	}

	/**
	 * Base class for {@link QueryLookupStrategy} implementations that need
	 * access to an {@link com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper}.
	 *
	 * @author Michael Lavelle
	 */
	private abstract static class AbstractQueryLookupStrategy implements QueryLookupStrategy {

		protected DynamoDBOperations dynamoDBOperations;

		public AbstractQueryLookupStrategy(DynamoDBOperations dynamoDBOperations) {

			this.dynamoDBOperations = dynamoDBOperations;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.springframework.data.repository.query.QueryLookupStrategy#
		 * resolveQuery(java.lang.reflect.Method,
		 * org.springframework.data.repository.core.RepositoryMetadata,
		 * org.springframework.data.repository.core.NamedQueries)
		 */
		@Override
        public final RepositoryQuery resolveQuery(Method method, RepositoryMetadata metadata, NamedQueries namedQueries) {

			return createDynamoDBQuery(method, metadata, metadata.getDomainType(), metadata.getIdType(), namedQueries);
		}

		protected abstract <T, ID extends Serializable> RepositoryQuery createDynamoDBQuery(Method method,
				RepositoryMetadata metadata, Class<T> entityClass, Class<ID> idClass, NamedQueries namedQueries);
	}

	/**
	 * {@link QueryLookupStrategy} to create a query from the method name.
	 *
	 * @author Michael Lavelle
	 */
	private static class CreateQueryLookupStrategy extends AbstractQueryLookupStrategy {

		public CreateQueryLookupStrategy(DynamoDBOperations dynamoDBOperations) {

			super(dynamoDBOperations);
		}

		@Override
		protected <T, ID extends Serializable> RepositoryQuery createDynamoDBQuery(Method method, RepositoryMetadata metadata,
				Class<T> entityClass, Class<ID> idClass, NamedQueries namedQueries) {
			try {
				return new PartTreeDynamoDBQuery<T, ID>(dynamoDBOperations, new DynamoDBQueryMethod<T, ID>(method, metadata));
			} catch (IllegalArgumentException e) {
				throw new IllegalArgumentException(String.format("Could not create query metamodel for method %s!",
						method.toString()), e);
			}
		}

	}

	/**
	 * {@link QueryLookupStrategy} that tries to detect a declared query
	 * declared via {@link Query} annotation
	 *
	 * @author Michael Lavelle
	 */
	private static class DeclaredQueryLookupStrategy extends AbstractQueryLookupStrategy {

		public DeclaredQueryLookupStrategy(DynamoDBOperations dynamoDBOperations) {

			super(dynamoDBOperations);
		}

		@Override
		protected <T, ID extends Serializable> RepositoryQuery createDynamoDBQuery(Method method, RepositoryMetadata metadata,
				Class<T> entityClass, Class<ID> idClass, NamedQueries namedQueries) {
			throw new UnsupportedOperationException("Declared Queries not supported at this time");
		}

	}

	/**
	 * {@link QueryLookupStrategy} to try to detect a declared query first (
	 * {@link org.springframework.data.jpa.repository.Query}. In case none is
	 * found we fall back on query creation.
	 *
	 * @author Michael Lavelle
	 */
	private static class CreateIfNotFoundQueryLookupStrategy extends AbstractQueryLookupStrategy {

		private final DeclaredQueryLookupStrategy strategy;
		private final CreateQueryLookupStrategy createStrategy;

		public CreateIfNotFoundQueryLookupStrategy(DynamoDBOperations dynamoDBOperations) {

			super(dynamoDBOperations);
			this.strategy = new DeclaredQueryLookupStrategy(dynamoDBOperations);
			this.createStrategy = new CreateQueryLookupStrategy(dynamoDBOperations);
		}

		@Override
		protected <T, ID extends Serializable> RepositoryQuery createDynamoDBQuery(Method method, RepositoryMetadata metadata,
				Class<T> entityClass, Class<ID> idClass, NamedQueries namedQueries) {
			try {
				return strategy.createDynamoDBQuery(method, metadata, entityClass, idClass, namedQueries);
			} catch (IllegalStateException e) {
				return createStrategy.createDynamoDBQuery(method, metadata, entityClass, idClass, namedQueries);
			} catch (UnsupportedOperationException e) {
				return createStrategy.createDynamoDBQuery(method, metadata, entityClass, idClass, namedQueries);
			}

		}
	}

	/**
	 * Creates a {@link QueryLookupStrategy} for the given
	 * {@link DynamoDBMapper} and {@link Key}.
	 *
	 * @param dynamoDBOperations
	 * @param key
	 * @return
	 */
	public static QueryLookupStrategy create(DynamoDBOperations dynamoDBOperations, Key key) {

		if (key == null) {
			return new CreateQueryLookupStrategy(dynamoDBOperations);
		}

		switch (key) {
		case CREATE:
			return new CreateQueryLookupStrategy(dynamoDBOperations);
		case USE_DECLARED_QUERY:
			throw new IllegalArgumentException(String.format("Unsupported query lookup strategy %s!", key));
		case CREATE_IF_NOT_FOUND:
			return new CreateIfNotFoundQueryLookupStrategy(dynamoDBOperations);
		default:
			throw new IllegalArgumentException(String.format("Unsupported query lookup strategy %s!", key));
		}
	}

}
