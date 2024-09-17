package com.lsdconsulting.generatorui

import com.github.dockerjava.api.model.ExposedPort
import com.github.dockerjava.api.model.HostConfig
import com.github.dockerjava.api.model.PortBinding
import com.github.dockerjava.api.model.Ports
import com.lsdconsulting.generatorui.repository.TestRepository
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.lsdconsulting.lsd.distributed.connector.model.InteractionType
import io.lsdconsulting.lsd.distributed.connector.model.InterceptedInteraction
import io.lsdconsulting.lsd.distributed.postgres.config.LibraryConfig
import org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.context.annotation.Import
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.containers.PostgreSQLContainer
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

private const val POSTGRES_PORT = 5432
private const val POSTGRES_IMAGE = "postgres:15.3-alpine3.18"
private const val TABLE_NAME = "lsd_database"

@SpringBootTest(webEnvironment = RANDOM_PORT, classes = [LsdGeneratorUiApplication::class])
@ActiveProfiles("test")
@Import(LibraryConfig::class)
class ComponentTestBase {

    @Value("\${lsd.dist.connectionString}")
    private lateinit var dbConnectionString: String

    @Autowired
    lateinit var testRepository: TestRepository

    @BeforeEach
    fun setupDb() {
        val config = HikariConfig()
        config.jdbcUrl = dbConnectionString
        config.driverClassName = "org.postgresql.Driver"
        testRepository.createTable(HikariDataSource(config))
        testRepository.clearTable(HikariDataSource(config))
    }

    companion object {
        private var postgreSQLContainer: PostgreSQLContainer<*> = PostgreSQLContainer(POSTGRES_IMAGE)
            .withDatabaseName(TABLE_NAME)
            .withUsername("sa")
            .withPassword("sa")
            .withExposedPorts(POSTGRES_PORT)
            .withCreateContainerCmdModifier { cmd ->
                cmd.withHostConfig(
                    HostConfig().withPortBindings(
                        PortBinding(
                            Ports.Binding.bindPort(5433), ExposedPort(
                                POSTGRES_PORT
                            )
                        )
                    )
                )
            }

        @BeforeAll
        @JvmStatic
        internal fun beforeAll() {
            postgreSQLContainer.start()
        }

        @AfterAll
        @JvmStatic
        internal fun afterAll() {
            postgreSQLContainer.stop()
        }
    }

    internal fun buildInterceptedInteraction() = InterceptedInteraction(
        traceId = randomAlphanumeric(10),
        body = randomAlphanumeric(100),
        requestHeaders = mapOf(),
        responseHeaders = mapOf(),
        serviceName = randomAlphanumeric(30),
        target = randomAlphanumeric(30),
        path = randomAlphanumeric(100),
        httpStatus = HttpStatus.entries[Random().nextInt(0, HttpStatus.entries.size - 1)].name,
        httpMethod = HttpMethod.values()[Random().nextInt(0, HttpMethod.values().size - 1)].name(),
        interactionType = InteractionType.entries[Random().nextInt(0, InteractionType.entries.size - 1)],
        profile = randomAlphanumeric(20),
        elapsedTime = Random().nextLong(),
        createdAt = ZonedDateTime.now(ZoneId.of("UTC"))
    )
}
