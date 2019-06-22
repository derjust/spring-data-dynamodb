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

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMarshaller;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

/**
 * @author Michael Lavelle
 * @author Sebastian Just
 * @deprecated According to
 *             {@code com.amazonaws.services.dynamodbv2.datamodeling.marshallers.CustomMarshaller.marshall(Object)}
 *             at some point {@link DynamoDBMarshaller} might be cached -
 *             whereas {@link DateFormat} is not thread-safe. <br>
 *             Use
 *             {@link org.socialsignin.spring.data.dynamodb.marshaller.DateDynamoDBMarshaller}
 *             instead.
 * @see org.socialsignin.spring.data.dynamodb.marshaller.DateDynamoDBMarshaller
 */
@Deprecated
public class AbstractDynamoDBDateMarshaller implements DynamoDBMarshaller<Date> {

	private DateFormat dateFormat;

	public AbstractDynamoDBDateMarshaller(DateFormat dateFormat) {
		this.dateFormat = dateFormat;
	}

	@Override
	public String marshall(Date getterReturnResult) {
		if (getterReturnResult == null) {
			return null;
		} else {
			return dateFormat.format(getterReturnResult);
		}
	}

	@Override
	public Date unmarshall(Class<Date> clazz, String obj) throws IllegalArgumentException {
		if (obj == null) {
			return null;
		} else {
			try {
				return dateFormat.parse(obj);
			} catch (ParseException e) {
				throw new IllegalArgumentException("Could not unmarshall '" + obj + "' via " + dateFormat, e);
			}
		}
	}

}
