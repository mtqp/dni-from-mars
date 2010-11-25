for i in {1..15}
do
	echo -e "cliente$i\n0\n0\n3\n" | ./cliente $1 &
done
