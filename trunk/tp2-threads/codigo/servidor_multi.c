#include <pthread.h>
#include <signal.h>
#include <errno.h>
#include <stdio.h>		//para hacer malloc

#include "biblioteca.h"

pthread_mutex_t mutex[ALTO_AULA][ANCHO_AULA];
pthread_mutex_t seccion_critica;

pthread_mutex_t mutex_rescatistas = PTHREAD_MUTEX_INITIALIZER;
pthread_cond_t  condicion_rescatistas = PTHREAD_COND_INITIALIZER;

/* Estructura que almacena los datos de una reserva. */
typedef struct {
	int posiciones[ALTO_AULA][ANCHO_AULA];
	int cantidad_de_personas;
	
	int rescatistas_disponibles;
} t_aula;

typedef struct {
	int fd_alumno;
	t_aula* aula;	
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
	
	// libera al rescatista
	pthread_mutex_lock( &mutex_rescatistas );
		un_aula->rescatistas_disponibles++;
		pthread_cond_signal( &condicion_rescatistas );
	pthread_mutex_unlock( &mutex_rescatistas );
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
	
	bool entre_limites = (fila >= 0) && (columna >= 0) &&
	     (fila < ALTO_AULA) && (columna < ANCHO_AULA);
	
	//Si quiere moverse a una posición inválida, no tiene sentido continuar con la consulta
	if( !entre_limites && !alumno->salio )
		return false;

	//Solo uno (thread) a la vez puede bloquear celdas
	pthread_mutex_lock(&seccion_critica);
		//Bloquea la celda en la que esta actualmente
		pthread_mutex_lock(&mutex[alumno->posicion_fila][alumno->posicion_columna]);
		//Si no sale, bloquea la celda a la que va a moverse
		if( !alumno->salio )
		{
			pthread_mutex_lock(&mutex[fila][columna]);
		}
	//Fin sección crítica (general)
	pthread_mutex_unlock(&seccion_critica);
	
	//Verifica posibilidad de moverse
	bool pudo_moverse = alumno->salio || el_aula->posiciones[fila][columna] < MAXIMO_POR_POSICION;
	
	if (pudo_moverse)
	{
		//Si no salió, aumenta la cantidad de personas de la celda destino
		if (!alumno->salio)
		{
			el_aula->posiciones[fila][columna]++;
			pthread_mutex_unlock(&mutex[fila][columna]);
		}
		//Como se movió, decrementa la posicion en la que estaba y libera la celda
		el_aula->posiciones[alumno->posicion_fila][alumno->posicion_columna]--;
		pthread_mutex_unlock(&mutex[alumno->posicion_fila][alumno->posicion_columna]);
		alumno->posicion_fila = fila;
		alumno->posicion_columna = columna;
	}
	else
	{
		//Libera los locks de memoria sin ningun cambio
		pthread_mutex_unlock(&mutex[fila][columna]);
		pthread_mutex_unlock(&mutex[alumno->posicion_fila][alumno->posicion_columna]);
	}

	return pudo_moverse;
}

void colocar_mascara(t_aula *el_aula, t_persona *alumno)
{
	printf("Esperando rescatista. Ya casi %s!\n", alumno->nombre);	

	// espera a que haya un rescatista libre
	pthread_mutex_lock( &mutex_rescatistas );
		while( el_aula->rescatistas_disponibles == 0 )
			pthread_cond_wait( &condicion_rescatistas, &mutex_rescatistas );
		el_aula->rescatistas_disponibles--;
	pthread_mutex_unlock( &mutex_rescatistas );
	
	alumno->tiene_mascara = true;
	
	//printf("Rescatista atendiendo a %s.", alumno->nombre );
	//printf("Rescatistas libres: %d\n", el_aula->rescatistas_disponibles );

	// sleep(5);
}


void *atendedor_de_alumno(void* params)
{
	thread_params* tparams = (thread_params *) params;
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
		
		/// Tratamos de movernos en nuestro modelo
		bool pudo_moverse = intentar_moverse(tparams->aula, &alumno, direccion);

		printf("%s se movio a: (%d, %d)\n", alumno.nombre, alumno.posicion_fila, alumno.posicion_columna);

		/// Avisamos que ocurrio
		enviar_respuesta(tparams->fd_alumno, pudo_moverse ? OK : OCUPADO);		
		
		if (alumno.salio)
		{
			break;
		}
	}
	
	colocar_mascara(tparams->aula, &alumno);

	t_aula_liberar(tparams->aula, &alumno);
	enviar_respuesta(tparams->fd_alumno, LIBRE);
	
	printf("Listo, %s es libre!\n", alumno.nombre);

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
	
	int i,j;
	//Inicializa mutex global a todos los threads 
	pthread_mutex_init(&seccion_critica, NULL);			
	
	//Crea un mutex por cada posicion de la matriz (cada mt² del aula)
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
			//Guarda en Struct todos los parametros para pasarle al thread
			pthread_t thread;
			thread_params * params  = (thread_params*) malloc(sizeof(thread_params));
			//Chequeo error malloc
			if(params == NULL)
			{
				printf("!! Error al crear estructura para thread\n");
				close(socketfd_cliente);
			}
			params->fd_alumno 	    = socketfd_cliente;
			params->aula 		    = &el_aula; 

			//Crea thread y chequea que la operación sea exitosa
			if(pthread_create(&thread, NULL, atendedor_de_alumno, (void*) params) != 0)
			{
				printf("!! Error al crear thread\n");
				close(socketfd_cliente);
			} 
			
			
		}
	}

	//tenemos q cerrar alguna vez el mutex?
	pthread_exit(NULL);//Esta linea nunca llega
	return 0;
}

