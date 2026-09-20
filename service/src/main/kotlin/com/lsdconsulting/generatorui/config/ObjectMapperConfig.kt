package com.lsdconsulting.generatorui.config

import com.lsdconsulting.generatorui.config.objectmapper.ObjectMapperCreator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter
import tools.jackson.databind.json.JsonMapper

@Configuration
class ObjectMapperConfig {

    /**
     * Named jsonMapper (not objectMapper) so Jackson 2 ObjectMapper beans from
     * connectors (e.g. postgres LibraryConfig) can register as objectMapper
     * without BeanDefinitionOverrideException under Boot 4.
     */
    @Bean
    fun jsonMapper(): JsonMapper = ObjectMapperCreator().create()

    @Bean
    fun converter(jsonMapper: JsonMapper): JacksonJsonHttpMessageConverter {
        return JacksonJsonHttpMessageConverter(jsonMapper)
    }
}
