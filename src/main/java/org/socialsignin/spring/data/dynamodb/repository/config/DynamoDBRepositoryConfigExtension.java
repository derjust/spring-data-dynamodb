/**
 * Copyright © 2018 spring-data-dynamodb (https://github.com/derjust/spring-data-dynamodb)
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
import org.socialsignin.spring.data.dynamodb.core.DynamoDBTemplate;
import org.socialsignin.spring.data.dynamodb.mapping.DynamoDBMappingContext;
import org.socialsignin.spring.data.dynamodb.repository.support.DynamoDBRepositoryFactoryBean;
import org.socialsignin.spring.data.dynamodb.repository.util.DynamoDBMappingContextProcessor;
import org.socialsignin.spring.data.dynamodb.repository.util.Entity2DynamoDBTableSynchronizer;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.data.config.ParsingUtils;
import org.springframework.data.repository.config.AnnotationRepositoryConfigurationSource;
import org.springframework.data.repository.config.RepositoryConfigurationExtensionSupport;
import org.springframework.data.repository.config.RepositoryConfigurationSource;
import org.springframework.data.repository.config.XmlRepositoryConfigurationSource;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Michael Lavelle
 * @author Sebastian Just
 */
public class DynamoDBRepositoryConfigExtension extends RepositoryConfigurationExtensionSupport {

	private static final Logger LOGGER = LoggerFactory.getLogger(DynamoDBRepositoryConfigExtension.class);

	private static final String DEFAULT_AMAZON_DYNAMO_DB_BEAN_NAME = "amazonDynamoDB";

	private static final String DYNAMO_DB_MAPPER_CONFIG_REF = "dynamodb-mapper-config-ref";

	private static final String DYNAMO_DB_OPERATIONS_REF = "dynamodb-operations-ref";

	private static final String AMAZON_DYNAMODB_REF = "amazon-dynamodb-ref";

	private static final String MAPPING_CONTEXT_REF = "mapping-context-ref";

	private BeanDefinitionRegistry registry;
	private String defaultDynamoDBMappingContext;

	@Override
	public String getRepositoryFactoryBeanClassName() {
		return DynamoDBRepositoryFactoryBean.class.getName();
	}

	@Override
	public void postProcess(BeanDefinitionBuilder builder, AnnotationRepositoryConfigurationSource config) {
		AnnotationAttributes attributes = config.getAttributes();

		String repositoryBeanName = config.generateBeanName(builder.getBeanDefinition());

		postProcess(builder, repositoryBeanName, attributes.getString("amazonDynamoDBRef"),
				attributes.getString("dynamoDBMapperConfigRef"), attributes.getString("dynamoDBOperationsRef"),
				attributes.getString("mappingContextRef"));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.data.repository.config.
	 * RepositoryConfigurationExtensionSupport
	 * #postProcess(org.springframework.beans
	 * .factory.support.BeanDefinitionBuilder, org.springframework.data.repository
	 * .config.XmlRepositoryConfigurationSource)
	 */
	@Override
	public void postProcess(BeanDefinitionBuilder builder, XmlRepositoryConfigurationSource config) {
		Element element = config.getElement();

		ParsingUtils.setPropertyReference(builder, element, AMAZON_DYNAMODB_REF, "amazonDynamoDB");
		ParsingUtils.setPropertyReference(builder, element, DYNAMO_DB_MAPPER_CONFIG_REF, "dynamoDBMapperConfig");
		ParsingUtils.setPropertyReference(builder, element, DYNAMO_DB_OPERATIONS_REF, "dynamoDBOperations");

		String dynamoDBMappingContextRef = element.getAttribute(MAPPING_CONTEXT_REF);

		if (!StringUtils.hasText(dynamoDBMappingContextRef)) {
			// Register DynamoDBMappingContext only once if necessary
			if (defaultDynamoDBMappingContext == null) {
				defaultDynamoDBMappingContext = registerDynamoDBMappingContext(registry);
			}
			dynamoDBMappingContextRef = defaultDynamoDBMappingContext;
		}
		registerAndSetPostProcessingBeans(builder, registry, dynamoDBMappingContextRef);
	}

