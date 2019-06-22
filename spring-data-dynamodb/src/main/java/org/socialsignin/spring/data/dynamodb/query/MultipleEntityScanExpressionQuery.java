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

import java.util.List;

public class MultipleEntityScanExpressionQuery<T> extends AbstractMultipleEntityQuery<T> {

	private DynamoDBScanExpression scanExpression;

	public MultipleEntityScanExpressionQuery(DynamoDBOperations dynamoDBOperations, Class<T> clazz,
			DynamoDBScanExpression scanExpression) {
		super(dynamoDBOperations, clazz);
		this.scanExpression = scanExpression;
	}

	@Override
	public List<T> getResultList() {
		assertScanEnabled(isScanEnabled());
		return dynamoDBOperations.scan(clazz, scanExpression);
	}

	public void assertScanEnabled(boolean scanEnabled) {
		Assert.isTrue(scanEnabled, "Scanning for this query is not enabled.  "
				+ "To enable annotate your repository method with @EnableScan, or "
				+ "enable scanning for all repository methods by annotating your repository interface with @EnableScan");
	}

}
