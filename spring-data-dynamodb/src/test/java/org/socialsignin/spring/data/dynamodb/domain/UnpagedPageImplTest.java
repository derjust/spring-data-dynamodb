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
package org.socialsignin.spring.data.dynamodb.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Pageable;

@RunWith(MockitoJUnitRunner.class)
public class UnpagedPageImplTest {

	@Mock
	private List<Object> content;
	@Mock
	private Iterator<Object> iterator;
	private long total = new Random().nextInt(Integer.MAX_VALUE - 1) + 1; // Ensure it's never null

	private UnpagedPageImpl<Object> underTest;

	@Before
	public void setUp() {
		when(content.iterator()).thenReturn(iterator);
		underTest = new UnpagedPageImpl<Object>(content, total);
	}

	@Test
	public void testStaticValues() {
		assertSame(iterator, underTest.iterator());
		assertSame(Pageable.unpaged().getSort(), underTest.getSort());

		assertEquals(0, underTest.getNumber());
		assertEquals(1, underTest.getTotalPages());
		assertEquals(total, underTest.getNumberOfElements());
		assertEquals(total, underTest.getTotalElements());
		assertEquals(total, underTest.getSize());

		assertSame(content, underTest.getContent());
		assertTrue(underTest.hasContent());

		assertTrue(underTest.isFirst());
		assertFalse(underTest.hasPrevious());
		assertTrue(underTest.isLast());
		assertFalse(underTest.hasNext());
		assertNull(underTest.nextPageable());
		assertNull(underTest.previousPageable());
	}

	@Test
	public void testEquals() {
		assertFalse(underTest.equals(null));
		assertFalse(underTest.equals(new Object()));

		assertTrue(underTest.equals(underTest));

		assertTrue(underTest.equals(new UnpagedPageImpl<Object>(content, total)));
		assertFalse(underTest.equals(new UnpagedPageImpl<Object>(content, total - 1)));
		assertFalse(underTest.equals(new UnpagedPageImpl<Object>(Collections.emptyList(), 0)));
	}

	@Test
	public void testHashCode() {
		assertEquals(underTest.hashCode(), underTest.hashCode());
	}

	@Test
	public void testToString() {
		String actual = underTest.toString();

		assertTrue(actual, actual.startsWith("Page 1 of 1 containing org.mockito.codegen.Iterator$MockitoMock"));
	}

	@Test
	public void testLongContent() {
		underTest = new UnpagedPageImpl<>(content, Long.MAX_VALUE);

		assertEquals(Integer.MAX_VALUE, underTest.getNumberOfElements());
	}

	@Test
	public void testEmptyContent() {
		underTest = new UnpagedPageImpl<>(content, 0);

		assertFalse(underTest.hasContent());

		String actual = underTest.toString();
		assertEquals("Page 1 of 1 containing UNKNOWN instances", actual);
	}
}
