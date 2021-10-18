package com.lsdconsulting.generatorui.service

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
        val title = "Diagram for traceIds: ${traceIds.asList().joinToString()}"
        val scenario = scenarioBuilder.build(title = title, events = events, traceIds = traceIds.asList(), participants = participants)
        return htmlReportRenderer.render(scenario)
    }

    // TODO Is it required?
    private val participants = listOf<Participant>()
}