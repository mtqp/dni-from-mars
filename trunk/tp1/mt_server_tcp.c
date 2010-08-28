#include "mt.h"
int main()
{
	int sock;
	struct sockaddr_in name;
	char buf[MAX_MSG_LENGTH];

	FILE* fd = NULL;
	char output[MAX_MSG_LENGTH];
	int output_size;
	/* Crear socket sobre el que se lee: dominio INET, protocolo TCP (STREAM)*/
	sock = socket(AF_INET, SOCK_DGRAM, 0);
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
	/* Recibimos mensajes hasta que alguno sea el que marca el final. */
	for (;;) {
		read(sock, buf, MAX_MSG_LENGTH);
		if (strncmp(buf, END_STRING, MAX_MSG_LENGTH) == 0)
			break;
		printf("Comando: %s", buf);
		fd = popen(buf,"r");
		output_size = fread( output, sizeof(char), MAX_MSG_LENGTH, fd );
		/*output[output_size]=0;
		sendto( sock, output, output_size, 0, (struct sockaddr*) &name, sizeof(name));*/
		fclose(fd);
	}
	/* Cerrar socket de recepción. */
	close(sock);
	return 0;
}

