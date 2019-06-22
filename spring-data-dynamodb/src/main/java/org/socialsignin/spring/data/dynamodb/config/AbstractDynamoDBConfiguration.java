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

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socialsignin.spring.data.dynamodb.mapping.DynamoDBMappingContext;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * Base class for Spring Data DynamoDB configuration using JavaConfig.
 *
 * @author Vito Limandibhrata
 */
@Configuration
public abstract class AbstractDynamoDBConfiguration {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDynamoDBConfiguration.class);

	public abstract AmazonDynamoDB amazonDynamoDB();

	public abstract AWSCredentials amazonAWSCredentials();

	/**
	 * Return the base packages to scan for mapped {@link DynamoDBTable}s. Will
	 * return the package name of the configuration class' (the concrete class, not
	 * this one here) by default. So if you have a {@code com.acme.AppConfig}
	 * extending {@link AbstractDynamoDBConfiguration} the base package will be
	 * considered {@code com.acme} unless the method is overriden to implement
	 * alternate behaviour.
	 *
	 * @return the base package to scan for mapped {@link DynamoDBTable} classes or
	 *         {@literal null} to not enable scanning for entities.
	 */
	protected String[] getMappingBasePackages() {

		Package mappingBasePackage = getClass().getPackage();
		String basePackage = mappingBasePackage == null ? null : mappingBasePackage.getName();

		return new String[]{basePackage};
	}

	/**
	 * Creates a {@link DynamoDBMappingContext} equipped with entity classes scanned
	 * from the mapping base package.
	 *
	 * @see #getMappingBasePackages()
	 * @return A newly created {@link DynamoDBMappingContext}
	 * @throws ClassNotFoundException
	 *             if the class with {@link DynamoDBTable} annotation can't be
	 *             loaded
	 */
	@Bean
	public DynamoDBMappingContext dynamoDBMappingContext() throws ClassNotFoundException {

		DynamoDBMappingContext mappingContext = new DynamoDBMappingContext();
		mappingContext.setInitialEntitySet(getInitialEntitySet());

		return mappingContext;
	}

	/**
	 * Scans the mapping base package for classes annotated with
	 * {@link DynamoDBTable}.
	 *
	 * @see #getMappingBasePackages()
	 * @return All classes with {@link DynamoDBTable} annotation
	 * @throws ClassNotFoundException
	 *             if the class with {@link DynamoDBTable} annotation can't be
	 *             loaded
	 */
	protected Set<Class<?>> getInitialEntitySet() throws ClassNotFoundException {

		Set<Class<?>> initialEntitySet = new HashSet<>();

		String[] basePackages = getMappingBasePackages();

		for (String basePackage : basePackages) {
			LOGGER.trace("getInitialEntitySet. basePackage: {}", basePackage);

			if (StringUtils.hasText(basePackage)) {
				ClassPathScanningCandidateComponentProvider componentProvider = new ClassPathScanningCandidateComponentProvider(
						false);
				componentProvider.addIncludeFilter(new AnnotationTypeFilter(DynamoDBTable.class));

				for (BeanDefinition candidate : componentProvider.findCandidateComponents(basePackage)) {
					String candidateClass = candidate.getBeanClassName();
					if (candidateClass != null) {
						LOGGER.trace("getInitialEntitySet. candidate: {}", candidateClass);
						initialEntitySet.add(ClassUtils.forName(candidateClass,
								AbstractDynamoDBConfiguration.class.getClassLoader()));
					} else {
						LOGGER.warn("getInitialEntitySet. candidate: {} did not provide a class", candidate);
					}
				}
			}
		}

		return initialEntitySet;
	}

}