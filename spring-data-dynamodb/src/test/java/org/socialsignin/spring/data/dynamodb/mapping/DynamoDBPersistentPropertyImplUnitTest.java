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
package org.socialsignin.spring.data.dynamodb.mapping;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for {@link DynamoDBPersistentPropertyImpl}.
 * 
 * @author Michael Lavelle
 * @author Sebastian Just
 */
@RunWith(MockitoJUnitRunner.class)
public class DynamoDBPersistentPropertyImplUnitTest {

	DynamoDBMappingContext context;
	DynamoDBPersistentEntity<?> entity;

	@Before
	public void setUp() {

		context = new DynamoDBMappingContext();
		entity = context.getPersistentEntity(Sample.class);
	}

	/**
	 * @see DATAJPA-284
	 */
	@Test
	public void considersOtherPropertiesAsNotTransient() {

		DynamoDBPersistentProperty property = entity.getPersistentProperty("otherProp");
		assertThat(property, is(notNullValue()));
	}

	/**
	 * @see DATAJPA-376
	 */
	@Test
	public void considersDynamoDBIgnoredPropertiesAsTransient() {
		assertThat(entity.getPersistentProperty("ignoredProp"), is(nullValue()));
	}

	@DynamoDBTable(tableName = "sample")
	static class Sample {

		private String ignoredProp = "ignored";
		private String otherProp = "other";

		public String getOtherProp() {
			return otherProp;
		}

		public void setOtherProp(String otherProp) {
			this.otherProp = otherProp;
		}

		@DynamoDBIgnore
		public String getIgnoredProp() {
			return ignoredProp;
		}

		public void setIgnoredProp(String ignoredProp) {
			this.ignoredProp = ignoredProp;
		}
	}

}
