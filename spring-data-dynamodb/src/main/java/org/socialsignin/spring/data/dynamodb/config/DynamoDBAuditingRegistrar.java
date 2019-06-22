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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socialsignin.spring.data.dynamodb.mapping.DynamoDBMappingContext;
import org.socialsignin.spring.data.dynamodb.mapping.event.AuditingEventListener;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.data.auditing.IsNewAwareAuditingHandler;
import org.springframework.data.auditing.config.AuditingBeanDefinitionRegistrarSupport;
import org.springframework.data.auditing.config.AuditingConfiguration;
import org.springframework.data.config.ParsingUtils;
import org.springframework.util.Assert;

import java.lang.annotation.Annotation;

import static org.socialsignin.spring.data.dynamodb.config.BeanNames.MAPPING_CONTEXT_BEAN_NAME;
import static org.springframework.beans.factory.config.BeanDefinition.ROLE_INFRASTRUCTURE;

/**
 * {@link org.springframework.context.annotation.ImportBeanDefinitionRegistrar}
 * to enable {@link EnableDynamoDBAuditing} annotation.
 *
 * @author Vito Limandibhrata
 */
class DynamoDBAuditingRegistrar extends AuditingBeanDefinitionRegistrarSupport {

	private static Logger LOGGER = LoggerFactory.getLogger(DynamoDBAuditingRegistrar.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.data.auditing.config.
	 * AuditingBeanDefinitionRegistrarSupport#getAnnotation()
	 */
	@Override
	protected Class<? extends Annotation> getAnnotation() {
		return EnableDynamoDBAuditing.class;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.data.auditing.config.
	 * AuditingBeanDefinitionRegistrarSupport#getAuditingHandlerBeanName()
	 */
	@Override
	protected String getAuditingHandlerBeanName() {
		return "dynamoDBAuditingHandler";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.data.auditing.config.
	 * AuditingBeanDefinitionRegistrarSupport#registerBeanDefinitions(org.
	 * springframework.core.type.AnnotationMetadata,
	 * org.springframework.beans.factory.support.BeanDefinitionRegistry)
	 */
	@Override
	public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry registry) {
		LOGGER.trace("registerBeanDefinitions");
		Assert.notNull(annotationMetadata, "AnnotationMetadata must not be null!");
		Assert.notNull(registry, "BeanDefinitionRegistry must not be null!");

		defaultDependenciesIfNecessary(registry, annotationMetadata);
		super.registerBeanDefinitions(annotationMetadata, registry);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.data.auditing.config.
	 * AuditingBeanDefinitionRegistrarSupport#getAuditHandlerBeanDefinitionBuilder(
	 * org.springframework.data.auditing.config.AuditingConfiguration)
	 */
	@Override
	protected BeanDefinitionBuilder getAuditHandlerBeanDefinitionBuilder(AuditingConfiguration configuration) {
		LOGGER.trace("getAuditHandlerBeanDefinitionBuilder");
		Assert.notNull(configuration, "AuditingConfiguration must not be null!");

		BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(IsNewAwareAuditingHandler.class);
		builder.addConstructorArgReference(MAPPING_CONTEXT_BEAN_NAME);
		return configureDefaultAuditHandlerAttributes(configuration, builder);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.data.auditing.config.
	 * AuditingBeanDefinitionRegistrarSupport#registerAuditListener(org.
	 * springframework.beans.factory.config.BeanDefinition,
	 * org.springframework.beans.factory.support.BeanDefinitionRegistry)
	 */
	@Override
	protected void registerAuditListenerBeanDefinition(BeanDefinition auditingHandlerDefinition,
			BeanDefinitionRegistry registry) {
		LOGGER.trace("registerAuditListenerBeanDefinition");
		Assert.notNull(auditingHandlerDefinition, "BeanDefinition must not be null!");
		Assert.notNull(registry, "BeanDefinitionRegistry must not be null!");

		BeanDefinitionBuilder listenerBeanDefinitionBuilder = BeanDefinitionBuilder
				.rootBeanDefinition(AuditingEventListener.class);
		listenerBeanDefinitionBuilder.addConstructorArgValue(
				ParsingUtils.getObjectFactoryBeanDefinition(getAuditingHandlerBeanName(), registry));

		registerInfrastructureBeanWithId(listenerBeanDefinitionBuilder.getBeanDefinition(),
				AuditingEventListener.class.getName(), registry);
	}

	/**
	 * Register default bean definitions for a {@link DynamoDBMappingContext} and an
	 * {@link org.springframework.data.support.IsNewStrategyFactory} in case we
	 * don't find beans with the assumed names in the registry.
	 *
	 * @param registry
	 *            the {@link BeanDefinitionRegistry} to use to register the
	 *            components into.
	 * @param source
	 *            the source which the registered components shall be registered
	 *            with
	 */
	private void defaultDependenciesIfNecessary(BeanDefinitionRegistry registry, Object source) {
		LOGGER.trace("defaultDependenciesIfNecessary. source:{}", source);
		LOGGER.trace("is registry.containsBeanDefinition {} {}", MAPPING_CONTEXT_BEAN_NAME,
				registry.containsBeanDefinition(MAPPING_CONTEXT_BEAN_NAME));
		if (!registry.containsBeanDefinition(MAPPING_CONTEXT_BEAN_NAME)) {

			RootBeanDefinition definition = new RootBeanDefinition(DynamoDBMappingContext.class);
			definition.setRole(ROLE_INFRASTRUCTURE);
			definition.setSource(source);

			registry.registerBeanDefinition(MAPPING_CONTEXT_BEAN_NAME, definition);
		}
	}
}