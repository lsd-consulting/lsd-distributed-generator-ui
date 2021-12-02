package com.lsdconsulting.generatorui.service

import com.lsd.events.Message
import com.lsd.events.SequenceEvent
import com.lsd.events.SynchronousResponse
import com.lsd.report.model.Participant
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
        val publishMessage = Message.builder()
            .from("Participant1")
            .to("Participant2")
            .label("Publish Event")
            .build()
        val consumeMessage = Message.builder()
            .from("Participant2")
            .to("Participant3")
            .label("Consume Message")
            .build()
        val events = mutableListOf<SequenceEvent>(publishMessage, consumeMessage)

        val result = underTest.generateParticipants(events)

        assertThat(result, hasSize(equalTo(3)))
        assertThat(result, hasElement(Participant("participant Participant1")))
        assertThat(result, hasElement(Participant("entity Participant2")))
        assertThat(result, hasElement(Participant("participant Participant3")))
    }

    @Test
    fun `generate an actor as first requester`() {
        val request = Message.builder()
            .from("Participant1")
            .to("Participant2")
            .label("Message")
            .build()
        val response = SynchronousResponse.builder()
            .from("Participant2")
            .to("Participant1")
            .label("Message")
            .build()
        val events = mutableListOf<SequenceEvent>(request, response)

        val result = underTest.generateParticipants(events)

        assertThat(result, hasSize(equalTo(2)))
        assertThat(result, hasElement(Participant("actor Participant1")))
        assertThat(result, hasElement(Participant("participant Participant2")))
    }

    @Test
    fun `generate participants from sync messages`() {
        val request1 = Message.builder()
            .from("Participant1")
            .to("Participant2")
            .label(randomAlphanumeric(30))
            .build()
        val request2 = Message.builder()
            .from("Participant2")
            .to("Participant3")
            .label(randomAlphanumeric(30))
            .build()
        val response1 = SynchronousResponse.builder()
            .from("Participant3")
            .to("Participant2")
            .label(randomAlphanumeric(30))
            .build()
        val response2 = SynchronousResponse.builder()
            .from("Participant3")
            .to("Participant2")
            .label(randomAlphanumeric(30))
            .build()
        val events = mutableListOf<SequenceEvent>(request1, request2, response1, response2)

        val result = underTest.generateParticipants(events)

        assertThat(result, hasSize(equalTo(3)))
        assertThat(result, hasElement(Participant("actor Participant1")))
        assertThat(result, hasElement(Participant("participant Participant2")))
        assertThat(result, hasElement(Participant("participant Participant3")))
    }
}