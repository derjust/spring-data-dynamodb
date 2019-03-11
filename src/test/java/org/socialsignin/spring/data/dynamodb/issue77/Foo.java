package org.socialsignin.spring.data.dynamodb.issue77;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName = "foo")
public class Foo {

	private long id;

	@DynamoDBHashKey(attributeName = "Id")
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	
}
