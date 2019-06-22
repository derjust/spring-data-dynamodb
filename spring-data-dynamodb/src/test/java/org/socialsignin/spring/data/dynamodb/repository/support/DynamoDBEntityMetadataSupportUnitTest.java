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

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMarshaller;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.socialsignin.spring.data.dynamodb.domain.sample.User;

@RunWith(MockitoJUnitRunner.class)
public class DynamoDBEntityMetadataSupportUnitTest {

	@Test
	public void testGetMarshallerForProperty_WhenAnnotationIsOnField_AndReturnsDynamoDBMarshaller() {
		DynamoDBEntityMetadataSupport<User, ?> support = new DynamoDBEntityMetadataSupport<>(User.class);
		@SuppressWarnings("deprecation")
		DynamoDBMarshaller<?> fieldAnnotation = support.getMarshallerForProperty("joinYear");
		Assert.assertNotNull(fieldAnnotation);
	}

	@Test
	public void testGetMarshallerForProperty_WhenAnnotationIsOnMethod_AndReturnsDynamoDBMarshaller() {
		DynamoDBEntityMetadataSupport<User, ?> support = new DynamoDBEntityMetadataSupport<>(User.class);
		@SuppressWarnings("deprecation")
		DynamoDBMarshaller<?> methodAnnotation = support.getMarshallerForProperty("leaveDate");
		Assert.assertNotNull(methodAnnotation);
	}
}
