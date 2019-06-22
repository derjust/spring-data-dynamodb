/**
 * Copyright © 2013-2019 spring-data-dynamodb (https://github.com/derjust/spring-data-dynamodb/spring-data-dynamodb-examples/spring-data-dynamodb-examples-multirepo)
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
package com.github.derjust.spring_data_dynamodb_examples.multirepo;

import java.util.Date;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Configuration
@Import(DynamoDBConfig.class)
public class Application {

	private static final Logger log = LoggerFactory.getLogger(Application.class);

	public static void main(String[] args) {
		new SpringApplicationBuilder(Application.class).run(args);
	}

	@Bean
	public CommandLineRunner multirepo(ConfigurableApplicationContext ctx, CustomerRepository jpaRepository,
			DeviceRepository dynamoDBRepository) {
		return (args) -> {
			demoJPA(jpaRepository);

			// Tables are autocreated
			demoDynamoDB(dynamoDBRepository);

			ctx.close();
		};
	}

	private void demoDynamoDB(DeviceRepository dynamoDBRepository) {
		// save a couple of devices
		dynamoDBRepository.save(new Device(1L, "Product A", "A", new Date()));
		dynamoDBRepository.save(new Device(1L, "Product B", "B", new Date()));
		dynamoDBRepository.save(new Device(2L, "Product C", "C", new Date()));

		// fetch all devices
		log.info("Devices found with findAll():");
		log.info("-------------------------------");
		for (Device device : dynamoDBRepository.findAll()) {
			log.info(device.toString());
		}
		log.info("");

	}

	private void demoJPA(CustomerRepository jpaRepository) {
		// save a couple of customers
		jpaRepository.save(new Customer("Jack", "Bauer"));
		jpaRepository.save(new Customer("Chloe", "O'Brian"));
		jpaRepository.save(new Customer("Kim", "Bauer"));
		jpaRepository.save(new Customer("David", "Palmer"));
		jpaRepository.save(new Customer("Michelle", "Dessler"));

		// fetch all customers
		log.info("Customers found with findAll():");
		log.info("-------------------------------");
		for (Customer customer : jpaRepository.findAll()) {
			log.info(customer.toString());
		}
		log.info("");

		// fetch an individual customer by ID
		Optional<Customer> customer = jpaRepository.findById(1L);
		log.info("Customer found with findOne(1L):");
		log.info("--------------------------------");
		log.info(customer.get().toString());
		log.info("");

		// fetch customers by last name
		log.info("Customer found with findByLastName('Bauer'):");
		log.info("--------------------------------------------");
		//TODO
		/*for (Customer bauer : jpaRepository.findByLastName("Bauer")) {
			log.info(bauer.toString());
		}*/
		log.info("");
	}

}
