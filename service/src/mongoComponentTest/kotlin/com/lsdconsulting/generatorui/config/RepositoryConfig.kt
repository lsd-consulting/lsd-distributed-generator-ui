package com.lsdconsulting.generatorui.config

import com.lsdconsulting.generatorui.repository.TestRepository
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
class RepositoryConfig {
    @Bean
    fun testRepository(): TestRepository {
        return TestRepository()
    }
}
