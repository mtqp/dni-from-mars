#include <pthread.h>
#include <signal.h>
#include <errno.h>
#include <stdio.h>		//para hacer malloc

#include "biblioteca.h"

pthread_mutex_t mutex[ALTO_AULA][ANCHO_AULA];
pthread_mutex_t seccion_critica;

/* Estructura que almacena los datos de una reserva. */
typedef struct {
	int posiciones[ALTO_AULA][ANCHO_AULA];
	int cantidad_de_personas;
	
	int rescatistas_disponibles;
} t_aula;

typedef struct {
	int fd_alumno;
	t_aula* aula;	
	//algo mas?
} thread_params;

void t_aula_iniciar_vacia(t_aula *un_aula)
{
	int i, j;
	for(i = 0; i < ALTO_AULA; i++)
	{
		for (j = 0; j < ANCHO_AULA; j++)
		{
			un_aula->posiciones[i][j] = 0;
		}
	}
	
	un_aula->cantidad_de_personas = 0;
	
	un_aula->rescatistas_disponibles = RESCATISTAS;
}

void t_aula_ingresar(t_aula *un_aula, t_persona *alumno)
{
	un_aula->cantidad_de_personas++;
	un_aula->posiciones[alumno->posicion_fila][alumno->posicion_columna]++;
}

void t_aula_liberar(t_aula *un_aula, t_persona *alumno)
{
	un_aula->cantidad_de_personas--;
	un_aula->posiciones[alumno->posicion_fila][alumno->posicion_columna]--;
}

static void terminar_servidor_de_alumno(int socket_fd, t_aula *aula, t_persona *alumno) {
	printf(">> Se interrumpió la comunicación con una consola.\n");
		
	close(socket_fd);
	
	t_aula_liberar(aula, alumno);
	exit(-1);
}


t_comando intentar_moverse(t_aula *el_aula, t_persona *alumno, t_direccion dir)
{
	int fila = alumno->posicion_fila;
	int columna = alumno->posicion_columna;
	alumno->salio = direccion_moverse_hacia(dir, &fila, &columna);

	///char buf[STRING_MAXIMO];
	///t_direccion_convertir_a_string(dir, buf);
	///printf("%s intenta moverse hacia %s (%d, %d)... ", alumno->nombre, buf, fila, columna);
	
	
	bool entre_limites = (fila >= 0) && (columna >= 0) &&
	     (fila < ALTO_AULA) && (columna < ANCHO_AULA);

	pthread_mutex_lock(&seccion_critica);
//		printf("lock %d,%d\n",alumno->posicion_fila, alumno->posicion_columna);
		pthread_mutex_lock(&mutex[alumno->posicion_fila][alumno->posicion_columna]);
		if( !alumno->salio )
		{
//			printf("lock alumno no salio %d,%d\n",fila, columna);
			pthread_mutex_lock(&mutex[fila][columna]);
		}
	pthread_mutex_unlock(&seccion_critica);
		     
	bool pudo_moverse = alumno->salio ||
	    (entre_limites && el_aula->posiciones[fila][columna] < MAXIMO_POR_POSICION);
	
	
	if (pudo_moverse)
	{
		if (!alumno->salio)
		{
			el_aula->posiciones[fila][columna]++;
			pthread_mutex_unlock(&mutex[fila][columna]);
//			printf("unlock pudo moverse & alumno no salio %d,%d\n",fila,columna);
		}
		el_aula->posiciones[alumno->posicion_fila][alumno->posicion_columna]--;
		pthread_mutex_unlock(&mutex[alumno->posicion_fila][alumno->posicion_columna]);
//		printf("unlock pudo moverse %d,%d\n",alumno->posicion_fila,alumno->posicion_columna);
		alumno->posicion_fila = fila;
		alumno->posicion_columna = columna;
	}
	else
	{
		pthread_mutex_unlock(&mutex[alumno->posicion_fila][alumno->posicion_columna]);
//		printf("unlock NO pudo moverse %d,%d\n",alumno->posicion_fila,alumno->posicion_columna);
	}
	
	//~ if (pudo_moverse)
		//~ printf("OK!\n");
	//~ else
		//~ printf("Ocupado!\n");


	return pudo_moverse;
}

void colocar_mascara(t_aula *el_aula, t_persona *alumno)
{
	printf("Esperando rescatista. Ya casi %s!\n", alumno->nombre);
		
	alumno->tiene_mascara = true;
}


