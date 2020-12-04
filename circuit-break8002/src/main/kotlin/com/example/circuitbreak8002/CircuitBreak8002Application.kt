package com.example.circuitbreak8002

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

inline fun<reified T> logger(): Logger {
	return LoggerFactory.getLogger(T::class.java)!!
}

@SpringBootApplication
class CircuitBreak8002Application

fun main(args: Array<String>) {
	runApplication<CircuitBreak8002Application>(*args)
}
