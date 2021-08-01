package com.example.demo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.data.redis.RedisProperties
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisSentinelConfiguration
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.ListOperations
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.stream.Task
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import javax.annotation.Resource
import kotlin.system.measureTimeMillis

@SpringBootApplication
class DemoApplication

fun main(args: Array<String>) {
	runApplication<DemoApplication>(*args)
}

@Configuration
class RedisConfiguration {

	@Bean
	fun redisConnectionFactory(redisProperties: RedisProperties) =
		LettuceConnectionFactory(RedisStandaloneConfiguration(redisProperties.host, redisProperties.port))

	@Bean
	fun taskRepository(connectionFactory: LettuceConnectionFactory) =
		TaskRepository().also {
			it.setConnectionFactory(connectionFactory)
		}
}

class TaskRepository : RedisTemplate<Long, String>()

@Component
class Test(
	private val taskRepository: TaskRepository,
) {

	@EventListener(ApplicationReadyEvent::class)
	fun test() {
		measureTimeMillis {
			taskRepository.opsForList().leftPush(1, "1")
		}.also { elapsed ->
			println("push took $elapsed ms")
		}

		measureTimeMillis {
			taskRepository.opsForList().leftPush(2, "2")
		}.also { elapsed ->
			println("push took $elapsed ms")
		}

		val first = taskRepository.opsForList().leftPop(1)
		println(first)

		val second = taskRepository.opsForList().leftPop(2)
		println(second)

		val firstAgain = taskRepository.opsForList().leftPop(1)
		println(firstAgain)

		val third = taskRepository.opsForList().leftPop(3)
		println(third)
	}
}
