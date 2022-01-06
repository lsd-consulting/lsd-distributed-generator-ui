package com.lsdconsulting.generatorui.service

import io.lsdconsulting.lsd.distributed.generator.diagram.InteractionGenerator
import org.springframework.stereotype.Service
import java.util.*

@Service
class LsdGenerator(
    private val interactionGenerator: InteractionGenerator,
    private val htmlReportRenderer: HtmlReportRenderer,
    private val scenarioBuilder: ScenarioBuilder,
    private val participantListGenerator: ParticipantListGenerator
) {
    fun captureInteractionsFromDatabase(vararg traceIds: String): String {
        val traceIdToColourMap: HashMap<String, Optional<String>> = HashMap()
        traceIds.forEach { x: String -> traceIdToColourMap[x] = Optional.empty() }
        val eventContainer = interactionGenerator.generate(traceIdToColourMap)
        val participants = participantListGenerator.generateParticipants(eventContainer.events)
        val title = "Diagram for traceIds: ${traceIds.asList().joinToString()}"
        val scenario = scenarioBuilder.build(title = title, events = eventContainer.events, traceIds = traceIds.asList(), participants = participants.toList())
        return htmlReportRenderer.render(scenario = scenario, startTime = eventContainer.startTime, finishTime = eventContainer.finishTime)
    }
}
