package com.example.gateway8001

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.ServiceInstance
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.cloud.gateway.route.builder.routes
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.bind.annotation.RestController

//@Configuration
//@RestController
//@EnableDiscoveryClient
//class GatewayConfig {
//
//	@Autowired
//	@Qualifier("my-instance")
//	lateinit var serviceInstance: ServiceInstance
//
//	@Bean("my-instance")
//	fun getServiceInstance(loadBalancerClient: LoadBalancerClient): ServiceInstance {
//		return loadBalancerClient.choose("fun-provider")
//	}
//
//	@Bean
//	fun customRouterLocator(builder: RouteLocatorBuilder): RouteLocator {
//		return builder.routes {
//			route("demo-rb-fb") {
//				path("/**")
//                uri("http://127.0.0.1:8082")
//			}
//		}
//	}
//}

@SpringBootApplication
class Gateway8001Application

fun main(args: Array<String>) {
	runApplication<Gateway8001Application>(*args)
}




