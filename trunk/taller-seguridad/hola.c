#include <stdio.h>

int main(int argc, char* argv[]) {
	char nombre[80];
	printf("Ingrese su nombre: ");
	gets(nombre);
	printf("Hola %s!\n", nombre);
	return 0;
}
