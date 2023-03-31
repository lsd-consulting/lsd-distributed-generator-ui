package com.lsdconsulting.generatorui

import com.lsdconsulting.generatorui.config.RepositoryConfig
import com.lsdconsulting.generatorui.repository.TestRepository
import io.lsdconsulting.lsd.distributed.access.model.InteractionType
import io.lsdconsulting.lsd.distributed.access.model.InteractionType.REQUEST
import io.lsdconsulting.lsd.distributed.access.model.InteractionType.RESPONSE
import io.lsdconsulting.lsd.distributed.access.model.InterceptedInteraction
import io.lsdconsulting.lsd.distributed.access.repository.InterceptedDocumentRepository
import io.lsdconsulting.lsd.distributed.mongo.config.LibraryConfig
import org.approvaltests.Approvals
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.getForEntity
import org.springframework.context.annotation.Import
import org.springframework.http.ResponseEntity
import org.springframework.test.context.TestPropertySource
import java.time.Instant.EPOCH
import java.time.ZoneId
import java.time.ZonedDateTime

@SpringBootTest(webEnvironment = DEFINED_PORT, classes = [LsdGeneratorUiApplication::class])
@TestPropertySource("classpath:application-test.properties")
@Import(RepositoryConfig::class, LibraryConfig::class)
class LsdGeneratorUiApproval(
    @Autowired private val interceptedDocumentRepository: InterceptedDocumentRepository,
    @Autowired private val testRestTemplate: TestRestTemplate
) {

    val traceId = "someTraceId"
    val body = "someBody"
    val requestHeaders = mapOf("header" to listOf("value"))
    val responseHeaders = mapOf("header" to listOf("value"))
    val httpMethod = "GET"
    val profile = "TEST"
    val elapsedTime = 25L
    val createdAt: ZonedDateTime = ZonedDateTime.ofInstant(EPOCH, ZoneId.of("UTC"))

    companion object {
        @BeforeAll
        @JvmStatic
        internal fun beforeAll() {
            TestRepository.setupDatabase()
        }

        @AfterAll
        @JvmStatic
        internal fun afterAll() {
            TestRepository.tearDownDatabase()
        }
    }

    @Test
    fun shouldReturnApprovedResponse() {
        // Remove the LSD interceptors
        testRestTemplate.restTemplate.interceptors.clear()

        interceptedDocumentRepository.save(buildInterceptedInteraction(null, REQUEST))
        interceptedDocumentRepository.save(buildInterceptedInteraction("200", RESPONSE))

        val result: ResponseEntity<String> = testRestTemplate.getForEntity("/lsd/$traceId")

        Approvals.verifyHtml(result.body)
    }

    private fun buildInterceptedInteraction(
        status: String?,
        type: InteractionType
    ): InterceptedInteraction? = InterceptedInteraction.builder()
        .traceId(traceId)
        .body(body)
        .requestHeaders(requestHeaders)
        .responseHeaders(responseHeaders)
        .serviceName("Source")
        .target("Target")
        .path("/path")
        .httpStatus(status)
        .httpMethod(httpMethod)
        .interactionType(type)
        .profile(profile)
        .elapsedTime(elapsedTime)
        .createdAt(createdAt)
        .build()
}