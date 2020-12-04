package com.example.demo2

import org.apache.logging.log4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.client.RestTemplate

@Component
@FeignClient("fun-provider")
interface MyInfoServer {

	@GetMapping("/info")
	fun info(): String
}

inline fun <reified T> logger(): org.slf4j.Logger {
	return LoggerFactory.getLogger(T::class.java)!!
}

@Component
class Functions {

	@Autowired
	lateinit var server: MyInfoServer

	@Bean
	fun consumeInfo(): () -> String {
		return {
			logger<Functions>().trace("trace level log");
			logger<Functions>().debug("debug level log");
			logger<Functions>().info("info level log");
			logger<Functions>().warn("warn level log");
			logger<Functions>().error("error level log");
			"Info from server: ${server.info()}"
		}
	}
}

@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication
class Demo2Application {

	@Autowired
	lateinit var loadBalancerClient: LoadBalancerClient

	@Bean
	fun consume(): ()-> String {
		return {
			val instance = loadBalancerClient.choose("fun-provider")
            val obj = object {
            	val uri = instance.uri
				val host = instance.host
				val port = instance.port
				override fun toString(): String = "uri: $uri, host: $host, port: $port"
                fun getEntity(): String? {
					return RestTemplate()
							.getForObject("$uri/info", String::class.java)
                }
			}
			"consumer: ${obj.getEntity()}"
		}
	}
}

fun main(args: Array<String>) {
	runApplication<Demo2Application>(*args)
}
