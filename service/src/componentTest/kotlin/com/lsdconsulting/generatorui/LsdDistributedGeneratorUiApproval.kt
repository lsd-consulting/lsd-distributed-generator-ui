package com.lsdconsulting.generatorui

import com.lsd.core.IdGenerator
import io.lsdconsulting.lsd.distributed.access.model.InteractionType
import io.lsdconsulting.lsd.distributed.access.model.InteractionType.REQUEST
import io.lsdconsulting.lsd.distributed.access.model.InteractionType.RESPONSE
import io.lsdconsulting.lsd.distributed.access.model.InterceptedInteraction
import io.lsdconsulting.lsd.distributed.access.repository.InterceptedDocumentRepository
import org.approvaltests.Approvals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.getForEntity
import org.springframework.http.ResponseEntity
import java.time.Instant.EPOCH
import java.time.ZoneId
import java.time.ZonedDateTime

class LsdGeneratorUiApproval(
    @Autowired private val interceptedDocumentRepository: InterceptedDocumentRepository,
    @Autowired private val testRestTemplate: TestRestTemplate,
    @Autowired private val idGenerator: IdGenerator
): ComponentTestBase() {

    val traceId = "someTraceId"
    val body = "someBody"
    val requestHeaders = mapOf("header" to listOf("value"))
    val responseHeaders = mapOf("header" to listOf("value"))
    val httpMethod = "GET"
    val profile = "TEST"
    val elapsedTime = 25L
    val createdAt: ZonedDateTime = ZonedDateTime.ofInstant(EPOCH, ZoneId.of("UTC"))

    @BeforeEach
    fun resetIdGenerator() {
        idGenerator.reset()
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