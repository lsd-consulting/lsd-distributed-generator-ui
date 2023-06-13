package com.lsdconsulting.generatorui.controller

import com.lsdconsulting.generatorui.config.logger.log
import com.lsdconsulting.generatorui.service.LsdGenerator
import com.lsdconsulting.generatorui.service.LsdService
import io.lsdconsulting.lsd.distributed.connector.model.InterceptedInteraction
import io.lsdconsulting.stub.annotation.GenerateWireMockStub
import org.springframework.http.MediaType.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@GenerateWireMockStub
@RestController
class LsdController(
    private val lsdGenerator: LsdGenerator,
    private val lsdService: LsdService
) {

    @GetMapping(value  = ["/lsd/{traceId}", "/lsds/{traceId}"], produces = [TEXT_HTML_VALUE])
    fun findByTraceId(@PathVariable traceId: String): String {
        log().info("Received lsd request for traceId={}", traceId)
        return lsdGenerator.captureInteractionsFromDatabase(traceId)
    }

    @GetMapping(value  = ["/lsds"], produces = [APPLICATION_JSON_VALUE])
    fun findByTraceIds(@RequestParam vararg traceIds:String): ResponseEntity<List<InterceptedInteraction>> {
        log().info("Received lsd request for traceIds={}", traceIds)
        return ResponseEntity.ok(lsdService.findInteractionsByTraceIds(*traceIds))
    }

    @PostMapping("/lsds")
    fun store(@RequestBody interceptedInteraction: InterceptedInteraction): ResponseEntity<InterceptedInteraction> {
        log().info("Received interceptedInteraction={}", interceptedInteraction)
        lsdService.storeInteractionsInDatabase(interceptedInteraction)
        return ResponseEntity.ok(interceptedInteraction)
    }

    @GetMapping(value  = ["/lsdFlows"], produces = [APPLICATION_JSON_VALUE])
    fun findRecentFlows(@RequestParam(required = false) resultSizeLimit: Int?): ResponseEntity<List<InterceptedFlowResponse>> {
        log().info("Received lsd request for recent flows")
        return ResponseEntity.ok(lsdService.findMostRecentFlows(resultSizeLimit))
    }
}
