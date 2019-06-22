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
package org.socialsignin.spring.data.dynamodb.repository.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;

public class DynamoDBMapperConfigPostProcessor implements BeanPostProcessor {
	private static final Logger LOGGER = LoggerFactory.getLogger(DynamoDBMapperConfigPostProcessor.class);

	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof DynamoDBMapperConfig) {
			DynamoDBMapperConfig dynamoDBMapperConfig = (DynamoDBMapperConfig) bean;
			if (dynamoDBMapperConfig == DynamoDBMapperConfig.DEFAULT) {
				return bean;
			}
			// #146, #81 #157
			// Trying to fix half-initialized DynamoDBMapperConfigs here.
			// The old documentation advised to start with an empty builder. Therefore we
			// try here to set required fields to their defaults -
			// As the documentation at
			// https://github.com/derjust/spring-data-dynamodb/wiki/Alter-table-name-during-runtime
			// (same as https://git.io/DynamoDBMapperConfig)
			// now does: Start with #DEFAULT and add what's required
			DynamoDBMapperConfig.Builder emptyBuilder = DynamoDBMapperConfig.builder(); // empty (!) builder

			if (dynamoDBMapperConfig.getConversionSchema() == null) {
				LOGGER.warn(
						"No ConversionSchema set in the provided dynamoDBMapperConfig! Merging with DynamoDBMapperConfig.DEFAULT - Please see https://git.io/DynamoDBMapperConfig");
				// DynamoDBMapperConfig#DEFAULT comes with a ConversionSchema
				emptyBuilder.withConversionSchema(DynamoDBMapperConfig.DEFAULT.getConversionSchema());
			}

			if (dynamoDBMapperConfig.getTypeConverterFactory() == null) {
				LOGGER.warn(
						"No TypeConverterFactory set in the provided dynamoDBMapperConfig! Merging with DynamoDBMapperConfig.DEFAULT - Please see https://git.io/DynamoDBMapperConfig");
				// DynamoDBMapperConfig#DEFAULT comes with a TypeConverterFactory
				emptyBuilder.withTypeConverterFactory(DynamoDBMapperConfig.DEFAULT.getTypeConverterFactory());
			}

			return createDynamoDBMapperConfig(dynamoDBMapperConfig, emptyBuilder);

		} else {
			return bean;
		}
	}

	@SuppressWarnings("deprecation")
	private DynamoDBMapperConfig createDynamoDBMapperConfig(DynamoDBMapperConfig dynamoDBMapperConfig,
			DynamoDBMapperConfig.Builder emptyBuilder) {
		// Deprecated but the only way how DynamoDBMapperConfig#merge is exposed
		return new DynamoDBMapperConfig(dynamoDBMapperConfig, emptyBuilder.build());
	}
}
