/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.socialsignin.spring.data.dynamodb.repository.query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.socialsignin.spring.data.dynamodb.core.DynamoDBOperations;
import org.socialsignin.spring.data.dynamodb.mapping.DefaultDynamoDBDateMarshaller;
import org.socialsignin.spring.data.dynamodb.query.Query;
import org.socialsignin.spring.data.dynamodb.repository.support.DynamoDBEntityInformation;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMarshaller;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.amazonaws.services.dynamodbv2.model.Select;

/**
 * @author Michael Lavelle
 */
public abstract class AbstractDynamoDBQueryCriteria<T, ID extends Serializable> implements DynamoDBQueryCriteria<T, ID> {

	protected Class<T> clazz;
	private DynamoDBEntityInformation<T, ID> entityInformation;
	private Map<String, String> attributeNamesByPropertyName;
	private String hashKeyPropertyName;

	protected MultiValueMap<String, Condition> attributeConditions;
	protected MultiValueMap<String, Condition> propertyConditions;

	protected Object hashKeyAttributeValue;
	protected Object hashKeyPropertyValue;
	protected String globalSecondaryIndexName;
	protected Sort sort;

	public abstract boolean isApplicableForLoad();

	protected QueryRequest buildQueryRequest(String tableName, String theIndexName, String hashKeyAttributeName,
			String rangeKeyAttributeName, String rangeKeyPropertyName, List<Condition> hashKeyConditions,
			List<Condition> rangeKeyConditions) {

		// TODO Set other query request properties based on config
		QueryRequest queryRequest = new QueryRequest();
		queryRequest.setTableName(tableName);
		queryRequest.setIndexName(theIndexName);

		if (isApplicableForGlobalSecondaryIndex()) {
			List<String> allowedSortProperties = new ArrayList<String>();

			for (Entry<String, List<Condition>> singlePropertyCondition : propertyConditions.entrySet()) {
				if (entityInformation.getGlobalSecondaryIndexNamesByPropertyName().keySet()
						.contains(singlePropertyCondition.getKey())) {
					allowedSortProperties.add(singlePropertyCondition.getKey());
				}
			}

			HashMap<String, Condition> keyConditions = new HashMap<String, Condition>();

			if (hashKeyConditions != null && hashKeyConditions.size() > 0) {
				for (Condition hashKeyCondition : hashKeyConditions) {
					keyConditions.put(hashKeyAttributeName, hashKeyCondition);
					allowedSortProperties.add(hashKeyPropertyName);
				}
			}
			if (rangeKeyConditions != null && rangeKeyConditions.size() > 0) {
				for (Condition rangeKeyCondition : rangeKeyConditions) {
					keyConditions.put(rangeKeyAttributeName, rangeKeyCondition);
					allowedSortProperties.add(rangeKeyPropertyName);
				}
			}

			for (Entry<String, List<Condition>> singleAttributeConditions : attributeConditions.entrySet()) {

				for (Condition condition : singleAttributeConditions.getValue()) {
					keyConditions.put(singleAttributeConditions.getKey(), condition);
				}
			}

			queryRequest.setKeyConditions(keyConditions);
			queryRequest.setSelect(Select.ALL_PROJECTED_ATTRIBUTES);
			applySortIfSpecified(queryRequest, new ArrayList<String>(new HashSet<String>(allowedSortProperties)));
		}
		return queryRequest;
	}

	protected void applySortIfSpecified(DynamoDBQueryExpression<T> queryExpression, List<String> permittedPropertyNames) {
		if (permittedPropertyNames.size() > 1) {
			throw new UnsupportedOperationException("Can only sort by at most a single range or index range key");

		}
		if (sort != null) {
			boolean sortAlreadySet = false;
			for (Order order : sort) {
				if (permittedPropertyNames.contains(order.getProperty())) {
					if (sortAlreadySet) {
						throw new UnsupportedOperationException("Sorting by multiple attributes not possible");

					}
					queryExpression.setScanIndexForward(order.getDirection().equals(Direction.ASC));
					sortAlreadySet = true;
				} else {
					throw new UnsupportedOperationException("Sorting only possible by " + permittedPropertyNames
							+ " for the criteria specified");
				}
			}
		}
	}

