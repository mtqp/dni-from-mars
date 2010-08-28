#include "mt.h"

int main()
{
    char str[MAX_MSG_LENGTH];
	struct sockaddr_in name;

	/* Crear nombre, INADDR_ANY para indicar que cualquiera puede enviar aquí. */
	name.sin_family = AF_INET;
	name.sin_port = htons(PORT);
	inet_aton("127.0.0.1", &name.sin_addr);
	// TODO: checkear errores y direccion por parametro

	int sock;
	char buf[MAX_MSG_LENGTH];
	/* Crear socket sobre el que se lee: dominio INET, protocolo UDP (DGRAM). */
	sock = socket(AF_INET, SOCK_STREAM, 0);
	if (sock < 0) {
		perror("abriendo socket");
		exit(1);
	}

	if( connect(sock,(struct sockaddr*)&name,sizeof(name)) == -1 )
	{
		perror("conectando");
		exit(1);
	}

	/* Establecer la dirección a la cual conectarse para escuchar. */
    while(printf("> "), fgets(str, MAX_MSG_LENGTH, stdin), !feof(stdin)) {
        if (sendto(sock, str, strlen(str)+1, 0, (struct sockaddr*) &name, sizeof(name)) == -1) {
            perror("enviando");
            exit(1);
        }
		if ( strcmp( str, "chau\n" ) == 0 )
			break;
		read(sock, buf, MAX_MSG_LENGTH);
		printf("ROCK: %s",buf);
    }

	close(sock);
	return 0;
}
