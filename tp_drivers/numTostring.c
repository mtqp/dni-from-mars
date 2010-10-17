#include <stdlib.h>
#include <stdio.h>

char charToString (int num){
	return (num%10)+48;
}

void assign_value(char* num_string, int* i, int num){
	num_string[*i] = charToString(num);
	(*i)++;
}

#define really_big_buf 4096

int main(int argc, char** argv){
	//supongamos que siempre le paso el numero q quiero que me convierta, no chequeo error
	int i;
	//int num = (int) argv[1];
	int num = 123456789;//hecho harcodeado, dsp nos ocupamos de el argv
	int print_num = num;
	int counter_string_size = 0;
	char* num_string = malloc(really_big_buf);

	//realizo conversion
	if(num<10){
		assign_value(num_string, &counter_string_size, num);
	}
	else {
		while(num>10){
			assign_value(num_string, &counter_string_size, num);
			num /= 10;
		}
		assign_value(num_string, &counter_string_size, num);
	}
	
	//Aplico reverso
	char* without_shit_num_string = malloc(counter_string_size);
	for(i=0;i<counter_string_size;i++){
		without_shit_num_string[i] = num_string[counter_string_size-i-1];	//fijarse q no le haya errado
	}
		
	printf("Have we parsed correctly???\n\tNumber %d = %s String\n",print_num,without_shit_num_string);
	
	return 0;
}
