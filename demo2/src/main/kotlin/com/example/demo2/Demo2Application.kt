package com.example.demo2

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient
import org.springframework.context.annotation.Bean
import org.springframework.web.client.RestTemplate

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
