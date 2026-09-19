package com.lsdconsulting.generatorui.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

/**
 * Boot 4 no longer auto-exposes a Jackson 2 ObjectMapper (it uses Jackson 3).
 * The published postgres connector still injects com.fasterxml.jackson.databind.ObjectMapper;
 * provide one for component tests until that connector ships its own fallback.
 */
@TestConfiguration
open class Jackson2ObjectMapperTestConfig {
    @Bean
    @ConditionalOnMissingBean(ObjectMapper::class)
    open fun jackson2ObjectMapper(): ObjectMapper = ObjectMapper().findAndRegisterModules()
}
