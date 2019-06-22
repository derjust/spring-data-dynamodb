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

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * {@link Page} implementation that uses only the methods from the
 * {@link Iterable} interface thus the lazy list from the AWS SDK used as result
 * set can be properly used
 *
 * @param <T>
 *            The type of the list's elements
 */
public class UnpagedPageImpl<T> implements Page<T> {

	private final List<T> content;
	private final Pageable pageable;
	private final long total;

	public UnpagedPageImpl(@NonNull List<T> content, long total) {

		Assert.notNull(content, "content must not be null!");

		this.pageable = Pageable.unpaged();
		this.content = content;
		this.total = total;
	}

	@Override
	public int getNumber() {
		return 0;
	}

	@Override
	public int getSize() {
		return getNumberOfElements();
	}

	@Override
	public int getNumberOfElements() {
		if (total > Integer.MAX_VALUE) {
			return Integer.MAX_VALUE;
		} else {
			return (int) total;
		}
	}

	@Override
	public Sort getSort() {
		return pageable.getSort();
	}

	@Override
	public boolean isFirst() {
		return true;
	}

	@Override
	public boolean isLast() {
		return true;
	}

	@Override
	public boolean hasNext() {
		return false;
	}

	@Override
	public boolean hasPrevious() {
		return false;
	}

	@Override
	@Nullable
	public Pageable nextPageable() {
		return null;
	}

	@Override
	@Nullable
	public Pageable previousPageable() {
		return null;
	}

	@Override
	public Iterator<T> iterator() {
		return this.content.iterator();
	}

	@Override
	public int getTotalPages() {
		return 1;
	}

	@Override
	public long getTotalElements() {
		return this.total;
	}

	@Override
	public <U> UnpagedPageImpl<U> map(Function<? super T, ? extends U> converter) {
		List<U> convertedContent = this.content.stream().map(converter).collect(Collectors.toList());

		return new UnpagedPageImpl<>(convertedContent, this.total);
	}

	@Override
	public List<T> getContent() {
		return content;
	}

	@Override
	public boolean hasContent() {
		return total > 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		String contentType = "UNKNOWN";

		if (this.total > 0) {
			contentType = iterator().getClass().getName();
		}

		return String.format("Page %s of %d containing %s instances", getNumber() + 1, getTotalPages(), contentType);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(/* @Nullable */ Object obj) {

		if (this == obj) {
			return true;
		}

		if (!(obj instanceof UnpagedPageImpl<?>)) {
			return false;
		}

		UnpagedPageImpl<?> that = (UnpagedPageImpl<?>) obj;

		return this.total == that.total && this.pageable.equals(that.pageable) && this.content.equals(that.content);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {

		int result = 17;

		result += 31 * (int) (total ^ total >>> 32);
		result += 31 * pageable.hashCode();
		result += 31 * content.hashCode();

		return result;
	}
}
