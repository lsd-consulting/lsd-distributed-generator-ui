package com.lsdconsulting.generatorui.controller

import com.lsdconsulting.generatorui.service.LsdGenerator
import com.lsdconsulting.generatorui.service.LsdService
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import io.lsdconsulting.lsd.distributed.access.model.InterceptedInteraction
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric
import org.jeasy.random.EasyRandom
import org.jeasy.random.EasyRandomParameters
import org.junit.jupiter.api.Test
import org.springframework.http.ResponseEntity

internal class LsdControllerShould {

    private val lsdGenerator = mockk<LsdGenerator>()
    private val lsdService = mockk<LsdService>(relaxed = true)
    private val underTest = LsdController(lsdGenerator = lsdGenerator, lsdService = lsdService)
    private val traceId = randomAlphanumeric(6)
    private val expectedResult = randomAlphanumeric(30)
    private var easyRandom = EasyRandom(EasyRandomParameters().seed(System.currentTimeMillis()))

    @Test
    fun passTraceIdToGenerator() {
        every { lsdGenerator.captureInteractionsFromDatabase(traceId) } returns expectedResult

        val result = underTest.findByTraceId(traceId)

        assertThat(result, equalTo(expectedResult))
        verify { lsdGenerator.captureInteractionsFromDatabase(traceId) }
    }

    @Test
    fun storeInterceptedInteraction() {
        every { lsdGenerator.captureInteractionsFromDatabase(traceId) } returns expectedResult
        val interceptedInteraction = easyRandom.nextObject(InterceptedInteraction::class.java)

        val result = underTest.store(interceptedInteraction)

        assertThat(result, equalTo(ResponseEntity.ok(interceptedInteraction)))
        verify { lsdService.storeInteractionsInDatabase(interceptedInteraction) }
    }
}
