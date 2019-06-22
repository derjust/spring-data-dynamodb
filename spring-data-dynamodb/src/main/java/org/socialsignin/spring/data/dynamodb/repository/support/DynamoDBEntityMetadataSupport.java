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

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMarshaller;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMarshalling;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBVersionAttribute;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Michael Lavelle
 * @author Sebastian Just
 */
public class DynamoDBEntityMetadataSupport<T, ID> implements DynamoDBHashKeyExtractingEntityMetadata<T> {

	private final Class<T> domainType;
	private boolean hasRangeKey;
	private String hashKeyPropertyName;
	private List<String> globalIndexHashKeyPropertyNames;
	private List<String> globalIndexRangeKeyPropertyNames;

	private String dynamoDBTableName;
	private Map<String, String[]> globalSecondaryIndexNames = new HashMap<>();

	@Override
	public String getDynamoDBTableName() {
		return dynamoDBTableName;
	}

	/**
	 * Creates a new {@link DynamoDBEntityMetadataSupport} for the given domain
	 * type.
	 *
	 * @param domainType
	 *            must not be {@literal null}.
	 */
	public DynamoDBEntityMetadataSupport(final Class<T> domainType) {

		Assert.notNull(domainType, "Domain type must not be null!");
		this.domainType = domainType;

		DynamoDBTable table = this.domainType.getAnnotation(DynamoDBTable.class);
		Assert.notNull(table, "Domain type must by annotated with DynamoDBTable!");

		this.dynamoDBTableName = table.tableName();
		this.hashKeyPropertyName = null;
		this.globalSecondaryIndexNames = new HashMap<>();
		this.globalIndexHashKeyPropertyNames = new ArrayList<>();
		this.globalIndexRangeKeyPropertyNames = new ArrayList<>();
		ReflectionUtils.doWithMethods(domainType, method -> {
			if (method.getAnnotation(DynamoDBHashKey.class) != null) {
				hashKeyPropertyName = getPropertyNameForAccessorMethod(method);
			}
			if (method.getAnnotation(DynamoDBRangeKey.class) != null) {
				hasRangeKey = true;
			}
			DynamoDBIndexRangeKey dynamoDBRangeKeyAnnotation = method.getAnnotation(DynamoDBIndexRangeKey.class);
			DynamoDBIndexHashKey dynamoDBHashKeyAnnotation = method.getAnnotation(DynamoDBIndexHashKey.class);

			if (dynamoDBRangeKeyAnnotation != null) {
				addGlobalSecondaryIndexNames(method, dynamoDBRangeKeyAnnotation);
			}
			if (dynamoDBHashKeyAnnotation != null) {
				addGlobalSecondaryIndexNames(method, dynamoDBHashKeyAnnotation);
			}
		});
		ReflectionUtils.doWithFields(domainType, field -> {
			if (field.getAnnotation(DynamoDBHashKey.class) != null) {
				hashKeyPropertyName = getPropertyNameForField(field);
			}
			if (field.getAnnotation(DynamoDBRangeKey.class) != null) {
				hasRangeKey = true;
			}
			DynamoDBIndexRangeKey dynamoDBRangeKeyAnnotation = field.getAnnotation(DynamoDBIndexRangeKey.class);
			DynamoDBIndexHashKey dynamoDBHashKeyAnnotation = field.getAnnotation(DynamoDBIndexHashKey.class);

			if (dynamoDBRangeKeyAnnotation != null) {
				addGlobalSecondaryIndexNames(field, dynamoDBRangeKeyAnnotation);
			}
			if (dynamoDBHashKeyAnnotation != null) {
				addGlobalSecondaryIndexNames(field, dynamoDBHashKeyAnnotation);
			}
		});
		Assert.notNull(hashKeyPropertyName, "Unable to find hash key field or getter method on " + domainType + "!");
	}

