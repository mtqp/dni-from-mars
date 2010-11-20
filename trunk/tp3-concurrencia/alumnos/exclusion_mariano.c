#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "mpi.h"

#define TAG_TERMINO      14
#define TAG_SERVPED      13
#define TAG_SERVREP      12
#define TAG_PEDIDO       11
#define TAG_LIBERO       10
#define FALSE 0
#define TRUE  1

/* Variables globales. */
int np, rank;            /* Variables de MPI. */
static MPI_Comm igualesami;     /* Communicator para todos los clientes (sincronizar ejec. de programas exclusi�n) */


void servidorcontrol(int micliente)
{
   MPI_Status status;
   int origen, tag;
   int ret=-1;                       // Variable auxiliar para MPI_Send sin dato importante.
   int cantserv= np/2;               // np = #servers y clientes

   /* Variables utilizadas por los servidores */
   int seqnrorecibida;

   while (1){
      MPI_Recv(&seqnrorecibida, 1, MPI_INT, MPI_ANY_SOURCE, MPI_ANY_TAG, MPI_COMM_WORLD, &status);
      origen=status.MPI_SOURCE;
      tag=status.MPI_TAG;

      // Si es TAG_TERMINO 
      if ((tag == TAG_TERMINO) && (origen == micliente)) {
           printf("Servidor %d: Mi cliente me pidi� terminar.\n",rank/2);
           break;
      } else {
	  	if (MYClientSentMessage){
	  		if(wantsResource){
	  			askForResource();	//-->broadcast REQUEST
	  		} else {
	  			freeResource();		//-->broadcast to queue resourse now is FREE
	  		}
	  	} else { 
	  		//there's a server requesting to do something
	  		Server s = origen;
	  		if(serverAcceptedMYRequest) {
				//me fijo si puedo llamar al cliente, o todavia resta alguien por esperar respuesta
				//si lo puedo llamar, seteo variable que estoy en el lock
	  			checkCallingMYClient(s);
	  		} else {
	  			//un server hizo request del lock
	  			if(!iHaveLock) {
	  				if(iWantLock) {
	  					if(heBeatsMeUp) {	//-->su timestamp es menor
	  						sendReplyToServer(s);
	  						resetTimestamp();
	  					} else {
	  						addInQueue(s);	//-->'ak' fuck you, i go first
	  					}
	  				} else {
	  					sendReplyToServer(s);
	  				}
	  			} else {
					addInQueue(s);
	  			}
	  			
	  		}
	  	}
	  }


   }
   MPI_Barrier(MPI_COMM_WORLD);
}

void cliente(int miserv)
{
	FILE *fichero;
	char linea[82];
	char nombre[10] = "datos";
	char comando[3][10] = {"pedir","liberar", "escribir"};
        int env=-1;
	MPI_Status status;
	char seccion_critica = FALSE;

	/* Abro el archivo que me indica los pasos a seguir.
           Se enumeran los archivos de clientes de 0 a n sin tomar en cuenta los servidores */
	sprintf(nombre,"datos%d.txt",rank/2);
	fichero=fopen(nombre,"r");
	if (fichero==NULL){
	     printf("ERROR: No se pudo abrir el archivo %s\n", nombre);
	     exit(1);
	}

    	/* Espero a todos los electores a que lleguen a este lugar para poder hacer pedidos simultaneos y ver como responde el algoritmo.*/
    	MPI_Barrier(igualesami);
        printf("Cliente %d: Comienzo ejecuci�n de programa.\n",rank/2);

 	fgets(linea,80,fichero);
	while (!feof(fichero)){
		if (strncmp (linea, comando[0], strlen(comando[0]))==0) {
		// Envio pedido de ingreso a la secci�n cr�tica.
		// Se le env�a un mensaje a myrank -1, que es el servidor del nodo 
		// para que haga el manejo de pedidos en forma global.
                      printf("Cliente %d: Pedido de ingreso a la secci�n cr�tica.\n", rank/2);
		      MPI_Send(&env, 1, MPI_INT, miserv, TAG_PEDIDO, MPI_COMM_WORLD);
		      printf("Cliente %d: Esperando el acceso a la secci�n cr�tica.\n",rank/2);
		      MPI_Recv(&env, 1, MPI_INT, miserv, TAG_PEDIDO, MPI_COMM_WORLD, &status);
		      printf("Cliente %d: Poseci�n de la secci�n cr�tica.\n",rank/2);
		      seccion_critica = TRUE;
		}

		if (strncmp(linea, comando[1], strlen(comando[1]))==0) {
		// Env�o de liberaci�n de la secci�n cr�tica.
		// Se le env�a un mensaje a myrank -1, que es el servidor del nodo 
		// para que haga el manejo de pedidos en forma global.
		      printf("Cliente %d: Liberaci�n de la secci�n cr�tica.\n", rank/2);
		      MPI_Send(&env, 1, MPI_INT, miserv, TAG_LIBERO, MPI_COMM_WORLD);
		      seccion_critica = FALSE;
		}
		
		/// Si el comando es "escribir".
		if (strncmp (linea, comando[2], strlen(comando[2]))==0) {
		// Se escribe en pantalla el n�mero de cliente y la l�nea leida.
		// Esto deber�a llamarse �nicamente dentro de la secci�n cr�tica.
			if (!seccion_critica) {
				printf("Cliente %d: ERROR! Pedido de escritura sin estar en secci�n cr�tica.\n", rank/2);
			} else {
				printf("Cliente %d: %s.\n",rank/2,linea);
			}
		}
		fgets(linea,80,fichero);
 
	}
       
	printf("Cliente %d: Finalizaci�n del programa.\n",rank/2);
	fclose(fichero);

        // Espero a todos los clientes para hacer que terminen los servidores de cada uno de ellos.
    	MPI_Barrier(igualesami);
        MPI_Send(&env, 1, MPI_INT, miserv, TAG_TERMINO, MPI_COMM_WORLD);
    	MPI_Barrier(MPI_COMM_WORLD);
}


int main(int argc, char *argv[])
{
	int status;
        int tipo;

	/* Inicializo MPI. */
	status= MPI_Init(&argc, &argv);
	if (status!=MPI_SUCCESS)
	{
		 fprintf(stderr, "Error de MPI al inicializar.\n");
		 MPI_Abort(MPI_COMM_WORLD, status);
	}
	MPI_Comm_size(MPI_COMM_WORLD, &np);
	MPI_Comm_rank(MPI_COMM_WORLD, &rank);

        tipo=rank % 2;
        MPI_Comm_split(MPI_COMM_WORLD, tipo, rank, &igualesami);

        /* Control el buffering: sin buffering. 
           NOTA: Descomentar las siguientes l�neas para poder ver los printf de los procesos
                 cuando son realizados y no agrupados al final
        */
//	setbuf(stdout, NULL);
//	setbuf(stderr, NULL);
	printf("\nLanzando proceso %u\n", rank);
	if (tipo==0)
	{
		 /* Soy el proceso que recibe los pedidos de secci�n cr�tica. */
		 servidorcontrol(rank+1);
	}
	else if (tipo==1)
	{
		 /* Soy el proceso que ejecuta en el nodo y desea ingresar a la secci�n cr�tica. */
		 cliente(rank-1);
	}

	/* Limpio MPI. */
	MPI_Finalize();
	return 0;
}
