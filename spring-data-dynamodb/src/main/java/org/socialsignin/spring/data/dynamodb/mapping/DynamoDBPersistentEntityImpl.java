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

import org.springframework.data.mapping.MappingException;
import org.springframework.data.mapping.model.BasicPersistentEntity;
import org.springframework.data.util.TypeInformation;

import java.util.Comparator;

/**
 * DynamoDB specific {@link DynamoDBPersistentEntity} implementation
 * 
 * @author Michael Lavelle
 * @author Sebastian Just
 */
public class DynamoDBPersistentEntityImpl<T> extends BasicPersistentEntity<T, DynamoDBPersistentProperty>
		implements
			DynamoDBPersistentEntity<T> {
	public DynamoDBPersistentEntityImpl(TypeInformation<T> information,
			Comparator<DynamoDBPersistentProperty> comparator) {
		super(information, comparator);
	}

	/**
	 * Returns the given property if it is a better candidate for the id property
	 * than the current id property.
	 * 
	 * @param property
	 *            the new id property candidate, will never be {@literal null}.
	 * @return the given id property or {@literal null} if the given property is not
	 *         an id property.
	 */
	protected DynamoDBPersistentProperty returnPropertyIfBetterIdPropertyCandidateOrNull(
			DynamoDBPersistentProperty property) {

		if (!property.isIdProperty()) {
			return null;
		}

		DynamoDBPersistentProperty idProperty = getIdProperty();
		if (idProperty != null) {

			if (idProperty.isCompositeIdProperty() && property.isHashKeyProperty()) {
				// Do nothing - favour id annotated properties over hashkey
				return null;
			} else if (idProperty.isHashKeyProperty() && property.isCompositeIdProperty()) {
				return property;
			} else {
				throw new MappingException(String.format(
						"Attempt to add id property %s but already have property %s registered "
								+ "as id. Check your mapping configuration!",
						property.getField(), idProperty.getField()));
			}
		}

		return property;
	}

}
