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
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.FieldCallback;
import org.springframework.util.ReflectionUtils.MethodCallback;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author Michael Lavelle
 * @author Sebastian Just
 */
public class DynamoDBHashAndRangeKeyMethodExtractorImpl<T> implements DynamoDBHashAndRangeKeyMethodExtractor<T> {

	private final Class<T> idType;
	private Method hashKeyMethod;
	private Method rangeKeyMethod;

	private Field hashKeyField;
	private Field rangeKeyField;

	/**
	 * Creates a new {@link DynamoDBHashAndRangeKeyMethodExtractor} for the given
	 * domain type.
	 *
	 * @param idType
	 *            must not be {@literal null}.
	 */
	public DynamoDBHashAndRangeKeyMethodExtractorImpl(final Class<T> idType) {

		Assert.notNull(idType, "Id type must not be null!");
		this.idType = idType;
		ReflectionUtils.doWithMethods(idType, new MethodCallback() {
			@Override
			public void doWith(Method method) {
				if (method.getAnnotation(DynamoDBHashKey.class) != null) {
					Assert.isNull(hashKeyMethod,
							"Multiple methods annotated by @DynamoDBHashKey within type " + idType.getName() + "!");
					ReflectionUtils.makeAccessible(method);
					hashKeyMethod = method;
				}
			}
		});
		ReflectionUtils.doWithFields(idType, new FieldCallback() {
			@Override
			public void doWith(Field field) {
				if (field.getAnnotation(DynamoDBHashKey.class) != null) {
					Assert.isNull(hashKeyField,
							"Multiple fields annotated by @DynamoDBHashKey within type " + idType.getName() + "!");
					ReflectionUtils.makeAccessible(field);

					hashKeyField = field;
				}
			}
		});
		ReflectionUtils.doWithMethods(idType, new MethodCallback() {
			@Override
			public void doWith(Method method) {
				if (method.getAnnotation(DynamoDBRangeKey.class) != null) {
					Assert.isNull(rangeKeyMethod,
							"Multiple methods annotated by @DynamoDBRangeKey within type " + idType.getName() + "!");
					ReflectionUtils.makeAccessible(method);
					rangeKeyMethod = method;
				}
			}
		});
		ReflectionUtils.doWithFields(idType, new FieldCallback() {
			@Override
			public void doWith(Field field) {
				if (field.getAnnotation(DynamoDBRangeKey.class) != null) {
					Assert.isNull(rangeKeyField,
							"Multiple fields annotated by @DynamoDBRangeKey within type " + idType.getName() + "!");
					ReflectionUtils.makeAccessible(field);
					rangeKeyField = field;
				}
			}
		});
		if (hashKeyMethod == null && hashKeyField == null) {
			throw new IllegalArgumentException(
					"No method or field annotated by @DynamoDBHashKey within type " + idType.getName() + "!");
		}
		if (rangeKeyMethod == null && rangeKeyField == null) {
			throw new IllegalArgumentException(
					"No method or field annotated by @DynamoDBRangeKey within type " + idType.getName() + "!");
		}
		if (hashKeyMethod != null && hashKeyField != null) {
			throw new IllegalArgumentException(
					"Both method and field annotated by @DynamoDBHashKey within type " + idType.getName() + "!");
		}
		if (rangeKeyMethod != null && rangeKeyField != null) {
			throw new IllegalArgumentException(
					"Both method and field annotated by @DynamoDBRangeKey within type " + idType.getName() + "!");
		}
	}

	@Override
	public Class<T> getJavaType() {
		return idType;
	}

	@Override
	public Method getHashKeyMethod() {

		return hashKeyMethod;
	}

	@Override
	public Method getRangeKeyMethod() {
		return rangeKeyMethod;
	}

	@Override
	public Field getHashKeyField() {

		return hashKeyField;
	}

	@Override
	public Field getRangeKeyField() {
		return rangeKeyField;
	}

}