	private Map<String, String> dynamoDBTemplateCache = new HashMap<>();
	private void postProcess(BeanDefinitionBuilder builder, String repositoryName, String amazonDynamoDBRef,
			String dynamoDBMapperConfigRef, String dynamoDBOperationsRef, String dynamoDBMappingContextRef) {

		if (StringUtils.hasText(dynamoDBOperationsRef)) {
			builder.addPropertyReference("dynamoDBOperations", dynamoDBOperationsRef);
			Assert.isTrue(!StringUtils.hasText(amazonDynamoDBRef),
					"Cannot specify both amazonDynamoDB bean and dynamoDBOperationsBean in repository configuration");
			Assert.isTrue(!StringUtils.hasText(dynamoDBMapperConfigRef),
					"Cannot specify both dynamoDBMapperConfigBean bean and dynamoDBOperationsBean in repository configuration");
		} else {

			if (StringUtils.isEmpty(dynamoDBOperationsRef)) {

				String dynamoDBRef;
				if (StringUtils.hasText(amazonDynamoDBRef)) {
					dynamoDBRef = amazonDynamoDBRef;
				} else {
					dynamoDBRef = DEFAULT_AMAZON_DYNAMO_DB_BEAN_NAME;
				}

				dynamoDBOperationsRef = dynamoDBTemplateCache
						.computeIfAbsent(getBeanNameWithModulePrefix("DynamoDBTemplate-" + dynamoDBRef), ref -> {
							BeanDefinitionBuilder dynamoDBTemplateBuilder = BeanDefinitionBuilder
									.genericBeanDefinition(DynamoDBTemplate.class);
							dynamoDBTemplateBuilder.addConstructorArgReference(dynamoDBRef);

							registry.registerBeanDefinition(ref, dynamoDBTemplateBuilder.getBeanDefinition());
							return ref;
						});
			}

			builder.addPropertyReference("dynamoDBOperations", dynamoDBOperationsRef);

			/*
			 * Fix issue #233 if (StringUtils.hasText(dynamoDBMapperConfigRef)) {
			 * builder.addPropertyReference("dynamoDBMapperConfig",
			 * dynamoDBMapperConfigRef); }
			 */
		}

		if (!StringUtils.hasText(dynamoDBMappingContextRef)) {
			// Register DynamoDBMappingContext only once if necessary
			if (defaultDynamoDBMappingContext == null) {
				defaultDynamoDBMappingContext = registerDynamoDBMappingContext(registry);
			}
			dynamoDBMappingContextRef = defaultDynamoDBMappingContext;
		}

		builder.addPropertyReference("dynamoDBMappingContext", dynamoDBMappingContextRef);
		registerAndSetPostProcessingBeans(builder, registry, dynamoDBMappingContextRef);
	}

	protected void registerAndSetPostProcessingBeans(BeanDefinitionBuilder builder, BeanDefinitionRegistry registry,
			String dynamoDBMappingContextRef) {
		String tableSynchronizerName = registerEntity2DynamoDBTableSynchronizer(registry, dynamoDBMappingContextRef);
		builder.addPropertyReference("entity2DynamoDBTableSynchronizer", tableSynchronizerName);

		String dynamoDBMappingContextProcessorName = registerDynamoDBMappingContextProcessor(registry,
				dynamoDBMappingContextRef);
		builder.addPropertyReference("dynamoDBMappingContextProcessor", dynamoDBMappingContextProcessorName);
	}

