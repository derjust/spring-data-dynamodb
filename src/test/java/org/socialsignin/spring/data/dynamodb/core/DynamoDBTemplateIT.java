package org.socialsignin.spring.data.dynamodb.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.socialsignin.spring.data.dynamodb.domain.sample.User;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

/**
 * Integration test that interacts with DynamoDB Local instance.
 */
public class DynamoDBTemplateIT {

    private static final String PORT = System.getProperty("dynamodb.port");
    private DynamoDBTemplate dynamoDBTemplate;

    @Before
    public void setUpOnce() {
        assertNotNull(PORT);

        AmazonDynamoDBClient dynamoDB = new AmazonDynamoDBClient(new BasicAWSCredentials("AWS-Key", ""));
        dynamoDB.setEndpoint(String.format("http://localhost:%s", PORT));
        this.dynamoDBTemplate = new DynamoDBTemplate(dynamoDB);
    }

    @Test
    public void testUser_CRUD() {
        // Given a entity to save.
        User user = new User();
        user.setName("John Doe");
        user.setNumberOfPlaylists(10);
        user.setId(UUID.randomUUID().toString());

        // Save it to DB.
        dynamoDBTemplate.save(user);

        // Retrieve it from DB.
        User retrievedUser = dynamoDBTemplate.load(User.class, user.getId());

        // Verify the details on the entity.
        assertEquals(retrievedUser.getName(), user.getName());
        assertEquals(retrievedUser.getId(), user.getId());
        assertEquals(retrievedUser.getNumberOfPlaylists(), user.getNumberOfPlaylists());

        // Update the entity and save.
        retrievedUser.setNumberOfPlaylists(20);
        dynamoDBTemplate.save(retrievedUser);

        retrievedUser = dynamoDBTemplate.load(User.class, user.getId());

        assertEquals(new Integer(20), retrievedUser.getNumberOfPlaylists());

        // Delete.
        dynamoDBTemplate.delete(retrievedUser);

        // Get again.
        assertNull(dynamoDBTemplate.load(User.class, user.getId()));
    }

}
