#include <stdio.h>
#include <string.h>
#include "md5.h"

void entrega(void) {
	char admin[32], hash[32], pass[64];

	memcpy(admin, "572ce02c83f90d47764f3f2c0c675ada", sizeof(admin));
	memset(pass, 0,  sizeof(pass));

	/* Pregunta el nombre del grupo */
	printf("Grupo: ");
	gets(pass);
	/* Muestra el nombre del grupo */
	fwrite(pass, 1, sizeof(pass), stdout);
	printf("\n");

	/* Calcula el hexadecimal (en minúscula) del nombre del grupo en digest */
	md5_buf_to_hex(pass, sizeof(pass), hash);

	/* Verifica el md5 */
	if (memcmp(hash, admin, 32) == 0) {
		printf("Nota: APROBADO\n");
	} else {
		printf("Nota: NO APROBADO\n");
	}
	return;
}

int main(void) {
	entrega();
	return 0;
}
