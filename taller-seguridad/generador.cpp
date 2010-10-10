
#include<string.h>
#include<stdlib.h>
#include "md5.h"

int main(int argc, char** argv)
{
	int cantidad = atoi( argv[1] );
	char hash[32];
	char pass[] = "De Sousa Bispo, Grosso\0";
	int i;
	
	md5_buf_to_hex(pass, sizeof(pass), hash);

	for( i = 0 ; i < cantidad ; i++ )
	{
		printf('A');
	}
	cout << hash;

	return 0;
}
