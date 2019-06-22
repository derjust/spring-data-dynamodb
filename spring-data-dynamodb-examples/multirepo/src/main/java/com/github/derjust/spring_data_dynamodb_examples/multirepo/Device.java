/**
 * Copyright © 2013-2019 spring-data-dynamodb (https://github.com/derjust/spring-data-dynamodb/spring-data-dynamodb-examples/spring-data-dynamodb-examples-multirepo)
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
package com.github.derjust.spring_data_dynamodb_examples.multirepo;

import java.util.Date;

import org.springframework.data.annotation.Id;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName = "Device")
public class Device {
	@Id
	private DeviceKey key;

	@DynamoDBAttribute(attributeName = "Name")
	private String name;

	@DynamoDBAttribute(attributeName = "LastChange")
	private Date lastChange;

	private Device(DeviceKey key, String name, Date lastChange) {
		this.key = key;
		this.name = name;
		this.lastChange = lastChange;
	}

	public Device(Long vendorId, String productId, String name, Date lastChange) {
		this(new DeviceKey(vendorId, productId), name, lastChange);
	}

	public Device() {
	}

	@DynamoDBHashKey(attributeName = "VendorId")
	public Long getVendorId() {
		return (key != null) ? key.getVendorId() : null;
	}

	public void setVendorId(Long vendorId) {
		if (key == null) {
			key = new DeviceKey();
		}
		key.setVendorId(vendorId);
	}

	@DynamoDBRangeKey(attributeName = "ProductId")
	public String getProductId() {
		return (key != null) ? key.getProduct() : null;
	}

	public void setProductId(String product) {
		if (key == null) {
			key = new DeviceKey();
		}
		key.setProduct(product);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getLastChange() {
		return lastChange;
	}

	public void setLastChange(Date lastChange) {
		this.lastChange = lastChange;
	}

	@Override
	public String toString() {
		return String.format("Device[id=%s, name='%s', lastChange='%s']", key, name, lastChange);
	}
}
