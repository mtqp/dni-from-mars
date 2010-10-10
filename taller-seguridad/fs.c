#include <stdio.h>

#define MAX_BUF 2048

void echo(void) {
	char echobuf[MAX_BUF];
	fgets(echobuf, MAX_BUF, stdin);
	printf(echobuf);
}

int main(int argc, char* argv[]) {
	echo();
	echo();
	return 0;
}

