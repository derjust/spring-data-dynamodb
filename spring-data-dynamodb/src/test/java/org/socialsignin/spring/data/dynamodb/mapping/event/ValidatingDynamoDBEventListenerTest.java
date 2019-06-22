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
package org.socialsignin.spring.data.dynamodb.mapping.event;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.socialsignin.spring.data.dynamodb.domain.sample.User;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ValidatingDynamoDBEventListenerTest {
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	private final User sampleEntity = new User();
	@Mock
	private Validator validator;
	private ValidatingDynamoDBEventListener underTest;

	@Before
	public void setUp() {
		underTest = new ValidatingDynamoDBEventListener(validator);
	}

	@Test
	public void testWrongConstructor() {
		expectedException.expectMessage("validator must not be null!");
		expectedException.expect(IllegalArgumentException.class);

		new ValidatingDynamoDBEventListener(null);
	}

	@Test
	public void testEmptyResult() {

		underTest.onBeforeSave(sampleEntity);

		assertTrue(true);
	}

	@Test
	public void testValidationException() {
		expectedException.expect(ConstraintViolationException.class);
		expectedException.expectMessage(
				allOf(containsString("Test Validation Exception 1"), containsString("Test Validation Exception 2")));

		Set<ConstraintViolation<User>> validationResult = new HashSet<>();

		@SuppressWarnings("unchecked")
		ConstraintViolation<User> vc1 = mock(ConstraintViolation.class);
		when(vc1.toString()).thenReturn("Test Validation Exception 1");
		validationResult.add(vc1);

		@SuppressWarnings("unchecked")
		ConstraintViolation<User> vc2 = mock(ConstraintViolation.class);
		when(vc2.toString()).thenReturn("Test Validation Exception 2");
		validationResult.add(vc2);
		when(validator.validate(sampleEntity)).thenReturn(validationResult);

		underTest.onBeforeSave(sampleEntity);
	}
}
