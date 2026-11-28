#!/bin/sh
# Compila y arma cliente.jar / servidor.jar (requiere JDK 17+)
set -e
rm -rf out && mkdir out
javac -encoding UTF-8 -d out $(find src -name '*.java')
jar cfe cliente.jar batallanaval.cliente.MainCliente -C out .
jar cfe servidor.jar batallanaval.servidor.MainServidor -C out .
echo "Listo: cliente.jar y servidor.jar"
