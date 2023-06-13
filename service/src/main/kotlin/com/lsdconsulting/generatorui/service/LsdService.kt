package com.lsdconsulting.generatorui.service

import com.lsdconsulting.generatorui.controller.InterceptedFlowResponse
import io.lsdconsulting.lsd.distributed.connector.model.InterceptedInteraction
import io.lsdconsulting.lsd.distributed.connector.repository.InterceptedDocumentAdminRepository
import io.lsdconsulting.lsd.distributed.connector.repository.InterceptedDocumentRepository
import org.springframework.stereotype.Service
import java.time.temporal.ChronoUnit

@Service
class LsdService(
    private val interceptedDocumentRepository: InterceptedDocumentRepository,
    private val interceptedDocumentAdminRepository: InterceptedDocumentAdminRepository
) {
    fun storeInteractionsInDatabase(interceptedInteraction: InterceptedInteraction) {
        interceptedDocumentRepository.save(interceptedInteraction)
    }

    fun findInteractionsByTraceIds(vararg traceIds: String): List<InterceptedInteraction> =
        interceptedDocumentRepository.findByTraceIds(*traceIds)

    fun findMostRecentFlows(resultSizeLimit: Int?): List<InterceptedFlowResponse> =
        interceptedDocumentAdminRepository.findRecentFlows(resultSizeLimit?:10)
            .map {
                InterceptedFlowResponse(
                    traceId = it.initialInteraction.traceId,
                    initialInteraction = it.initialInteraction.serviceName + " -> " + it.initialInteraction.target,
                    interactionType = it.initialInteraction.interactionType.name,
                    profile = it.initialInteraction.profile ?: "",
                    duration = ChronoUnit.MILLIS.between(
                        it.initialInteraction.createdAt,
                        it.finalInteraction.createdAt
                    ),
                    initiatedAt = it.initialInteraction.createdAt,
                    finishedAt = it.finalInteraction.createdAt,
                    numberOfInteractions = it.totalCapturedInteractions,
                )
            }
}
