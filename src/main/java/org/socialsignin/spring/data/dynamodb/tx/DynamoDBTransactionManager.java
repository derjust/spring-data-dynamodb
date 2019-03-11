package org.socialsignin.spring.data.dynamodb.tx;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.transactions.Transaction;
import com.amazonaws.services.dynamodbv2.transactions.TransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

@Component
public class DynamoDBTransactionManager extends AbstractPlatformTransactionManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(DynamoDBTransactionManager.class);

    private final AmazonDynamoDB client;
    private final TransactionManager txManager;
    private final String transactionsTableName;
    private final String transactionsImageTableName;
    private final long readCapacityUnits;
    private final long writeCapacityUnits;
    private final long waitTimeSeconds;

    @Autowired
    public DynamoDBTransactionManager(AmazonDynamoDB client, @Value("${Transactions}") String transactionsTableName,
                                      @Value("${TransactionImages}") String transactionsImageTableName,
                                      @Value("${TransactionImages}") long readCapacityUnits,
                                      @Value("${TransactionImages}") long writeCapacityUnits,
                                      @Value("${TransactionImages}") long waitTimeSeconds) {
        this.client = client;
        this.transactionsTableName = transactionsTableName;
        this.transactionsImageTableName = transactionsImageTableName;

        this.readCapacityUnits = readCapacityUnits;
        this.writeCapacityUnits = writeCapacityUnits;
        this.waitTimeSeconds = waitTimeSeconds;

        try {
            initialize();
        } catch (InterruptedException e) {
            throw new BeanInitializationException("Initialization of " + this.transactionsTableName
                    + "/"  + this.transactionsImageTableName+ " failed!", e);
        }

        this.txManager = new TransactionManager (client, this.transactionsTableName, this.transactionsImageTableName) ;
    }

    private void initialize() throws InterruptedException {
        LOGGER.trace("Initialize tables");

        TransactionManager.verifyOrCreateTransactionTable(client, this.transactionsTableName,
                this.readCapacityUnits, this.writeCapacityUnits, this.waitTimeSeconds);

        TransactionManager.verifyOrCreateTransactionImagesTable(client, this.transactionsImageTableName,
                this.readCapacityUnits, this.writeCapacityUnits, this.waitTimeSeconds);

        LOGGER.debug("Finished table initialization");
    }

    private Transaction getTransaction(DefaultTransactionStatus status) {
        return (Transaction)status.getTransaction();
    }

    @Override
    protected Object doGetTransaction() throws TransactionException {
        // Create a new transaction from the transaction manager
        Transaction tx = txManager.newTransaction();

        return tx;
    }

    @Override
    protected void doBegin(Object transaction, TransactionDefinition definition) throws TransactionException {
        Transaction tx = (Transaction)transaction;

        LOGGER.debug("Begin for <{}>", tx);
    }

    @Override
    protected void doCommit(DefaultTransactionStatus status) throws TransactionException {
        Transaction tx = getTransaction(status);
        LOGGER.debug("Commit for <{}>", tx);

        tx.commit();
        LOGGER.trace("Delete for <{}>", tx);
        tx.delete();
    }

    @Override
    protected void doRollback(DefaultTransactionStatus status) throws TransactionException {
        Transaction tx = getTransaction(status);
        LOGGER.debug("Rollback for <{}>", tx);

        tx.rollback();
        LOGGER.trace("Delete for <{}>", tx);
        tx.delete();
    }

}
