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
package org.socialsignin.spring.data.dynamodb.repository.query;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import org.socialsignin.spring.data.dynamodb.core.DynamoDBOperations;
import org.socialsignin.spring.data.dynamodb.domain.UnpagedPageImpl;
import org.socialsignin.spring.data.dynamodb.exception.BatchDeleteException;
import org.socialsignin.spring.data.dynamodb.query.Query;
import org.socialsignin.spring.data.dynamodb.utils.ExceptionHandler;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.repository.query.ParameterAccessor;
import org.springframework.data.repository.query.Parameters;
import org.springframework.data.repository.query.ParametersParameterAccessor;
import org.springframework.data.repository.query.RepositoryQuery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author Michael Lavelle
 * @author Sebastian Just
 */
public abstract class AbstractDynamoDBQuery<T, ID> implements RepositoryQuery, ExceptionHandler {

	protected final DynamoDBOperations dynamoDBOperations;
	private final DynamoDBQueryMethod<T, ID> method;

	public AbstractDynamoDBQuery(DynamoDBOperations dynamoDBOperations, DynamoDBQueryMethod<T, ID> method) {
		this.dynamoDBOperations = dynamoDBOperations;
		this.method = method;
	}

	protected QueryExecution<T, ID> getExecution() {
		if (method.isCollectionQuery() && !isSingleEntityResultsRestriction()) {
			return new CollectionExecution();
		} else if (method.isSliceQuery() && !isSingleEntityResultsRestriction()) {
			return new SlicedExecution(method.getParameters());
		} else if (method.isPageQuery() && !isSingleEntityResultsRestriction()) {
			return new PagedExecution(method.getParameters());
		} else if (method.isModifyingQuery()) {
			throw new UnsupportedOperationException("Modifying queries not yet supported");
		} else if (isSingleEntityResultsRestriction()) {
			return new SingleEntityLimitedExecution();
		} else if (isDeleteQuery()) {
			return new DeleteExecution();
		} else {
			return new SingleEntityExecution();
		}
	}

	protected abstract Query<T> doCreateQuery(Object[] values);
	protected abstract Query<Long> doCreateCountQuery(Object[] values, boolean pageQuery);
	protected abstract boolean isCountQuery();
	protected abstract boolean isExistsQuery();
	protected abstract boolean isDeleteQuery();

	protected abstract Integer getResultsRestrictionIfApplicable();
	protected abstract boolean isSingleEntityResultsRestriction();

	protected Query<T> doCreateQueryWithPermissions(Object values[]) {
		Query<T> query = doCreateQuery(values);
		query.setScanEnabled(method.isScanEnabled());
		return query;
	}

	protected Query<Long> doCreateCountQueryWithPermissions(Object values[], boolean pageQuery) {
		Query<Long> query = doCreateCountQuery(values, pageQuery);
		query.setScanCountEnabled(method.isScanCountEnabled());
		return query;
	}

	private interface QueryExecution<T, ID> {
		Object execute(AbstractDynamoDBQuery<T, ID> query, Object[] values);
	}

	class CollectionExecution implements QueryExecution<T, ID> {

		@Override
		public Object execute(AbstractDynamoDBQuery<T, ID> dynamoDBQuery, Object[] values) {
			Query<T> query = dynamoDBQuery.doCreateQueryWithPermissions(values);
			if (getResultsRestrictionIfApplicable() != null) {
				return restrictMaxResultsIfNecessary(query.getResultList().iterator());
			} else
				return query.getResultList();
		}

		private List<T> restrictMaxResultsIfNecessary(Iterator<T> iterator) {
			int processed = 0;
			List<T> resultsPage = new ArrayList<>();
			while (iterator.hasNext() && processed < getResultsRestrictionIfApplicable()) {
				resultsPage.add(iterator.next());
				processed++;
			}
			return resultsPage;
		}

	}

	/**
	 * Executes the {@link AbstractDynamoDBQuery} to return a
	 * {@link org.springframework.data.domain.Page} of entities.
	 */
	class PagedExecution implements QueryExecution<T, ID> {

		private final Parameters<?, ?> parameters;

		public PagedExecution(Parameters<?, ?> parameters) {

			this.parameters = parameters;
		}

		private long scanThroughResults(Iterator<T> iterator, long resultsToScan) {
			long processed = 0;
			while (iterator.hasNext() && processed < resultsToScan) {
				iterator.next();
				processed++;
			}
			return processed;
		}

		private List<T> readPageOfResultsRestrictMaxResultsIfNecessary(Iterator<T> iterator, int pageSize) {
			int processed = 0;
			int toProcess = getResultsRestrictionIfApplicable() != null
					? Math.min(pageSize, getResultsRestrictionIfApplicable())
					: pageSize;
			List<T> resultsPage = new ArrayList<>();
			while (iterator.hasNext() && processed < toProcess) {
				resultsPage.add(iterator.next());
				processed++;
			}
			return resultsPage;
		}

		@Override
		public Object execute(AbstractDynamoDBQuery<T, ID> dynamoDBQuery, Object[] values) {

			ParameterAccessor accessor = new ParametersParameterAccessor(parameters, values);
			Pageable pageable = accessor.getPageable();
			Query<T> query = dynamoDBQuery.doCreateQueryWithPermissions(values);

			List<T> results = query.getResultList();
			return createPage(results, pageable, dynamoDBQuery, values);
		}