	public DynamoDBEntityInformation<T, ID> getEntityInformation() {

		if (hasRangeKey) {
			DynamoDBHashAndRangeKeyExtractingEntityMetadataImpl<T, ID> metadata = new DynamoDBHashAndRangeKeyExtractingEntityMetadataImpl<T, ID>(
					domainType);
			return new DynamoDBIdIsHashAndRangeKeyEntityInformationImpl<>(domainType, metadata);
		} else {
			return new DynamoDBIdIsHashKeyEntityInformationImpl<>(domainType, this);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.data.repository.core.EntityMetadata#getJavaType()
	 */
	@Override
	public Class<T> getJavaType() {
		return domainType;
	}

	@Override
	public boolean isHashKeyProperty(String propertyName) {
		return hashKeyPropertyName.equals(propertyName);
	}

	protected boolean isFieldAnnotatedWith(final String propertyName, final Class<? extends Annotation> annotation) {

		Field field = findField(propertyName);
		return field != null && field.getAnnotation(annotation) != null;
	}

	private String toGetMethodName(String propertyName) {
		String methodName = propertyName.substring(0, 1).toUpperCase();
		if (propertyName.length() > 1) {
			methodName = methodName + propertyName.substring(1);
		}
		return "get" + methodName;
	}

	protected String toSetterMethodNameFromAccessorMethod(Method method) {
		String accessorMethodName = method.getName();
		if (accessorMethodName.startsWith("get")) {
			return "set" + accessorMethodName.substring(3);
		} else if (accessorMethodName.startsWith("is")) {
			return "is" + accessorMethodName.substring(2);
		}
		return null;
	}

	private String toIsMethodName(String propertyName) {
		String methodName = propertyName.substring(0, 1).toUpperCase();
		if (propertyName.length() > 1) {
			methodName = methodName + propertyName.substring(1);
		}
		return "is" + methodName;
	}

	private Method findMethod(String propertyName) {
		Method method = ReflectionUtils.findMethod(domainType, toGetMethodName(propertyName));
		if (method == null) {
			method = ReflectionUtils.findMethod(domainType, toIsMethodName(propertyName));
		}
		return method;

	}

	private Field findField(String propertyName) {
		return ReflectionUtils.findField(domainType, propertyName);
	}

	public String getOverriddenAttributeName(Method method) {

		if (method != null) {
			if (method.getAnnotation(DynamoDBAttribute.class) != null
					&& !StringUtils.isEmpty(method.getAnnotation(DynamoDBAttribute.class).attributeName())) {
				return method.getAnnotation(DynamoDBAttribute.class).attributeName();
			}
			if (method.getAnnotation(DynamoDBHashKey.class) != null
					&& !StringUtils.isEmpty(method.getAnnotation(DynamoDBHashKey.class).attributeName())) {
				return method.getAnnotation(DynamoDBHashKey.class).attributeName();
			}
			if (method.getAnnotation(DynamoDBRangeKey.class) != null
					&& !StringUtils.isEmpty(method.getAnnotation(DynamoDBRangeKey.class).attributeName())) {
				return method.getAnnotation(DynamoDBRangeKey.class).attributeName();
			}
			if (method.getAnnotation(DynamoDBIndexRangeKey.class) != null
					&& !StringUtils.isEmpty(method.getAnnotation(DynamoDBIndexRangeKey.class).attributeName())) {
				return method.getAnnotation(DynamoDBIndexRangeKey.class).attributeName();
			}
			if (method.getAnnotation(DynamoDBIndexHashKey.class) != null
					&& !StringUtils.isEmpty(method.getAnnotation(DynamoDBIndexHashKey.class).attributeName())) {
				return method.getAnnotation(DynamoDBIndexHashKey.class).attributeName();
			}
			if (method.getAnnotation(DynamoDBVersionAttribute.class) != null
					&& !StringUtils.isEmpty(method.getAnnotation(DynamoDBVersionAttribute.class).attributeName())) {
				return method.getAnnotation(DynamoDBVersionAttribute.class).attributeName();
			}
		}
		return null;

	}

	@Override
	public Optional<String> getOverriddenAttributeName(final String propertyName) {

		Method method = findMethod(propertyName);
		if (method != null) {
			if (method.getAnnotation(DynamoDBAttribute.class) != null
					&& !StringUtils.isEmpty(method.getAnnotation(DynamoDBAttribute.class).attributeName())) {
				return Optional.of(method.getAnnotation(DynamoDBAttribute.class).attributeName());
			}
			if (method.getAnnotation(DynamoDBHashKey.class) != null
					&& !StringUtils.isEmpty(method.getAnnotation(DynamoDBHashKey.class).attributeName())) {
				return Optional.of(method.getAnnotation(DynamoDBHashKey.class).attributeName());
			}
			if (method.getAnnotation(DynamoDBRangeKey.class) != null
					&& !StringUtils.isEmpty(method.getAnnotation(DynamoDBRangeKey.class).attributeName())) {
				return Optional.of(method.getAnnotation(DynamoDBRangeKey.class).attributeName());
			}
			if (method.getAnnotation(DynamoDBIndexRangeKey.class) != null
					&& !StringUtils.isEmpty(method.getAnnotation(DynamoDBIndexRangeKey.class).attributeName())) {
				return Optional.of(method.getAnnotation(DynamoDBIndexRangeKey.class).attributeName());
			}
			if (method.getAnnotation(DynamoDBIndexHashKey.class) != null
					&& !StringUtils.isEmpty(method.getAnnotation(DynamoDBIndexHashKey.class).attributeName())) {
				return Optional.of(method.getAnnotation(DynamoDBIndexHashKey.class).attributeName());
			}
			if (method.getAnnotation(DynamoDBVersionAttribute.class) != null
					&& !StringUtils.isEmpty(method.getAnnotation(DynamoDBVersionAttribute.class).attributeName())) {
				return Optional.of(method.getAnnotation(DynamoDBVersionAttribute.class).attributeName());
			}
		}

		Field field = findField(propertyName);
		if (field != null) {
			if (field.getAnnotation(DynamoDBAttribute.class) != null
					&& !StringUtils.isEmpty(field.getAnnotation(DynamoDBAttribute.class).attributeName())) {
				return Optional.of(field.getAnnotation(DynamoDBAttribute.class).attributeName());
			}
			if (field.getAnnotation(DynamoDBHashKey.class) != null
					&& !StringUtils.isEmpty(field.getAnnotation(DynamoDBHashKey.class).attributeName())) {
				return Optional.of(field.getAnnotation(DynamoDBHashKey.class).attributeName());
			}
			if (field.getAnnotation(DynamoDBRangeKey.class) != null
					&& !StringUtils.isEmpty(field.getAnnotation(DynamoDBRangeKey.class).attributeName())) {
				return Optional.of(field.getAnnotation(DynamoDBRangeKey.class).attributeName());
			}
			if (field.getAnnotation(DynamoDBIndexRangeKey.class) != null
					&& !StringUtils.isEmpty(field.getAnnotation(DynamoDBIndexRangeKey.class).attributeName())) {
				return Optional.of(field.getAnnotation(DynamoDBIndexRangeKey.class).attributeName());
			}
			if (field.getAnnotation(DynamoDBIndexHashKey.class) != null
					&& !StringUtils.isEmpty(field.getAnnotation(DynamoDBIndexHashKey.class).attributeName())) {
				return Optional.of(field.getAnnotation(DynamoDBIndexHashKey.class).attributeName());
			}
			if (field.getAnnotation(DynamoDBVersionAttribute.class) != null
					&& !StringUtils.isEmpty(field.getAnnotation(DynamoDBVersionAttribute.class).attributeName())) {
				return Optional.of(field.getAnnotation(DynamoDBVersionAttribute.class).attributeName());
			}
		}
		return Optional.empty();

	}

	@Override
	@SuppressWarnings("deprecation")
	public <V extends DynamoDBMarshaller<?>> V getMarshallerForProperty(final String propertyName) {
		// TODO #28
		DynamoDBMarshalling annotation = null;

		Method method = findMethod(propertyName);
		if (method != null) {
			annotation = method.getAnnotation(DynamoDBMarshalling.class);
		}

		if (annotation == null) {
			Field field = findField(propertyName);
			if (field != null) {
				annotation = field.getAnnotation(DynamoDBMarshalling.class);
			}
		}

		if (annotation != null) {
			try {
				@SuppressWarnings("unchecked")
				Class<V> marshallerClazz = (Class<V>) annotation.marshallerClass();
				return marshallerClazz.getDeclaredConstructor().newInstance();
			} catch (InstantiationException | IllegalAccessException | NoSuchMethodException
					| InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		}

		return null;
	}

	@Override
	public DynamoDBTypeConverter<?, ?> getTypeConverterForProperty(final String propertyName) {
		DynamoDBTypeConverted annotation = null;

		Method method = findMethod(propertyName);
		if (method != null) {
			annotation = method.getAnnotation(DynamoDBTypeConverted.class);
		}

		if (annotation == null) {
			Field field = findField(propertyName);
			if (field != null) {
				annotation = field.getAnnotation(DynamoDBTypeConverted.class);
			}
		}

		if (annotation != null) {
			try {
				return annotation.converter().getDeclaredConstructor().newInstance();
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				throw new RuntimeException(e);
			}
		}

		return null;
	}

	protected String getPropertyNameForAccessorMethod(Method method) {
		String methodName = method.getName();
		String propertyName = null;
		if (methodName.startsWith("get")) {
			propertyName = methodName.substring(3);
		} else if (methodName.startsWith("is")) {
			propertyName = methodName.substring(2);
		}
		Assert.notNull(propertyName, "Hash or range key annotated accessor methods must start with 'get' or 'is'");

		String firstLetter = propertyName.substring(0, 1);
		String remainder = propertyName.substring(1);
		return firstLetter.toLowerCase() + remainder;
	}

	protected String getPropertyNameForField(Field field) {
		return field.getName();
	}

	@Override
	public String getHashKeyPropertyName() {
		return hashKeyPropertyName;
	}

	private void addGlobalSecondaryIndexNames(Method method, DynamoDBIndexRangeKey dynamoDBIndexRangeKey) {

		if (dynamoDBIndexRangeKey.globalSecondaryIndexNames() != null
				&& dynamoDBIndexRangeKey.globalSecondaryIndexNames().length > 0) {
			String propertyName = getPropertyNameForAccessorMethod(method);

			globalSecondaryIndexNames.put(propertyName,
					method.getAnnotation(DynamoDBIndexRangeKey.class).globalSecondaryIndexNames());
			globalIndexRangeKeyPropertyNames.add(propertyName);

		}
		if (dynamoDBIndexRangeKey.globalSecondaryIndexName() != null
				&& dynamoDBIndexRangeKey.globalSecondaryIndexName().trim().length() > 0) {
			String propertyName = getPropertyNameForAccessorMethod(method);
			globalSecondaryIndexNames.put(propertyName,
					new String[]{method.getAnnotation(DynamoDBIndexRangeKey.class).globalSecondaryIndexName()});
			globalIndexRangeKeyPropertyNames.add(propertyName);

		}

	}

	private void addGlobalSecondaryIndexNames(Field field, DynamoDBIndexRangeKey dynamoDBIndexRangeKey) {

		if (dynamoDBIndexRangeKey.globalSecondaryIndexNames() != null
				&& dynamoDBIndexRangeKey.globalSecondaryIndexNames().length > 0) {
			String propertyName = getPropertyNameForField(field);

			globalSecondaryIndexNames.put(propertyName,
					field.getAnnotation(DynamoDBIndexRangeKey.class).globalSecondaryIndexNames());
			globalIndexRangeKeyPropertyNames.add(propertyName);

		}
		if (dynamoDBIndexRangeKey.globalSecondaryIndexName() != null
				&& dynamoDBIndexRangeKey.globalSecondaryIndexName().trim().length() > 0) {
			String propertyName = getPropertyNameForField(field);
			globalSecondaryIndexNames.put(propertyName,
					new String[]{field.getAnnotation(DynamoDBIndexRangeKey.class).globalSecondaryIndexName()});
			globalIndexRangeKeyPropertyNames.add(propertyName);

		}

	}

	private void addGlobalSecondaryIndexNames(Method method, DynamoDBIndexHashKey dynamoDBIndexHashKey) {

		if (dynamoDBIndexHashKey.globalSecondaryIndexNames() != null
				&& dynamoDBIndexHashKey.globalSecondaryIndexNames().length > 0) {
			String propertyName = getPropertyNameForAccessorMethod(method);

			globalSecondaryIndexNames.put(propertyName,
					method.getAnnotation(DynamoDBIndexHashKey.class).globalSecondaryIndexNames());
			globalIndexHashKeyPropertyNames.add(propertyName);

		}
		if (dynamoDBIndexHashKey.globalSecondaryIndexName() != null
				&& dynamoDBIndexHashKey.globalSecondaryIndexName().trim().length() > 0) {
			String propertyName = getPropertyNameForAccessorMethod(method);

			globalSecondaryIndexNames.put(propertyName,
					new String[]{method.getAnnotation(DynamoDBIndexHashKey.class).globalSecondaryIndexName()});
			globalIndexHashKeyPropertyNames.add(propertyName);

		}
	}

	private void addGlobalSecondaryIndexNames(Field field, DynamoDBIndexHashKey dynamoDBIndexHashKey) {

		if (dynamoDBIndexHashKey.globalSecondaryIndexNames() != null
				&& dynamoDBIndexHashKey.globalSecondaryIndexNames().length > 0) {
			String propertyName = getPropertyNameForField(field);

			globalSecondaryIndexNames.put(propertyName,
					field.getAnnotation(DynamoDBIndexHashKey.class).globalSecondaryIndexNames());
			globalIndexHashKeyPropertyNames.add(propertyName);

		}
		if (dynamoDBIndexHashKey.globalSecondaryIndexName() != null
				&& dynamoDBIndexHashKey.globalSecondaryIndexName().trim().length() > 0) {
			String propertyName = getPropertyNameForField(field);

			globalSecondaryIndexNames.put(propertyName,
					new String[]{field.getAnnotation(DynamoDBIndexHashKey.class).globalSecondaryIndexName()});
			globalIndexHashKeyPropertyNames.add(propertyName);

		}
	}

	@Override
	public Map<String, String[]> getGlobalSecondaryIndexNamesByPropertyName() {
		return globalSecondaryIndexNames;
	}

	@Override
	public boolean isGlobalIndexHashKeyProperty(String propertyName) {
		return globalIndexHashKeyPropertyNames.contains(propertyName);
	}

	@Override
	public boolean isGlobalIndexRangeKeyProperty(String propertyName) {
		return globalIndexRangeKeyPropertyNames.contains(propertyName);
	}

}
