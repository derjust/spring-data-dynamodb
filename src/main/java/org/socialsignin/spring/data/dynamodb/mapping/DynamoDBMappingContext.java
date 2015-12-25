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
package org.socialsignin.spring.data.dynamodb.mapping;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.springframework.data.mapping.context.AbstractMappingContext;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.data.util.TypeInformation;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

/**
 * Default implementation of a {@link org.springframework.data.mapping.context.MappingContext} for DynamoDB using
 * {@link DynamoDBPersistentEntityImpl} and {@link DynamoDBPersistentProperty}
 * as primary abstractions.
 *
 * @author Michael Lavelle
 */
public class DynamoDBMappingContext extends AbstractMappingContext<DynamoDBPersistentEntityImpl<?>, DynamoDBPersistentProperty> {
	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.data.mapping.context.AbstractMappingContext#
	 * shouldCreatePersistentEntityFor
	 * (org.springframework.data.util.TypeInformation)
	 */
	@Override
	protected <T> DynamoDBPersistentEntityImpl<?> createPersistentEntity(TypeInformation<T> typeInformation) {
		return new DynamoDBPersistentEntityImpl<T>(typeInformation, null);

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.data.mapping.AbstractMappingContext#
	 * createPersistentProperty(java.lang.reflect.Field,
	 * java.beans.PropertyDescriptor,
	 * org.springframework.data.mapping.MutablePersistentEntity,
	 * org.springframework.data.mapping.SimpleTypeHolder)
	 */
	@Override
	protected DynamoDBPersistentProperty createPersistentProperty(Field field, PropertyDescriptor descriptor,
			DynamoDBPersistentEntityImpl<?> owner, SimpleTypeHolder simpleTypeHolder) {
		return new DynamoDBPersistentPropertyImpl(field, descriptor, owner, simpleTypeHolder);

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.data.mapping.context.AbstractMappingContext#
	 * shouldCreatePersistentEntityFor
	 * (org.springframework.data.util.TypeInformation)
	 */
	@Override
	protected boolean shouldCreatePersistentEntityFor(TypeInformation<?> type) {

		boolean hasHashKey = false;
		boolean hasRangeKey = false;
		for (Method method : type.getType().getMethods()) {
			if (method.isAnnotationPresent(DynamoDBHashKey.class))
				hasHashKey = true;
			if (method.isAnnotationPresent(DynamoDBRangeKey.class))
				hasRangeKey = true;

		}
		for (Field field : type.getType().getFields()) {
			if (field.isAnnotationPresent(DynamoDBHashKey.class))
				hasHashKey = true;
			if (field.isAnnotationPresent(DynamoDBRangeKey.class))
				hasRangeKey = true;

		}
		return type.getType().isAnnotationPresent(DynamoDBTable.class) || (hasHashKey && hasRangeKey);
	}

}
