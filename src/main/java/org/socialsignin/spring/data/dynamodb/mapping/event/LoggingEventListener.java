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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * {@link org.springframework.context.ApplicationListener} for DynamoDB mapping events logging the events.
 *
 * @author Michael Lavelle
 */
public class LoggingEventListener extends AbstractDynamoDBEventListener<Object> {

	private static final Logger LOGGER = LoggerFactory.getLogger(LoggingEventListener.class);


	/*
	 * (non-Javadoc)
	 * @see org.socialsignin.spring.data.dynamodb.mapping.event.AbstractDynamoDBEventListener#onBeforeSave(java.lang.Object)
	 */
	@Override
	public void onBeforeSave(Object source) {
		LOGGER.trace("onBeforeSave: {}", source);
	}

	/*
	 * (non-Javadoc)
	 * @see org.socialsignin.spring.data.dynamodb.mapping.event.AbstractDynamoDBEventListener#onAfterSave(java.lang.Object,)
	 */
	@Override
	public void onAfterSave(Object source) {
		LOGGER.trace("onAfterSave: {}", source);
	}

	/*
	 * (non-Javadoc)
	 * @see org.socialsignin.spring.data.dynamodb.mapping.event.AbstractDynamoDBEventListener#onAfterDelete(java.lang.Object,)
	 */
	@Override
	public void onAfterDelete(Object source) {
		LOGGER.trace("onAfterDelete: {}", source);
	}

	/*
	 * (non-Javadoc)
	 * @see org.socialsignin.spring.data.dynamodb.mapping.event.AbstractDynamoDBEventListener#onBeforeDelete(java.lang.Object)
	 */
	@Override
	public void onBeforeDelete(Object source) {
		LOGGER.trace("onBeforeDelete: {}", source);
	}

	/*
	 * (non-Javadoc)
	 * @see org.socialsignin.spring.data.dynamodb.mapping.event.AbstractDynamoDBEventListener#onAfterLoad(java.lang.Object)
	 */
	@Override
	public void onAfterLoad(Object source) {
		LOGGER.trace("onAfterLoad: {}", source);
	}

	/*
	 * (non-Javadoc)
	 * @see org.socialsignin.spring.data.dynamodb.mapping.event.AbstractDynamoDBEventListener#onAfterScan(java.lang.Object)
	 */
	@Override
	public void onAfterScan(Object source) {
		LOGGER.trace("onAfterScan: {}", source);
	}

	/*
	 * (non-Javadoc)
	 * @see org.socialsignin.spring.data.dynamodb.mapping.event.AbstractDynamoDBEventListener#onAfterQuery(java.lang.Object)
	 */
	@Override
	public void onAfterQuery(Object source) {
		LOGGER.trace("onAfterQuery: {}", source);
	}

}
