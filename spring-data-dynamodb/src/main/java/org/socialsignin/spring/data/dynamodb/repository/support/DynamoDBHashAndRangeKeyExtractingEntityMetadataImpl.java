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

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexRangeKey;
import org.springframework.data.annotation.Id;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Michael Lavelle
 * @author Sebastian Just
 */
public class DynamoDBHashAndRangeKeyExtractingEntityMetadataImpl<T, ID> extends DynamoDBEntityMetadataSupport<T, ID>
		implements
			DynamoDBHashAndRangeKeyExtractingEntityMetadata<T, ID> {

	private DynamoDBHashAndRangeKeyMethodExtractor<T> hashAndRangeKeyMethodExtractor;

	private Method hashKeySetterMethod;
	private Field hashKeyField;

	public DynamoDBHashAndRangeKeyExtractingEntityMetadataImpl(final Class<T> domainType) {
		super(domainType);
		this.hashAndRangeKeyMethodExtractor = new DynamoDBHashAndRangeKeyMethodExtractorImpl<T>(getJavaType());
		ReflectionUtils.doWithMethods(domainType, method -> {
			if (method.getAnnotation(DynamoDBHashKey.class) != null) {
				String setterMethodName = toSetterMethodNameFromAccessorMethod(method);
				if (setterMethodName != null) {
					hashKeySetterMethod = ReflectionUtils.findMethod(domainType, setterMethodName,
							method.getReturnType());
				}
			}
		});
		ReflectionUtils.doWithFields(domainType, field -> {
			if (field.getAnnotation(DynamoDBHashKey.class) != null) {

				hashKeyField = ReflectionUtils.findField(domainType, field.getName());

			}
		});
		Assert.isTrue(hashKeySetterMethod != null || hashKeyField != null,
				"Unable to find hash key field or setter method on " + domainType + "!");
		Assert.isTrue(hashKeySetterMethod == null || hashKeyField == null,
				"Found both hash key field and setter method on " + domainType + "!");

	}

	@Override
	public <H> HashAndRangeKeyExtractor<ID, H> getHashAndRangeKeyExtractor(Class<ID> idClass) {
		return new CompositeIdHashAndRangeKeyExtractor<>(idClass);
	}

	@Override
	public String getRangeKeyPropertyName() {
		return getPropertyNameForAccessorMethod(hashAndRangeKeyMethodExtractor.getRangeKeyMethod());
	}

	@Override
	public Set<String> getIndexRangeKeyPropertyNames() {
		final Set<String> propertyNames = new HashSet<>();
		ReflectionUtils.doWithMethods(getJavaType(), method -> {
			if (method.getAnnotation(DynamoDBIndexRangeKey.class) != null) {
				if ((method.getAnnotation(DynamoDBIndexRangeKey.class).localSecondaryIndexName() != null && method
						.getAnnotation(DynamoDBIndexRangeKey.class).localSecondaryIndexName().trim().length() > 0)
						|| (method.getAnnotation(DynamoDBIndexRangeKey.class).localSecondaryIndexNames() != null
								&& method.getAnnotation(DynamoDBIndexRangeKey.class)
										.localSecondaryIndexNames().length > 0)) {
					propertyNames.add(getPropertyNameForAccessorMethod(method));
				}
			}
		});
		ReflectionUtils.doWithFields(getJavaType(), field -> {
			if (field.getAnnotation(DynamoDBIndexRangeKey.class) != null) {
				if ((field.getAnnotation(DynamoDBIndexRangeKey.class).localSecondaryIndexName() != null && field
						.getAnnotation(DynamoDBIndexRangeKey.class).localSecondaryIndexName().trim().length() > 0)
						|| (field.getAnnotation(DynamoDBIndexRangeKey.class).localSecondaryIndexNames() != null && field
								.getAnnotation(DynamoDBIndexRangeKey.class).localSecondaryIndexNames().length > 0)) {
					propertyNames.add(getPropertyNameForField(field));
				}
			}
		});
		return propertyNames;
	}

	public T getHashKeyPropotypeEntityForHashKey(Object hashKey) {

		try {
			T entity = getJavaType().getDeclaredConstructor().newInstance();
			if (hashKeySetterMethod != null) {
				ReflectionUtils.invokeMethod(hashKeySetterMethod, entity, hashKey);
			} else {
				ReflectionUtils.setField(hashKeyField, entity, hashKey);
			}

			return entity;
		} catch (InstantiationException | IllegalAccessException | NoSuchMethodException
				| InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean isCompositeHashAndRangeKeyProperty(String propertyName) {
		return isFieldAnnotatedWith(propertyName, Id.class);
	}

}
