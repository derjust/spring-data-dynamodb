package org.socialsignin.spring.data.dynamodb.repository.config;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class DynamoDBRepositoryNameSpaceHandlerTest {

    private DynamoDBRepositoryNameSpaceHandler underTest;

    @Before
    public void setUp() {
        underTest = new DynamoDBRepositoryNameSpaceHandler();
    }

    @Test
    public void testInit() {
        underTest.init();

        assertTrue("init() should not throw any exception", true);
    }
}
