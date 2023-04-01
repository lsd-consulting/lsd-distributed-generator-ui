package com.lsdconsulting.generatorui.controller

import com.lsdconsulting.generatorui.config.logger.log
import com.lsdconsulting.generatorui.service.LsdGenerator
import io.lsdconsulting.stub.annotation.GenerateWireMockStub
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@GenerateWireMockStub
@RestController
class LsdController(private val lsdGenerator: LsdGenerator) {

    @GetMapping("/lsd/{traceId}")
    fun find(@PathVariable traceId: String): String {
        log().info("Received lsd request for traceId={}", traceId)
        return lsdGenerator.captureInteractionsFromDatabase(traceId)
    }
}
