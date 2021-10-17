package com.lsdconsulting.generatorui.config

import com.lsdconsulting.generatorui.repository.TestRepository
import com.lsdconsulting.generatorui.repository.TestRepository.Companion.setupDatabase
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
class RepositoryConfig {
    companion object {
        // This is because the configs in spring.factories run always before any test configs.
        init {
            setupDatabase()
        }
    }

    @Bean
    fun testRepository(): TestRepository {
        return TestRepository()
    }
}
