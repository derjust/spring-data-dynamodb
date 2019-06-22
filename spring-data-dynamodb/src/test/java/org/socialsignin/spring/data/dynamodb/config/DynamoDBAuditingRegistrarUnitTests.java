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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.type.AnnotationMetadata;

/**
 * Unit tests for {@link DynamoDBAuditingRegistrar}.
 *
 * @author Vito Limandibhrata
 */
@RunWith(MockitoJUnitRunner.class)
public class DynamoDBAuditingRegistrarUnitTests {

	DynamoDBAuditingRegistrar registrar = new DynamoDBAuditingRegistrar();

	@Mock
	AnnotationMetadata metadata;
	@Mock
	BeanDefinitionRegistry registry;

	@Test(expected = IllegalArgumentException.class)
	public void rejectsNullAnnotationMetadata() {
		registrar.registerBeanDefinitions(null, registry);
	}

	@Test(expected = IllegalArgumentException.class)
	public void rejectsNullBeanDefinitionRegistry() {
		registrar.registerBeanDefinitions(metadata, null);
	}
}
