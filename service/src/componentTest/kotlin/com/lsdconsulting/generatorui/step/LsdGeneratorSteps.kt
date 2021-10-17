package com.lsdconsulting.generatorui.step

import com.lsdconsulting.generatorui.LsdGeneratorUiApplication
import com.lsdconsulting.generatorui.config.RepositoryConfig
import com.lsdconsulting.generatorui.repository.TestRepository
import io.cucumber.java.en.Then
import io.cucumber.java8.En
import io.cucumber.java8.HookNoArgsBody
import io.cucumber.spring.CucumberContextConfiguration
import io.lsdconsulting.lsd.distributed.access.model.InterceptedInteraction
import io.lsdconsulting.lsd.distributed.access.model.Type
import io.lsdconsulting.lsd.distributed.access.repository.InterceptedDocumentRepository
//import io.lsdconsulting.lsd.distributed.captor.repository.InterceptedDocumentRepository
//import io.lsdconsulting.lsd.distributed.captor.repository.model.InterceptedInteraction
//import io.lsdconsulting.lsd.distributed.captor.repository.model.Type
import org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric
import org.apache.commons.lang3.RandomUtils.nextLong
//import org.approvaltests.Approvals
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.getForEntity
import org.springframework.context.annotation.Import
import org.springframework.http.ResponseEntity
import org.springframework.test.context.TestPropertySource
import java.time.ZoneId
import java.time.ZonedDateTime

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = DEFINED_PORT, classes = [LsdGeneratorUiApplication::class])
@TestPropertySource("classpath:application-test.properties")
@Import(RepositoryConfig::class)
class LsdGeneratorSteps(
    private val testRestTemplate: TestRestTemplate,
    private val interceptedDocumentRepository: InterceptedDocumentRepository
) : En {

    val traceId: String = randomAlphanumeric(30)
    val body: String = randomAlphanumeric(30)
    val requestHeaders: Map<String, Collection<String>> = mapOf("header" to listOf("value"))
    val responseHeaders: Map<String, Collection<String>> = mapOf("header" to listOf("value"))
//    val serviceName: String = randomAlphanumeric(30)
//    val target: String = randomAlphanumeric(30)
//    val path: String = randomAlphanumeric(30)
//    val httpStatus: String = "200"
    val httpMethod: String = "GET"
//    val type: Type = Type.REQUEST
    val profile: String = "TEST"
    val elapsedTime: Long = nextLong()
    val createdAt: ZonedDateTime = ZonedDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC"))

    lateinit var entity: ResponseEntity<String>

    init {

        testRestTemplate.restTemplate.interceptors.clear()

        Given("existing captured interactions in the database") {
            val i1 = InterceptedInteraction.builder()
                .traceId(traceId)
                .body(body)
                .requestHeaders(requestHeaders)
                .responseHeaders(responseHeaders)
                .serviceName("Source")
                .target("Target")
                .path("/path")
                .httpMethod(httpMethod)
                .type(Type.REQUEST)
                .profile(profile)
                .elapsedTime(elapsedTime)
                .createdAt(createdAt)
                .build()
            val i2 = InterceptedInteraction.builder()
                .traceId(traceId)
                .body(body)
                .requestHeaders(requestHeaders)
                .responseHeaders(responseHeaders)
                .serviceName("Source")
                .target("Target")
                .path("/path")
                .httpStatus("200")
                .httpMethod(httpMethod)
                .type(Type.RESPONSE)
                .profile(profile)
                .elapsedTime(elapsedTime)
                .createdAt(createdAt)
                .build()
            interceptedDocumentRepository.save(i1)
            interceptedDocumentRepository.save(i2)
        }

        When("an lsd request is received") {
            entity = testRestTemplate.getForEntity("/lsd/$traceId")
        }

//        Then("the correct diagram is generated") {
//        }

        After(HookNoArgsBody { TestRepository.tearDownDatabase() })
    }

    @Then("the correct diagram is generated")
    fun ddd() {
        println(entity.body)
//        Approvals.verify(entity.body)
    }
}
