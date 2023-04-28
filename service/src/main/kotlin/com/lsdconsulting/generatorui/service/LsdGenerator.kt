package com.lsdconsulting.generatorui.service

import com.lsd.core.LsdContext
import io.lsdconsulting.lsd.distributed.generator.diagram.InteractionGenerator
import io.lsdconsulting.lsd.distributed.generator.diagram.dto.EventContainer
import org.springframework.stereotype.Service

private val lsd = LsdContext.instance
private val noColour: String? = null

@Service
class LsdGenerator(
    private val interactionGenerator: InteractionGenerator,
    private val participantListGenerator: ParticipantListGenerator,
) {
    
    fun captureInteractionsFromDatabase(vararg traceIds: String): String {
        val eventContainer = interactionGenerator.generate(traceIds.associateWith { noColour })
        val sequenceEvents = eventContainer.events
        val participants = participantListGenerator.generateParticipants(sequenceEvents)

        lsd.clear()
        lsd.addFacts(traceIds = traceIds, eventContainer = eventContainer)
        lsd.addParticipants(participants = participants.toTypedArray())
        lsd.capture(events = sequenceEvents.toTypedArray())
        lsd.completeScenario("Diagram for traceIds")
        return lsd.renderReport("Event Report")
    }
}

private fun LsdContext.addFacts(vararg traceIds: String, eventContainer: EventContainer) {
    addFact("traceIds", traceIds.joinToString())
    eventContainer.startTime?.let { addFact("startTime", it.toString()) }
    eventContainer.finishTime?.let { addFact("finishTime", it.toString()) }
}
