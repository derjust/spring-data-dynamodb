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

/*
 * Copyright 2014 by the original author(s).
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

import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedScanList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.core.GenericTypeResolver;

import java.util.List;
import java.util.function.Consumer;

/**
 * Base class to implement domain class specific {@link ApplicationListener}s.
 *
 * @author Michael Lavelle
 * @author Sebastian Just
 */
public abstract class AbstractDynamoDBEventListener<E> implements ApplicationListener<DynamoDBMappingEvent<?>> {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractDynamoDBEventListener.class);
	private final Class<?> domainClass;

	/**
	 * Creates a new {@link AbstractDynamoDBEventListener}.
	 */
	public AbstractDynamoDBEventListener() {
		Class<?> typeArgument = GenericTypeResolver.resolveTypeArgument(this.getClass(),
				AbstractDynamoDBEventListener.class);
		this.domainClass = typeArgument == null ? Object.class : typeArgument;
	}

	protected Class<?> getDomainClass() {
		return this.domainClass;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.context.ApplicationListener#onApplicationEvent(org
	 * .springframework.context.ApplicationEvent)
	 */
	@Override
	public void onApplicationEvent(DynamoDBMappingEvent<?> event) {

		@SuppressWarnings("unchecked")
		E source = (E) event.getSource();

		// source can not be null as java.util.EventObject can not be constructed with
		// null
		assert source != null;

		if (event instanceof AfterScanEvent) {

			publishEachElement((PaginatedScanList<?>) source, this::onAfterScan);
			return;
		} else if (event instanceof AfterQueryEvent) {

			publishEachElement((PaginatedQueryList<?>) source, this::onAfterQuery);
			return;
		}
		// Check for matching domain type and invoke callbacks
		else if (domainClass.isAssignableFrom(source.getClass())) {
			if (event instanceof BeforeSaveEvent) {
				onBeforeSave(source);
				return;
			} else if (event instanceof AfterSaveEvent) {
				onAfterSave(source);
				return;
			} else if (event instanceof BeforeDeleteEvent) {
				onBeforeDelete(source);
				return;
			} else if (event instanceof AfterDeleteEvent) {
				onAfterDelete(source);
				return;
			} else if (event instanceof AfterLoadEvent) {
				onAfterLoad(source);
				return;
			}
		}
		// we should never end up here
		assert false;
	}

	@SuppressWarnings("unchecked")
	private void publishEachElement(List<?> list, Consumer<E> publishMethod) {
		list.stream().filter(o -> domainClass.isAssignableFrom(o.getClass())).map(o -> (E) o).forEach(publishMethod);
	}

	public void onBeforeSave(E source) {
		LOG.debug("onBeforeSave({}, {})", source);
	}

	public void onAfterSave(E source) {
		LOG.debug("onAfterSave({}, {})", source);
	}

	public void onAfterLoad(E source) {
		LOG.debug("onAfterLoad({})", source);
	}

	public void onAfterDelete(E source) {
		LOG.debug("onAfterDelete({})", source);
	}

	public void onBeforeDelete(E source) {
		LOG.debug("onBeforeDelete({})", source);
	}

	public void onAfterScan(E source) {
		LOG.debug("onAfterScan({})", source);
	}

	public void onAfterQuery(E source) {
		LOG.debug("onAfterQuery({})", source);
	}

}
