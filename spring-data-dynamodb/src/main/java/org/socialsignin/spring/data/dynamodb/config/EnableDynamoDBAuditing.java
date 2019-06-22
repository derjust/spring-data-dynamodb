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
package org.socialsignin.spring.data.dynamodb.config;

import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to enable auditing in DynamoDB via annotation configuration.
 *
 * @author Vito Limandibhrata
 */
@Inherited
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(DynamoDBAuditingRegistrar.class)
public @interface EnableDynamoDBAuditing {

	/**
	 * @return Configures the {@link org.springframework.data.domain.AuditorAware}
	 *         bean to be used to lookup the current principal.
	 */
	String auditorAwareRef() default "";

	/**
	 * @return Configures whether the creation and modification dates are set.
	 *         Defaults to {@literal true}.
	 */
	boolean setDates() default true;

	/**
	 * @return Configures whether the entity shall be marked as modified on
	 *         creation. Defaults to {@literal true}.
	 */
	boolean modifyOnCreate() default true;

	/**
	 * @return Configures a
	 *         {@link org.springframework.data.auditing.DateTimeProvider} bean name
	 *         that allows customizing the {@link org.joda.time.DateTime} to be used
	 *         for setting creation and modification dates.
	 */
	String dateTimeProviderRef() default "";
}