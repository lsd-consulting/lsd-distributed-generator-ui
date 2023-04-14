package com.lsdconsulting.generatorui.service

import com.lsd.core.builders.MessageBuilder
import com.lsd.core.domain.*
import com.lsd.core.domain.MessageType.SYNCHRONOUS_RESPONSE
import com.lsd.core.domain.ParticipantType.*
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.hasElement
import com.natpryce.hamkrest.hasSize
import org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric
import org.junit.jupiter.api.Test

internal class ParticipantListGeneratorShould {

    private val underTest: ParticipantListGenerator = ParticipantListGenerator()

    @Test
    fun `generate participants from async messages`() {
        val publishMessage = MessageBuilder.messageBuilder()
            .from("Participant1")
            .to("Participant2")
            .label("Publish Event")
            .build()
        val consumeMessage = MessageBuilder.messageBuilder()
            .from("Participant2")
            .to("Participant3")
            .label("Consume Message")
            .build()
        val events = mutableListOf<SequenceEvent>(publishMessage, consumeMessage)

        val result = underTest.generateParticipants(events)

        assertThat(result, hasSize(equalTo(3)))
        assertThat(result, hasElement(PARTICIPANT.called(name = "Participant1", colour = "powderblue")))
        assertThat(result, hasElement(ENTITY.called(name = "Participant2", colour = "green")))
        assertThat(result, hasElement(PARTICIPANT.called(name = "Participant3", colour = "powderblue")))
    }

    @Test
    fun `generate an actor as first requester`() {
        val request = MessageBuilder.messageBuilder()
            .from("Participant1")
            .to("Participant2")
            .label("Message")
            .build()
        val response = MessageBuilder.messageBuilder()
            .from("Participant2")
            .to("Participant1")
            .label("Message")
            .type(SYNCHRONOUS_RESPONSE)
            .build()
        val events = mutableListOf<SequenceEvent>(request, response)

        val result = underTest.generateParticipants(events)

        assertThat(result, hasSize(equalTo(2)))
        assertThat(result, hasElement(ACTOR.called(name = "Participant1", colour = "orange")))
        assertThat(result, hasElement(PARTICIPANT.called(name = "Participant2", colour = "powderblue")))
    }

    @Test
    fun `generate participants from sync messages`() {
        val request1 = MessageBuilder.messageBuilder()
            .from("Participant1")
            .to("Participant2")
            .label(randomAlphanumeric(30))
            .build()
        val request2 = MessageBuilder.messageBuilder()
            .from("Participant2")
            .to("Participant3")
            .label(randomAlphanumeric(30))
            .build()
        val response1 = MessageBuilder.messageBuilder()
            .from("Participant3")
            .to("Participant2")
            .label(randomAlphanumeric(30))
            .type(SYNCHRONOUS_RESPONSE)
            .build()
        val response2 = MessageBuilder.messageBuilder()
            .from("Participant3")
            .to("Participant2")
            .label(randomAlphanumeric(30))
            .type(SYNCHRONOUS_RESPONSE)
            .build()
        val events = mutableListOf<SequenceEvent>(request1, request2, response1, response2)

        val result = underTest.generateParticipants(events)

        assertThat(result, hasSize(equalTo(3)))
        assertThat(result, hasElement(ACTOR.called(name = "Participant1", colour = "orange")))
        assertThat(result, hasElement(PARTICIPANT.called(name = "Participant2", colour = "powderblue")))
        assertThat(result, hasElement(PARTICIPANT.called(name = "Participant3", colour = "powderblue")))
    }

    @Test
    fun `ignore synchronous response messages`() {
        val request1 = MessageBuilder.messageBuilder()
            .from("Participant1")
            .to("Participant2")
            .label(randomAlphanumeric(30))
            .build()
        val request2 = MessageBuilder.messageBuilder()
            .from("Participant2")
            .to("Participant3")
            .label(randomAlphanumeric(30))
            .build()
        val response1 = MessageBuilder.messageBuilder()
            .from(randomAlphanumeric(10))
            .to(randomAlphanumeric(10))
            .label(randomAlphanumeric(30))
            .type(SYNCHRONOUS_RESPONSE)
            .build()
        val response2 = MessageBuilder.messageBuilder()
            .from(randomAlphanumeric(10))
            .to(randomAlphanumeric(10))
            .label(randomAlphanumeric(30))
            .type(SYNCHRONOUS_RESPONSE)
            .build()
        val events = mutableListOf<SequenceEvent>(request1, request2, response1, response2)

        val result = underTest.generateParticipants(events)

        assertThat(result, hasSize(equalTo(3)))
        assertThat(result, hasElement(ACTOR.called(name = "Participant1", colour = "orange")))
        assertThat(result, hasElement(PARTICIPANT.called(name = "Participant2", colour = "powderblue")))
        assertThat(result, hasElement(PARTICIPANT.called(name = "Participant3", colour = "powderblue")))
    }

    @Test
    fun `ignore non-message events`() {
        val request1 = MessageBuilder.messageBuilder()
            .from(ComponentName("Participant1"))
            .to(ComponentName("Participant2"))
            .label(randomAlphanumeric(30))
            .build()
        val request2 = MessageBuilder.messageBuilder()
            .from("Participant2")
            .to("Participant3")
            .label(randomAlphanumeric(30))
            .build()
        val noteRight = NoteRight(randomAlphanumeric(10))
        val noteLeft = NoteLeft(randomAlphanumeric(10))
        val pageTitle = PageTitle(randomAlphanumeric(10))
        val newpage = Newpage(PageTitle(randomAlphanumeric(10)))
        val events = mutableListOf(request1, request2, noteRight, noteLeft, pageTitle, newpage)

        val result = underTest.generateParticipants(events)

        assertThat(result, hasSize(equalTo(3)))
        assertThat(result, hasElement(ACTOR.called(name = "Participant1", colour = "orange")))
        assertThat(result, hasElement(PARTICIPANT.called(name = "Participant2", colour = "powderblue")))
        assertThat(result, hasElement(PARTICIPANT.called(name = "Participant3", colour = "powderblue")))
    }

    @Test
    fun `handle empty events`() {
        val events = mutableListOf<SequenceEvent>()

        val result = underTest.generateParticipants(events)

        assertThat(result, hasSize(equalTo(0)))
    }
}
