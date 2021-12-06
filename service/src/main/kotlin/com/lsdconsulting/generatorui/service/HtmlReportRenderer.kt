package com.lsdconsulting.generatorui.service

import com.lsd.report.model.Scenario
import com.lsd.report.pebble.LsdPebbleExtension
import com.mitchellbosecke.pebble.PebbleEngine
import com.mitchellbosecke.pebble.template.PebbleTemplate
import org.springframework.stereotype.Component
import java.io.StringWriter
import java.io.Writer

@Component
class HtmlReportRenderer {
    private val engine: PebbleEngine = PebbleEngine.Builder()
        .extension(LsdPebbleExtension())
        .build()
    private val compiledTemplate: PebbleTemplate = engine.getTemplate("templates/custom-html-report.peb")

    fun render(scenario: Scenario): String {
        val writer: Writer = StringWriter()
        compiledTemplate.evaluate(writer, mapOf("scenario" to scenario))
        return writer.toString()
    }
}
