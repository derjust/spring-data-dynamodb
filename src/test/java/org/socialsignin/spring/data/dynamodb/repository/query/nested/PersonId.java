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

import java.io.Serializable;

public class PersonId implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private String area;

    @DynamoDBRangeKey
    public String getArea() {
      return area;
    }

    @DynamoDBHashKey
    public String getName() {
      return name;
    }

    public PersonId() {}

    public PersonId(String name, String area) {
      this.name = name;
      this.area = area;
    }
    public void setName(String name) {
      this.name = name;
    }
    public void setArea(String area) {
      this.area = area;
    }
}

