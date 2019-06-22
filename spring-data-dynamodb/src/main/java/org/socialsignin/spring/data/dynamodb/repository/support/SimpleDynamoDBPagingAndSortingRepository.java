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

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedScanList;
import org.socialsignin.spring.data.dynamodb.core.DynamoDBOperations;
import org.socialsignin.spring.data.dynamodb.repository.DynamoDBPagingAndSortingRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Default implementation of the
 * {@link org.springframework.data.repository.PagingAndSortingRepository}
 * interface.
 * 
 * Due to DynamoDB limitations, sorting is not supported for find-all operations
 * 
 * Due to DynamoDB limitations, paging for find-all queries is not possible
 * using an integer page number For paged requests, attempt to approximate
 * paging behavior by limiting the number of items which will be scanned, and by
 * returning a sublist of the result-set.
 * 
 * NB: Number of results scanned for a given page request is proportional to the
 * page number requested!
 * 
 * 
 * @author Michael Lavelle
 * @author Sebastian Just
 * 
 * @param <T>
 *            the type of the entity to handle
 * @param <ID>
 *            the type of the entity's identifier
 */
public class SimpleDynamoDBPagingAndSortingRepository<T, ID> extends SimpleDynamoDBCrudRepository<T, ID>
		implements
			DynamoDBPagingAndSortingRepository<T, ID> {

	public SimpleDynamoDBPagingAndSortingRepository(DynamoDBEntityInformation<T, ID> entityInformation,
			DynamoDBOperations dynamoDBOperations, EnableScanPermissions enableScanPermissions) {
		super(entityInformation, dynamoDBOperations, enableScanPermissions);

	}

	@Override
	public Iterable<T> findAll(Sort sort) {
		return throwUnsupportedSortOperationException();
	}

	@Override
	public Page<T> findAll(Pageable pageable) {

		ensureNoSort(pageable);

		DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
		// Scan to the end of the page after the requested page
		long scanTo = pageable.getOffset() + (2 * pageable.getPageSize());
		scanExpression.setLimit((int) Math.min(scanTo, Integer.MAX_VALUE));
		PaginatedScanList<T> paginatedScanList = dynamoDBOperations.scan(domainType, scanExpression);
		Iterator<T> iterator = paginatedScanList.iterator();
		if (pageable.getOffset() > 0) {
			long processedCount = scanThroughResults(iterator, pageable.getOffset());
			if (processedCount < pageable.getOffset())
				return new PageImpl<>(new ArrayList<T>());
		}
		// Scan ahead to retrieve the next page count
		List<T> results = readPageOfResults(iterator, pageable.getPageSize());

		assertScanEnabled(enableScanPermissions.isFindAllPaginatedScanEnabled(), "findAll(Pageable pageable)");
		assertScanCountEnabled(enableScanPermissions.isFindAllUnpaginatedScanCountEnabled(),
				"findAll(Pageable pageable)");

		int totalCount = dynamoDBOperations.count(domainType, scanExpression);

		return new PageImpl<>(results, pageable, totalCount);

	}

	private long scanThroughResults(Iterator<T> paginatedScanListIterator, long resultsToScan) {
		long processed = 0;
		while (paginatedScanListIterator.hasNext() && processed < resultsToScan) {
			paginatedScanListIterator.next();
			processed++;
		}
		return processed;
	}

	private List<T> readPageOfResults(Iterator<T> paginatedScanListIterator, int pageSize) {
		int processed = 0;
		List<T> resultsPage = new ArrayList<>();
		while (paginatedScanListIterator.hasNext() && processed < pageSize) {
			resultsPage.add(paginatedScanListIterator.next());
			processed++;
		}
		return resultsPage;
	}

	public void assertScanCountEnabled(boolean countScanEnabled, String methodName) {
		Assert.isTrue(countScanEnabled, "Scanning for the total counts for unpaginated " + methodName
				+ " queries is not enabled.  " + "To enable, re-implement the " + methodName
				+ "() method in your repository interface and annotate with @EnableScanCount, or "
				+ "enable total count scanning for all repository methods by annotating your repository interface with @EnableScanCount");
	}

}
