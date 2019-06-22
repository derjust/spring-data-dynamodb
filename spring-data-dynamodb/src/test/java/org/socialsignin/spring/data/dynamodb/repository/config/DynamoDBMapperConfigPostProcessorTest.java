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
package org.socialsignin.spring.data.dynamodb.repository.config;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig.TableNameOverride;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

@RunWith(MockitoJUnitRunner.class)
public class DynamoDBMapperConfigPostProcessorTest {

	@Mock
	private DynamoDBMapper dynamoDBMapper;
	@Mock
	private DynamoDBMapperConfig dynamoDBMapperConfig;
	@Mock
	private AmazonDynamoDB dynamoDB;

	DynamoDBMapperConfigPostProcessor underTest;

	@Before
	public void setUp() throws Exception {
		underTest = new DynamoDBMapperConfigPostProcessor();
	}

	@Test
	public void testGetOverriddenTableName_WithTableNameResolver_defaultConfig() {

		DynamoDBMapperConfig actual = (DynamoDBMapperConfig) underTest
				.postProcessAfterInitialization(DynamoDBMapperConfig.DEFAULT, null);

		assertSame(DynamoDBMapperConfig.DEFAULT, actual);
	}

	@Test
	public void testGetOverriddenTableName_WithTableNameResolver_defaultBuilder() {
		final String overridenTableName = "someOtherTableName";

		DynamoDBMapperConfig.Builder builder = new DynamoDBMapperConfig.Builder();
		// Inject the table name overrider bean
		builder.setTableNameOverride(new TableNameOverride(overridenTableName));

		DynamoDBMapperConfig actual = (DynamoDBMapperConfig) underTest.postProcessAfterInitialization(builder.build(),
				null);

		String overriddenTableName = actual.getTableNameOverride().getTableName();
		assertEquals(overridenTableName, overriddenTableName);

		assertDynamoDBMapperConfigCompletness(actual);
	}

	@Test
	public void testGetOverriddenTableName_WithTableNameResolver_emptyBuilder() {
		final String overridenTableName = "someOtherTableName";

		DynamoDBMapperConfig.Builder builder = DynamoDBMapperConfig.builder();
		// Inject the table name overrider bean
		builder.setTableNameOverride(new TableNameOverride(overridenTableName));

		DynamoDBMapperConfig actual = (DynamoDBMapperConfig) underTest.postProcessAfterInitialization(builder.build(),
				null);

		String overriddenTableName = actual.getTableNameOverride().getTableName();
		assertEquals(overridenTableName, overriddenTableName);

		assertDynamoDBMapperConfigCompletness(actual);
	}

	private void assertDynamoDBMapperConfigCompletness(DynamoDBMapperConfig effectiveConfig) {
		assertNotNull(effectiveConfig);
		assertNotNull(effectiveConfig.getConversionSchema());
		assertNotNull(effectiveConfig.getTypeConverterFactory());
	}

}
