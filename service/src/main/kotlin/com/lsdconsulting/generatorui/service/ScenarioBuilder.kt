package com.lsdconsulting.generatorui.service

import com.lsd.IdGenerator
import com.lsd.diagram.ComponentDiagramGenerator
import com.lsd.diagram.SequenceDiagramGenerator
import com.lsd.events.SequenceEvent
import com.lsd.report.model.DataHolder
import com.lsd.report.model.Participant
import com.lsd.report.model.Scenario
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap
import org.springframework.stereotype.Component
import java.util.stream.Collectors

@Component
class ScenarioBuilder(
    private val idGenerator: IdGenerator
) {

    fun build(title: String, events: MutableList<SequenceEvent>, traceIds: List<String>, participants: List<Participant>): Scenario {

        val facts = ArrayListValuedHashMap<String?, String?>()
        facts.putAll("traceIds", traceIds)

        return Scenario.builder()
            .id(idGenerator.next())
            .title(title)
            .facts(facts)
            .dataHolders(
                events.stream()
                    .filter { obj: SequenceEvent? ->
                        DataHolder::class.java.isInstance(
                            obj
                        )
                    }
                    .map { obj: SequenceEvent? ->
                        DataHolder::class.java.cast(
                            obj
                        )
                    }
                    .collect(Collectors.toList())
            )
            .sequenceDiagram(
                SequenceDiagramGenerator.builder()
                    .idGenerator(idGenerator)
                    .events(events)
                    .participants(participants)
                    .includes(setOf())
                    .build()
                    .diagram(20)
                    .orElse(null)
            )
            .componentDiagram(
                ComponentDiagramGenerator.builder()
                    .idGenerator(idGenerator)
                    .events(events)
                    .participants(participants)
                    .build()
                    .diagram()
                    .orElse(null)
            )
            .build()
    }
}
