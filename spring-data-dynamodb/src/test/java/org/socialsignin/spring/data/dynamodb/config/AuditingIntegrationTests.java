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
package org.socialsignin.spring.data.dynamodb.config;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import org.joda.time.DateTime;
import org.junit.Test;
import org.socialsignin.spring.data.dynamodb.mapping.DynamoDBMappingContext;
import org.socialsignin.spring.data.dynamodb.mapping.event.BeforeSaveEvent;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Integration test for the auditing support.
 * 
 * @author Vito Limandibhrata
 */
public class AuditingIntegrationTests {

	@Test
	public void enablesAuditingAndSetsPropertiesAccordingly() throws Exception {

		AbstractApplicationContext context = new ClassPathXmlApplicationContext("auditing.xml", getClass());

		DynamoDBMappingContext mappingContext = context.getBean(DynamoDBMappingContext.class);
		mappingContext.getPersistentEntity(Entity.class);

		Entity entity = new Entity();
		BeforeSaveEvent<Entity> event = new BeforeSaveEvent<Entity>(entity);
		context.publishEvent(event);

		assertThat(entity.created, is(notNullValue()));
		assertThat(entity.modified, is(entity.created));

		Thread.sleep(10);
		entity.id = 1L;
		event = new BeforeSaveEvent<Entity>(entity);
		context.publishEvent(event);

		assertThat(entity.created, is(notNullValue()));
		assertThat(entity.modified, is(not(entity.created)));
		context.close();
	}

	@DynamoDBTable(tableName = "Entity")
	class Entity {

		@Id
		Long id;
		@CreatedDate
		DateTime created;
		DateTime modified;

		@LastModifiedDate
		public DateTime getModified() {
			return modified;
		}
	}
}
