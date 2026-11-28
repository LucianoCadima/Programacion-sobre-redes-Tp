#!/bin/sh
# Uso: ./cliente.sh [ip puerto]
exec java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -jar "$(dirname "$0")/cliente.jar" "$@"
