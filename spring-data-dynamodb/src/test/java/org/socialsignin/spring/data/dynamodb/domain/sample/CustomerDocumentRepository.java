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
package org.socialsignin.spring.data.dynamodb.domain.sample;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CustomerDocumentRepository extends CrudRepository<CustomerDocument, CustomerDocumentId> {

	List<CustomerDocument> findByCustomerDocumentKey(String customerDocumentKey);

	default List<CustomerDocument> findByCustomerDocumentKey(String customerId, String downloadType) {
		return findByCustomerDocumentKey(CustomerDocumentId.buildCustomerDocumentKey(customerId, downloadType));
	}

}