	protected void applySortIfSpecified(QueryRequest queryRequest, List<String> permittedPropertyNames) {
		if (permittedPropertyNames.size() > 2) {
			throw new UnsupportedOperationException("Can only sort by at most a single global hash and range key");
		}

		if (sort != null) {
			boolean sortAlreadySet = false;
			for (Order order : sort) {
				if (permittedPropertyNames.contains(order.getProperty())) {
					if (sortAlreadySet) {
						throw new UnsupportedOperationException("Sorting by multiple attributes not possible");

					}
					if (queryRequest.getKeyConditions().size() > 1 && !hasIndexHashKeyEqualCondition()) {
						throw new UnsupportedOperationException(
								"Sorting for global index queries with criteria on both hash and range not possible");

					}
					queryRequest.setScanIndexForward(order.getDirection().equals(Direction.ASC));
					sortAlreadySet = true;
				} else {
					throw new UnsupportedOperationException("Sorting only possible by " + permittedPropertyNames
							+ " for the criteria specified");
				}
			}
		}
	}

	public boolean comparisonOperatorsPermittedForQuery() {
		List<ComparisonOperator> comparisonOperatorsPermittedForQuery = Arrays.asList(new ComparisonOperator[] {
				ComparisonOperator.EQ, ComparisonOperator.LE, ComparisonOperator.LT, ComparisonOperator.GE,
				ComparisonOperator.GT, ComparisonOperator.BEGINS_WITH, ComparisonOperator.BETWEEN });

		// Can only query on subset of Conditions
		for (Collection<Condition> conditions : attributeConditions.values()) {
			for (Condition condition : conditions) {
				if (!comparisonOperatorsPermittedForQuery
						.contains(ComparisonOperator.fromValue(condition.getComparisonOperator()))) {
					return false;
				}
			}
		}
		return true;
	}

	protected List<Condition> getHashKeyConditions() {
		List<Condition> hashKeyConditions = null;
		if (isApplicableForGlobalSecondaryIndex()
				&& entityInformation.getGlobalSecondaryIndexNamesByPropertyName().keySet().contains(getHashKeyPropertyName())) {
			hashKeyConditions = getHashKeyAttributeValue() == null ? null : Arrays.asList(createSingleValueCondition(
					getHashKeyPropertyName(), ComparisonOperator.EQ, getHashKeyAttributeValue(), getHashKeyAttributeValue()
							.getClass(), true));
			if (hashKeyConditions == null) {
				if (attributeConditions.containsKey(getHashKeyAttributeName())) {
					hashKeyConditions = attributeConditions.get(getHashKeyAttributeName());
				}

			}

		}
		return hashKeyConditions;
	}

	public AbstractDynamoDBQueryCriteria(DynamoDBEntityInformation<T, ID> dynamoDBEntityInformation) {
		this.clazz = dynamoDBEntityInformation.getJavaType();
		this.attributeConditions = new LinkedMultiValueMap<String, Condition>();
		this.propertyConditions = new LinkedMultiValueMap<String, Condition>();
		this.hashKeyPropertyName = dynamoDBEntityInformation.getHashKeyPropertyName();
		this.entityInformation = dynamoDBEntityInformation;
		this.attributeNamesByPropertyName = new HashMap<String, String>();

	}

	private String getFirstDeclaredIndexNameForAttribute(Map<String,String[]> indexNamesByAttributeName,List<String> indexNamesToCheck,String attributeName)
	{
		String indexName = null;
		String[] declaredOrderedIndexNamesForAttribute = indexNamesByAttributeName.get(attributeName);
		for (String declaredOrderedIndexNameForAttribute : declaredOrderedIndexNamesForAttribute)
		{
			if (indexName == null && indexNamesToCheck.contains(declaredOrderedIndexNameForAttribute))
			{
					indexName = declaredOrderedIndexNameForAttribute;
			}
		}

		return indexName;
	}

