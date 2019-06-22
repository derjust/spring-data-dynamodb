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

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import org.springframework.data.annotation.Id;

@DynamoDBTable(tableName = "customerhistory")
public class CustomerHistory {
	@Id
	private CustomerHistoryId id;

	private String tag;

	@DynamoDBIndexHashKey(globalSecondaryIndexName = "idx_global_tag")
	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	@DynamoDBHashKey(attributeName = "customerId")
	public String getId() {
		return id != null ? id.getCustomerId() : null;
	}

	public void setId(String customerId) {
		if (this.id == null) {
			this.id = new CustomerHistoryId();
		}
		this.id.setCustomerId(customerId);
	}

	@DynamoDBRangeKey(attributeName = "createDt")
	public String getCreateDt() {
		return id != null ? id.getCreateDt() : null;
	}

	public void setCreateDt(String createDt) {
		if (this.id == null) {
			this.id = new CustomerHistoryId();
		}

		this.id.setCreateDt(createDt);
	}
}