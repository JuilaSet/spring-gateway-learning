package com.example.lbdemo

import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.function.client.WebClient
import java.time.Duration


@EnableDiscoveryClient
@SpringBootApplication
class LbDemoApplication {

	@Bean
	fun webClientBuilder(filter: ReactorLoadBalancerExchangeFilterFunction): WebClient.Builder {
		val baseUri = "http://demo-client/"
		return WebClient.builder().filter(filter).baseUrl(baseUri)
	}

	@Bean
	fun commandLineRunner(builder: WebClient.Builder): CommandLineRunner {
		return CommandLineRunner { args: Array<String> ->
			builder.build().get().uri("info")
					.retrieve().bodyToMono(String::class.java)
					.onErrorReturn("Failed to call endpoint")
					.doOnNext { s: String -> println(">>>>>>>>>>>>>> Server Response: $s") }
					.delayElement(Duration.ofMillis(500))
					.repeat(3)
					.subscribe()
		}
	}
}

fun main(args: Array<String>) {
	runApplication<LbDemoApplication>(*args)
}