	protected String getGlobalSecondaryIndexName() {


		// Lazy evaluate the globalSecondaryIndexName if not already set

		// We must have attribute conditions specified in order to use a global secondary index, otherwise return null for index name
		// Also this method only evaluates the
		if (globalSecondaryIndexName == null  && attributeConditions != null && !attributeConditions.isEmpty())
		{
			// Declare map of index names by attribute name which we will populate below - this will be used to determine which index to use if multiple indexes are applicable
			Map<String,String[]> indexNamesByAttributeName =  new HashMap<String,String[]>();

			// Declare map of attribute lists by index name which we will populate below - this will be used to determine whether we have an exact match index for specified attribute conditions
			MultiValueMap<String,String> attributeListsByIndexName = new LinkedMultiValueMap<String,String>();

			// Populate the above maps
			for (Entry<String, String[]> indexNamesForPropertyNameEntry : entityInformation.getGlobalSecondaryIndexNamesByPropertyName().entrySet())
			{
				String propertyName = indexNamesForPropertyNameEntry.getKey();
				String attributeName = getAttributeName(propertyName);
				indexNamesByAttributeName.put(attributeName, indexNamesForPropertyNameEntry.getValue());
				for (String indexNameForPropertyName : indexNamesForPropertyNameEntry.getValue())
				{
					attributeListsByIndexName.add(indexNameForPropertyName, attributeName);
				}
			}

			// Declare lists to store matching index names
			List<String> exactMatchIndexNames = new ArrayList<String>();
			List<String> partialMatchIndexNames = new ArrayList<String>();

			// Populate matching index name lists - an index is either an exact match ( the index attributes match all the specified criteria exactly)
			// or a partial match ( the properties for the specified criteria are contained within the property set for an index )
			for (Entry<String, List<String>> attributeListForIndexNameEntry : attributeListsByIndexName.entrySet())
			{
				String indexNameForAttributeList = attributeListForIndexNameEntry.getKey();
				List<String> attributeList = attributeListForIndexNameEntry.getValue();
				if (attributeList.containsAll(attributeConditions.keySet()))
				{
					if (attributeConditions.keySet().containsAll(attributeList))
					{
						exactMatchIndexNames.add(indexNameForAttributeList);
					}
					else
					{
						partialMatchIndexNames.add(indexNameForAttributeList);
					}
				}
			}

			if (exactMatchIndexNames.size() > 1)
			{
				throw new RuntimeException("Multiple indexes defined on same attribute set:" + attributeConditions.keySet());
			}
			else if (exactMatchIndexNames.size() == 1)
			{
				globalSecondaryIndexName = exactMatchIndexNames.get(0);
			}
			else if (partialMatchIndexNames.size() > 1)
			{
				if (attributeConditions.size() == 1)
				{
					globalSecondaryIndexName = getFirstDeclaredIndexNameForAttribute(indexNamesByAttributeName, partialMatchIndexNames, attributeConditions.keySet().iterator().next());
				}
				if (globalSecondaryIndexName == null)
				{
					globalSecondaryIndexName = partialMatchIndexNames.get(0);
				}
			}
			else if (partialMatchIndexNames.size() == 1)
			{
				globalSecondaryIndexName = partialMatchIndexNames.get(0);
			}
		}
		return globalSecondaryIndexName;
	}
	protected boolean isHashKeyProperty(String propertyName) {
		return hashKeyPropertyName.equals(propertyName);
	}

	protected String getHashKeyPropertyName() {
		return hashKeyPropertyName;
	}

	protected String getHashKeyAttributeName() {
		return getAttributeName(getHashKeyPropertyName());
	}


	protected boolean hasIndexHashKeyEqualCondition()
	{
		boolean hasIndexHashKeyEqualCondition = false;
		for (Map.Entry<String, List<Condition>> propertyConditionList : propertyConditions.entrySet())
		{
			if (entityInformation.isGlobalIndexHashKeyProperty(propertyConditionList.getKey()))
			{
				for (Condition condition : propertyConditionList.getValue())
				{
					if ( condition.getComparisonOperator().equals(ComparisonOperator.EQ.name()))
					{
							 	hasIndexHashKeyEqualCondition = true;
					}
				}
			}
		}
		if (hashKeyAttributeValue != null && entityInformation.isGlobalIndexHashKeyProperty(hashKeyPropertyName))
		{
			hasIndexHashKeyEqualCondition = true;
		}
		return hasIndexHashKeyEqualCondition;
	}

