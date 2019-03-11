package org.socialsignin.spring.data.dynamodb.tx;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableResult;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.amazonaws.services.dynamodbv2.model.TableStatus;
import com.amazonaws.services.dynamodbv2.model.UpdateItemRequest;
import com.amazonaws.services.dynamodbv2.model.UpdateItemResult;
import com.amazonaws.services.dynamodbv2.model.UpdateTableRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionStatus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Random;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.intThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DynamoDBTransactionManagerTest {
    private static final Random r = new Random();
    private final long readCapacityUnits = r.nextLong();
    private final long writeCapacityUnits = r.nextLong();
    private final long waitTimeSeconds = 1;
    private final String transactionsTableName = "transactionsTableName";
    private final String transactionsImageTableName = "transactionsImageTableName";
    @Mock
    private AmazonDynamoDB client;
    @Mock
    private TransactionDefinition txDefinition;

    private DynamoDBTransactionManager underTest;

    @Before
    public void setUp() {

        when(client.describeTable(any(DescribeTableRequest.class))).thenReturn(
                 new DescribeTableResult()
                        .withTable(new TableDescription()
                                .withAttributeDefinitions(new AttributeDefinition()
                                        .withAttributeName("_TxId")
                                        .withAttributeType(ScalarAttributeType.S))
                                .withKeySchema(new KeySchemaElement()
                                        .withAttributeName("_TxId")
                                        .withKeyType(KeyType.HASH))
                                .withTableStatus(TableStatus.ACTIVE)),
                new DescribeTableResult()
                        .withTable(new TableDescription()
                                .withAttributeDefinitions(new AttributeDefinition()
                                        .withAttributeName("_TxI")
                                        .withAttributeType(ScalarAttributeType.S))
                                .withKeySchema(new KeySchemaElement()
                                        .withAttributeName("_TxI")
                                        .withKeyType(KeyType.HASH))
                                .withTableStatus(TableStatus.ACTIVE))
        );

        when(client.updateItem(any(UpdateItemRequest.class))).thenReturn(
                new UpdateItemResult().withAttributes(Collections.emptyMap()));

        underTest = new DynamoDBTransactionManager(client, transactionsTableName,
                transactionsImageTableName, readCapacityUnits, writeCapacityUnits, waitTimeSeconds);

    }

    @Test
    public void simulateCommit() {
        TransactionDefinition txDefinition = new DefaultTransactionDefinition();
        TransactionStatus tx = underTest.getTransaction(txDefinition);


        underTest.commit(tx);

    }

}
