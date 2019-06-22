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

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import org.socialsignin.spring.data.dynamodb.core.DynamoDBOperations;
import org.springframework.util.Assert;

public class ScanExpressionCountQuery<T> extends AbstractSingleEntityQuery<Long> implements Query<Long> {

	private DynamoDBScanExpression scanExpression;

	private Class<T> domainClass;

	private boolean pageQuery;

	public ScanExpressionCountQuery(DynamoDBOperations dynamoDBOperations, Class<T> clazz,
			DynamoDBScanExpression scanExpression, boolean pageQuery) {
		super(dynamoDBOperations, Long.class);
		this.scanExpression = scanExpression;
		this.domainClass = clazz;
		this.pageQuery = pageQuery;
	}

	@Override
	public Long getSingleResult() {
		assertScanCountEnabled(isScanCountEnabled());
		return Long.valueOf(dynamoDBOperations.count(domainClass, scanExpression));
	}

	public void assertScanCountEnabled(boolean scanCountEnabled) {
		if (pageQuery) {
			Assert.isTrue(scanCountEnabled, "Scanning for the total counts for this query is not enabled.  "
					+ "To enable annotate your repository method with @EnableScanCount, or "
					+ "enable scanning for all repository methods by annotating your repository interface with @EnableScanCount.  This total count is required to serve this Page query - if total counts are not desired an alternative approach could be to replace the Page query with a Slice query ");

		} else {
			Assert.isTrue(scanCountEnabled, "Scanning for counts for this query is not enabled.  "
					+ "To enable annotate your repository method with @EnableScanCount, or "
					+ "enable scanning for all repository methods by annotating your repository interface with @EnableScanCount");
		}
	}

}
