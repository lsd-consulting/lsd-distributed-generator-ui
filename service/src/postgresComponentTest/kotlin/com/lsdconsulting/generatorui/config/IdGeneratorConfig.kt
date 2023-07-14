package com.lsdconsulting.generatorui.config

import com.lsd.core.IdGenerator
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
class IdGeneratorConfig {
    @Bean
    fun idGenerator() = IdGenerator(true)
}
