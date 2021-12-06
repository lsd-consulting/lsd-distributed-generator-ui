package com.lsdconsulting.generatorui

import com.lsdconsulting.generatorui.config.RepositoryConfig
import com.lsdconsulting.generatorui.repository.TestRepository
import io.lsdconsulting.lsd.distributed.access.model.InterceptedInteraction
import io.lsdconsulting.lsd.distributed.access.model.Type.REQUEST
import io.lsdconsulting.lsd.distributed.access.model.Type.RESPONSE
import io.lsdconsulting.lsd.distributed.access.repository.InterceptedDocumentRepository
import org.approvaltests.Approvals
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
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

@TestInstance(PER_CLASS)
@SpringBootTest(webEnvironment = DEFINED_PORT, classes = [LsdGeneratorUiApplication::class])
@TestPropertySource("classpath:application-test.properties")
@Import(RepositoryConfig::class)
class LsdGeneratorIT {

    @Autowired
    private lateinit var testRestTemplate: TestRestTemplate

    @Autowired
    private lateinit var interceptedDocumentRepository: InterceptedDocumentRepository

    val traceId = "someTraceId"
    val body = "someBody"
    val requestHeaders = mapOf("header" to listOf("value"))
    val responseHeaders = mapOf("header" to listOf("value"))

    val httpMethod = "GET"
    val profile = "TEST"
    val elapsedTime = 25L
    val createdAt: ZonedDateTime = ZonedDateTime.ofInstant(EPOCH, ZoneId.of("UTC"))

    @AfterAll
    fun tearDown() {
        TestRepository.tearDownDatabase()
    }

    @Test
    fun returnApprovedResponse() {
        testRestTemplate.restTemplate.interceptors.clear()

        val i1 = InterceptedInteraction.builder()
            .traceId(traceId)
            .body(body)
            .requestHeaders(requestHeaders)
            .responseHeaders(responseHeaders)
            .serviceName("Source")
            .target("Target")
            .path("/path")
            .httpMethod(httpMethod)
            .type(REQUEST)
            .profile(profile)
            .elapsedTime(elapsedTime)
            .createdAt(createdAt)
            .build()
        val i2 = InterceptedInteraction.builder()
            .traceId(traceId)
            .body(body)
            .requestHeaders(requestHeaders)
            .responseHeaders(responseHeaders)
            .serviceName("Source")
            .target("Target")
            .path("/path")
            .httpStatus("200")
            .httpMethod(httpMethod)
            .type(RESPONSE)
            .profile(profile)
            .elapsedTime(elapsedTime)
            .createdAt(createdAt)
            .build()
        interceptedDocumentRepository.save(i1)
        interceptedDocumentRepository.save(i2)

        val entity: ResponseEntity<String> = testRestTemplate.getForEntity("/lsd/$traceId")

        Approvals.verify(entity.body)
    }
}
