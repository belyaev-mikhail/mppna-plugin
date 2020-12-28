#!/bin/bash
cat > $1/libmylib.def <<EOF
headers = mylib.h
headerFilter = *
compilerOpts = -I$1/include
linkerOpts = -L$1/lib -rpath=$1/lib -lmylib
EOF
gcc -I $1/include -c $1/lib/mylib.c -o $1/lib/mylib.o
gcc -shared -o $1/lib/libmylib.so $1/lib/mylib.o
