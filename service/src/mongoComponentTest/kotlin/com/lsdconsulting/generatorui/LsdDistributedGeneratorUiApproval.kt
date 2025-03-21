package com.lsdconsulting.generatorui

import io.lsdconsulting.lsd.distributed.connector.model.InteractionType
import io.lsdconsulting.lsd.distributed.connector.model.InteractionType.REQUEST
import io.lsdconsulting.lsd.distributed.connector.model.InteractionType.RESPONSE
import io.lsdconsulting.lsd.distributed.connector.model.InterceptedInteraction
import io.lsdconsulting.lsd.distributed.connector.repository.InterceptedDocumentRepository
import org.approvaltests.Approvals
import org.approvaltests.core.Options
import org.approvaltests.core.Scrubber
import org.approvaltests.scrubbers.RegExScrubber
import org.approvaltests.scrubbers.Scrubbers
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType.TEXT_HTML
import org.springframework.test.annotation.DirtiesContext
import java.time.Instant.EPOCH
import java.time.ZoneId
import java.time.ZonedDateTime

@DirtiesContext
class LsdDistributedGeneratorUiApproval(
    @Autowired private val interceptedDocumentRepository: InterceptedDocumentRepository,
): ComponentTestBase() {

    @Autowired
    private lateinit var testRestTemplate: TestRestTemplate

    val traceId = "someTraceId"
    val body = "someBody"
    val requestHeaders = mutableMapOf("header" to listOf("value") as Collection<String>)
    val responseHeaders = mapOf("header" to listOf("value"))
    val httpMethod = "GET"
    val profile = "TEST"
    val elapsedTime = 25L
    val createdAt: ZonedDateTime = ZonedDateTime.ofInstant(EPOCH, ZoneId.of("UTC"))

    private val durationScrubber: Scrubber = RegExScrubber(">\\d+\\.\\d+s<", ">0.01s<")
    private val scrubber = Scrubbers.scrubAll(durationScrubber)

    @Test
    fun shouldReturnApprovedResponse() {
        // Remove the LSD interceptors
        testRestTemplate.restTemplate.interceptors.clear()

        interceptedDocumentRepository.save(buildInterceptedInteraction(null, REQUEST))
        interceptedDocumentRepository.save(buildInterceptedInteraction("200", RESPONSE))

        val headers = HttpHeaders()
        headers.accept = listOf(TEXT_HTML)

        val result = testRestTemplate.exchange("/lsd/$traceId", HttpMethod.GET, HttpEntity<Nothing>(headers), String::class.java)

        Approvals.verifyHtml(result.body, Options(scrubber))
    }

    private fun buildInterceptedInteraction(
        status: String?,
        type: InteractionType
    ) = InterceptedInteraction(
        traceId = traceId,
        body = body,
        requestHeaders = requestHeaders,
        responseHeaders = responseHeaders,
        serviceName = "Source",
        target = "Target",
        path = "/path",
        httpStatus = status,
        httpMethod = httpMethod,
        interactionType = type,
        profile = profile,
        elapsedTime = elapsedTime,
        createdAt = createdAt
    )
}