void *atendedor_de_alumno(void* params)//int socket_fd, t_aula *el_aula)
{
	thread_params* tparams = (thread_params *) params; //no se si es necesario
	t_persona alumno;
	t_persona_inicializar(&alumno);
	
	if (recibir_nombre_y_posicion(tparams->fd_alumno, &alumno) != 0) {
		/* O la consola cortó la comunicación, o hubo un error. Cerramos todo. */
		terminar_servidor_de_alumno(tparams->fd_alumno, NULL, NULL);
	}
	
	printf("Atendiendo a %s en la posicion (%d, %d)\n", alumno.nombre, alumno.posicion_fila, alumno.posicion_columna);
		
	t_aula_ingresar(tparams->aula, &alumno);
	
	/// Loop de espera de pedido de movimiento.
	for(;;) {
		t_direccion direccion;
		
		/// Esperamos un pedido de movimiento.
		if (recibir_direccion(tparams->fd_alumno, &direccion) != 0) {
			/* O la consola cortó la comunicación, o hubo un error. Cerramos todo. */
			terminar_servidor_de_alumno(tparams->fd_alumno, tparams->aula, &alumno);
		}
		
		/// Tratamos de movernos en nuestro modelo -- 	LO TIENE QUE HACER CON LOCK
		bool pudo_moverse = intentar_moverse(tparams->aula, &alumno, direccion);

		printf("%s se movio a: (%d, %d)\n", alumno.nombre, alumno.posicion_fila, alumno.posicion_columna);

		/// Avisamos que ocurrio
		enviar_respuesta(tparams->fd_alumno, pudo_moverse ? OK : OCUPADO);		
		//printf("aca3\n");
		
		if (alumno.salio)
			break;
	}
	
	colocar_mascara(tparams->aula, &alumno);

	t_aula_liberar(tparams->aula, &alumno);
	enviar_respuesta(tparams->fd_alumno, LIBRE);
	
	printf("Listo, %s es libre!\n", alumno.nombre);

	pthread_exit(NULL); 
		
	return NULL;
	//close thread

}


int main(void)
{
	//signal(SIGUSR1, signal_terminar);
	int socketfd_cliente, socket_servidor, socket_size;
	struct sockaddr_in local, remoto;

	/* Crear un socket de tipo INET con TCP (SOCK_STREAM). */
	if ((socket_servidor = socket(AF_INET, SOCK_STREAM, 0)) == -1) {
		perror("creando socket");
	}

	/* Crear nombre, usamos INADDR_ANY para indicar que cualquiera puede conectarse aquí. */
	local.sin_family = AF_INET;
	local.sin_addr.s_addr = INADDR_ANY;
	local.sin_port = htons(PORT);
	
	if (bind(socket_servidor, (struct sockaddr *)&local, sizeof(local)) == -1) {
		perror("haciendo bind");
	}

	/* Escuchar en el socket y permitir 5 conexiones en espera. */
	if (listen(socket_servidor, 5) == -1) {
		perror("escuchando");
	}
	
	t_aula el_aula;
	t_aula_iniciar_vacia(&el_aula);
	
	/// Aceptar conexiones entrantes.
	socket_size = sizeof(remoto);
	
	//hay que crear el mutex
	//mutex sarasa;
	int i,j;
	pthread_mutex_init(&seccion_critica, NULL);			
	
	for( i = 0 ; i < ALTO_AULA ; i++ )
		for( j = 0 ; j < ANCHO_AULA ; j++ )	
			pthread_mutex_init(&mutex[i][j], NULL);
	
	for(;;){		
		if (-1 == (socketfd_cliente = accept(socket_servidor, (struct sockaddr*) &remoto, (socklen_t*) &socket_size)))
		{			
			printf("!! Error al aceptar conexion\n");
		}
		else
		{
			//Create Thread!
			pthread_t thread;
			/*pthread_mutex_t mem_mutex[ANCHO_AULA][ALTO_AULA];
			pthread_mutex_t seccion_critica;*/
			thread_params * params  = (thread_params*) malloc(sizeof(thread_params));	//SHALL WE CHECK ERRORS?
			params->fd_alumno 	    = socketfd_cliente;
			params->aula 		    = &el_aula; 
			/*params->mutex		    = &mem_mutex;
			params->seccion_critica	= &seccion_critica;*/
			/*estaria bueno q haya un mutex x cada posicion de la matriz
			**cosa q si tenemos muchos cores, si hay dos threads q estan negociando el moverse a un posicion 
			**distinta entre ellos puedan hacerlo
			*/
			int ret_createThread;
			//Hay q chequear el error!
			ret_createThread = pthread_create(&thread, NULL, atendedor_de_alumno, (void*) params);
			//exit thread?
		}
	}

	//tenemos q cerrar alguna vez el mutex?
	pthread_exit(NULL);//Esta linea nunca llega
	return 0;
}


/*inicializo todo
creo la matriz del aula... o ya existE?

for(siga_con_gente_aula){
	esperoquealguiense comunique conmigo
	armo estructura apra pasar a thread
	creo thread y se la paso
}

thread:
	while(nosalga){
		espero que me pida moverse para algun lado
		hago un lock de la matriz (aula)
			me fijo si puedo dejarlo moverse
			si puedo, lo marco (ademas si es q me da una pos valida)
		quito el lock
		le aviso:
			-- chequeo si salio y tiene la mascara y eso -- rompe el while eso
	}

*/
