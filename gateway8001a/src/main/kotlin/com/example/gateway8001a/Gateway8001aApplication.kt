package com.example.gateway8001a

import io.github.resilience4j.circuitbreaker.CallNotPermittedException
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.runApplication
import org.springframework.boot.web.reactive.error.ErrorAttributes
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.GatewayFilterSpec
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.adapter.WebHttpHandlerBuilder.applicationContext
import reactor.core.publisher.Mono


//@Component
//class ErrorGlobalFilter : GlobalFilter {
//
//	override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
//		val i = 1 / 0
//		return chain.filter(exchange)
//	}
//}

@Component
class RequestTimeFilter: GatewayFilter, Ordered {
	override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
		exchange.attributes[REQUEST_TIME_BEGIN] = System.currentTimeMillis()
		return chain.filter(exchange).then(
				Mono.fromRunnable {
					val startTime = exchange.getAttribute<Long>(REQUEST_TIME_BEGIN)
					if (startTime != null) {
						log.info(exchange.request.uri.rawPath + ": " + (System.currentTimeMillis() - startTime) + "ms")
					}
				}
		)
	}

	override fun getOrder(): Int = Ordered.LOWEST_PRECEDENCE

	companion object {
		private val log: Log = LogFactory.getLog(GatewayFilter::class.java)
		private const val REQUEST_TIME_BEGIN = "requestTimeBegin"
	}
}

class MyHandler: ErrorWebExceptionHandler {

	override fun handle(exchange: ServerWebExchange, ex: Throwable): Mono<Void> {
		exchange.response.statusCode = HttpStatus.PERMANENT_REDIRECT
		exchange.response.headers["Location"] = "http://www.baidu.com"
		return Mono.empty()
	}
}

@SpringBootApplication
class Gateway8001aApplication {

	@Bean
	fun errorWebExceptionHandler(errorAttributes: ErrorAttributes): ErrorWebExceptionHandler {
		return MyHandler()
	}

	@Bean
	fun customerRouteLocator(builder: RouteLocatorBuilder): RouteLocator {
		return builder.routes()
				.route {
					it.path("/**")
							.filters { f: GatewayFilterSpec -> f.filter(RequestTimeFilter()) }
							.uri("lb://fun-provider")
				}
				.build()
	}

//	@Bean
//	fun defaultCustomizer(): Customizer<ReactiveResilience4JCircuitBreakerFactory> {
//		return Customizer { factory: ReactiveResilience4JCircuitBreakerFactory ->
//			factory.configureDefault { id: String ->
//				Resilience4JConfigBuilder(id)
//						.circuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
//						.timeLimiterConfig(
//								TimeLimiterConfig
//										.custom()
//										.timeoutDuration(Duration.ofSeconds(3))
//										.build())
//						.build()
//			}
//		}
//	}
}

fun main(args: Array<String>) {
	runApplication<Gateway8001aApplication>(*args)
}
