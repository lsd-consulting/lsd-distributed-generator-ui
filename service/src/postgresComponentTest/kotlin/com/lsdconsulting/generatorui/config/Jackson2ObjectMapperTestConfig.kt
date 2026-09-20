package com.lsdconsulting.generatorui.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

/**
 * Boot 4 no longer auto-exposes a Jackson 2 ObjectMapper (it uses Jackson 3).
 * Postgres connector 4.0.57+ LibraryConfig registers its own ObjectMapper as
 * objectMapper when missing; this bean covers older connectors / ordering edge cases.
 * UI JsonMapper is registered as jsonMapper to avoid a same-name clash.
 */
@TestConfiguration
open class Jackson2ObjectMapperTestConfig {
    @Bean
    @ConditionalOnMissingBean(ObjectMapper::class)
    open fun objectMapper(): ObjectMapper = ObjectMapper().findAndRegisterModules()
}