	protected boolean hasIndexRangeKeyCondition()
	{
		boolean hasIndexRangeKeyCondition = false;
		for (Map.Entry<String, List<Condition>> propertyConditionList : propertyConditions.entrySet())
		{
			if (entityInformation.isGlobalIndexRangeKeyProperty(propertyConditionList.getKey()))
			{
				hasIndexRangeKeyCondition = true;
			}
		}
		if (hashKeyAttributeValue != null && entityInformation.isGlobalIndexRangeKeyProperty(hashKeyPropertyName))
		{
			hasIndexRangeKeyCondition = true;
		}
		return hasIndexRangeKeyCondition;
	}
	protected boolean isApplicableForGlobalSecondaryIndex() {
		boolean global = this.getGlobalSecondaryIndexName() != null;
		if (global && getHashKeyAttributeValue() != null
				&& !entityInformation.getGlobalSecondaryIndexNamesByPropertyName().keySet().contains(getHashKeyPropertyName())) {
			return false;
		}

		int attributeConditionCount = attributeConditions.keySet().size();
		boolean attributeConditionsAppropriate =  hasIndexHashKeyEqualCondition() && (attributeConditionCount  == 1 || (attributeConditionCount == 2 && hasIndexRangeKeyCondition()));
		return global && (attributeConditionCount == 0 || attributeConditionsAppropriate) && comparisonOperatorsPermittedForQuery();

	}

	public DynamoDBQueryCriteria<T, ID> withHashKeyEquals(Object value) {
		Assert.notNull(value, "Creating conditions on null hash keys not supported: please specify a value for '"
				+ getHashKeyPropertyName() + "'");

		hashKeyAttributeValue = getPropertyAttributeValue(getHashKeyPropertyName(), value);
		hashKeyPropertyValue = value;
		return this;
	}

	public boolean isHashKeySpecified() {
		return getHashKeyAttributeValue() != null;
	}

	public Object getHashKeyAttributeValue() {
		return hashKeyAttributeValue;
	}

	public Object getHashKeyPropertyValue() {
		return hashKeyPropertyValue;
	}

	protected String getAttributeName(String propertyName) {
		String attributeName = attributeNamesByPropertyName.get(propertyName);
		if (attributeName == null) {
			String overriddenName = entityInformation.getOverriddenAttributeName(propertyName);
			attributeName = overriddenName != null ? overriddenName : propertyName;
			attributeNamesByPropertyName.put(propertyName, attributeName);
		}
		return attributeName;

	}

	@Override
	public DynamoDBQueryCriteria<T, ID> withPropertyBetween(String propertyName, Object value1, Object value2, Class<?> type) {
		Condition condition = createCollectionCondition(propertyName, ComparisonOperator.BETWEEN, Arrays.asList(value1, value2),
				type);
		return withCondition(propertyName, condition);
	}

	@Override
	public DynamoDBQueryCriteria<T, ID> withPropertyIn(String propertyName, Iterable<?> value, Class<?> propertyType) {

		Condition condition = createCollectionCondition(propertyName, ComparisonOperator.IN, value, propertyType);
		return withCondition(propertyName, condition);
	}

	@Override
	public DynamoDBQueryCriteria<T, ID> withSingleValueCriteria(String propertyName, ComparisonOperator comparisonOperator,
			Object value, Class<?> propertyType) {
		if (comparisonOperator.equals(ComparisonOperator.EQ)) {
			return withPropertyEquals(propertyName, value, propertyType);
		} else {
			Condition condition = createSingleValueCondition(propertyName, comparisonOperator, value, propertyType, false);
			return withCondition(propertyName, condition);
		}
	}

	@Override
	public Query<T> buildQuery(DynamoDBOperations dynamoDBOperations) {
		if (isApplicableForLoad()) {
			return buildSingleEntityLoadQuery(dynamoDBOperations);
		} else {
			return buildFinderQuery(dynamoDBOperations);
		}
	}