	private final Map<String, String> entity2DynamoDBTableSynchronizerCache = new ConcurrentHashMap<>();
	private String registerEntity2DynamoDBTableSynchronizer(BeanDefinitionRegistry registry,
			String dynamoDBMappingContextRef) {
		assert registry != null;

		return entity2DynamoDBTableSynchronizerCache.computeIfAbsent(dynamoDBMappingContextRef, ref -> {
			BeanDefinitionBuilder entity2DynamoDBTableSynchronizerBuilder = BeanDefinitionBuilder
					.genericBeanDefinition(Entity2DynamoDBTableSynchronizer.class);
			String tableSynchronizerName = getBeanNameWithModulePrefix(
					"Entity2DynamoDBTableSynchronizer-" + dynamoDBMappingContextRef);
			registry.registerBeanDefinition(tableSynchronizerName,
					entity2DynamoDBTableSynchronizerBuilder.getBeanDefinition());

			return tableSynchronizerName;
		});
	}

	private final Map<String, String> dynamoDBMappingContextProcessorCache = new ConcurrentHashMap<>();
	private String registerDynamoDBMappingContextProcessor(BeanDefinitionRegistry registry,
			String dynamoDBMappingContextRef) {
		assert registry != null;

		return dynamoDBMappingContextProcessorCache.computeIfAbsent(dynamoDBMappingContextRef, ref -> {
			BeanDefinitionBuilder dynamoDBMappingContextProcessorBuilder = BeanDefinitionBuilder
					.genericBeanDefinition(DynamoDBMappingContextProcessor.class);
			dynamoDBMappingContextProcessorBuilder.addConstructorArgReference(dynamoDBMappingContextRef);

			String dynamoDBMappingContextProcessorRef = getBeanNameWithModulePrefix(
					"DynamoDBMappingContextProcessor-" + dynamoDBMappingContextRef);
			registry.registerBeanDefinition(dynamoDBMappingContextProcessorRef,
					dynamoDBMappingContextProcessorBuilder.getBeanDefinition());

			return dynamoDBMappingContextProcessorRef;

		});
	}

	private String registerDynamoDBMappingContext(BeanDefinitionRegistry registry) {
		assert registry != null;

		BeanDefinitionBuilder dynamoDBMappingContextBuilder = BeanDefinitionBuilder
				.genericBeanDefinition(DynamoDBMappingContext.class);
		String dynamoDBMappingContextRef = getBeanNameWithModulePrefix("DynamoDBMappingContext");

		LOGGER.debug("Adding bean <{}> of type <{}>", dynamoDBMappingContextRef,
				dynamoDBMappingContextBuilder.getBeanDefinition());

		registry.registerBeanDefinition(dynamoDBMappingContextRef, dynamoDBMappingContextBuilder.getBeanDefinition());

		return dynamoDBMappingContextRef;
	}

	@Override
	public void registerBeansForRoot(BeanDefinitionRegistry registry,
			RepositoryConfigurationSource configurationSource) {
		super.registerBeansForRoot(registry, configurationSource);

		// Store for later to be used by #postProcess, too
		this.registry = registry;

		// Fix issue #233
		Optional dynamoDBMapperConfigRef = configurationSource.getAttribute("dynamoDBMapperConfigRef");
		if (!dynamoDBMapperConfigRef.isPresent()) {
			BeanDefinitionBuilder dynamoDBMapperConfigBuiilder = BeanDefinitionBuilder
					.genericBeanDefinition(DynamoDBMapperConfigFactory.class);
			registry.registerBeanDefinition(getBeanNameWithModulePrefix("DynamoDBMapperConfig"),
					dynamoDBMapperConfigBuiilder.getBeanDefinition());
		}

		BeanDefinitionBuilder dynamoDBMapperBuilder = BeanDefinitionBuilder
				.genericBeanDefinition(DynamoDBMapperFactory.class);
		registry.registerBeanDefinition(getBeanNameWithModulePrefix("DynamoDBMapper"),
				dynamoDBMapperBuilder.getBeanDefinition());
	}

	protected String getBeanNameWithModulePrefix(String baseBeanName) {
		return String.format("%s-%s", getModulePrefix(), baseBeanName);
	}

	@Override
	protected String getModulePrefix() {
		return "dynamoDB";
	}
}
