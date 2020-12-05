package main

import (
	"errors"
	"fmt"
	"github.com/gin-gonic/gin"
	"github.com/robertkrimen/otto"
	"io/ioutil"
	"net/http"
	"os"
	"time"
)

var halt = errors.New("stop")

func runUnsafe(unsafe string) (otto.Value, error) {
	start := time.Now()
	defer func() {
		duration := time.Since(start)
		if caught := recover(); caught != nil {
			if caught == halt {
				fmt.Fprintf(os.Stderr, "Some code took to long! Stopping after: %v\n", duration)
				return
			}
			panic(caught) // Something else happened, repanic!
		}
		fmt.Fprintf(os.Stderr, "Ran code successfully: %v\n", duration)
	}()

	vm := otto.New()
	vm.Interrupt = make(chan func(), 1) // The buffer prevents blocking

	go func() {
		time.Sleep(2 * time.Second) // Stop after two seconds
		vm.Interrupt <- func() {
			panic(halt)
		}
	}()

	return vm.Run(unsafe) // Here be dragons (risky code)
}

//func Router() *gin.Engine {
//	r := gin.Default()
//
//	r.GET("/", func(c *gin.Context) {
//		//jsCode, err := ioutil.ReadAll(c.Request.Body)
//		//if err != nil {
//		//	c.JSON(http.StatusBadRequest, gin.H{
//		//		"ok": false,
//		//		"error": err.Error(),
//		//	})
//		//	return
//		//}
//
//		jsCode := `
//			for(;;){}
//			return "a"
//			`
//
//		value, err := runSafe("(function main(){" + string(jsCode) + "})()")
//
//		errorMsg := ""
//		if err != nil {
//			errorMsg = err.Error()
//		}
//
//		c.JSON(http.StatusOK, gin.H{
//			"ok": true,
//			"result": value.String(),
//			"error": errorMsg,
//		})
//	})
//	return r
//}

func main() {
	//r := Router()
	//r.GET("/t", func(c *gin.Context) {
	//	jsCode := `for(;;){}`
	//	runSafe(jsCode)
	//})

	r := gin.Default()
	r.GET("/", func(c *gin.Context) {
		jsCode, _ := ioutil.ReadAll(c.Request.Body)
		v, _ := runUnsafe(string(jsCode))
		c.JSON(http.StatusAccepted, gin.H{
			"msg": v.String(),
		})
	})
	http.ListenAndServe(":8888", r)
}
