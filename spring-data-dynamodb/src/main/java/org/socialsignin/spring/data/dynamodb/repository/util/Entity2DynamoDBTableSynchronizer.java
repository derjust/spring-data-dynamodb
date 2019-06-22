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
package org.socialsignin.spring.data.dynamodb.repository.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socialsignin.spring.data.dynamodb.repository.support.DynamoDBEntityInformation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.data.repository.core.support.RepositoryProxyPostProcessor;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.DeleteTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableResult;
import com.amazonaws.services.dynamodbv2.model.Projection;
import com.amazonaws.services.dynamodbv2.model.ProjectionType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.amazonaws.services.dynamodbv2.util.TableUtils;
import com.amazonaws.services.dynamodbv2.util.TableUtils.TableNeverTransitionedToStateException;

/**
 * This is the base class for all classes performing the validation or
 * auto-creation of tables based on the entity classes.
 * 
 * //TODO: It would be nice if the checks would run in parallel via a
 * TaskScheduler (if available)
 * 
 * @see Entity2DDL
 */
public class Entity2DynamoDBTableSynchronizer<T, ID> extends EntityInformationProxyPostProcessor<T, ID>
		implements
			RepositoryProxyPostProcessor,
			ApplicationListener<ApplicationContextEvent> {
	private static final Logger LOGGER = LoggerFactory.getLogger(Entity2DynamoDBTableSynchronizer.class);

	private static final String CONFIGURATION_KEY_entity2ddl_auto = "${spring.data.dynamodb.entity2ddl.auto:none}";
	private static final String CONFIGURATION_KEY_entity2ddl_gsiProjectionType = "${spring.data.dynamodb.entity2ddl.gsiProjectionType:ALL}";
	private static final String CONFIGURATION_KEY_entity2ddl_readCapacity = "${spring.data.dynamodb.entity2ddl.readCapacity:10}";
	private static final String CONFIGURATION_KEY_entity2ddl_writeCapacity = "${spring.data.dynamodb.entity2ddl.writeCapacity:1}";

	private final AmazonDynamoDB amazonDynamoDB;
	private final DynamoDBMapper mapper;

	private final Entity2DDL mode;
	private final ProjectionType gsiProjectionType;
	private final ProvisionedThroughput pt;

	private final Collection<DynamoDBEntityInformation<T, ID>> registeredEntities = new ArrayList<>();

	public Entity2DynamoDBTableSynchronizer(AmazonDynamoDB amazonDynamoDB, DynamoDBMapper mapper, Entity2DDL mode) {
		this(amazonDynamoDB, mapper, mode.getConfigurationValue(), ProjectionType.ALL.name(), 10L, 10L);
	}

	@Autowired
	public Entity2DynamoDBTableSynchronizer(AmazonDynamoDB amazonDynamoDB, DynamoDBMapper mapper,
			@Value(CONFIGURATION_KEY_entity2ddl_auto) String mode,
			@Value(CONFIGURATION_KEY_entity2ddl_gsiProjectionType) String gsiProjectionType,
			@Value(CONFIGURATION_KEY_entity2ddl_readCapacity) long readCapacity,
			@Value(CONFIGURATION_KEY_entity2ddl_writeCapacity) long writeCapacity) {
		this.amazonDynamoDB = amazonDynamoDB;
		this.mapper = mapper;

		this.mode = Entity2DDL.fromValue(mode);
		this.pt = new ProvisionedThroughput(readCapacity, writeCapacity);
		this.gsiProjectionType = ProjectionType.fromValue(gsiProjectionType);
	}

	@Override
	protected void registeredEntity(DynamoDBEntityInformation<T, ID> entityInformation) {
		this.registeredEntities.add(entityInformation);
	}

	@Override
	public void onApplicationEvent(ApplicationContextEvent event) {
		LOGGER.info("Checking repository classes with DynamoDB tables {} for {}",
				registeredEntities.stream().map(e -> e.getDynamoDBTableName()).collect(Collectors.joining(", ")),
				event.getClass().getSimpleName());

		for (DynamoDBEntityInformation<T, ID> entityInformation : registeredEntities) {

			try {
				synchronize(entityInformation, event);
			} catch (TableNeverTransitionedToStateException | InterruptedException e) {
				throw new RuntimeException("Could not perform Entity2DDL operation " + mode + " on "
						+ entityInformation.getDynamoDBTableName(), e);
			}
		}
	}

	protected void synchronize(DynamoDBEntityInformation<T, ID> entityInformation, ApplicationContextEvent event)
			throws TableNeverTransitionedToStateException, InterruptedException {

		if (event instanceof ContextRefreshedEvent) {
			switch (mode) {
				case CREATE_DROP :
				case CREATE :
					performDrop(entityInformation);
					// TODO implement wait for deletion
				case CREATE_ONLY :
					performCreate(entityInformation);
					break;
				case VALIDATE :
					performValidate(entityInformation);
					break;
				case DROP :
				case NONE :
				default :
					LOGGER.debug("No auto table DDL performed on start");
					break;
			}
		} else if (event instanceof ContextStoppedEvent) {
			switch (mode) {
				case CREATE_DROP :
				case DROP :
					performDrop(entityInformation);
					performCreate(entityInformation);
					break;

				case CREATE :
				case VALIDATE :
				case NONE :
				default :
					LOGGER.debug("No auto table DDL performed on stop");
					break;
			}
		} else {
			LOGGER.trace("Ignored ApplicationContextEvent: {}", event);
		}

	}

	private boolean performCreate(DynamoDBEntityInformation<T, ID> entityInformation)
			throws TableNeverTransitionedToStateException, InterruptedException {
		Class<T> domainType = entityInformation.getJavaType();

		CreateTableRequest ctr = mapper.generateCreateTableRequest(domainType);
		LOGGER.trace("Creating table {} for entity {}", ctr.getTableName(), domainType);
		ctr.setProvisionedThroughput(pt);

		if (ctr.getGlobalSecondaryIndexes() != null) {
			ctr.getGlobalSecondaryIndexes().forEach(gsi -> {
				gsi.setProjection(new Projection().withProjectionType(gsiProjectionType));
				gsi.setProvisionedThroughput(pt);
			});
		}

		boolean result = TableUtils.createTableIfNotExists(amazonDynamoDB, ctr);
		if (result) {
			TableUtils.waitUntilActive(amazonDynamoDB, ctr.getTableName());
			LOGGER.debug("Created table {} for entity {}", ctr.getTableName(), domainType);
		}
		return result;
	}

	private boolean performDrop(DynamoDBEntityInformation<T, ID> entityInformation) {
		Class<T> domainType = entityInformation.getJavaType();

		DeleteTableRequest dtr = mapper.generateDeleteTableRequest(domainType);
		LOGGER.trace("Dropping table {} for entity {}", dtr.getTableName(), domainType);

		boolean result = TableUtils.deleteTableIfExists(amazonDynamoDB, dtr);
		if (result) {
			LOGGER.debug("Deleted table {} for entity {}", dtr.getTableName(), domainType);
		}

		return result;
	}

	/**
	 * @param entityInformation
	 *            The entity to check for it's table
	 * @throws IllegalStateException
	 *             is thrown if the existing table doesn't match the entity's
	 *             annotation
	 */
	private DescribeTableResult performValidate(DynamoDBEntityInformation<T, ID> entityInformation)
			throws IllegalStateException {
		Class<T> domainType = entityInformation.getJavaType();

		CreateTableRequest expected = mapper.generateCreateTableRequest(domainType);
		DescribeTableResult result = amazonDynamoDB.describeTable(expected.getTableName());
		TableDescription actual = result.getTable();

		if (!expected.getKeySchema().equals(actual.getKeySchema())) {
			throw new IllegalStateException("KeySchema is not as expected. Expected: <" + expected.getKeySchema()
					+ "> but found <" + actual.getKeySchema() + ">");
		}
		LOGGER.debug("KeySchema is valid");

		if (expected.getGlobalSecondaryIndexes() != null) {
			if (!Arrays.deepEquals(expected.getGlobalSecondaryIndexes().toArray(),
					actual.getGlobalSecondaryIndexes().toArray())) {
				throw new IllegalStateException("Global Secondary Indexes are not as expected. Expected: <"
						+ expected.getGlobalSecondaryIndexes() + "> but found <" + actual.getGlobalSecondaryIndexes()
						+ ">");
			}
		}
		LOGGER.debug("Global Secondary Indexes are valid");

		LOGGER.info("Validated table {} for entity{}", expected.getTableName(), domainType);
		return result;
	}

}
