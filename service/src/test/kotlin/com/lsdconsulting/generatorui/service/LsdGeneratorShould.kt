package com.lsdconsulting.generatorui.service

import io.lsdconsulting.lsd.distributed.generator.diagram.InteractionGenerator
import io.lsdconsulting.lsd.distributed.generator.diagram.dto.EventContainer
import io.mockk.every
import io.mockk.mockk
import org.apache.commons.lang3.RandomStringUtils.secure
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime

class LsdGeneratorShould {
    private val interactionGenerator = mockk<InteractionGenerator>(relaxed = true)
    private val participantListGenerator = mockk<ParticipantListGenerator>(relaxed = true)

    private val underTest: LsdGenerator = LsdGenerator(interactionGenerator, participantListGenerator)

    @Test
    fun `handle empty event container`() {
        val traceId = secure().nextAlphanumeric(10)
        every { interactionGenerator.generate(mapOf(traceId to null)) } returns EventContainer(events = listOf())
        every { participantListGenerator.generateParticipants(mutableListOf()) } returns setOf()

        underTest.captureInteractionsFromDatabase(traceId)
    }

    @Test
    fun `handle start time`() {
        val traceId = secure().nextAlphanumeric(10)
        every { interactionGenerator.generate(mapOf(traceId to null)) } returns EventContainer(
            events = listOf(),
            startTime = ZonedDateTime.now()
        )
        every { participantListGenerator.generateParticipants(mutableListOf()) } returns setOf()

        underTest.captureInteractionsFromDatabase(traceId)
    }

    @Test
    fun `handle finish time`() {
        val traceId = secure().nextAlphanumeric(10)
        every { interactionGenerator.generate(mapOf(traceId to null)) } returns EventContainer(
            events = listOf(),
            finishTime = ZonedDateTime.now()
        )
        every { participantListGenerator.generateParticipants(mutableListOf()) } returns setOf()

        underTest.captureInteractionsFromDatabase(traceId)
    }

    @Test
    fun `handle start time and finish time`() {
        val traceId = secure().nextAlphanumeric(10)
        val zonedDateTime = ZonedDateTime.now()
        every { interactionGenerator.generate(mapOf(traceId to null)) } returns EventContainer(
            events = listOf(),
            startTime = zonedDateTime,
            finishTime = zonedDateTime.plusSeconds(1L)
        )
        every { participantListGenerator.generateParticipants(mutableListOf()) } returns setOf()

        underTest.captureInteractionsFromDatabase(traceId)
    }
}
