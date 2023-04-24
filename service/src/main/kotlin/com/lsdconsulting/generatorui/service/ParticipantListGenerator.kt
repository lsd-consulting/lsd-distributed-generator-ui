package com.lsdconsulting.generatorui.service

import com.lsd.core.domain.*
import com.lsd.core.domain.ParticipantType.*
import org.springframework.stereotype.Component

@Component
class ParticipantListGenerator {

    fun generateParticipants(events: MutableList<SequenceEvent>): Set<Participant> {
        return linkedSetOf<Participant>().also { participants ->
            events.forEach {
                if (it is Message && it.type != MessageType.SYNCHRONOUS_RESPONSE) {
                    val fromName = it.from.componentName.normalisedName
                    val toName = it.to.componentName.normalisedName
                    
                    when (it.label.lowercase()) {
                        "publish event" -> {
                            participants.add(PARTICIPANT.called(fromName, colour = "powderblue"))
                            participants.add(ENTITY.called(toName, colour = "green"))
                        }

                        "consume message" -> {
                            participants.add(PARTICIPANT.called(toName, colour = "powderblue"))
                            participants.add(ENTITY.called(fromName, colour = "green"))
                        }

                    else -> {
                            participants.add(
                                if (participants.isEmpty())
                                    ACTOR.called(fromName, colour = "orange")
                                else
                                    PARTICIPANT.called(fromName, colour = "powderblue")
                            )
                            participants.add(PARTICIPANT.called(toName, colour = "powderblue"))
                        }
                    }
                }
            }
        }
    }
}
