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
package org.socialsignin.spring.data.dynamodb.marshaller;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMarshaller;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import org.springframework.util.StringUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

@SuppressWarnings("deprecation")
public abstract class DateDynamoDBMarshaller implements DynamoDBTypeConverter<String, Date>, DynamoDBMarshaller<Date> {

	public abstract DateFormat getDateFormat();

	@Override
	public String convert(Date object) {
		return marshall(object);
	}

	@Override
	public String marshall(Date getterReturnResult) {
		if (getterReturnResult == null) {
			return null;
		} else {
			return getDateFormat().format(getterReturnResult);
		}
	}

	@Override
	public Date unconvert(String object) {
		return unmarshall(Date.class, object);
	}

	@Override
	public Date unmarshall(Class<Date> clazz, String obj) {
		if (StringUtils.isEmpty(obj)) {
			return null;
		} else {
			try {
				return getDateFormat().parse(obj);
			} catch (ParseException e) {
				throw new RuntimeException(e);
			}
		}
	}

}
