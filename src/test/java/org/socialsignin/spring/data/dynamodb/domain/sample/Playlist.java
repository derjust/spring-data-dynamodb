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

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import org.springframework.data.annotation.Id;

@DynamoDBTable(tableName = "playlist")
public class Playlist {

	@Id
	private PlaylistId playlistId;
	private String displayName;
	private String genre;

	public Playlist() {
	}

	public Playlist(PlaylistId playlistId) {
		this.playlistId = playlistId;
	}
	
	@DynamoDBIgnore
	public PlaylistId getId() {
		return this.playlistId;
	}

	@DynamoDBAttribute(attributeName="DisplayName")
	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	@DynamoDBHashKey(attributeName = "UserName")
	public String getUserName() {
		return playlistId != null ? playlistId.getUserName() : null;
	}

	public void setUserName(String userName) {
		if (playlistId == null) {
			playlistId = new PlaylistId();
		}
		playlistId.setUserName(userName);
	}

	@DynamoDBRangeKey(attributeName = "PlaylistName")
	@DynamoDBIndexRangeKey(attributeName = "PlaylistName", globalSecondaryIndexName="Genre-PlaylistName-index")
	public String getPlaylistName() {
		return playlistId != null ? playlistId.getPlaylistName() : null;
	}

	public void setPlaylistName(String playlistName) {
		if (playlistId == null) {
			playlistId = new PlaylistId();
		}
		playlistId.setPlaylistName(playlistName);
	}

	@DynamoDBIndexHashKey(attributeName = "Genre", globalSecondaryIndexName="Genre-PlaylistName-index")
	public String getGenre() {
		return this.genre;
	}

	public void setGenre(String genre) {
		this.genre = genre;
	}
}
