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
package org.socialsignin.spring.data.dynamodb.repository.query;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperTableModel;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.amazonaws.services.dynamodbv2.model.Select;
import org.socialsignin.spring.data.dynamodb.core.DynamoDBOperations;
import org.socialsignin.spring.data.dynamodb.query.CountByHashAndRangeKeyQuery;
import org.socialsignin.spring.data.dynamodb.query.MultipleEntityQueryExpressionQuery;
import org.socialsignin.spring.data.dynamodb.query.MultipleEntityQueryRequestQuery;
import org.socialsignin.spring.data.dynamodb.query.MultipleEntityScanExpressionQuery;
import org.socialsignin.spring.data.dynamodb.query.Query;
import org.socialsignin.spring.data.dynamodb.query.QueryExpressionCountQuery;
import org.socialsignin.spring.data.dynamodb.query.QueryRequestCountQuery;
import org.socialsignin.spring.data.dynamodb.query.ScanExpressionCountQuery;
import org.socialsignin.spring.data.dynamodb.query.SingleEntityLoadByHashAndRangeKeyQuery;
import org.socialsignin.spring.data.dynamodb.repository.support.DynamoDBIdIsHashAndRangeKeyEntityInformation;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * @author Michael Lavelle
 * @author Sebastian Just
 */
public class DynamoDBEntityWithHashAndRangeKeyCriteria<T, ID> extends AbstractDynamoDBQueryCriteria<T, ID> {

	private Object rangeKeyAttributeValue;
	private Object rangeKeyPropertyValue;
	private String rangeKeyPropertyName;
	private Set<String> indexRangeKeyPropertyNames;
	private DynamoDBIdIsHashAndRangeKeyEntityInformation<T, ID> entityInformation;

	protected String getRangeKeyAttributeName() {
		return getAttributeName(getRangeKeyPropertyName());
	}

	protected String getRangeKeyPropertyName() {
		return rangeKeyPropertyName;
	}

	protected boolean isRangeKeyProperty(String propertyName) {
		return rangeKeyPropertyName.equals(propertyName);
	}

	public DynamoDBEntityWithHashAndRangeKeyCriteria(
			DynamoDBIdIsHashAndRangeKeyEntityInformation<T, ID> entityInformation,
			DynamoDBMapperTableModel<T> tableModel) {

		super(entityInformation, tableModel);
		this.rangeKeyPropertyName = entityInformation.getRangeKeyPropertyName();
		this.indexRangeKeyPropertyNames = entityInformation.getIndexRangeKeyPropertyNames();
		if (indexRangeKeyPropertyNames == null) {
			indexRangeKeyPropertyNames = new HashSet<>();
		}
		this.entityInformation = entityInformation;
	}

	public Set<String> getIndexRangeKeyAttributeNames() {
		Set<String> indexRangeKeyAttributeNames = new HashSet<>();
		for (String indexRangeKeyPropertyName : indexRangeKeyPropertyNames) {
			indexRangeKeyAttributeNames.add(getAttributeName(indexRangeKeyPropertyName));
		}
		return indexRangeKeyAttributeNames;
	}

	protected Object getRangeKeyAttributeValue() {
		return rangeKeyAttributeValue;
	}

	protected Object getRangeKeyPropertyValue() {
		return rangeKeyPropertyValue;
	}

	protected boolean isRangeKeySpecified() {
		return getRangeKeyAttributeValue() != null;
	}

	protected Query<T> buildSingleEntityLoadQuery(DynamoDBOperations dynamoDBOperations) {
		return new SingleEntityLoadByHashAndRangeKeyQuery<>(dynamoDBOperations, entityInformation.getJavaType(),
				getHashKeyPropertyValue(), getRangeKeyPropertyValue());
	}

	protected Query<Long> buildSingleEntityCountQuery(DynamoDBOperations dynamoDBOperations) {
		return new CountByHashAndRangeKeyQuery<>(dynamoDBOperations, entityInformation.getJavaType(),
				getHashKeyPropertyValue(), getRangeKeyPropertyValue());
	}

