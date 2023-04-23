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
import de.flapdoodle.embed.mongo.distribution.Version.Main.V5_0
import de.flapdoodle.embed.process.runtime.Network.localhostIsIPv6
import java.io.IOException

class TestRepository {
    companion object {
        private const val MONGODB_HOST = "localhost"
        private const val MONGODB_PORT = 27017
        private lateinit var mongoClient: MongoClient
        private lateinit var mongodExecutable: MongodExecutable

        fun setupDatabase() {
            try {
                val mongodConfig: MongodConfig = MongodConfig.builder()
                    .version(V5_0)
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
