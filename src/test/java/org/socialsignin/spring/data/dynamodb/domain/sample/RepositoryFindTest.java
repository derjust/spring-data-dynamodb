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
package org.socialsignin.spring.data.dynamodb.domain.sample;


import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories;
import org.socialsignin.spring.data.dynamodb.utils.DynamoDBLocalResource;
import org.socialsignin.spring.data.dynamodb.utils.DynamoDBResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.Instant;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {DynamoDBLocalResource.class, RepositoryFindTest.TestAppConfig.class})
public class RepositoryFindTest {

    @Configuration
    @EnableDynamoDBRepositories(basePackages = "org.socialsignin.spring.data.dynamodb.domain.sample")
    public static class TestAppConfig {
    }

    @Autowired
    private AmazonDynamoDB ddb;
    @Autowired
    private UserRepository userRepository;

    @Before
    public void setUp() {
        DynamoDBLocalResource.createTable(ddb, User.class);
    }

    @Test
    public void testFindAll() {
        //List<User> actual1 = userRepository.findByLeaveDate(Instant.now());

        List<User> actual3 = userRepository.findByNameOrderByNameAsc("x");

//        List<User> actual = userRepository.findAllByOrderByName();
    }
}