	@Override
	public Query<Long> buildCountQuery(DynamoDBOperations dynamoDBOperations,boolean pageQuery) {
		if (isApplicableForLoad()) {
			return buildSingleEntityCountQuery(dynamoDBOperations);
		} else {
			return buildFinderCountQuery(dynamoDBOperations,pageQuery);
		}
	}


	protected abstract Query<T> buildSingleEntityLoadQuery(DynamoDBOperations dynamoDBOperations);

	protected abstract Query<Long> buildSingleEntityCountQuery(DynamoDBOperations dynamoDBOperations);


	protected abstract Query<T> buildFinderQuery(DynamoDBOperations dynamoDBOperations);

	protected abstract Query<Long> buildFinderCountQuery(DynamoDBOperations dynamoDBOperations,boolean pageQuery);


	protected abstract boolean isOnlyHashKeySpecified();

	@Override
	public DynamoDBQueryCriteria<T, ID> withNoValuedCriteria(String propertyName, ComparisonOperator comparisonOperator) {
		Condition condition = createNoValueCondition(propertyName, comparisonOperator);
		return withCondition(propertyName, condition);

	}

	public DynamoDBQueryCriteria<T, ID> withCondition(String propertyName, Condition condition) {
		attributeConditions.add(getAttributeName(propertyName), condition);
		propertyConditions.add(propertyName, condition);

		return this;
	}

	@SuppressWarnings("unchecked")
	protected <V> Object getPropertyAttributeValue(String propertyName, Object value) {
		DynamoDBMarshaller<V> marshaller = (DynamoDBMarshaller<V>) entityInformation.getMarshallerForProperty(propertyName);

		if (marshaller != null) {
			return marshaller.marshall((V) value);
		} else {
			return value;
		}
	}

	protected <V> Condition createNoValueCondition(String propertyName, ComparisonOperator comparisonOperator) {

		Condition condition = new Condition().withComparisonOperator(comparisonOperator);

		return condition;
	}

	private List<String> getNumberListAsStringList(List<Number> numberList) {
		List<String> list = new ArrayList<String>();
		for (Number number : numberList) {
			if (number != null) {
				list.add(number.toString());
			} else {
				list.add(null);
			}
		}
		return list;
	}

	private List<String> getDateListAsStringList(List<Date> dateList) {
		DynamoDBMarshaller<Date> marshaller = new DefaultDynamoDBDateMarshaller();
		List<String> list = new ArrayList<String>();
		for (Date date : dateList) {
			if (date != null) {
				list.add(marshaller.marshall(date));
			} else {
				list.add(null);
			}
		}
		return list;
	}

	private List<String> getBooleanListAsStringList(List<Boolean> booleanList) {
		List<String> list = new ArrayList<String>();
		for (Boolean booleanValue : booleanList) {
			if (booleanValue != null) {
				list.add(booleanValue.booleanValue() ? "1" : "0");
			} else {
				list.add(null);
			}
		}
		return list;
	}

	@SuppressWarnings("unchecked")
	private <P> List<P> getAttributeValueAsList(Object attributeValue) {
		boolean isIterable = ClassUtils.isAssignable(Iterable.class, attributeValue.getClass());
		List<P> attributeValueAsList = null;
		if (isIterable) {
			attributeValueAsList = new ArrayList<P>();
			Iterable<P> iterable = (Iterable<P>) attributeValue;
			for (P attributeValueElement : iterable) {
				attributeValueAsList.add(attributeValueElement);
			}
			return attributeValueAsList;
		}
		return null;
	}

