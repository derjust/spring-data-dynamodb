package org.socialsignin.spring.data.dynamodb.domain.sample;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.GlobalSecondaryIndex;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.Projection;
import com.amazonaws.services.dynamodbv2.model.ProjectionType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories;
import org.socialsignin.spring.data.dynamodb.utils.DynamoDBLocalResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {DynamoDBLocalResource.class, DuplicateColumnUsageTest.DuplicateColumnUsageTestConfig.class})
public class DuplicateColumnUsageTest {


    @Configuration
    @EnableDynamoDBRepositories(basePackages = "org.socialsignin.spring.data.dynamodb.domain.sample")
    static class DuplicateColumnUsageTestConfig {

    }


    @Before
    public void setUp() {
    //    DynamoDBLocalResource.createTable(ddb, Playlist.class);
        List<AttributeDefinition> ad = new ArrayList<>();
        ad.add(new AttributeDefinition("Genre", ScalarAttributeType.S));
        ad.add(new AttributeDefinition("PlaylistName", ScalarAttributeType.S));
        ad.add(new AttributeDefinition("UserName", ScalarAttributeType.S));

        List<KeySchemaElement> ks = new ArrayList<>();
        ks.add(new KeySchemaElement("UserName", KeyType.HASH));
        ks.add(new KeySchemaElement("PlaylistName", KeyType.RANGE));

        List<KeySchemaElement> gsiks = new ArrayList<>();
        gsiks.add(new KeySchemaElement("Genre", KeyType.HASH));
        gsiks.add(new KeySchemaElement("PlaylistName", KeyType.RANGE));

        List<GlobalSecondaryIndex> gsis = new ArrayList<>();
        gsis.add(new GlobalSecondaryIndex()
                .withIndexName("Genre-PlaylistName-index")
                .withKeySchema(gsiks)
                .withProjection(new Projection().withProjectionType(ProjectionType.ALL))
                .withProvisionedThroughput(new ProvisionedThroughput(10L, 10L)));

        CreateTableRequest ctr = new CreateTableRequest();
        ctr.withTableName("playlist");
        ctr.withAttributeDefinitions(ad);
        ctr.withKeySchema(ks);
        ctr.withGlobalSecondaryIndexes(gsis);
        ctr.withProvisionedThroughput(new ProvisionedThroughput(10L, 10L));

        ddb.createTable(ctr);
    }


    @Autowired
    private PlaylistRepository playlistRepository;
    @Autowired
    private AmazonDynamoDB ddb;


    private Playlist generatePlaylist(String genre, String playlistName) {
        final String userName = "userName-" + UUID.randomUUID().toString();
        final String displayName = "displayName-" + UUID.randomUUID().toString();
        PlaylistId id = new PlaylistId(userName, playlistName);

        Playlist playlist = new Playlist(id);
        playlist.setDisplayName(displayName);
        playlist.setGenre(genre);

        return playlist;
    }

    @Test
    public void testGsiSharedRange() {
        final String GENRE = "GENRE";
        playlistRepository.save(generatePlaylist(GENRE, "1"));
        playlistRepository.save(generatePlaylist(GENRE, "2"));
        playlistRepository.save(generatePlaylist(GENRE, "3"));
        try {
            Thread.sleep(2000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals(3, playlistRepository.findByGenre(GENRE).size());

        assertEquals(1, playlistRepository.findByGenreAndPlaylistName(GENRE, "2").size());

        List<Playlist> actual = playlistRepository.findByGenreAndPlaylistNameBetween(GENRE, "2", "3");

        assertEquals(2, actual.size());
    }

}
