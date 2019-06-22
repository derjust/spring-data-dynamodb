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
package org.socialsignin.spring.data.dynamodb.utils;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

/**
 * Clue {@link Configuration} class for all integration tests. It exposes the
 * {@link AmazonDynamoDB} pre-configured to use the launched local DynamoDB by
 * Maven's integration-test.
 */
@Configuration
public class DynamoDBResource {
	private static final String DYNAMODB_PORT_PROPERTY = "dynamodb.port";
	private static final String PORT = System.getProperty(DYNAMODB_PORT_PROPERTY);

	@Bean
	public AmazonDynamoDB amazonDynamoDB() {
		Assert.notNull(PORT, "System property '" + DYNAMODB_PORT_PROPERTY + " not set!");

		AmazonDynamoDBClientBuilder builder = AmazonDynamoDBClientBuilder.standard();
		builder.withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials("AWS-Key", "")));
		builder.withEndpointConfiguration(
				new AwsClientBuilder.EndpointConfiguration(String.format("http://localhost:%s", PORT), "us-east-1"));

		return builder.build();
	}
}
