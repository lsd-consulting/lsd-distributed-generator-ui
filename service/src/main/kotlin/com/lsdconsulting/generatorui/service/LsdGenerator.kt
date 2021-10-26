package com.lsdconsulting.generatorui.service

import com.lsd.ParticipantType
import com.lsd.ParticipantType.ACTOR
import com.lsd.ParticipantType.PARTICIPANT
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
        val participants = linkedSetOf<Participant>()
        events.forEach {
            if (it is Message && it !is SynchronousResponse) {
                when (it.label) {
                    "publish event" -> {
                        participants.add(PARTICIPANT.called(it.from))
                        participants.add(ParticipantType.ENTITY.called(it.to))
                    }
                    "consume message" -> {
                        participants.add(PARTICIPANT.called(it.to))
                        participants.add(ParticipantType.ENTITY.called(it.from))
                    }
                    else -> {
                        participants.add(if (participants.isEmpty()) ACTOR.called(it.from) else PARTICIPANT.called(it.from))
                        participants.add(PARTICIPANT.called(it.to))
                    }
                }
            }
        }
        return participants
    }
}