		private Page<T> createPage(List<T> allResults, Pageable pageable, AbstractDynamoDBQuery<T, ID> dynamoDBQuery,
				Object[] values) {

			// Get the result = this list might be a lazy list
			Iterator<T> iterator = allResults.iterator();

			// Check if the pageable request is 'beyond' the result set
			if (!pageable.isUnpaged() && pageable.getOffset() > 0) {
				long processedCount = scanThroughResults(iterator, pageable.getOffset());
				if (processedCount < pageable.getOffset()) {
					return new PageImpl<>(Collections.emptyList());
				}
			}

			// Then Count the result set size
			Query<Long> countQuery = dynamoDBQuery.doCreateCountQueryWithPermissions(values, true);
			long count = countQuery.getSingleResult();

			// Finally wrap the result in a page -
			if (!pageable.isUnpaged()) {
				// either seek to the proper part of the result set
				if (getResultsRestrictionIfApplicable() != null) {
					count = Math.min(count, getResultsRestrictionIfApplicable());
				}

				List<T> results = readPageOfResultsRestrictMaxResultsIfNecessary(iterator, pageable.getPageSize());
				return new PageImpl<>(results, pageable, count);
			} else {
				// or treat the whole (lazy) list as the result page if it's unpaged
				return new UnpagedPageImpl<>(allResults, count);
			}
		}
	}

	class SlicedExecution implements QueryExecution<T, ID> {

		private final Parameters<?, ?> parameters;

		public SlicedExecution(Parameters<?, ?> parameters) {

			this.parameters = parameters;
		}

		private long scanThroughResults(Iterator<T> iterator, long resultsToScan) {
			long processed = 0;
			while (iterator.hasNext() && processed < resultsToScan) {
				iterator.next();
				processed++;
			}
			return processed;
		}

		private List<T> readPageOfResultsRestrictMaxResultsIfNecessary(Iterator<T> iterator, int pageSize) {
			int processed = 0;
			int toProcess = getResultsRestrictionIfApplicable() != null
					? Math.min(pageSize, getResultsRestrictionIfApplicable())
					: pageSize;

			List<T> resultsPage = new ArrayList<>();
			while (iterator.hasNext() && processed < toProcess) {
				resultsPage.add(iterator.next());
				processed++;
			}
			return resultsPage;
		}

		@Override
		public Object execute(AbstractDynamoDBQuery<T, ID> dynamoDBQuery, Object[] values) {

			ParameterAccessor accessor = new ParametersParameterAccessor(parameters, values);
			Pageable pageable = accessor.getPageable();
			Query<T> query = dynamoDBQuery.doCreateQueryWithPermissions(values);
			List<T> results = query.getResultList();
			return createSlice(results, pageable);
		}

		private Slice<T> createSlice(List<T> allResults, Pageable pageable) {

			Iterator<T> iterator = allResults.iterator();
			if (pageable.getOffset() > 0) {
				long processedCount = scanThroughResults(iterator, pageable.getOffset());
				if (processedCount < pageable.getOffset())
					return new SliceImpl<>(new ArrayList<T>());
			}
			List<T> results = readPageOfResultsRestrictMaxResultsIfNecessary(iterator, pageable.getPageSize());
			// Scan ahead to retrieve the next page count
			boolean hasMoreResults = scanThroughResults(iterator, 1) > 0;
			if (getResultsRestrictionIfApplicable() != null
					&& getResultsRestrictionIfApplicable().intValue() <= results.size())
				hasMoreResults = false;
			return new SliceImpl<>(results, pageable, hasMoreResults);
		}
	}

	class DeleteExecution implements QueryExecution<T, ID> {

		@Override
		public Object execute(AbstractDynamoDBQuery<T, ID> dynamoDBQuery, Object[] values) throws BatchDeleteException {
			List<T> entities = dynamoDBQuery.doCreateQueryWithPermissions(values).getResultList();
			List<DynamoDBMapper.FailedBatch> failedBatches = dynamoDBOperations.batchDelete(entities);
			if (failedBatches.isEmpty()) {
				return entities;
			} else {
				throw repackageToException(failedBatches, BatchDeleteException.class);
			}
		}
	}

	class SingleEntityExecution implements QueryExecution<T, ID> {

		@Override
		public Object execute(AbstractDynamoDBQuery<T, ID> dynamoDBQuery, Object[] values) {
			if (isCountQuery()) {
				return dynamoDBQuery.doCreateCountQueryWithPermissions(values, false).getSingleResult();
			} else if (isExistsQuery()) {
				return !dynamoDBQuery.doCreateQueryWithPermissions(values).getResultList().isEmpty();
			} else {
				return dynamoDBQuery.doCreateQueryWithPermissions(values).getSingleResult();
			}

		}
	}

	class SingleEntityLimitedExecution implements QueryExecution<T, ID> {

		@Override
		public Object execute(AbstractDynamoDBQuery<T, ID> dynamoDBQuery, Object[] values) {
			if (isCountQuery()) {
				return dynamoDBQuery.doCreateCountQueryWithPermissions(values, false).getSingleResult();
			} else {
				List<T> resultList = dynamoDBQuery.doCreateQueryWithPermissions(values).getResultList();
				return resultList.size() == 0 ? null : resultList.get(0);

			}

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.data.repository.query.RepositoryQuery#execute(java
	 * .lang.Object[])
	 */
	public Object execute(Object[] parameters) {

		return getExecution().execute(this, parameters);
	}

	@Override
	public DynamoDBQueryMethod<T, ID> getQueryMethod() {
		return this.method;
	}

}
