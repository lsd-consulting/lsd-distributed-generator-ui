package com.lsdconsulting.generatorui

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.containsSubstring
import com.natpryce.hamkrest.equalTo
import io.lsdconsulting.lsd.distributed.connector.model.InterceptedInteraction
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod.GET
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.MediaType.TEXT_HTML
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit.MILLIS


class LsdDistributedGeneratorUiApiTest: ComponentTestBase() {

    @Autowired
    private lateinit var testRestTemplate: TestRestTemplate

    private lateinit var interceptedInteraction: InterceptedInteraction

    @BeforeEach
    fun setup() {
        // Remove the LSD interceptors
        testRestTemplate.restTemplate.interceptors.clear()

        interceptedInteraction = saveInterceptedInteraction()
    }

    @Test
    fun shouldSaveInteractionAndRetrieveHtml() {
        val headers = HttpHeaders()
        headers.accept = listOf(TEXT_HTML)

        val url = "/lsd/${interceptedInteraction.traceId}"
        val resultBody = testRestTemplate.exchange(url, GET, HttpEntity<Nothing>(headers), String::class.java).body!!

        assertThat(resultBody, containsSubstring("Diagram for traceIds"))
        assertThat(resultBody, containsSubstring(interceptedInteraction.traceId))
    }

    @Test
    fun shouldStoreAndRetrieveInteractionAsJson() {
        val headers = HttpHeaders()
        headers.accept = listOf(APPLICATION_JSON)

        val url = "/lsds?traceIds=${interceptedInteraction.traceId}"
        val result = testRestTemplate.exchange(url, GET, HttpEntity<Nothing>(headers),
            object : ParameterizedTypeReference<List<InterceptedInteraction>>() {})

        assertThat(result.body!!, equalTo(listOf(interceptedInteraction.copy(createdAt = interceptedInteraction.createdAt.withFixedOffsetZone()))))
    }

    @Test
    fun shouldStoreAndRetrieveInteractionsAsJson() {
        val secondInterceptedInteraction = saveInterceptedInteraction()

        val headers = HttpHeaders()
        headers.accept = listOf(APPLICATION_JSON)

        val url = "/lsds?traceIds=${interceptedInteraction.traceId}&traceIds=${secondInterceptedInteraction.traceId}"
        val result = testRestTemplate.exchange(url, GET, HttpEntity<Nothing>(headers),
            object : ParameterizedTypeReference<List<InterceptedInteraction>>() {})

        assertThat(result.body!!, equalTo(listOf(interceptedInteraction.copy(createdAt = interceptedInteraction.createdAt.withFixedOffsetZone()), secondInterceptedInteraction.copy(createdAt = secondInterceptedInteraction.createdAt.withFixedOffsetZone()))))
    }

    private fun saveInterceptedInteraction(): InterceptedInteraction {
        val interceptedInteraction = buildInterceptedInteraction().copy(
            createdAt = ZonedDateTime.now(ZoneId.of("UTC")).truncatedTo(MILLIS))
        val request = HttpEntity(interceptedInteraction)
        testRestTemplate.postForEntity("/lsds", request, Nothing::class.java)
        return interceptedInteraction
    }
}
