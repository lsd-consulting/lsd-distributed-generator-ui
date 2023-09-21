package com.lsdconsulting.generatorui

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.containsSubstring
import com.natpryce.hamkrest.equalTo
import io.lsdconsulting.lsd.distributed.connector.model.InterceptedInteraction
import org.apache.commons.lang3.RandomStringUtils.randomAlphabetic
import org.jeasy.random.EasyRandom
import org.jeasy.random.EasyRandomParameters
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


class LsdDistributedGeneratorUiApiTest(
    @Autowired private val testRestTemplate: TestRestTemplate
) : ComponentTestBase() {

    private var easyRandom = EasyRandom(EasyRandomParameters().seed(System.currentTimeMillis()))

    private lateinit var interceptedInteraction: InterceptedInteraction

    @BeforeEach
    fun setup() {
        // Remove the LSD interceptors
        testRestTemplate.restTemplate.interceptors.clear()

        interceptedInteraction = saveInterceptedInteraction()
    }

    @Test
    fun shouldGenerateDiff() {
        val headers = HttpHeaders()
        headers.accept = listOf(TEXT_HTML)
        val traceId1 = randomAlphabetic(5)
        val traceId2 = randomAlphabetic(5)

        val i1 = generateInterceptedInteraction(traceId1)
        val i2 = generateInterceptedInteraction(traceId2)
        val i3 = generateInterceptedInteraction(traceId1)
        val i4 = generateInterceptedInteraction(traceId2)

        saveInteraction(i1)
        saveInteraction(i1.copy(traceId = traceId2))
        saveInteraction(i2)
        saveInteraction(i3)
        saveInteraction(i4)
        saveInteraction(i4.copy(traceId = traceId1))

        val url = "/lsds/diff?traceId1=$traceId1&traceId2=$traceId2"
        val resultBody =
            testRestTemplate.exchange(url, GET, HttpEntity<Nothing>(headers), String::class.java).body!!.uppercase()

        println("resultBody=$resultBody")
        assertThat(resultBody, containsSubstring("--- " + i1.traceId.uppercase()))
        assertThat(resultBody, containsSubstring("+++ " + i2.traceId.uppercase()))
        assertThat(resultBody, containsSubstring("@STARTUML"))
        assertThat(resultBody, containsSubstring("@ENDUML"))
        assertThat(resultBody, containsSubstring("HIDE UNLINKED"))
        assertThat(resultBody, containsSubstring(" PARTICIPANT " + i1.serviceName.uppercase()))
        assertThat(resultBody, containsSubstring(" PARTICIPANT " + i1.target.uppercase()))
        assertThat(resultBody, containsSubstring("+PARTICIPANT " + i2.serviceName.uppercase()))
        assertThat(resultBody, containsSubstring("+PARTICIPANT " + i2.target.uppercase()))
        assertThat(resultBody, containsSubstring("-PARTICIPANT " + i3.serviceName.uppercase()))
        assertThat(resultBody, containsSubstring("-PARTICIPANT " + i3.target.uppercase()))
        assertThat(resultBody, containsSubstring(" PARTICIPANT " + i4.serviceName.uppercase()))
        assertThat(resultBody, containsSubstring(" PARTICIPANT " + i4.target.uppercase()))
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

        assertThat(result.body!!, equalTo(listOf(interceptedInteraction)))
    }

    @Test
    fun shouldStoreAndRetrieveInteractionsAsJson() {
        val secondInterceptedInteraction = saveInterceptedInteraction()

        val headers = HttpHeaders()
        headers.accept = listOf(APPLICATION_JSON)

        val url = "/lsds?traceIds=${interceptedInteraction.traceId}&traceIds=${secondInterceptedInteraction.traceId}"
        val result = testRestTemplate.exchange(url, GET, HttpEntity<Nothing>(headers),
            object : ParameterizedTypeReference<List<InterceptedInteraction>>() {})

        assertThat(result.body!!, equalTo(listOf(interceptedInteraction, secondInterceptedInteraction)))
    }

    private fun saveInterceptedInteraction(): InterceptedInteraction {
        return generateInterceptedInteraction().also {
            saveInteraction(it)
        }
    }

    private fun generateInterceptedInteraction(traceId: String? = null): InterceptedInteraction {
        return with(easyRandom.nextObject(InterceptedInteraction::class.java)) {
            this.copy(
                traceId = traceId ?: this.traceId,
                createdAt = ZonedDateTime.now(ZoneId.of("UTC")).truncatedTo(MILLIS)
            )
        }
    }

    private fun saveInteraction(interceptedInteraction: InterceptedInteraction) {
        val request = HttpEntity(interceptedInteraction)
        testRestTemplate.postForEntity("/lsds", request, Nothing::class.java)
    }
}
