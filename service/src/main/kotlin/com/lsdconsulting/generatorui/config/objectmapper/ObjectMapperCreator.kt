package com.lsdconsulting.generatorui.config.objectmapper

import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.kotlinModule

/**
 * Builds a Jackson 3 [JsonMapper] for Spring Boot 4.
 * java.time support is built into Jackson 3 (no JavaTimeModule).
 */
class ObjectMapperCreator {
    fun create(): JsonMapper =
        JsonMapper.builder()
            .addModule(kotlinModule())
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build()
}
