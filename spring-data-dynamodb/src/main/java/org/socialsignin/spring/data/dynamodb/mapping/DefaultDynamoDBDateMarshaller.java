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

import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * @author Michael Lavelle
 * @author Sebastian Just
 * @deprecated Consider using
 *             {@link org.socialsignin.spring.data.dynamodb.marshaller.Date2IsoDynamoDBMarshaller}
 */
@Deprecated
public class DefaultDynamoDBDateMarshaller extends AbstractDynamoDBDateMarshaller {

	private static final class UTCSimpleDateFormat extends SimpleDateFormat {
		private static final long serialVersionUID = 1L;
		private UTCSimpleDateFormat() {
			super("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			setTimeZone(TimeZone.getTimeZone("UTC"));
		}

		@Override
		public String toString() {
			return toPattern();
		}
	}

	public DefaultDynamoDBDateMarshaller() {
		super(new UTCSimpleDateFormat());
	}
}
