package com.lsdconsulting.generatorui.service

import io.lsdconsulting.lsd.distributed.access.model.InterceptedInteraction
import io.lsdconsulting.lsd.distributed.access.repository.InterceptedDocumentRepository
import org.springframework.stereotype.Service

@Service
class LsdService(
    private val interceptedDocumentRepository: InterceptedDocumentRepository
) {
    fun storeInteractionsInDatabase(interceptedInteraction: InterceptedInteraction) {
        interceptedDocumentRepository.save(interceptedInteraction)
    }

    fun findInteractionsByTraceIds(vararg traceIds: String): List<InterceptedInteraction> =
        interceptedDocumentRepository.findByTraceIds(*traceIds)
}
