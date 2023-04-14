package com.lsdconsulting.generatorui.service

import com.lsd.core.IdGenerator
import io.lsdconsulting.lsd.distributed.generator.diagram.InteractionGenerator
import io.lsdconsulting.lsd.distributed.generator.diagram.dto.EventContainer
import io.mockk.every
import io.mockk.mockk
import org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric
import org.junit.jupiter.api.Test
import java.util.*

class LsdGeneratorShould {
    private val idGenerator = mockk<IdGenerator>(relaxed = true)
    private val interactionGenerator = mockk<InteractionGenerator>(relaxed = true)
    private val participantListGenerator = mockk<ParticipantListGenerator>(relaxed = true)

    private val underTest: LsdGenerator = LsdGenerator(interactionGenerator, participantListGenerator, idGenerator)

    @Test
    fun handleEmptyEventContainer() {
        val traceId = randomAlphanumeric(10)
        every {idGenerator.next()} returns randomAlphanumeric(10)
        every { interactionGenerator.generate(mapOf(traceId to Optional.empty())) } returns EventContainer.builder()
            .events(
                listOf()
            ).build()
        every { participantListGenerator.generateParticipants(mutableListOf())} returns setOf()

        underTest.captureInteractionsFromDatabase(traceId)
    }
}
