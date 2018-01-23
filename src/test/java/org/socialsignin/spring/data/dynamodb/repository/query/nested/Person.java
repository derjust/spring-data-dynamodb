/**
 * Copyright © 2013 spring-data-dynamodb (https://github.com/derjust/spring-data-dynamodb)
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
package org.socialsignin.spring.data.dynamodb.repository.query.nested;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import org.springframework.data.annotation.Id;

@DynamoDBTable(tableName = "Person")
public class Person {

    @Id
    private PersonId personId;

    private String phone;
    private Address address;

    @DynamoDBHashKey
    public String getName() {
        return personId != null ? personId.getName() : null;
    }

    @DynamoDBRangeKey
    public String getArea() {
        return personId != null ? personId.getArea() : null;
    }
    public Address getAddress() {
        return address;
    }
    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
    public void setName(String name) {
        if (personId == null) {
            personId = new PersonId();
        }
        this.personId.setName(name);
    }
    public void setArea(String area) {
        if (personId == null) {
            personId = new PersonId();
        }
        this.personId.setArea(area);
    }
    public void setAddress(Address address) {
        this.address = address;
    }
}