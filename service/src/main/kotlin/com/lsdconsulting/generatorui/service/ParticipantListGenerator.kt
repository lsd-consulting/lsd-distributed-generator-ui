package com.lsdconsulting.generatorui.service

import com.lsd.core.domain.Message
import com.lsd.core.domain.MessageType
import com.lsd.core.domain.Participant
import com.lsd.core.domain.ParticipantType.*
import com.lsd.core.domain.SequenceEvent
import org.springframework.stereotype.Component

@Component
class ParticipantListGenerator {

    fun generateParticipants(events: MutableList<SequenceEvent>): Set<Participant> {
        return linkedSetOf<Participant>().also { participants ->
            events.forEach {
                if (it is Message && it.type != MessageType.SYNCHRONOUS_RESPONSE) {
                    when (it.label.lowercase()) {
                        "publish event" -> {
                            participants.add(PARTICIPANT.called(it.from.name, colour = "powderblue"))
                            participants.add(ENTITY.called(it.to.name, colour = "green"))
                        }

                        "consume message" -> {
                            participants.add(PARTICIPANT.called(it.to.name, colour = "powderblue"))
                            participants.add(ENTITY.called(it.from.name, colour = "green"))
                        }

                        else -> {
                            participants.add(
                                if (participants.isEmpty())
                                    ACTOR.called(it.from.name, colour = "orange")
                                else
                                    PARTICIPANT.called(it.from.name, colour = "powderblue")
                            )
                            participants.add(PARTICIPANT.called(it.to.name, colour = "powderblue"))
                        }
                    }
                }
            }
        }
    }
}
