package com.lsdconsulting.generatorui

import com.lsdconsulting.generatorui.config.RepositoryConfig
import com.lsdconsulting.generatorui.repository.TestRepository
import io.lsdconsulting.lsd.distributed.mongo.config.LibraryConfig
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT
import org.springframework.context.annotation.Import
import org.springframework.test.context.TestPropertySource

@SpringBootTest(webEnvironment = DEFINED_PORT, classes = [LsdGeneratorUiApplication::class])
@TestPropertySource("classpath:application-test.properties")
@Import(RepositoryConfig::class, LibraryConfig::class)
class ComponentTestBase {
    companion object {
        @BeforeAll
        @JvmStatic
        internal fun beforeAll() {
            TestRepository.setupDatabase()
        }

        @AfterAll
        @JvmStatic
        internal fun afterAll() {
            TestRepository.tearDownDatabase()
        }
    }
}
