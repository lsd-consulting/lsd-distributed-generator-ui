package com.lsdconsulting.generatorui.service

import io.lsdconsulting.lsd.distributed.generator.diagram.InteractionGenerator
import io.lsdconsulting.lsd.distributed.generator.diagram.dto.EventContainer
import io.mockk.every
import io.mockk.mockk
import org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric
import org.junit.jupiter.api.Test

class LsdGeneratorShould {
    private val interactionGenerator = mockk<InteractionGenerator>(relaxed = true)
    private val participantListGenerator = mockk<ParticipantListGenerator>(relaxed = true)

    private val underTest: LsdGenerator = LsdGenerator(interactionGenerator, participantListGenerator)

    @Test
    fun handleEmptyEventContainer() {
        val traceId = randomAlphanumeric(10)
        every { interactionGenerator.generate(mapOf(traceId to null)) } returns EventContainer(events = listOf())
        every { participantListGenerator.generateParticipants(mutableListOf()) } returns setOf()

        underTest.captureInteractionsFromDatabase(traceId)
    }
}
