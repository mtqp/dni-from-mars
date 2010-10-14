#include <linux/init.h>
#include <linux/module.h>
#include <linux/kernel.h>

unsigned long fib_actual;

static int __init fibonacci_init(void){
	printk(KERN_ALERT "Modulo Fibonacci inicializado\n");
	return 0;
}

static void __exit fibonacci_exit(void){
	printk(KERN_ALERT "Desintalando el modulo Fibonacci\n");
}

module_init(fibonacci_init);
module_exit(fibonacci_exit);

MODULE_LICENSE("GPL");
MODULE_AUTHOR("Mariano De Sousa Bispo, Daniel Grosso");
MODULE_DESCRIPTION("Calcula Fibonacci");
