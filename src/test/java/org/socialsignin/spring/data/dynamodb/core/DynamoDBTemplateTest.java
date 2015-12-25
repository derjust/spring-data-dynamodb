package org.socialsignin.spring.data.dynamodb.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.socialsignin.spring.data.dynamodb.mapping.event.AfterDeleteEvent;
import org.socialsignin.spring.data.dynamodb.mapping.event.AfterLoadEvent;
import org.socialsignin.spring.data.dynamodb.mapping.event.AfterQueryEvent;
import org.socialsignin.spring.data.dynamodb.mapping.event.AfterSaveEvent;
import org.socialsignin.spring.data.dynamodb.mapping.event.BeforeDeleteEvent;
import org.socialsignin.spring.data.dynamodb.mapping.event.BeforeSaveEvent;
import org.socialsignin.spring.data.dynamodb.mapping.event.DynamoDBMappingEvent;
import org.springframework.context.ApplicationContext;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig.TableNameOverride;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.KeyPair;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedScanList;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.amazonaws.services.dynamodbv2.model.QueryResult;
import com.amazonaws.services.dynamodbv2.model.Select;

@RunWith(MockitoJUnitRunner.class)
public class DynamoDBTemplateTest {

    @Mock
    DynamoDBMapper dynamoDBMapper;
    @Mock
    AmazonDynamoDB amazonDynamoDB;
    @Mock
    DynamoDBMapperConfig dynamoDBMapperConfig;
    @Mock
    ApplicationContext applicationContext;

    @InjectMocks
    private DynamoDBTemplate underTest;

    @Before
    public void setUp() {
        underTest.setApplicationContext(applicationContext);
    }

    @Test
    public void testCountQuery() {
        DynamoDBQueryExpression<Object> queryExpression = mock(DynamoDBQueryExpression.class);
        when(underTest.count(Object.class, queryExpression)).thenReturn(42);
        int actual = underTest.count(Object.class, queryExpression);

        assertEquals(42, actual);
    }

    @Test
    public void testCountScan() {
        DynamoDBScanExpression scanExpression = mock(DynamoDBScanExpression.class);
        when(underTest.count(Object.class, scanExpression)).thenReturn(42);
        int actual = underTest.count(Object.class, scanExpression);

        assertEquals(42, actual);
    }

    @Test
    public void testCount() {
        QueryRequest queryRequest = mock(QueryRequest.class);
        QueryResult queryResult1 = mock(QueryResult.class);
        when(queryResult1.getCount()).thenReturn(23);
        Map<String, AttributeValue> lastEvaluatedKey = new HashMap<String, AttributeValue>();
        when(queryResult1.getLastEvaluatedKey()).thenReturn(lastEvaluatedKey);
        QueryResult queryResult2 = mock(QueryResult.class);
        when(queryResult2.getCount()).thenReturn(42);
        when(queryResult2.getLastEvaluatedKey()).thenReturn(null);
        when(amazonDynamoDB.query(queryRequest)).thenReturn(queryResult1, queryResult2);

        int actual = underTest.count(Object.class, queryRequest);

        verify(queryRequest).setSelect(Select.COUNT);
        assertEquals(23+42, actual);
    }

    @Test
    public void testLoadHashKeyRangeKey() {
        Object actual = underTest.load(Object.class, "hashKey", "rangeKey");
        assertNull(actual);
        verify(applicationContext, never()).publishEvent(any(AfterLoadEvent.class));

        Object value = new Object();
        when(underTest.load(Object.class, "hashKey", "rangeKey")).thenReturn(value);
        actual = underTest.load(Object.class, "hashKey", "rangeKey");
        assertSame(value, actual);
        verify(applicationContext).publishEvent(new AfterLoadEvent<Object>(value));
    }

    @Test
    public void testLoadHashKey() {
        Object actual = underTest.load(Object.class, "hashKey");
        assertNull(actual);
        verify(applicationContext, never()).publishEvent(any(AfterLoadEvent.class));

        Object value = new Object();
        when(underTest.load(Object.class, "hashKey")).thenReturn(value);
        actual = underTest.load(Object.class, "hashKey");
        assertSame(value, actual);
        verify(applicationContext).publishEvent(new AfterLoadEvent<Object>(value));
    }

    @Test
    public void testScan() {
        DynamoDBScanExpression scanExpression = mock(DynamoDBScanExpression.class);
        PaginatedScanList<Object> retValue = mock(PaginatedScanList.class);
        when(dynamoDBMapper.scan(Object.class, scanExpression)).thenReturn(retValue);

        PaginatedScanList<Object> actual = underTest.scan(Object.class, scanExpression);

        assertEquals(retValue, actual);
        verify(applicationContext).publishEvent(any(AfterLoadEvent.class));
    }

