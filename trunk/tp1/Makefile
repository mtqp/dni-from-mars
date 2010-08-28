BIN=mt_server client mt_server_tcp client_tcp

all: $(BIN)

%: %.o
	$(CC) $(CFLAGS) -o $@ $<

%.o: %.c

clean:
	rm $(BIN)

