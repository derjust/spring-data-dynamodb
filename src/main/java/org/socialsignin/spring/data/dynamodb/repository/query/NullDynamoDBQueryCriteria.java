package org.socialsignin.spring.data.dynamodb.repository.query;

import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import org.socialsignin.spring.data.dynamodb.core.DynamoDBOperations;
import org.socialsignin.spring.data.dynamodb.query.Query;
import org.springframework.data.domain.Sort;

import java.util.Collections;
import java.util.List;

public class NullDynamoDBQueryCriteria<T, ID> implements DynamoDBQueryCriteria<T, ID> {

    private static final Query<Object> EMPTY_QUERY_RESULT = new Query<Object>() {
        @Override
        public List<Object> getResultList() {
            return Collections.emptyList();
        }

        @Override
        public Object getSingleResult() {
            return null;
        }

        @Override
        public void setScanEnabled(boolean scanEnabled) {
        }

        @Override
        public void setScanCountEnabled(boolean scanCountEnabled) {
        }
    };

    private static final Query<Long> ZERO_QUERY_RESULT = new Query<Long>() {
        private final Long ZERO = Long.valueOf(0L);
        private final List<Long> ZERO_LIST = Collections.singletonList(ZERO);

        @Override
        public List<Long> getResultList() {
            return ZERO_LIST;
        }

        @Override
        public Long getSingleResult() {
            return ZERO;
        }

        @Override
        public void setScanEnabled(boolean scanEnabled) {
        }

        @Override
        public void setScanCountEnabled(boolean scanCountEnabled) {
        }
    };


    @Override
    public DynamoDBQueryCriteria<T, ID> withSingleValueCriteria(String propertyName, ComparisonOperator comparisonOperator, Object value, Class<?> type) {
        return this;
    }

    @Override
    public DynamoDBQueryCriteria<T, ID> withNoValuedCriteria(String segment, ComparisonOperator null1) {
        return this;
    }

    @Override
    public DynamoDBQueryCriteria<T, ID> withPropertyEquals(String segment, Object next, Class<?> type) {
        return this;
    }

    @Override
    public DynamoDBQueryCriteria<T, ID> withPropertyIn(String segment, Iterable<?> o, Class<?> type) {
        return this;
    }

    @Override
    public DynamoDBQueryCriteria<T, ID> withPropertyBetween(String segment, Object value1, Object value2, Class<?> type) {
        return this;
    }

    @Override
    public DynamoDBQueryCriteria<T, ID> withSort(Sort sort) {
        return this;
    }

    @Override
    public Query<T> buildQuery(DynamoDBOperations dynamoDBOperations) {
        return (Query<T>)EMPTY_QUERY_RESULT;
    }

    @Override
    public Query<Long> buildCountQuery(DynamoDBOperations dynamoDBOperations, boolean pageQuery) {
        return ZERO_QUERY_RESULT;
    }
}
