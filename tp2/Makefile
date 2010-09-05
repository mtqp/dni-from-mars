BIN=nofork
SOURCES=$(patsubst %,%.c,$(BIN))

all: $(BIN)

%: %.c
	$(CC) $(CFLAGS) -o $@ $<

clean:
	rm $(BIN)

entrega: $(SOURCES) Makefile informe.pdf mister 
	tar zcvf grupo-20-taller-syscalls.tar.gz $^

