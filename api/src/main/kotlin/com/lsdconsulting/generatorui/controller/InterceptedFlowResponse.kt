package com.lsdconsulting.generatorui.controller

import java.time.ZonedDateTime

data class InterceptedFlowResponse(
    val traceId: String,
    val initialInteraction: String,
    val interactionType: String,
    val profile: String = "",
    val duration: Long,
    val initiatedAt: ZonedDateTime,
    val finishedAt: ZonedDateTime,
    val numberOfInteractions: Int,
)