	private void checkComparisonOperatorPermittedForCompositeHashAndRangeKey(ComparisonOperator comparisonOperator) {

		if (!ComparisonOperator.EQ.equals(comparisonOperator) && !ComparisonOperator.CONTAINS.equals(comparisonOperator)
				&& !ComparisonOperator.BEGINS_WITH.equals(comparisonOperator)) {
			throw new UnsupportedOperationException(
					"Only EQ,CONTAINS,BEGINS_WITH supported for composite id comparison");
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public DynamoDBQueryCriteria<T, ID> withSingleValueCriteria(String propertyName,
			ComparisonOperator comparisonOperator, Object value, Class<?> propertyType) {

		if (entityInformation.isCompositeHashAndRangeKeyProperty(propertyName)) {
			checkComparisonOperatorPermittedForCompositeHashAndRangeKey(comparisonOperator);
			Object hashKey = entityInformation.getHashKey((ID) value);
			Object rangeKey = entityInformation.getRangeKey((ID) value);
			if (hashKey != null) {
				withSingleValueCriteria(getHashKeyPropertyName(), comparisonOperator, hashKey, hashKey.getClass());
			}
			if (rangeKey != null) {
				withSingleValueCriteria(getRangeKeyPropertyName(), comparisonOperator, rangeKey, rangeKey.getClass());
			}
			return this;
		} else {
			return super.withSingleValueCriteria(propertyName, comparisonOperator, value, propertyType);
		}
	}

	public DynamoDBQueryExpression<T> buildQueryExpression() {
		DynamoDBQueryExpression<T> queryExpression = new DynamoDBQueryExpression<T>();
		if (isHashKeySpecified()) {
			T hashKeyPrototype = entityInformation.getHashKeyPropotypeEntityForHashKey(getHashKeyPropertyValue());
			queryExpression.withHashKeyValues(hashKeyPrototype);
			queryExpression.withRangeKeyConditions(new HashMap<String, Condition>());
		}

		if (isRangeKeySpecified() && !isApplicableForGlobalSecondaryIndex()) {
			Condition rangeKeyCondition = createSingleValueCondition(getRangeKeyPropertyName(), ComparisonOperator.EQ,
					getRangeKeyAttributeValue(), getRangeKeyAttributeValue().getClass(), true);
			queryExpression.withRangeKeyCondition(getRangeKeyAttributeName(), rangeKeyCondition);
			applySortIfSpecified(queryExpression, Arrays.asList(new String[]{getRangeKeyPropertyName()}));

		} else if (isOnlyASingleAttributeConditionAndItIsOnEitherRangeOrIndexRangeKey()
				|| (isApplicableForGlobalSecondaryIndex())) {

			Entry<String, List<Condition>> singlePropertyConditions = propertyConditions.entrySet().iterator().next();

			List<String> allowedSortProperties = new ArrayList<>();
			for (Entry<String, List<Condition>> singlePropertyCondition : propertyConditions.entrySet()) {
				if (entityInformation.getGlobalSecondaryIndexNamesByPropertyName().keySet()
						.contains(singlePropertyCondition.getKey())) {
					allowedSortProperties.add(singlePropertyCondition.getKey());
				}
			}
			if (allowedSortProperties.size() == 0) {
				allowedSortProperties.add(singlePropertyConditions.getKey());
			}

			for (Entry<String, List<Condition>> singleAttributeConditions : attributeConditions.entrySet()) {
				for (Condition condition : singleAttributeConditions.getValue()) {
					queryExpression.withRangeKeyCondition(singleAttributeConditions.getKey(), condition);
				}
			}

			applySortIfSpecified(queryExpression, allowedSortProperties);
			if (getGlobalSecondaryIndexName() != null) {
				queryExpression.setIndexName(getGlobalSecondaryIndexName());
			}
		} else {
			applySortIfSpecified(queryExpression, Arrays.asList(new String[]{getRangeKeyPropertyName()}));
		}

		if (projection.isPresent()) {
			queryExpression.setSelect(Select.SPECIFIC_ATTRIBUTES);
			queryExpression.setProjectionExpression(projection.get());
		}
		limit.ifPresent(queryExpression::setLimit);
		return queryExpression;
	}

	protected List<Condition> getRangeKeyConditions() {
		List<Condition> rangeKeyConditions = null;
		if (isApplicableForGlobalSecondaryIndex() && entityInformation.getGlobalSecondaryIndexNamesByPropertyName()
				.keySet().contains(getRangeKeyPropertyName())) {
			rangeKeyConditions = getRangeKeyAttributeValue() == null
					? null
					: Arrays.asList(createSingleValueCondition(getRangeKeyPropertyName(), ComparisonOperator.EQ,
							getRangeKeyAttributeValue(), getRangeKeyAttributeValue().getClass(), true));

		}
		return rangeKeyConditions;
	}

	protected Query<T> buildFinderQuery(DynamoDBOperations dynamoDBOperations) {
		if (isApplicableForQuery()) {
			if (isApplicableForGlobalSecondaryIndex()) {
				String tableName = dynamoDBOperations.getOverriddenTableName(clazz,
						entityInformation.getDynamoDBTableName());
				QueryRequest queryRequest = buildQueryRequest(tableName, getGlobalSecondaryIndexName(),
						getHashKeyAttributeName(), getRangeKeyAttributeName(), this.getRangeKeyPropertyName(),
						getHashKeyConditions(), getRangeKeyConditions());
				return new MultipleEntityQueryRequestQuery<>(dynamoDBOperations, entityInformation.getJavaType(),
						queryRequest);
			} else {
				DynamoDBQueryExpression<T> queryExpression = buildQueryExpression();
				return new MultipleEntityQueryExpressionQuery<>(dynamoDBOperations, entityInformation.getJavaType(),
						queryExpression);
			}
		} else {
			return new MultipleEntityScanExpressionQuery<>(dynamoDBOperations, clazz, buildScanExpression());
		}
	}

	protected Query<Long> buildFinderCountQuery(DynamoDBOperations dynamoDBOperations, boolean pageQuery) {
		if (isApplicableForQuery()) {
			if (isApplicableForGlobalSecondaryIndex()) {
				String tableName = dynamoDBOperations.getOverriddenTableName(clazz,
						entityInformation.getDynamoDBTableName());
				QueryRequest queryRequest = buildQueryRequest(tableName, getGlobalSecondaryIndexName(),
						getHashKeyAttributeName(), getRangeKeyAttributeName(), this.getRangeKeyPropertyName(),
						getHashKeyConditions(), getRangeKeyConditions());
				return new QueryRequestCountQuery(dynamoDBOperations, queryRequest);

			} else {
				DynamoDBQueryExpression<T> queryExpression = buildQueryExpression();
				return new QueryExpressionCountQuery<>(dynamoDBOperations, entityInformation.getJavaType(),
						queryExpression);

			}
		} else {
			return new ScanExpressionCountQuery<T>(dynamoDBOperations, clazz, buildScanExpression(), pageQuery);
		}
	}

	@Override
	public boolean isApplicableForLoad() {
		return attributeConditions.size() == 0 && isHashAndRangeKeySpecified();
	}

	protected boolean isHashAndRangeKeySpecified() {
		return isHashKeySpecified() && isRangeKeySpecified();
	}

	protected boolean isOnlyASingleAttributeConditionAndItIsOnEitherRangeOrIndexRangeKey() {
		boolean isOnlyASingleAttributeConditionAndItIsOnEitherRangeOrIndexRangeKey = false;
		if (!isRangeKeySpecified() && attributeConditions.size() == 1) {
			Entry<String, List<Condition>> conditionsEntry = attributeConditions.entrySet().iterator().next();
			if (conditionsEntry.getKey().equals(getRangeKeyAttributeName())
					|| getIndexRangeKeyAttributeNames().contains(conditionsEntry.getKey())) {
				if (conditionsEntry.getValue().size() == 1) {
					isOnlyASingleAttributeConditionAndItIsOnEitherRangeOrIndexRangeKey = true;
				}
			}
		}
		return isOnlyASingleAttributeConditionAndItIsOnEitherRangeOrIndexRangeKey;

	}

	@Override
	protected boolean hasIndexHashKeyEqualCondition() {

		boolean hasCondition = super.hasIndexHashKeyEqualCondition();
		if (!hasCondition) {
			if (rangeKeyAttributeValue != null
					&& entityInformation.isGlobalIndexHashKeyProperty(rangeKeyPropertyName)) {
				hasCondition = true;
			}
		}
		return hasCondition;
	}

	@Override
	protected boolean hasIndexRangeKeyCondition() {
		boolean hasCondition = super.hasIndexRangeKeyCondition();
		if (!hasCondition) {
			if (rangeKeyAttributeValue != null
					&& entityInformation.isGlobalIndexRangeKeyProperty(rangeKeyPropertyName)) {
				hasCondition = true;
			}
		}
		return hasCondition;
	}

	protected boolean isApplicableForGlobalSecondaryIndex() {
		boolean global = super.isApplicableForGlobalSecondaryIndex();
		if (global && getRangeKeyAttributeValue() != null && !entityInformation
				.getGlobalSecondaryIndexNamesByPropertyName().keySet().contains(getRangeKeyPropertyName())) {
			return false;
		}

		return global;

	}

	protected String getGlobalSecondaryIndexName() {
		// Get the target global secondary index name using the property
		// conditions
		String globalSecondaryIndexName = super.getGlobalSecondaryIndexName();

		// Hash and Range Entities store range key equals conditions as
		// rangeKeyAttributeValue attribute instead of as property condition
		// Check this attribute and if specified in the query conditions and
		// it's the only global secondary index range candidate,
		// then set the index range key to be that associated with the range key
		if (globalSecondaryIndexName == null) {
			if (this.hashKeyAttributeValue == null && getRangeKeyAttributeValue() != null) {
				String[] rangeKeyIndexNames = entityInformation.getGlobalSecondaryIndexNamesByPropertyName()
						.get(this.getRangeKeyPropertyName());
				globalSecondaryIndexName = rangeKeyIndexNames != null && rangeKeyIndexNames.length > 0
						? rangeKeyIndexNames[0]
						: null;
			}
		}
		return globalSecondaryIndexName;
	}

	public boolean isApplicableForQuery() {

		return isOnlyHashKeySpecified()
				|| (isHashKeySpecified() && isOnlyASingleAttributeConditionAndItIsOnEitherRangeOrIndexRangeKey()
						&& comparisonOperatorsPermittedForQuery())
				|| isApplicableForGlobalSecondaryIndex();

	}

	public DynamoDBScanExpression buildScanExpression() {

		ensureNoSort(sort);

		DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
		if (isHashKeySpecified()) {
			scanExpression.addFilterCondition(getHashKeyAttributeName(),
					createSingleValueCondition(getHashKeyPropertyName(), ComparisonOperator.EQ,
							getHashKeyAttributeValue(), getHashKeyAttributeValue().getClass(), true));
		}
		if (isRangeKeySpecified()) {
			scanExpression.addFilterCondition(getRangeKeyAttributeName(),
					createSingleValueCondition(getRangeKeyPropertyName(), ComparisonOperator.EQ,
							getRangeKeyAttributeValue(), getRangeKeyAttributeValue().getClass(), true));
		}
		for (Map.Entry<String, List<Condition>> conditionEntry : attributeConditions.entrySet()) {
			for (Condition condition : conditionEntry.getValue()) {
				scanExpression.addFilterCondition(conditionEntry.getKey(), condition);
			}
		}
		return scanExpression;
	}

	public DynamoDBQueryCriteria<T, ID> withRangeKeyEquals(Object value) {
		Assert.notNull(value, "Creating conditions on null range keys not supported: please specify a value for '"
				+ getRangeKeyPropertyName() + "'");

		rangeKeyAttributeValue = getPropertyAttributeValue(getRangeKeyPropertyName(), value);
		rangeKeyPropertyValue = value;
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public DynamoDBQueryCriteria<T, ID> withPropertyEquals(String propertyName, Object value, Class<?> propertyType) {
		if (isHashKeyProperty(propertyName)) {
			return withHashKeyEquals(value);
		} else if (isRangeKeyProperty(propertyName)) {
			return withRangeKeyEquals(value);
		} else if (entityInformation.isCompositeHashAndRangeKeyProperty(propertyName)) {
			Assert.notNull(value,
					"Creating conditions on null composite id properties not supported: please specify a value for '"
							+ propertyName + "'");
			Object hashKey = entityInformation.getHashKey((ID) value);
			Object rangeKey = entityInformation.getRangeKey((ID) value);
			if (hashKey != null) {
				withHashKeyEquals(hashKey);
			}
			if (rangeKey != null) {
				withRangeKeyEquals(rangeKey);
			}
			return this;
		} else {
			Condition condition = createSingleValueCondition(propertyName, ComparisonOperator.EQ, value, propertyType,
					false);
			return withCondition(propertyName, condition);
		}

	}

	@Override
	protected boolean isOnlyHashKeySpecified() {
		return isHashKeySpecified() && attributeConditions.size() == 0 && !isRangeKeySpecified();
	}

}
