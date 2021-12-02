package com.lsdconsulting.generatorui.controller

import com.lsdconsulting.generatorui.service.LsdGenerator
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric
import org.junit.jupiter.api.Test

internal class LsdControllerShould {

    private val lsdGenerator = mockk<LsdGenerator>()
    private val underTest = LsdController(lsdGenerator = lsdGenerator)
    private val traceId = randomAlphanumeric(6)
    private val expectedResult = randomAlphanumeric(30)

    @Test
    fun passTraceIdToGenerator() {
        every { lsdGenerator.captureInteractionsFromDatabase(traceId) } returns expectedResult

        val result = underTest.find(traceId)

        assertThat(result, equalTo(expectedResult))
        verify { lsdGenerator.captureInteractionsFromDatabase(traceId) }
    }
}