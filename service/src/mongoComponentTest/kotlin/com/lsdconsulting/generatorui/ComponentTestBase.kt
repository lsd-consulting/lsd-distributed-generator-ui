package com.lsdconsulting.generatorui

import com.lsdconsulting.generatorui.config.logger.log
import com.mongodb.client.MongoClients
import io.lsdconsulting.lsd.distributed.mongo.config.LibraryConfig
import org.junit.jupiter.api.AfterEach
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

private const val DATABASE_NAME = "lsd"
private const val COLLECTION_NAME = "interceptedInteraction"

@SpringBootTest(webEnvironment = RANDOM_PORT, classes = [LsdGeneratorUiApplication::class])
@ActiveProfiles("test")
@Import(LibraryConfig::class)
class ComponentTestBase: TestContainersMongoTest() {

    @AfterEach
    fun emptyDatabase() {
        val uri = mongoDBContainer.connectionString
        val mongoClient = MongoClients.create(uri)
        val database = mongoClient.getDatabase(DATABASE_NAME)
        val collection = database.getCollection(COLLECTION_NAME)
        log().info("Dropping the $COLLECTION_NAME collection")
        collection.drop()
    }
}
