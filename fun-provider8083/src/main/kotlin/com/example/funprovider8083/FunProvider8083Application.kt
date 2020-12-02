package com.example.funprovider8083

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class FunProvider8083Application {

	@Value("\${server.port}")
	var serverPort: String? = "Unknown";

	@Value("\${spring.application.name}")
	var serverName: String? = "Unknown";

	@Bean
	fun info(): ()-> String {
		return { "${serverName}:${serverPort}" }
	}
}

fun main(args: Array<String>) {
	runApplication<FunProvider8083Application>(*args)
}