	protected <P> List<AttributeValue> addAttributeValue(List<AttributeValue> attributeValueList, Object attributeValue,
			String propertyName, Class<P> propertyType, boolean expandCollectionValues) {
		AttributeValue attributeValueObject = new AttributeValue();

		if (ClassUtils.isAssignable(String.class, propertyType)) {
			List<String> attributeValueAsList = getAttributeValueAsList(attributeValue);
			if (expandCollectionValues && attributeValueAsList != null) {
				attributeValueObject.withSS(attributeValueAsList);
			} else {
				attributeValueObject.withS((String) attributeValue);
			}
		} else if (ClassUtils.isAssignable(Number.class, propertyType)) {

			List<Number> attributeValueAsList = getAttributeValueAsList(attributeValue);
			if (expandCollectionValues && attributeValueAsList != null) {
				List<String> attributeValueAsStringList = getNumberListAsStringList(attributeValueAsList);
				attributeValueObject.withNS(attributeValueAsStringList);
			} else {
				attributeValueObject.withN(attributeValue.toString());
			}
		} else if (ClassUtils.isAssignable(Boolean.class, propertyType)) {
			List<Boolean> attributeValueAsList = getAttributeValueAsList(attributeValue);
			if (expandCollectionValues && attributeValueAsList != null) {
				List<String> attributeValueAsStringList = getBooleanListAsStringList(attributeValueAsList);
				attributeValueObject.withNS(attributeValueAsStringList);
			} else {
				boolean boolValue = ((Boolean) attributeValue).booleanValue();
				attributeValueObject.withN(boolValue ? "1" : "0");
			}
		} else if (ClassUtils.isAssignable(Date.class, propertyType)) {
			List<Date> attributeValueAsList = getAttributeValueAsList(attributeValue);
			if (expandCollectionValues && attributeValueAsList != null) {
				List<String> attributeValueAsStringList = getDateListAsStringList(attributeValueAsList);
				attributeValueObject.withSS(attributeValueAsStringList);
			} else {
				Date date = (Date) attributeValue;
				String marshalledDate = new DefaultDynamoDBDateMarshaller().marshall(date);
				attributeValueObject.withS(marshalledDate);
			}
		} else {
			throw new RuntimeException("Cannot create condition for type:" + attributeValue.getClass()
					+ " property conditions must be String,Number or Boolean, or have a DynamoDBMarshaller configured");
		}
		attributeValueList.add(attributeValueObject);

		return attributeValueList;
	}

	protected Condition createSingleValueCondition(String propertyName, ComparisonOperator comparisonOperator, Object o,
			Class<?> propertyType, boolean alreadyMarshalledIfRequired) {

		Assert.notNull(o, "Creating conditions on null property values not supported: please specify a value for '"
				+ propertyName + "'");

		Object attributeValue = !alreadyMarshalledIfRequired ? getPropertyAttributeValue(propertyName, o) : o;

		boolean marshalled = !alreadyMarshalledIfRequired && attributeValue != o
				&& !entityInformation.isCompositeHashAndRangeKeyProperty(propertyName);

		Class<?> targetPropertyType = marshalled ? String.class : propertyType;
		List<AttributeValue> attributeValueList = new ArrayList<AttributeValue>();
		attributeValueList = addAttributeValue(attributeValueList, attributeValue, propertyName, targetPropertyType, true);
		return new Condition().withComparisonOperator(comparisonOperator).withAttributeValueList(attributeValueList);

	}

	protected Condition createCollectionCondition(String propertyName, ComparisonOperator comparisonOperator, Iterable<?> o,
			Class<?> propertyType) {

		Assert.notNull(o, "Creating conditions on null property values not supported: please specify a value for '"
				+ propertyName + "'");
		List<AttributeValue> attributeValueList = new ArrayList<AttributeValue>();
		boolean marshalled = false;
		for (Object object : o) {
			Object attributeValue = getPropertyAttributeValue(propertyName, object);
			if (attributeValue != null) {
				marshalled = attributeValue != object && !entityInformation.isCompositeHashAndRangeKeyProperty(propertyName);
			}
			Class<?> targetPropertyType = marshalled ? String.class : propertyType;
			attributeValueList = addAttributeValue(attributeValueList, attributeValue, propertyName, targetPropertyType, false);

		}

		return new Condition().withComparisonOperator(comparisonOperator).withAttributeValueList(attributeValueList);

	}

	@Override
	public DynamoDBQueryCriteria<T, ID> withSort(Sort sort) {
		this.sort = sort;
		return this;
	}

}
