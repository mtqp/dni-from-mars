#include "mt.h"

void checkArguments(int argc, char** argv)
{
	if( argc < 2 ) {
		printf("Uso: %s direccion_ip\n", argv[0]);
		exit(1);
	}
}

int main(int argc, char** argv)
{
	checkArguments(argc,argv);
	
    char str[MAX_MSG_LENGTH];
	struct sockaddr_in name;

	/* Crear nombre, INADDR_ANY para indicar que cualquiera puede enviar aquí. */
	name.sin_family = AF_INET;
	name.sin_port = htons(PORT);

	if( inet_aton(argv[1], &name.sin_addr) == 0 )
		exit(1);

	int sock;
	char buf[MAX_MSG_LENGTH];
	/* Crear socket sobre el que se lee: dominio INET, protocolo UDP (DGRAM). */
	sock = socket(AF_INET, SOCK_DGRAM, 0);
	if (sock < 0) {
		perror("abriendo socket");
		exit(1);
	}

	/* Establecer la dirección a la cual conectarse para escuchar. */
    while(printf("> "), fgets(str, MAX_MSG_LENGTH, stdin), !feof(stdin)) {
 		if ( strcmp( str, END_STRING ) == 0 )
			break;
        if (sendto(sock, str, strlen(str)+1, 0, (struct sockaddr*) &name, sizeof(name)) == -1) {
            perror("enviando");
            exit(1);
        }
    }

	close(sock);
	return 0;
}
