package com.lsdconsulting.generatorui.controller

import com.lsdconsulting.generatorui.config.logger.log
import com.lsdconsulting.generatorui.service.LsdGenerator
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.ResponseBody

@Controller
class LsdController(val lsdGenerator: LsdGenerator) {

    @GetMapping("/lsd/{traceId}")
    @ResponseBody
    fun find(@PathVariable traceId: String): String {
        log().info("Received lsd request for traceId={}", traceId)
        return lsdGenerator.captureInteractionsFromDatabase(traceId)
    }
}
