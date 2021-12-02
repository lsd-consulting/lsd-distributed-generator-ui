package com.lsdconsulting.generatorui.service

import com.lsd.ParticipantType
import com.lsd.events.Message
import com.lsd.events.SequenceEvent
import com.lsd.events.SynchronousResponse
import com.lsd.report.model.Participant
import org.springframework.stereotype.Component

@Component
class ParticipantListGenerator {

    fun generateParticipants(events: MutableList<SequenceEvent>): LinkedHashSet<Participant> {
        val participantNames = mutableSetOf<String>()
        val participants = linkedSetOf<Participant>()
        events.forEach {
            if (it is Message && it !is SynchronousResponse) {
                when (it.label.lowercase()) {
                    "publish event" -> {
                        addToParticipants(participantNames, it.from, participants, ParticipantType.PARTICIPANT.called(it.from))
                        addToParticipants(participantNames, it.to, participants, ParticipantType.ENTITY.called(it.to))
                    }
                    "consume message" -> {
                        addToParticipants(participantNames, it.to, participants, ParticipantType.PARTICIPANT.called(it.to))
                        addToParticipants(participantNames, it.from, participants, ParticipantType.ENTITY.called(it.from))
                    }
                    else -> {
                        addToParticipants(participantNames, it.from, participants, if (participants.isEmpty()) ParticipantType.ACTOR.called(it.from) else ParticipantType.PARTICIPANT.called(it.from))
                        addToParticipants(participantNames, it.to, participants, ParticipantType.PARTICIPANT.called(it.to))
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