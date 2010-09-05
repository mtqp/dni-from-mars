#include <sys/ptrace.h>
#include <sys/wait.h>
#include <sys/reg.h>
#include <unistd.h>
#include <syscall.h>
#include <stdio.h>
#include <stdlib.h>
#include <errno.h>

int main(int argc, char** argv){
	int status;
	int child;
	int sys_no;

	if(argc<2){
		printf("Uso: %s comando\n", argv[0]);
		exit(-1);
	}
	child = fork();
	if (child == -1) { perror("ERROR fork"); return 1; }
	  /* S ́lo se ejecuta en el Hijo */
	if (child == 0) {
		if (ptrace(PTRACE_TRACEME, 0, NULL, NULL)) {
			perror("ERROR child ptrace(PTRACE TRACEME, ...)"); exit(1);
		}
	  execvp(argv[1], argv+1);	//CHEQUEAR SI VA ESTO O DE SEGUNDO PARAMETRO VA NULL

	  perror("ERROR child exec(...)"); exit(1);  /* Si vuelve de exec() hubo un error */
	} else {
	  /* S ́lo se ejecuta en el Padre */
		while(1) {
			if (wait(&status) < 0) { perror("wait"); break; }

			if (WIFEXITED(status))
				break; /* Proceso terminado */
			sys_no = ptrace(PTRACE_PEEKUSER, child, 4*ORIG_EAX, NULL);
			if(sys_no==SYS_fork || sys_no==SYS_clone){
				printf("Syscall prohibida\n");
				ptrace(PTRACE_KILL, child, NULL, NULL);
				exit(0);
			}
			ptrace(PTRACE_SYSCALL, child, NULL, NULL); /* contina */
		}
		ptrace(PTRACE_DETACH, child, NULL, NULL);/*Liberamos al hijo*/
	}
	return 0;
}
