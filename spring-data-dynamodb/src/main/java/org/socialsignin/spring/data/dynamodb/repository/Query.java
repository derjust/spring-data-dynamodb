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
package org.socialsignin.spring.data.dynamodb.repository;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.socialsignin.spring.data.dynamodb.repository.QueryConstants.QUERY_LIMIT_UNLIMITED;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface Query {

	/**
	 * A string that identifies the attributes you want. To retrieve a single
	 * attribute, specify its name. For multiple attributes, the names must be
	 * comma-separated.
	 *
	 * @see <a href=
	 *      "https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Expressions.ProjectionExpressions.html">Projection
	 *      Expressions</a>
	 */
	String fields() default "";

	/**
	 * An integer to limit the number of elements returned.
	 *
	 * @see <a href=
	 *      "https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Expressions.ProjectionExpressions.html">Projection
	 *      Expressions</a>
	 */
	int limit() default QUERY_LIMIT_UNLIMITED;
}
