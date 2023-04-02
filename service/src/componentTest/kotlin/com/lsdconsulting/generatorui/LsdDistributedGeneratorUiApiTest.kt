package com.lsdconsulting.generatorui

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.containsSubstring
import io.lsdconsulting.lsd.distributed.access.model.InterceptedInteraction
import org.jeasy.random.EasyRandom
import org.jeasy.random.EasyRandomParameters
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.getForEntity
import org.springframework.http.HttpEntity
import org.springframework.http.ResponseEntity


class LsdGeneratorUiApiTest(
    @Autowired private val testRestTemplate: TestRestTemplate
): ComponentTestBase() {

    private var easyRandom = EasyRandom(EasyRandomParameters().seed(System.currentTimeMillis()))

    @Test
    fun shouldSaveInteraction() {
        // Remove the LSD interceptors
        testRestTemplate.restTemplate.interceptors.clear()

        val interceptedInteraction = easyRandom.nextObject(InterceptedInteraction::class.java)
        val request = HttpEntity(interceptedInteraction)
        testRestTemplate.postForEntity("/lsd", request, Nothing::class.java)

        val result: ResponseEntity<String> = testRestTemplate.getForEntity("/lsd/${interceptedInteraction.traceId}")

        assertThat(result.body!!, containsSubstring("Diagram for traceIds: ${interceptedInteraction.traceId}"))
    }
}
