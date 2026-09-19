package com.lsdconsulting.generatorui.config

import com.lsdconsulting.generatorui.config.objectmapper.ObjectMapperCreator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter
import tools.jackson.databind.json.JsonMapper

@Configuration
class ObjectMapperConfig {

    @Bean
    fun objectMapper(): JsonMapper = ObjectMapperCreator().create()

    @Bean
    fun converter(objectMapper: JsonMapper): JacksonJsonHttpMessageConverter {
        return JacksonJsonHttpMessageConverter(objectMapper)
    }
}
