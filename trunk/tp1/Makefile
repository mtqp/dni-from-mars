BIN=mt_server client mt_server_tcp client_tcp
SOURCES=$(patsubst %,%.c,$(BIN))
HEADERS=mt.h

all: $(BIN)

%: %.c mt.h
	$(CC) $(CFLAGS) -o $@ $<

clean:
	rm $(BIN)

entrega: $(ARCHIVOS) $(HEADER) Makefile informe.pdf
	tar zcvf grupo-20-taller-ipc.tar.gz $(SOURCES) $(HEADERS) Makefile informe.pdf

