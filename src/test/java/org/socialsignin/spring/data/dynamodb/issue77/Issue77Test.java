package org.socialsignin.spring.data.dynamodb.issue77;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.UpdateItemResult;

import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={Issue77Test.TestAppConfig.class})
public class Issue77Test {

    @Configuration
    @ComponentScan(basePackageClasses = FooService.class)
    @EnableDynamoDBRepositories(basePackages = "org.socialsignin.spring.data.dynamodb.issue77")
    public static class TestAppConfig {

    	@Bean
    	public AmazonDynamoDB amazonDynamoDB() {
    		AmazonDynamoDB amazonDynamoDB = Mockito.mock(AmazonDynamoDB.class);
    		UpdateItemResult result = new UpdateItemResult();
			when(amazonDynamoDB.updateItem(any())).thenReturn(result);
    		
    		return amazonDynamoDB;
    	}
    }
    
	@Autowired
	FooService fooService;
	@Autowired
	FooRepository fooRepository;
	
	@Test
	public void testIssue77() {
		Foo foo = new Foo();
		
		fooService.processFoo(foo);

		assertTrue(fooRepository.isDoNonstandardThingCalled());
		assertTrue(fooRepository.isSaveCalled());
	}
}