    @Test
    public void testBatchLoad() {
        Map<Class<?>, List<KeyPair>> itemsToGet = mock(Map.class);

        Map<String, List<Object>> retValue = new HashMap<String, List<Object>>();
        Object value = new Object();
        retValue.put("key", Arrays.asList(value));
        when(underTest.batchLoad(itemsToGet)).thenReturn(retValue );

        Map<String, List<Object>> actual = underTest.batchLoad(itemsToGet);

        assertEquals(retValue, actual);
        verify(applicationContext).publishEvent(new AfterLoadEvent<Object>(value));
    }

    @Test
    public void testSave() {
        Object obj = mock(Object.class);
        underTest.save(obj);

        verify(dynamoDBMapper).save(obj);

        verify(applicationContext).publishEvent(new BeforeSaveEvent<Object>(obj));

        verify(applicationContext).publishEvent(new AfterSaveEvent<Object>(obj));
    }

    @Test
    public void testBatchSave() {
        Object obj1 = mock(Object.class);
        Object obj2 = mock(Object.class);
        List<?> entities = Arrays.asList(obj1, obj2);
        underTest.batchSave(entities);

        verify(dynamoDBMapper).batchSave(entities);

        verify(applicationContext).publishEvent(new BeforeSaveEvent<Object>(obj1));
        verify(applicationContext).publishEvent(new BeforeSaveEvent<Object>(obj2));

        verify(applicationContext).publishEvent(new AfterSaveEvent<Object>(obj1));
        verify(applicationContext).publishEvent(new AfterSaveEvent<Object>(obj2));
    }

    @Test
    public void testDelete() {
        Object obj = mock(Object.class);

        underTest.delete(obj);

        verify(applicationContext).publishEvent(new BeforeDeleteEvent<Object>(obj));
        verify(applicationContext).publishEvent(new AfterDeleteEvent<Object>(obj));
    }

    @Test
    public void testBatchDelete() {
        Object obj1 = mock(Object.class);
        Object obj2 = mock(Object.class);
        List<?> entities = Arrays.asList(obj1, obj2);
        underTest.batchDelete(entities);

        verify(dynamoDBMapper).batchDelete(entities);

        verify(applicationContext).publishEvent(new BeforeDeleteEvent<Object>(obj1));
        verify(applicationContext).publishEvent(new BeforeDeleteEvent<Object>(obj2));

        verify(applicationContext).publishEvent(new AfterDeleteEvent<Object>(obj1));
        verify(applicationContext).publishEvent(new AfterDeleteEvent<Object>(obj2));
    }

    @Test
    public void testQueryQuery() {
        QueryRequest queryRequest = mock(QueryRequest.class);
        QueryResult queryResult = mock(QueryResult.class);
        when(amazonDynamoDB.query(queryRequest)).thenReturn(queryResult);

        PaginatedQueryList<Object> actual = underTest.query(Object.class, queryRequest);

        assertNotNull(actual);
        verify(dynamoDBMapperConfig).getPaginationLoadingStrategy();
    }


    @Test
    public void testQueryExpression() {
        PaginatedQueryList<Object> paginatedQueryList = mock(PaginatedQueryList.class);
        DynamoDBQueryExpression<Object> queryExpression = mock(DynamoDBQueryExpression.class);
        when(dynamoDBMapper.query(Object.class, queryExpression)).thenReturn(paginatedQueryList);

        PaginatedQueryList<Object> actual = underTest.query(Object.class, queryExpression);

        assertSame(actual, paginatedQueryList);
        verify(applicationContext).publishEvent(any(AfterQueryEvent.class));
    }

    @Test
    public void testGetOverriddenTableName() {
        String tableName = "narfzot42";
        assertEquals(tableName, underTest.getOverriddenTableName(tableName));

        String tableNameOverride = "narfzot23";
        TableNameOverride tableNameReplacement = TableNameOverride.withTableNameReplacement(tableNameOverride);
        when(dynamoDBMapperConfig.getTableNameOverride()).thenReturn(tableNameReplacement);
        assertEquals(tableNameOverride, underTest.getOverriddenTableName(tableName));

        TableNameOverride tableNamePrefix = TableNameOverride.withTableNamePrefix(tableNameOverride);
        when(dynamoDBMapperConfig.getTableNameOverride()).thenReturn(tableNamePrefix);
        assertEquals(tableNameOverride + tableName, underTest.getOverriddenTableName(tableName));
    }

    @Test
    public void testMaybeEmitEvent() {
        underTest.setApplicationContext(null);
        Object eventObject = new Object();
        DynamoDBMappingEvent<Object> event = new DynamoDBMappingEvent<Object>(eventObject);

        //Should not cause anything to fail
        underTest.maybeEmitEvent(event);

        ApplicationContext applicationContext = mock(ApplicationContext.class);
        underTest.setApplicationContext(applicationContext);

        underTest.maybeEmitEvent(event);
        verify(applicationContext).publishEvent(event);
    }


}
