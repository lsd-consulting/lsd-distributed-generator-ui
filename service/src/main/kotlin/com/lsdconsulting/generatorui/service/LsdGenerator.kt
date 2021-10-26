package com.lsdconsulting.generatorui.service

import com.lsd.ParticipantType.*
import com.lsd.events.Message
import com.lsd.events.SequenceEvent
import com.lsd.events.SynchronousResponse
import com.lsd.report.model.Participant
import io.lsdconsulting.lsd.distributed.generator.diagram.InteractionGenerator
import org.springframework.stereotype.Service
import java.util.*

@Service
class LsdGenerator(
    private val interactionGenerator: InteractionGenerator,
    private val htmlReportRenderer: HtmlReportRenderer,
    private val scenarioBuilder: ScenarioBuilder
) {
    fun captureInteractionsFromDatabase(vararg traceIds: String): String {
        val traceIdToColourMap: HashMap<String, Optional<String>> = HashMap()
        traceIds.forEach { x: String -> traceIdToColourMap[x] = Optional.empty() }
        val events = interactionGenerator.generate(traceIdToColourMap)
        val participants = generateParticipants(events)
        val title = "Diagram for traceIds: ${traceIds.asList().joinToString()}"
        val scenario = scenarioBuilder.build(title = title, events = events, traceIds = traceIds.asList(), participants = participants.toList())
        return htmlReportRenderer.render(scenario)
    }

    private fun generateParticipants(events: MutableList<SequenceEvent>): LinkedHashSet<Participant> {
        val participantNames = mutableSetOf<String>()
        val participants = linkedSetOf<Participant>()
        events.forEach {
            if (it is Message && it !is SynchronousResponse) {
                when (it.label) {
                    "publish event" -> {
                        addToParticipants(participantNames, it.from, participants, PARTICIPANT.called(it.from))
                        addToParticipants(participantNames, it.to, participants, ENTITY.called(it.to))
                    }
                    "consume message" -> {
                        addToParticipants(participantNames, it.to, participants, PARTICIPANT.called(it.to))
                        addToParticipants(participantNames, it.from, participants, ENTITY.called(it.from))
                    }
                    else -> {
                        addToParticipants(participantNames, it.from, participants, if (participants.isEmpty()) ACTOR.called(it.from) else PARTICIPANT.called(it.from))
                        addToParticipants(participantNames, it.to, participants, PARTICIPANT.called(it.to))
                    }
                }
            }
        }
        return participants
    }

    private fun addToParticipants(
        participantNames: MutableSet<String>,
        participantName: String,
        participants: LinkedHashSet<Participant>,
        participant: Participant
    ) {
        if (!participantNames.contains(participantName)) {
            participants.add(participant)
            participantNames.add(participantName)
        }
    }
}
