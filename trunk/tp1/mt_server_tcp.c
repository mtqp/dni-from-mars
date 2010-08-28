#include "mt.h"

int main()
{
	int sock, client_sock, client_addr_size;
	struct sockaddr_in name, client_addr;
	char buf[MAX_MSG_LENGTH];

	FILE* fd = NULL;
	char output[MAX_MSG_LENGTH];
	int output_size;
	/* Crear socket sobre el que se lee: dominio INET, protocolo TCP (STREAM)*/
	sock = socket(AF_INET, SOCK_STREAM, 0);
	if (sock < 0) {
		perror("abriendo socket");
		exit(1);
	}
	/* Crear nombre, usamos INADDR_ANY para indicar que cualquiera puede enviar aquí. */
	name.sin_family = AF_INET;
	name.sin_addr.s_addr = INADDR_ANY;
	name.sin_port = htons(PORT);
	if (bind(sock, (void*) &name, sizeof(name))) {
		perror("binding datagram socket");
		exit(1);
	}
	/* Pone el socket a escuchar conexiones entrantes */
	if( listen(sock,1) == -1 ){
		perror("escuchando");
		exit(1);
	}
	/* Acepta la conexión */
	client_addr_size = sizeof(client_addr);
	client_sock = accept(sock, (struct sockaddr*) &client_addr, &client_addr_size );
	if( client_sock == -1 ) {
		perror("aceptando la conexion");
		exit(1);
	}
	/* Recibimos mensajes hasta que alguno sea el que marca el final. */
	while(1){
		if( read(client_sock, buf, MAX_MSG_LENGTH) == -1 ) {
			perror("leyendo del socket");
			exit(1);
		}
		if (strncmp(buf, END_STRING, MAX_MSG_LENGTH) == 0){
			break;
		}
		printf("Comando: %s", buf);
		fd = popen(buf,"r");
		if( fd == 0 ) {
			perror("corriendo el proceso hijo");
			exit(1);
		}
		output_size = fread( output, sizeof(char), MAX_MSG_LENGTH, fd );
		output[output_size-1]=0;
		if( sendto( client_sock, output, output_size, 0, (struct sockaddr*) &client_addr, client_addr_size) == -1 ) {
			perror("enviando");
			exit(1);
		}
		fclose(fd);
	}
	close(client_sock);
	/* Cerrar socket de recepción. */
	close(sock);
	return 0;
}

