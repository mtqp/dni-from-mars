#include <linux/kernel.h>
#include <linux/fs.h>
#include <linux/init.h>
#include <linux/miscdevice.h>
#include <linux/module.h>
#include <linux/vmalloc.h>
#include <linux/time.h>
#include <linux/proc_fs.h>
#include <asm/uaccess.h>

#define procfs_name "fibocount" // Nombre del archivo en /proc
#define SUCCESS 	0
////habria que ver que todos los includes son necesarios/////

unsigned long fib_actual;
unsigned long fib_previo;
unsigned long init_fib_actual;
unsigned long init_fib_previo;
unsigned long cantidad_lecturas;

//Inicializacion device
static int __init fibonacci_init(void);
static void __exit fibonacci_exit(void);

static int device_open(struct inode *, struct file *);
static int device_release(struct inode *, struct file *);
static ssize_t device_read(struct file *, char *, size_t, loff_t *);
static ssize_t device_write(struct file *, const char *, size_t,loff_t *);

//Funciones auxiliares
static void recalculate_fib(void);

// Funciones de manejo del dispositivo en /dev
static struct file_operations fops =
{
    .read    = device_read,
    .write 	 = device_write,
    .open    = device_open,    
    .release = device_release
};

//Misc Struct
static struct miscdevice mi_dev = {
	MISC_DYNAMIC_MINOR,  
	"fibonacci",
	&fops
};

static struct proc_dir_entry *procFile; // Informacion de nuestro archivo en /proc

module_init(fibonacci_init);
module_exit(fibonacci_exit);

//Calcula el nuevo numero de fibonacci
///HEMOS DE CHEUQEAR SI CUANDO NOS PASAN DOS NUMEROS, PERTENECEN REALMENTE A LA SUCESION DE FIBONACCI?
static void recalculate_fib(){
	unsigned long swap;
	swap		 = fib_actual;
	fib_actual 	+= fib_previo;
	fib_previo 	 = swap;
}

// Maneja aperturas del archivo
static int device_open(struct inode *inode, struct file *file){
	try_module_get(THIS_MODULE);
    return SUCCESS;
}

// Maneja el evento de cierre del archivo
static int device_release(struct inode *inode, struct file *file){
    module_put(THIS_MODULE);
    return SUCCESS;
}

//Leer de /dev/fib
static ssize_t device_read(struct file *filp, char *buffer, size_t length, loff_t * off){
    cantidad_lecturas++;
    recalculate_fib();
    if(fib_actual < fib_previo){
    	printk(KERN_ALERT "Se produjo BUFFER OVERFLOW en fib. Serán reinicializadas las variables\n");
      	fib_actual = init_fib_actual;
      	fib_previo = init_fib_previo;
      	return 0;
    } else {
      	return sprintf(buffer,"%lu\n",fib_actual);
    }
}

unsigned long int getFirstNumber( const char* str, size_t length, unsigned int* digitos )
{
	int i,l=0,m=1;
	unsigned long int res = 0;

	// busca el primer no-número
	while( l < length && (str[l]>='0' && str[l]<='9') )
	{
		l++;
		m *= 10;
	}

	for( i=0 ; i < l ; i++ )
	{
		m /= 10;
		res += (str[i]-'0') * m;
	}

	if( digitos != NULL )
		*digitos = l;

	return res;
}

static ssize_t device_write(struct file *filp, const char *buffer, size_t length, loff_t *data){
	unsigned int digitos;
	fib_previo = getFirstNumber(buffer, length, &digitos);

	if( digitos+1 >= length )
	{
		printk(KERN_ALERT "Cantidad de parametros en Fibonacci incorrecto\n");
		return 1;
	}

	fib_actual = getFirstNumber(&buffer[digitos+1], length-(digitos+1), NULL);

	return 2*sizeof(unsigned long);
}

//leer de /proc/fibocount
int procFileRead( char *buffer, char **buffer_location, off_t offset, int buffer_length, int *eof, void *data){
   return sprintf(buffer, "Cantidad de lecturas a /dev/fibonacci: %lu\n", cantidad_lecturas);
}


//Inicializa Fib
static int __init fibonacci_init(void){
	fib_actual = 1;
	fib_previo = 0;
	init_fib_actual = fib_actual;
	init_fib_previo = fib_previo;
	cantidad_lecturas = 0;
	
    // Inicializamos el dispositivo en /dev   //che se puede suponer que lo registro correctamente?
    if(misc_register(&mi_dev)!=0) {
    	printk(KERN_ALERT "Error creando MISC DEVICE en FIB");
    }

    // Inicializamos el archivo en /proc
    procFile = create_proc_entry(procfs_name, 0666,NULL);
    if (procFile == NULL)
    {
            remove_proc_entry(procfs_name, procFile);
            printk(KERN_ALERT "Error: Could not initialize /proc/%s\n",procfs_name);
            //misc_deregister(&mi_dev);
            return -ENOMEM;
    }

    procFile->read_proc  = procFileRead;
//    procFile->owner      = THIS_MODULE;

  	printk(KERN_ALERT "Modulo Fibonacci inicializado\n");
	return 0;


}

//Destruye fib
static void __exit fibonacci_exit(void){
	misc_deregister(&mi_dev);
	printk(KERN_ALERT "Desintalando el modulo Fibonacci\n");
}

MODULE_LICENSE("GPL");
MODULE_AUTHOR("Mariano De Sousa Bispo, Daniel Grosso");
MODULE_DESCRIPTION("Calcula Fibonacci");
