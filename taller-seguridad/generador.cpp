
#include<cstring>
#include<cstdlib>
#include<iostream>

using namespace std;

int main(int argc, char** argv)
{
	int cantidad = atoi( argv[1] );

	for( int i = 0 ; i < cantidad ; i++ )
	{
		cout << (char)0xfa;
	}

	return 0;
}
