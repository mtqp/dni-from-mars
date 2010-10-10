CFLAGS=-fno-stack-protector -Wall -O0 -g -ggdb
NASMFLAGS=-felf -g -ggdb
LDFLAGS=-static
CC=gcc
CXX=g++

BINS=hola hola2 fs entrega
OBJS=$(BINS:=.o) md5.o

all: $(BINS)

# Dependencias

entrega: entrega.o md5.o

clean:
	rm -rf $(OBJS) $(BINS)
