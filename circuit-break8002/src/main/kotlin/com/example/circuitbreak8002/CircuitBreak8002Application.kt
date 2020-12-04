package com.example.circuitbreak8002

import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import io.github.resilience4j.timelimiter.TimeLimiterConfig
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder
import org.springframework.cloud.client.circuitbreaker.Customizer
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.Mono
import java.time.Duration


//import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker as CB

inline fun <reified T> logger(): Logger {
	return LoggerFactory.getLogger(T::class.java)!!
}

fun loggerMatrix(time: String, circuitBreaker: CircuitBreaker): String {
	val metrics: CircuitBreaker.Metrics = circuitBreaker.metrics
	// Returns the failure rate in percentage.
	val failureRate = metrics.failureRate
	// Returns the current number of buffered calls.
	val bufferedCalls = metrics.numberOfBufferedCalls
	// Returns the current number of failed calls.
	val failedCalls = metrics.numberOfFailedCalls
	// Returns the current number of succeeded calls.
	val successCalls = metrics.numberOfSuccessfulCalls
	// Returns the current number of not permitted calls.
	val notPermittedCalls = metrics.numberOfNotPermittedCalls

	return "${time}:: state=${circuitBreaker.state}, " +
			"metrics[ failureRate=$failureRate, " +
			"bufferedCalls=$bufferedCalls, " +
			"failedCalls=$failedCalls, " +
			"successCalls=$successCalls, " +
			"notPermittedCalls=$notPermittedCalls ]"
}

//
//@Component
//class FuncConnector {
//
//	val logger = logger<FuncConnector>()
//
//	@Autowired
//	lateinit var server: MyInfoServer
//
//	@CB(name = "backendA", fallbackMethod = "fallBack")
//	fun aopInfo(): String {
//		return server.info()
//	}
//
//	fun fallBack(e: CallNotPermittedException): String {
//		logger.info("熔断器打开中...")
//		return "$e"
//	}
//
//	fun fallBack(e: Throwable): String {
//		logger.info("服务降级中...")
//		return "server cannot use! $e"
//	}
//}
//
//@Component
//class Functions {
//
//	val logger = logger<Functions>()
//
//	@Autowired
//	lateinit var server: MyInfoServer
//
//	@Autowired
//	lateinit var connector: FuncConnector
//
//	@Bean
//	fun aopInfo(): ()-> String {
//		return {
//			connector.aopInfo()
//		}
//	}
//
//	@Bean
//	fun consumeInfo(): () -> String {
//		val circuitBreaker = CircuitBreaker.ofDefaults("backendA")
//        val supplier = circuitBreaker.decorateCheckedSupplier{
//			server.info()
//		}
//		return {
//			Try.of(supplier)
//					.recover(CallNotPermittedException::class.java) {
//						logger.info("熔断器打开中...")
//						"$it"
//					}
//					.recover {
//						logger.info("服务降级中...")
//						"server cannot use! $it"
//					}
//					.get()
//		}
//	}
//}

@Component
@FeignClient("fun-provider")
interface MyInfoServer {

	@GetMapping("/info")
	fun info(): String
}

@Component
class Functions {

	val log = logger<Functions>()

    @Autowired
	lateinit var cbFactory: ReactiveCircuitBreakerFactory<Resilience4JConfigBuilder.Resilience4JCircuitBreakerConfiguration, Resilience4JConfigBuilder>

	@Autowired
	lateinit var webClientBuilder: WebClient.Builder

	@Bean
	@Primary
	fun routers(): RouterFunction<ServerResponse> {
		return router {
			GET("/info") {
				val infoMsg = webClientBuilder.build().get()
						.uri("info").retrieve()
						.bodyToMono(String::class.java)
				val r4jInfoMsg = cbFactory.create("backendA")
						.run(infoMsg) { t: Throwable ->
							log.error("Failed to call hello endpoint: {}", t.message)
							Mono.just("sorry, ;-(")
						}
				ServerResponse.ok().body(r4jInfoMsg, String::class.java)
			}
		}
	}
}

@Configuration
class CircuitBreakerConfiguration {

	@Bean
	fun webClientBuilder(filter: ReactorLoadBalancerExchangeFilterFunction): WebClient.Builder {
		val baseUri = "http://fun-provider"
		return WebClient.builder().filter(filter).baseUrl(baseUri)
	}

	@Bean
	fun reactiveResilience4JCircuitBreakerFactory(): ReactiveCircuitBreakerFactory<Resilience4JConfigBuilder.Resilience4JCircuitBreakerConfiguration, Resilience4JConfigBuilder> {
		return ReactiveResilience4JCircuitBreakerFactory()
	}

	@Bean
	fun defaultCustomizer(): Customizer<ReactiveResilience4JCircuitBreakerFactory> {
		return Customizer { factory: ReactiveResilience4JCircuitBreakerFactory ->
			factory.configureDefault { id: String ->
				Resilience4JConfigBuilder(id)
						.circuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
						.timeLimiterConfig(TimeLimiterConfig.custom().timeoutDuration(Duration.ofSeconds(3)).build()).build()
			}
		}
	}
}

@EnableFeignClients
@SpringBootApplication
class CircuitBreak8002Application

fun main(args: Array<String>) {
	runApplication<CircuitBreak8002Application>(*args)
}
