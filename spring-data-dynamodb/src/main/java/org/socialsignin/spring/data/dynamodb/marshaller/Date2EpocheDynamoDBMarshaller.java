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

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.Date;

public class Date2EpocheDynamoDBMarshaller extends DateDynamoDBMarshaller {

	private static final class EpcoheDateFormat extends DateFormat {
		private static final long serialVersionUID = 2969564523817434535L;

		@Override
		public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {
			long epoche = date.getTime();
			toAppendTo.append(epoche);
			return toAppendTo;
		}

		@Override
		public Date parse(String source, ParsePosition pos) {
			long epoche = Long.parseLong(source);
			pos.setIndex(source.length());
			return new Date(epoche);
		}

	};

	@Override
	public DateFormat getDateFormat() {
		return new EpcoheDateFormat();
	}

}
