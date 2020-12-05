package context

import "github.com/robertkrimen/otto"

// 必须是无状态的
func SetVmParams(vm *otto.Otto) {
	// 定义参数
	vm.Set("add", func(a, b int) int {
		return a + b
	})

	vm.Set("Int", func(a int) int {
		return a
	})

	vm.Set("console", func() string {
		return "null"
	})
}
