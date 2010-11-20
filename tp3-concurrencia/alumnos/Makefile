.PHONY : all


CFLAGS+= -Wall -g $(INCL)
CC= 	gcc
MPIROOT= /usr/lib/mpich
MPICC=	$(MPIROOT)/bin/mpicc

INCL= -I$(MPIROOT)/include
SRCS= exclusion.c
OBJS= $(SRCS:%.c=%.o)
BIN= exclusion

all: exclusion

$(BIN): $(OBJS)
	$(MPICC) $(CFLAGS) -o $(BIN) $(OBJS) $(LIBS)

clean:
	rm -f $(OBJS) $(BIN)
