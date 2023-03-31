package com.lsdconsulting.generatorui.service

import com.lsd.core.IdGenerator
import com.lsd.core.abbreviate
import com.lsd.core.builders.ScenarioModelBuilder.Companion.scenarioModelBuilder
import com.lsd.core.builders.SequenceDiagramGeneratorBuilder.Companion.sequenceDiagramGeneratorBuilder
import com.lsd.core.diagram.ComponentDiagramGenerator
import com.lsd.core.domain.Fact
import com.lsd.core.domain.Message
import com.lsd.core.domain.Participant
import com.lsd.core.domain.SequenceEvent
import com.lsd.core.report.model.DataHolder
import com.lsd.core.report.model.ScenarioModel
import org.springframework.stereotype.Component

@Component
class ScenarioBuilder(
    private val idGenerator: IdGenerator
) {

    fun build(
        title: String,
        events: MutableList<SequenceEvent>,
        traceIds: List<String>,
        participants: List<Participant>
    ): ScenarioModel {

        return scenarioModelBuilder()
            .id(idGenerator.next())
            .title(title)
            .facts(listOf(Fact("traceIds", traceIds.joinToString())))
            .dataHolders(
                events
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
                sequenceDiagramGeneratorBuilder()
                    .idGenerator(idGenerator)
                    .events(events)
                    .participants(participants)
                    .build()
                    .diagram(300)
            )
            .componentDiagram(
                ComponentDiagramGenerator(
                    idGenerator = idGenerator,
                    events = events,
                    participants = participants
                ).diagram()
            )
            .build()
    }
}
