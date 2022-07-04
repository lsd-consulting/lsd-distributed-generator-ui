package com.lsdconsulting.generatorui.service

import com.lsd.report.model.Scenario
import com.mitchellbosecke.pebble.PebbleEngine
import com.mitchellbosecke.pebble.error.PebbleException
import com.mitchellbosecke.pebble.extension.AbstractExtension
import com.mitchellbosecke.pebble.extension.Filter
import com.mitchellbosecke.pebble.template.EvaluationContext
import com.mitchellbosecke.pebble.template.PebbleTemplate
import org.springframework.stereotype.Component
import java.io.StringWriter
import java.io.Writer
import java.time.ZonedDateTime

@Component
class HtmlReportRenderer {
    private val engine: PebbleEngine = PebbleEngine.Builder()
        .extension(LsdPebbleExtension())
        .build()
    private val compiledTemplate: PebbleTemplate = engine.getTemplate("templates/custom-html-report.peb")

    fun render(scenario: Scenario, startTime: ZonedDateTime?, finishTime: ZonedDateTime?): String {
        val writer: Writer = StringWriter()
        compiledTemplate.evaluate(writer, mapOf("scenario" to scenario, "startTime" to startTime, "finishTime" to finishTime))
        return writer.toString()
    }

}
class LsdPebbleExtension : AbstractExtension() {
    override fun getFilters(): Map<String, Filter> {
        return mapOf(
            "sanitise" to SanitiserFilter()
        )
    }
}

class SanitiserFilter : Filter {
    @Throws(PebbleException::class)
    override fun apply(
        input: Any,
        args: Map<String, Any>,
        self: PebbleTemplate,
        context: EvaluationContext,
        lineNumber: Int
    ): Any {
        return if (input is String) 
            sanitise(input) else input
    }

    override fun getArgumentNames(): List<String>? = null

    private fun sanitise(input: String?): String =
        input?.strip()
            ?.replace("<\\$.*?>".toRegex(), "")
            ?: ""
}
