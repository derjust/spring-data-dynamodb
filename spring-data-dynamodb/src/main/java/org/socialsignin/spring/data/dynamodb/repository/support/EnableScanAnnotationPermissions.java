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
package org.socialsignin.spring.data.dynamodb.repository.support;

import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.socialsignin.spring.data.dynamodb.repository.EnableScanCount;
import org.springframework.data.domain.Pageable;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

/**
 * 
 * @author Michael Lavelle
 * @author Sebastian Just
 * 
 */
public class EnableScanAnnotationPermissions implements EnableScanPermissions {

	private boolean findAllUnpaginatedScanEnabled = false;
	private boolean findAllPaginatedScanEnabled = false;

	private boolean findAllUnpaginatedScanCountEnabled = false;

	private boolean countUnpaginatedScanEnabled = false;
	private boolean deleteAllUnpaginatedScanEnabled = false;

	public EnableScanAnnotationPermissions(Class<?> repositoryInterface) {
		// Check to see if global EnableScan is declared at interface level
		if (repositoryInterface.isAnnotationPresent(EnableScan.class)) {
			this.findAllUnpaginatedScanEnabled = true;
			this.countUnpaginatedScanEnabled = true;
			this.deleteAllUnpaginatedScanEnabled = true;
			this.findAllPaginatedScanEnabled = true;
		} else {
			// Check declared methods for EnableScan annotation
			Method[] methods = ReflectionUtils.getAllDeclaredMethods(repositoryInterface);
			for (Method method : methods) {

				if (!method.isAnnotationPresent(EnableScan.class) || method.getParameterTypes().length > 0) {
					// Only consider methods which have the EnableScan
					// annotation and which accept no parameters
					continue;
				}

				if (method.getName().equals("findAll")) {
					findAllUnpaginatedScanEnabled = true;
					continue;
				}

				if (method.getName().equals("deleteAll")) {
					deleteAllUnpaginatedScanEnabled = true;
					continue;
				}

				if (method.getName().equals("count")) {
					countUnpaginatedScanEnabled = true;
					continue;
				}

			}
			for (Method method : methods) {

				if (!method.isAnnotationPresent(EnableScanCount.class) || method.getParameterTypes().length != 1) {
					// Only consider methods which have the EnableScanCount
					// annotation and which have a single pageable parameter
					continue;
				}

				if (method.getName().equals("findAll")
						&& Pageable.class.isAssignableFrom(method.getParameterTypes()[0])) {
					findAllUnpaginatedScanCountEnabled = true;
					continue;
				}

			}
			for (Method method : methods) {

				if (!method.isAnnotationPresent(EnableScan.class) || method.getParameterTypes().length != 1) {
					// Only consider methods which have the EnableScan
					// annotation and which have a single pageable parameter
					continue;
				}

				if (method.getName().equals("findAll")
						&& Pageable.class.isAssignableFrom(method.getParameterTypes()[0])) {
					findAllPaginatedScanEnabled = true;
					continue;
				}

			}
		}
		if (!findAllUnpaginatedScanCountEnabled && repositoryInterface.isAnnotationPresent(EnableScanCount.class)) {
			findAllUnpaginatedScanCountEnabled = true;
		}

	}

	@Override
	public boolean isFindAllUnpaginatedScanEnabled() {
		return findAllUnpaginatedScanEnabled;

	}

	@Override
	public boolean isDeleteAllUnpaginatedScanEnabled() {
		return deleteAllUnpaginatedScanEnabled;
	}

	@Override
	public boolean isCountUnpaginatedScanEnabled() {
		return countUnpaginatedScanEnabled;
	}

	@Override
	public boolean isFindAllUnpaginatedScanCountEnabled() {
		return findAllUnpaginatedScanCountEnabled;
	}

	@Override
	public boolean isFindAllPaginatedScanEnabled() {
		return findAllPaginatedScanEnabled;
	}

}
