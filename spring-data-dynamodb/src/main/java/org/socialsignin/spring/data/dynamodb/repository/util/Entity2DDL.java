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
package org.socialsignin.spring.data.dynamodb.repository.util;

/**
 * Configuration key is {@code spring.data.dynamodb.entity2ddl.auto} Inspired by
 * Hibernate's hbm2ddl
 * 
 * @see <a href=
 *      "https://docs.jboss.org/hibernate/orm/5.2/userguide/html_single/Hibernate_User_Guide.html#configurations-hbmddl">Hibernate
 *      User Guide</a>
 */
public enum Entity2DDL {
	/** No action will be performed. */
	NONE("none"),

	/** Database creation will be generated on ApplicationContext startup. */
	CREATE_ONLY("create-only"),

	/** Database dropping will be generated on ApplicationContext shutdown. */
	DROP("drop"),

	/**
	 * Database dropping will be generated followed by database creation on
	 * ApplicationContext startup.
	 */
	CREATE("create"),

	/**
	 * Drop the schema and recreate it on ApplicationContext startup. Additionally,
	 * drop the schema on ApplicationContext shutdown.
	 */
	CREATE_DROP("create-drop"),

	/** Validate the database schema */
	VALIDATE("validate");

	private final String configurationValue;

	Entity2DDL(String configurationValue) {
		this.configurationValue = configurationValue;
	}

	public String getConfigurationValue() {
		return this.configurationValue;
	}

	/**
	 * Use this in place of valueOf.
	 *
	 * @param value
	 *            real value
	 * @return Entity2DDL corresponding to the value
	 *
	 * @throws IllegalArgumentException
	 *             If the specified value does not map to one of the known values in
	 *             this enum.
	 */
	public static Entity2DDL fromValue(String value) {
		for (Entity2DDL resolvedConfig : Entity2DDL.values()) {
			if (resolvedConfig.configurationValue.equals(value)) {
				return resolvedConfig;
			}
		}
		throw new IllegalArgumentException(value + " is not a valid configuration value!");
	}
}