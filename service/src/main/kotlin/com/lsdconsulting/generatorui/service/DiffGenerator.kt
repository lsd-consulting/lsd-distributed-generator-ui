package com.lsdconsulting.generatorui.service

import com.lsd.core.builders.SequenceDiagramGeneratorBuilder
import io.lsdconsulting.lsd.distributed.generator.diagram.InteractionGenerator
import org.springframework.stereotype.Service

private val noColour: String? = null

@Service
class DiffGenerator(
    private val interactionGenerator: InteractionGenerator,
) {

    fun sourceDiff(traceId1: String, traceId2: String): String {
        val traceId1Events = interactionGenerator.generate(mapOf(traceId1 to noColour)).events
        val traceId2Events = interactionGenerator.generate(mapOf(traceId2 to noColour)).events
        val traceId1SourceUml = SequenceDiagramGeneratorBuilder.sequenceDiagramGeneratorBuilder()
            .events(traceId1Events)
            .build().diagram(500, false)?.uml
        val traceId2SourceUml = SequenceDiagramGeneratorBuilder.sequenceDiagramGeneratorBuilder()
            .events(traceId2Events)
            .build().diagram(500, false)?.uml

        return """
            --- $traceId1
            +++ $traceId2
            @@ -1 +1 @@
            
        """.trimIndent() +
        gitDiff(traceId1SourceUml!!, traceId2SourceUml!!)
    }
}

fun gitDiff(a: String, b: String): String {
    // Split the strings into lines.
    val linesA = listOf(*a.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
    val linesB = listOf(*b.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())

    // Get the difference between the two lists.
    val diff = diff(linesA, linesB)

    // Generate the git diff output.
    var gitDiff = ""
    diff.forEach { line ->
        gitDiff += line + "\n"
    }
    return gitDiff
}

private fun diff(a: List<String>, b: List<String>): List<String> {
    // Create a new list to store the differences.
    val diff: MutableList<String> = ArrayList()

    // Iterate over the two lists and compare each line.
    run {
        var i = 0
        while (i < a.size && i < b.size) {
            val lineA = a[i]
            val lineB = b[i]

            // If the two lines are different, add the difference to the diff list.
            if (lineA != lineB) {
                diff.add("-$lineA")
                diff.add("+$lineB")
            } else {
                diff.add(" $lineA")
            }
            i++
        }
    }

    // If either list is longer than the other, add the remaining lines to the diff list.
    for (i in a.size until b.size) {
        diff.add("+${b[i]}")
    }
    return diff
}
