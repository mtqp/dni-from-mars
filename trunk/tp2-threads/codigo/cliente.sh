for i in {1..15}
do
	echo -e "cliente$i\n0\n0\n3\n" | ./cliente 127.0.0.1 &
done
