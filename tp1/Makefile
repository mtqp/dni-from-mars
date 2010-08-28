BIN=mt_server client mt_server_tcp client_tcp

all: $(BIN)

%: %.o mt.h
	$(CC) $(CFLAGS) -o $@ $<

%.o: %.c

clean:
	rm $(BIN)

