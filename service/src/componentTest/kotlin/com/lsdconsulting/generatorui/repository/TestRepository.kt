package com.lsdconsulting.generatorui.repository

import com.lsdconsulting.generatorui.config.logger.log
import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import de.flapdoodle.embed.mongo.MongodExecutable
import de.flapdoodle.embed.mongo.MongodStarter
import de.flapdoodle.embed.mongo.config.MongodConfig
import de.flapdoodle.embed.mongo.config.Net
import de.flapdoodle.embed.mongo.distribution.Version.Main.PRODUCTION
import de.flapdoodle.embed.process.runtime.Network.localhostIsIPv6
import java.io.IOException

class TestRepository {
//    private val pojoCodecRegistry: CodecRegistry = CodecRegistries.fromRegistries(
//        MongoClientSettings.getDefaultCodecRegistry(),
//        CodecRegistries.fromCodecs(ZonedDateTimeCodec(), TypeCodec()),
//        CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build())
//    )
//    val collection: MongoCollection<Document>
//        get() {
//            val database: MongoDatabase = mongoClient.getDatabase(DATABASE_NAME)
//            return database.getCollection(COLLECTION_NAME).withCodecRegistry(pojoCodecRegistry)
//        }

//    fun findAll(traceId: String?): List<InterceptedInteraction> {
//        log().info("Retrieving interceptedInteractions for traceId:{}", traceId)
//        val database: MongoDatabase = mongoClient.getDatabase(DATABASE_NAME)
//        val collection: MongoCollection<Document> =
//            database.getCollection(COLLECTION_NAME).withCodecRegistry(pojoCodecRegistry)
//        val result: MutableList<InterceptedInteraction> = ArrayList<InterceptedInteraction>()
//        try {
//            collection.find(eq("traceId", traceId), InterceptedInteraction::class.java)
//                .iterator().use { cursor ->
//                while (cursor.hasNext()) {
//                    val interceptedInteraction: InterceptedInteraction = cursor.next()
//                    log().info("Retrieved interceptedInteraction:{}", interceptedInteraction)
//                    result.add(interceptedInteraction)
//                }
//            }
//        } catch (e: MongoException) {
//            log().error(
//                "Failed to retrieve interceptedInteractions - message:{}, stackTrace:{}",
//                e.message,
//                e.stackTrace
//            )
//        }
//        return result
//    }

    companion object {
        private const val MONGODB_HOST = "localhost"
        private const val MONGODB_PORT = 27017
        private const val DATABASE_NAME = "lsd"
        private const val COLLECTION_NAME = "interceptedInteraction"
        private lateinit var mongoClient: MongoClient
        private lateinit var mongodExecutable: MongodExecutable

        fun setupDatabase() {
            try {
                val mongodConfig: MongodConfig = MongodConfig.builder()
                    .version(PRODUCTION)
                    .net(Net(MONGODB_HOST, MONGODB_PORT, localhostIsIPv6()))
                    .build()
                mongodExecutable = MongodStarter.getDefaultInstance().prepare(mongodConfig)
                mongodExecutable.start()
                mongoClient = MongoClients.create(
                    MongoClientSettings.builder()
                        .applyConnectionString(ConnectionString("mongodb://$MONGODB_HOST:$MONGODB_PORT"))
                        .retryWrites(true)
                        .build()
                )
            } catch (e: IOException) {
                log().error(e.message, e)
            }
        }

        fun tearDownDatabase() {
            mongoClient.close()
            mongodExecutable.stop()
        }
    }
}
