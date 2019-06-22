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
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMarshaller;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.Optional;

/**
 * Encapsulates minimal information needed to load DynamoDB entities.
 *
 * This default implementation is NOT range-key aware - getRangeKey(ID id) will
 * always return null
 *
 * Delegates to wrapped DynamoDBHashKeyExtractingEntityMetadata component for
 * many operations - it is the responsibility of calling clients to ensure they
 * pass in a valid DynamoDBHashKeyExtractingEntityMetadata implementation for
 * this entity.
 *
 * Entities of type T must have a public getter method of return type ID
 * annotated with @DynamoDBHashKey to ensure correct behavior
 *
 * @author Michael Lavelle
 * @author Sebastian Just
 */
public class DynamoDBIdIsHashKeyEntityInformationImpl<T, ID> extends FieldAndGetterReflectionEntityInformation<T, ID>
		implements
			DynamoDBEntityInformation<T, ID> {

	private DynamoDBHashKeyExtractingEntityMetadata<T> metadata;
	private HashKeyExtractor<ID, ID> hashKeyExtractor;
	private Optional<String> projection = Optional.empty();
	private Optional<Integer> limit = Optional.empty();

	public DynamoDBIdIsHashKeyEntityInformationImpl(Class<T> domainClass,
			DynamoDBHashKeyExtractingEntityMetadata<T> metadata) {
		super(domainClass, DynamoDBHashKey.class);
		this.metadata = metadata;
		this.hashKeyExtractor = new HashKeyIsIdHashKeyExtractor<ID>(getIdType());
	}

	@Override
	public Optional<String> getProjection() {
		return projection;
	}

	@Override
	public Optional<Integer> getLimit() {
		return limit;
	}

	@Override
	public Object getHashKey(final ID id) {
		Assert.isAssignable(getIdType(), id.getClass(),
				"Expected ID type to be the same as the return type of the hash key method ( " + getIdType() + " ) : ");
		return hashKeyExtractor.getHashKey(id);
	}

	// The following methods simply delegate to metadata, or always return
	// constants

	@Override
	public Optional<String> getOverriddenAttributeName(String attributeName) {
		return metadata.getOverriddenAttributeName(attributeName);
	}

	@Override
	public boolean isHashKeyProperty(String propertyName) {
		return metadata.isHashKeyProperty(propertyName);
	}

	@Override
	public boolean isCompositeHashAndRangeKeyProperty(String propertyName) {
		return false;
	}

	@SuppressWarnings("deprecation")
	@Override
	public <V extends DynamoDBMarshaller<?>> V getMarshallerForProperty(String propertyName) {
		return metadata.getMarshallerForProperty(propertyName);
	}

	@Override
	public DynamoDBTypeConverter<?, ?> getTypeConverterForProperty(String propertyName) {
		return metadata.getTypeConverterForProperty(propertyName);
	}

	@Override
	public Object getRangeKey(ID id) {
		return null;
	}

	@Override
	public String getDynamoDBTableName() {
		return metadata.getDynamoDBTableName();
	}

	@Override
	public String getHashKeyPropertyName() {
		return metadata.getHashKeyPropertyName();
	}

	@Override
	public Map<String, String[]> getGlobalSecondaryIndexNamesByPropertyName() {
		return metadata.getGlobalSecondaryIndexNamesByPropertyName();
	}

	@Override
	public boolean isGlobalIndexHashKeyProperty(String propertyName) {
		return metadata.isGlobalIndexHashKeyProperty(propertyName);
	}

	@Override
	public boolean isGlobalIndexRangeKeyProperty(String propertyName) {
		return metadata.isGlobalIndexRangeKeyProperty(propertyName);
	}

}
