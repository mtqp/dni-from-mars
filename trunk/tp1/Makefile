BIN=mt_server client

all: $(BIN)

%: %.o
	$(CC) $(CFLAGS) -o $@ $<

%.o: %.c

clean:
	rm $(BIN)

