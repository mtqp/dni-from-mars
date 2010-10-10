#include <stdio.h>

void saludo(void) {
	char nombre[80];
	printf("Ingrese su nombre: ");
	gets(nombre);
	printf("Hola %s!\n", nombre);
}

int main(int argc, char* argv[]) {
	int temp[10240]; printf("esp = %.8X\n", (int)temp);
	saludo();
	return 0;
}
