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
package org.socialsignin.spring.data.dynamodb.mapping;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@SuppressWarnings("deprecation")
public class DefaultDynamoDBDateMarshallerTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private DefaultDynamoDBDateMarshaller underTest = new DefaultDynamoDBDateMarshaller();

	@Test
	public void testMarshall() {
		String actual = underTest.marshall(new Date(0));

		assertEquals("1970-01-01T00:00:00.000Z", actual);
	}

	@Test
	public void testMarshallNull() {
		String actual = underTest.marshall(null);

		assertNull(actual);
	}

	@Test
	public void testUnmarshall() {
		Date actual = underTest.unmarshall(Date.class, "1970-01-01T00:00:00.000Z");

		assertEquals(0L, actual.getTime());
	}

	@Test
	public void testUnmarshallNull() {
		Date actual = underTest.unmarshall(Date.class, null);

		assertNull(actual);
	}

	@Test
	public void testUnmarshallGarbage() {
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("Could not unmarshall 'garbage' via yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

		underTest.unmarshall(Date.class, "garbage");
	}

}
