package com.newy.task.integration.helper

import com.newy.task.integration.confing.TestcontainersConfiguration
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@DataJpaTest
@Import(TestcontainersConfiguration::class)
@ComponentScan(basePackages = ["com.newy.task"])
annotation class DataJpaTestHelper