package com.lsdconsulting.generatorui.service

import com.lsd.core.domain.*
import com.lsd.core.domain.ParticipantType.*
import org.springframework.stereotype.Component

@Component
class ParticipantListGenerator {

    fun generateParticipants(events: MutableList<SequenceEvent>): Set<Participant> {
        val participantNames = mutableSetOf<String>()
        val participants = linkedSetOf<Participant>()
        events.forEach {
            if (it is Message && it.type != MessageType.SYNCHRONOUS_RESPONSE) {
                when (it.label.lowercase()) {
                    "publish event" -> {
                        addToParticipants(participantNames, it.from, participants, PARTICIPANT.called(it.from.name, "", "powderblue"))
                        addToParticipants(participantNames, it.to, participants, ENTITY.called(it.to.name, "", "green"))
                    }
                    "consume message" -> {
                        addToParticipants(participantNames, it.to, participants, PARTICIPANT.called(it.to.name, "", "powderblue"))
                        addToParticipants(participantNames, it.from, participants, ENTITY.called(it.from.name, "", "green"))
                    }
                    else -> {
                        addToParticipants(participantNames, it.from, participants, if (participants.isEmpty()) ACTOR.called(it.from.name, "", "orange") else PARTICIPANT.called(it.from.name, "", "powderblue"))
                        addToParticipants(participantNames, it.to, participants, PARTICIPANT.called(it.to.name, "", "powderblue"))
                    }
                }
            }
        }
        return participants
    }

    private fun addToParticipants(
        participantNames: MutableSet<String>,
        participantName: ComponentName,
        participants: LinkedHashSet<Participant>,
        participant: Participant
    ) {
        if (!participantNames.contains(participantName.name)) {
            participants.add(participant)
            participantNames.add(participantName.name)
        }
    }
}
