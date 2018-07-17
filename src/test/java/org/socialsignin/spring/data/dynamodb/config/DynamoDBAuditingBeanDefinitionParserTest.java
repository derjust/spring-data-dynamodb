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
package org.socialsignin.spring.data.dynamodb.config;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.socialsignin.spring.data.dynamodb.mapping.DynamoDBMappingContext;
import org.socialsignin.spring.data.dynamodb.mapping.event.AuditingEventListener;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.beans.factory.xml.XmlReaderContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.socialsignin.spring.data.dynamodb.config.BeanNames.MAPPING_CONTEXT_BEAN_NAME;

@RunWith(MockitoJUnitRunner.class)
public class DynamoDBAuditingBeanDefinitionParserTest {

	private DynamoDBAuditingBeanDefinitionParser underTest;
	private Document configXmlDoc;

	@Mock
	private ParserContext parserContext;
	@Mock
	private BeanDefinitionRegistry registry;
	@Mock
	private BeanDefinitionBuilder builder;
	@Mock
	private XmlReaderContext readerContext;

	@Before
	public void setUp() throws Exception {
		underTest = new DynamoDBAuditingBeanDefinitionParser();

		DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		configXmlDoc = documentBuilder.newDocument();

		when(parserContext.getRegistry()).thenReturn(registry);
		when(parserContext.getReaderContext()).thenReturn(readerContext);
		when(readerContext.generateBeanName(any())).thenReturn("dynamoDBMappingContext");
	}

	@Test
	public void testGetBeanClass() {
		Element element = null;
		Class<?> actual = underTest.getBeanClass(element);

		assertEquals(AuditingEventListener.class, actual);
	}

	@Test
	public void testShouldGenerateId() {

		assertTrue(underTest.shouldGenerateId());
	}

	@Test
	public void testDoParseWithMappingContext() {

		String mappingContextRef = MAPPING_CONTEXT_BEAN_NAME;
		Element configElement = configXmlDoc.createElement("config");
		configElement.setAttribute("mapping-context-ref", mappingContextRef);

		underTest.doParse(configElement, parserContext, builder);

		verify(builder).addConstructorArgValue(any());
	}

	@Test
	public void testDoParseWithoutMappingContextAutocreatingBean() {
		// mappingContextRef not set
		String mappingContextRef = null;
		Element configElement = configXmlDoc.createElement("config");
		configElement.setAttribute("mapping-context-ref", mappingContextRef);

		// Default bean not yet registered
		when(registry.containsBeanDefinition(MAPPING_CONTEXT_BEAN_NAME)).thenReturn(false);

		underTest.doParse(configElement, parserContext, builder);

		verify(registry).registerBeanDefinition(MAPPING_CONTEXT_BEAN_NAME,
				new RootBeanDefinition(DynamoDBMappingContext.class));

		verify(builder).addConstructorArgValue(any());
	}

	@Test
	public void testDoParseWithoutMappingContextReusingBean() {
		// mappingContextRef not set
		String mappingContextRef = null;
		Element configElement = configXmlDoc.createElement("config");
		configElement.setAttribute("mapping-context-ref", mappingContextRef);

		// Default bean not yet registered
		when(registry.containsBeanDefinition(MAPPING_CONTEXT_BEAN_NAME)).thenReturn(true);

		underTest.doParse(configElement, parserContext, builder);

		verify(registry).registerBeanDefinition(eq(MAPPING_CONTEXT_BEAN_NAME), isA(GenericBeanDefinition.class));
		verify(builder).addConstructorArgValue(any());
	}
}
