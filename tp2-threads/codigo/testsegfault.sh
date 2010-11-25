./servidor_multi &
echo -e "cliente1\n0\n0\n3\n" | ./cliente 127.0.0.1
echo -e "cliente2\n0\n0\n3\n" | ./cliente 127.0.0.1
pkill servidor_multi
