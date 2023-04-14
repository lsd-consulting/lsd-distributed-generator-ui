package com.lsdconsulting.generatorui.service

import com.lsd.core.IdGenerator
import com.lsd.core.abbreviate
import com.lsd.core.builders.ScenarioModelBuilder.Companion.scenarioModelBuilder
import com.lsd.core.builders.SequenceDiagramGeneratorBuilder
import com.lsd.core.diagram.ComponentDiagramGenerator
import com.lsd.core.domain.Fact
import com.lsd.core.domain.Message
import com.lsd.core.report.HtmlReportRenderer
import com.lsd.core.report.model.DataHolder
import com.lsd.core.report.model.Report
import io.lsdconsulting.lsd.distributed.generator.diagram.InteractionGenerator
import io.lsdconsulting.lsd.distributed.generator.diagram.dto.EventContainer
import org.springframework.stereotype.Service
import java.util.*

private val htmlReportRenderer = HtmlReportRenderer()

@Service
class LsdGenerator(
    private val interactionGenerator: InteractionGenerator,
    private val participantListGenerator: ParticipantListGenerator,
    private val idGenerator: IdGenerator,

    ) {
    fun captureInteractionsFromDatabase(vararg traceIds: String): String {
        val traceIdToColourMap: HashMap<String, Optional<String>> = HashMap()
        traceIds.forEach { x: String -> traceIdToColourMap[x] = Optional.empty() }
        val eventContainer = interactionGenerator.generate(traceIdToColourMap)
        val participants = participantListGenerator.generateParticipants(eventContainer.events)

        return htmlReportRenderer.render(
            Report(
                title = "Event Report",
                showContentsMenu = false,
                useLocalStaticFiles = false,
                scenarios = listOf(scenarioModelBuilder()
                    .id(idGenerator.next())
                    .title("Diagram for traceIds")
                    .status("success")
                    .facts(compileFacts(traceIds, eventContainer))
                    .dataHolders(
                        eventContainer.events
                            .filterIsInstance<Message>()
                            .map {
                                DataHolder(
                                    id = it.id,
                                    abbreviatedLabel = it.label.abbreviate(),
                                    data = it.data
                                )
                            }
                    )
                    .sequenceDiagram(
                        SequenceDiagramGeneratorBuilder.sequenceDiagramGeneratorBuilder()
                            .idGenerator(idGenerator)
                            .events(eventContainer.events)
                            .participants(participants.toList())
                            .build()
                            .diagram(300)
                    )
                    .componentDiagram(
                        ComponentDiagramGenerator(
                            idGenerator = idGenerator,
                            events = eventContainer.events,
                            participants = participants.toList()
                        ).diagram()
                    )
                    .build())))
    }

    private fun compileFacts(traceIds: Array<out String>, eventContainer: EventContainer): MutableList<Fact> {
        val facts = mutableListOf(
            Fact("traceIds", traceIds.joinToString()),
        )
        eventContainer.startTime?.let {
            facts.add(Fact("startTime", eventContainer.startTime.toString()))
        }
        eventContainer.finishTime?.let {
            facts.add(Fact("finishTime", eventContainer.finishTime.toString()))
        }
        return facts
    }
}
