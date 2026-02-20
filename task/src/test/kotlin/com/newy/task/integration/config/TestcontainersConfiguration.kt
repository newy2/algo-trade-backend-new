package com.newy.task.integration.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.testcontainers.DockerClientFactory
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.images.builder.ImageFromDockerfile
import org.testcontainers.mysql.MySQLContainer
import org.testcontainers.postgresql.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import java.nio.file.Paths
import java.util.concurrent.CompletableFuture

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    @Profile("postgresql")
    fun postgresContainer(): PostgreSQLContainer {
        val customImageName = "local-task-infra-postgresql:latest"
        val compatibleName = DockerImageName.parse(customImageName).asCompatibleSubstituteFor("postgres")

        val hasCustomImage = try {
            DockerClientFactory.instance().client().inspectImageCmd(customImageName).exec()
            true
        } catch (_: Exception) {
            false
        }

        return if (hasCustomImage) {
            PostgreSQLContainer(compatibleName)
        } else {
            PostgreSQLContainer(compatibleName).apply {
                val imageDefinition = ImageFromDockerfile(customImageName, false)
                    .withDockerfile(Paths.get("..", "local-infra", "Dockerfile"))
                setImage(CompletableFuture.completedFuture(imageDefinition.get()))
            }
        }.apply {
            setWaitStrategy(Wait.forListeningPort())
            withEnv("POSTGRES_INITDB_ARGS", "--encoding=UTF8")
        }
    }

    @Bean
    @ServiceConnection
    @Profile("mysql")
    fun mysqlContainer(): MySQLContainer {
        return MySQLContainer("mysql:8").apply {
            withCommand(
                "mysqld",
                "--character-set-server=utf8mb4",
                "--innodb_ft_min_token_size=1",
                "--ft_min_word_len=1",
                "--ngram_token_size=1",
            )
        }
    }
}