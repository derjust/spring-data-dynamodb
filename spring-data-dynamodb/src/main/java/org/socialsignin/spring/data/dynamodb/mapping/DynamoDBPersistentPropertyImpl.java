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

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBVersionAttribute;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Reference;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mapping.Association;
import org.springframework.data.mapping.model.AnnotationBasedPersistentProperty;
import org.springframework.data.mapping.model.Property;
import org.springframework.data.mapping.model.SimpleTypeHolder;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * {@link DynamoDBPersistentProperty} implementation
 * 
 * @author Michael Lavelle
 * @author Sebastian Just
 */
class DynamoDBPersistentPropertyImpl extends AnnotationBasedPersistentProperty<DynamoDBPersistentProperty>
		implements
			DynamoDBPersistentProperty {

	private static final Collection<Class<? extends Annotation>> ASSOCIATION_ANNOTATIONS;
	private static final Collection<Class<? extends Annotation>> ID_ANNOTATIONS;

	static {

		Set<Class<? extends Annotation>> annotations = new HashSet<Class<? extends Annotation>>();

		annotations.add(Reference.class); // Reference not yet supported
		ASSOCIATION_ANNOTATIONS = Collections.unmodifiableSet(annotations);

		annotations = new HashSet<>();
		annotations.add(Id.class);
		annotations.add(DynamoDBHashKey.class);
		ID_ANNOTATIONS = annotations;
	}

	/**
	 * Creates a new {@link DynamoDBPersistentPropertyImpl}
	 * 
	 * @param property
	 *            must not be {@literal null}.
	 * @param owner
	 *            must not be {@literal null}.
	 * @param simpleTypeHolder
	 *            must not be {@literal null}.
	 */

	public DynamoDBPersistentPropertyImpl(Property property, DynamoDBPersistentEntityImpl<?> owner,
			SimpleTypeHolder simpleTypeHolder) {
		super(property, owner, simpleTypeHolder);
	}

	@Override
	public boolean isWritable() {
		return !isAnnotationPresent(DynamoDBIgnore.class);
	}

	public boolean isHashKeyProperty() {
		return isAnnotationPresent(DynamoDBHashKey.class);
	}

	public boolean isCompositeIdProperty() {
		return isAnnotationPresent(Id.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.data.mapping.model.AnnotationBasedPersistentProperty
	 * #isIdProperty()
	 */
	@Override
	public boolean isIdProperty() {

		for (Class<? extends Annotation> annotation : ID_ANNOTATIONS) {
			if (isAnnotationPresent(annotation)) {
				return true;
			}
		}

		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.data.mapping.model.AbstractPersistentProperty#isEntity ()
	 */
	// @Override

	public boolean isEntity() {

		return isAnnotationPresent(Reference.class);// Reference not Yet
		// Supported
		// return propertyDescriptor != null
		// && propertyDescriptor.getPropertyType().isAnnotationPresent(
		// DynamoDBTable.class);

		// return false;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.data.mapping.model.AnnotationBasedPersistentProperty
	 * #isAssociation()
	 */
	@Override
	public boolean isAssociation() {

		for (Class<? extends Annotation> annotationType : ASSOCIATION_ANNOTATIONS) {
			if (findAnnotation(annotationType) != null) {
				// No query lookup yet supported ( see
				// Repositories.getPersistentEntity(..) )
				// return !information.isCollectionLike();
				return true;
			}
		}

		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.data.mapping.model.AnnotationBasedPersistentProperty
	 * #isTransient()
	 */
	@Override
	public boolean isTransient() {
		return isAnnotationPresent(Transient.class) || super.isTransient() || isAnnotationPresent(DynamoDBIgnore.class);
	}

	@Override
	public boolean isVersionProperty() {
		return super.isVersionProperty() || isAnnotationPresent(DynamoDBVersionAttribute.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.data.mapping.model.AbstractPersistentProperty#
	 * createAssociation()
	 */
	@Override
	protected Association<DynamoDBPersistentProperty> createAssociation() {
		return new Association<DynamoDBPersistentProperty>(this, null);
	}
}
