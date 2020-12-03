package main

import (
	"fmt"
	"github.com/gin-gonic/gin"
	consulapi "github.com/hashicorp/consul/api"
	"log"
	"net/http"
)

const (
	ConsulAddress = "localhost:8500"
)

func main() {
	r := gin.Default()

	// consul健康检查回调函数
	r.GET("/", func(c *gin.Context) {
		c.JSON(200, gin.H{
			"message": "ok",
		})
	})

	r.GET("/info", func(c *gin.Context) {
		c.String(http.StatusOK, "go-fun-provider:7000")
	})

	r.DELETE("/info", func(c *gin.Context) {
		// 取消consul注册的服务
		ConsulDeRegister()
		c.JSON(200, gin.H{
			"message": "ok",
		})
	})

	r.POST("/info", func(c *gin.Context) {
		// 取消consul注册的服务
		ConsulRegister()
		c.JSON(200, gin.H{
			"message": "ok",
		})
	})

	// 注册服务到consul
	ConsulRegister()

	// 从consul中发现服务
	ConsulFindServer()

	// 取消consul注册的服务
	//ConsulDeRegister()


	http.ListenAndServe(":7000", r)
}

// 注册服务到consul
func ConsulRegister()  {
	// 创建连接consul服务配置
	config := consulapi.DefaultConfig()
	config.Address = "127.0.0.1:8500"
	client, err := consulapi.NewClient(config)
	if err != nil {
		log.Fatal("consul client error : ", err)
	}

	// 创建注册到consul的服务到
	registration := new(consulapi.AgentServiceRegistration)
	registration.ID = "go-fun-provider-7000"
	registration.Name = "fun-provider"
	registration.Port = 7000
	registration.Tags = []string{
		"go-consul-test",
	}
	registration.Address = "localhost"

	// 增加consul健康检查回调函数
	check := new(consulapi.AgentServiceCheck)
	check.HTTP = fmt.Sprintf("http://%s:%d", registration.Address, registration.Port)
	check.Timeout = "5s"
	check.Interval = "5s"
	check.DeregisterCriticalServiceAfter = "30s" // 故障检查失败30s后 consul自动将注册服务删除
	registration.Check = check

	// 注册服务到consul
	err = client.Agent().ServiceRegister(registration)
}

// 取消consul注册的服务
func ConsulDeRegister()  {
	var err error = nil

	// 创建连接consul服务配置
	config := consulapi.DefaultConfig()
	config.Address = "127.0.0.1:8500"
	client, err := consulapi.NewClient(config)
	if err != nil {
		log.Fatal("consul client error : ", err)
	}

	err = client.Agent().ServiceDeregister("go-fun-provider")
	if err != nil {
		log.Fatal("service deregister error : ", err)
	}
}

// 从consul中发现服务
func ConsulFindServer()  {
	// 创建连接consul服务配置
	config := consulapi.DefaultConfig()
	config.Address = ConsulAddress
	client, err := consulapi.NewClient(config)
	if err != nil {
		log.Fatal("consul client error : ", err)
	}

	// 获取所有service
	services, _ := client.Agent().Services()
	for _, value := range services{
		fmt.Println(value.Address)
		fmt.Println(value.Port)
	}

	//fmt.Println("=================================")
	//// 获取指定service
	//service, _, err := client.Agent().Service("fun-provider8001", nil)
	//if err == nil{
	//	fmt.Println(service.Address)
	//	fmt.Println(service.Port)
	//}
}

func ConsulCheckHeath()  {
	// 创建连接consul服务配置
	config := consulapi.DefaultConfig()
	config.Address = "172.16.242.129:8500"
	client, err := consulapi.NewClient(config)
	if err != nil {
		log.Fatal("consul client error : ", err)
	}

	// 健康检查
	a, b, _ := client.Agent().AgentHealthServiceByID("111")
	fmt.Println(a)
	fmt.Println(b)
}

func ConsulKVTest()  {
	// 创建连接consul服务配置
	config := consulapi.DefaultConfig()
	config.Address = "172.16.242.129:8500"
	client, err := consulapi.NewClient(config)
	if err != nil {
		log.Fatal("consul client error : ", err)
	}

	// KV, put值
	values := "test"
	key := "go-consul-test/172.16.242.129:8100"
	client.KV().Put(&consulapi.KVPair{Key:key, Flags:0, Value: []byte(values)}, nil)

	// KV get值
	data, _, _ := client.KV().Get(key, nil)
	fmt.Println(string(data.Value))

	// KV list
	dataset, _ , _:= client.KV().List("go", nil)
	for _ , value := range dataset {
		fmt.Println(value)
	}
	keys, _ , _ := client.KV().Keys("go", "", nil)
	fmt.Println(keys)
}