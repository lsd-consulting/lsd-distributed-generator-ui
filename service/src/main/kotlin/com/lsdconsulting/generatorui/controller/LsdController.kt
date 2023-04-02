package com.lsdconsulting.generatorui.controller

import com.lsdconsulting.generatorui.config.logger.log
import com.lsdconsulting.generatorui.service.LsdGenerator
import com.lsdconsulting.generatorui.service.LsdSaver
import io.lsdconsulting.lsd.distributed.access.model.InterceptedInteraction
import io.lsdconsulting.stub.annotation.GenerateWireMockStub
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@GenerateWireMockStub
@RestController
class LsdController(
    private val lsdGenerator: LsdGenerator,
    private val lsdSaver: LsdSaver
) {

    @GetMapping("/lsd/{traceId}")
    fun find(@PathVariable traceId: String): String {
        log().info("Received lsd request for traceId={}", traceId)
        return lsdGenerator.captureInteractionsFromDatabase(traceId)
    }

    @PostMapping("/lsd")
    fun store(@RequestBody interceptedInteraction: InterceptedInteraction): ResponseEntity<InterceptedInteraction> {
        log().info("Received interceptedInteraction={}", interceptedInteraction)
        lsdSaver.storeInteractionsInDatabase(interceptedInteraction)
        return ResponseEntity.ok(interceptedInteraction)
    }
}
