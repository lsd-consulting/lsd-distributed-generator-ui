package com.lsdconsulting.generatorui.controller

import com.lsdconsulting.generatorui.config.logger.log
import com.lsdconsulting.generatorui.service.LsdGenerator
import com.lsdconsulting.generatorui.service.LsdSarvice
import io.lsdconsulting.lsd.distributed.access.model.InterceptedInteraction
import io.lsdconsulting.stub.annotation.GenerateWireMockStub
import org.springframework.http.MediaType.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@GenerateWireMockStub
@RestController
class LsdController(
    private val lsdGenerator: LsdGenerator,
    private val lsdSarvice: LsdSarvice
) {

    @GetMapping(value  = ["/lsd/{traceId}", "/lsds/{traceId}"], produces = [TEXT_HTML_VALUE])
    fun findByTraceId(@PathVariable traceId: String): String {
        log().info("Received lsd request for traceId={}", traceId)
        return lsdGenerator.captureInteractionsFromDatabase(traceId)
    }

    @GetMapping(value  = ["/lsds"], produces = [APPLICATION_JSON_VALUE])
    fun findByTraceIds(@RequestParam vararg traceIds:String): ResponseEntity<List<InterceptedInteraction>> {
        log().info("Received lsd request for traceIds={}", traceIds)
        return ResponseEntity.ok(lsdSarvice.findInteractionsByTraceIds(*traceIds))
    }

    @PostMapping("/lsds")
    fun store(@RequestBody interceptedInteraction: InterceptedInteraction): ResponseEntity<InterceptedInteraction> {
        log().info("Received interceptedInteraction={}", interceptedInteraction)
        lsdSarvice.storeInteractionsInDatabase(interceptedInteraction)
        return ResponseEntity.ok(interceptedInteraction)
    }
}
