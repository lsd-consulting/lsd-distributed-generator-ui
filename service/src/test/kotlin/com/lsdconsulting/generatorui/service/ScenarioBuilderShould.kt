package com.lsdconsulting.generatorui.service

import com.lsd.core.IdGenerator
import com.lsd.core.abbreviate
import com.lsd.core.builders.MessageBuilder
import com.lsd.core.domain.Fact
import com.lsd.core.domain.NoteLeft
import com.lsd.core.domain.NoteRight
import com.lsd.core.report.model.DataHolder
import com.natpryce.hamkrest.*
import com.natpryce.hamkrest.assertion.assertThat
import io.mockk.every
import io.mockk.mockk
import org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class ScenarioBuilderShould {

    private val idGenerator = mockk<IdGenerator>()

    private val underTest = ScenarioBuilder(idGenerator)

    private val id = randomAlphanumeric(30)
    private val title = randomAlphanumeric(30)
    private val traceId = randomAlphanumeric(6)

    @BeforeEach
    fun setup() {
        every { idGenerator.next() } returns "generatedId"
    }

    @Test
    fun `generate scenario with a id`() {
        val result = underTest.build(title, mutableListOf(), listOf(), listOf())

        assertThat(result.id, equalTo("generatedId"))
    }

    @Test
    fun `generate scenario with a title`() {
        val result = underTest.build(title, mutableListOf(), listOf(), listOf())

        assertThat(result.title, equalTo(title))
    }

    @Test
    fun `generate scenario with traceIds as facts`() {
        val result = underTest.build(
            title = title,
            events = mutableListOf(),
            traceIds = listOf(traceId),
            participants = listOf()
        )

        assertThat(result.facts, hasSize(equalTo(1)))
        assertThat(result.facts, hasElement(Fact(key = "traceIds", value = traceId)))
    }

    @Test
    fun `generate scenario with dataHolders only`() {
        val message = MessageBuilder.messageBuilder()
            .id("generatedId")
            .from("Participant1")
            .to("Participant2")
            .label("Message")
            .build()

        val noteRight = NoteRight("markup")
        val noteLeft = NoteLeft("noteLeft")

        val result = underTest.build(
            title = title,
            events = mutableListOf(noteRight, message, noteLeft),
            traceIds = listOf(),
            participants = listOf()
        )

        assertThat(result.dataHolders, hasSize(equalTo(1)))
        assertThat(result.dataHolders, hasElement(DataHolder(message.id, message.label.abbreviate(), message.data)))
    }

    @Test
    fun `generate scenario with a sequence diagram with the given events`() {
        val request = MessageBuilder.messageBuilder()
            .id(id)
            .from("Participant1")
            .to("Participant2")
            .label("Message")
            .build()

        val markup = NoteRight("markup")
        val noteLeft = NoteLeft("noteLeft")

        val result = underTest.build(title, mutableListOf(markup, request, noteLeft), listOf(), listOf())

        assertThat(result.sequenceDiagram!!.id, equalTo("generatedId"))
        assertThat(
            result.sequenceDiagram!!.uml, allOf(
                containsSubstring("@startuml"),
                containsSubstring(id),
                containsSubstring("Participant1"),
                containsSubstring("Participant2"),
                containsSubstring("Message"),
                containsSubstring("markup"),
                containsSubstring("noteLeft"),
                containsSubstring("@enduml")
            )
        )
        assertThat(
            result.sequenceDiagram!!.svg, allOf(
                containsSubstring("<svg"),
                containsSubstring("</svg>")
            )
        )
    }

    @Test
    fun `generate scenario with a component diagram with the given events but no ma`() {
        val request = MessageBuilder.messageBuilder()
            .id(id)
            .from("Participant1")
            .to("Participant2")
            .label("Message")
            .build()

        val markup = NoteRight("markup")
        val noteLeft = NoteLeft("noteLeft")

        val result = underTest.build(title, mutableListOf(markup, request, noteLeft), listOf(), listOf())

        assertThat(result.componentDiagram!!.id, equalTo("generatedId"))
        assertThat(
            result.componentDiagram!!.uml, allOf(
                containsSubstring("@startuml"),
                containsSubstring("Participant1"),
                containsSubstring("Participant2"),
                containsSubstring("@enduml")
            )
        )
        assertThat(
            result.componentDiagram!!.svg, allOf(
                containsSubstring("<svg"),
                containsSubstring("</svg>")
            )
        )
    }
}
