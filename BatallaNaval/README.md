# Batalla Naval multijugador por consola (Java, Sockets TCP)

Servidor + cliente por consola, 2 jugadores por partida, varias partidas simultáneas.
Usa solo lo visto en clase: `ServerSocket`/`Socket`, `Thread` (uno por cliente), `ConcurrentHashMap`,
`AtomicBoolean`, `synchronized`, `ObjectInputStream`/`ObjectOutputStream` con objetos `Serializable`,
y la clase `Colores` (ANSI) del material.

## Estructura

```
src/main/java/batallanaval/
  modelo/     TipoCelda, Celda, Barco, Tablero, Flota        (reglas del juego)
  protocolo/  Mensaje, TipoMensaje, PosicionBarco            (objetos serializables que viajan por la red)
  servidor/   MainServidor, Servidor, Jugador, Partida, Historial
  cliente/    MainCliente, Cliente
  util/       Colores
Dockerfile, docker-compose.yml                               (servidor dockerizado)
compilar.sh, cliente.sh, cliente.jar, servidor.jar
```

## Cómo funciona

* **Servidor**: `Servidor` acepta conexiones y lanza un hilo `Jugador` por cada una. Luego del login
  (nickname único) el jugador entra a la **sala de espera**; al llegar el segundo se crea una `Partida`
  para ese par. Cada partida es independiente, así que puede haber muchas a la vez.
* **Consistencia entre hilos**: los dos hilos de una partida entran por `Partida.procesar()`, que es
  `synchronized`; la escritura en cada socket también está sincronizada.
* **Validación**: el servidor reconstruye la flota recibida en su propio `Tablero`, valida turnos y
  coordenadas, y es quien decide agua / impacto / hundido y el ganador. El cliente nunca ve los barcos del rival.
* **Cliente**: pide IP y puerto (o `./cliente.sh IP PUERTO`), coloca los barcos (manual o `auto`), muestra
  tu flota y el tablero enemigo lado a lado, y dispara en tu turno.
* **Extras**: sala de espera, chat (`/chat hola`), historial en `historial.txt`, múltiples partidas,
  victoria por abandono si el oponente se desconecta.

### Protocolo (todo es un `Mensaje` serializado)

| Mensaje | Sentido | Datos |
|---|---|---|
| LOGIN / LOGIN_OK / LOGIN_ERROR | C→S / S→C | nickname |
| ESPERA, PARTIDA_ENCONTRADA | S→C | nick del oponente |
| COLOCAR_BARCOS, BARCOS_LISTOS | S→C, C→S | lista de `PosicionBarco` |
| ESPERAR_OPONENTE, INICIO_JUEGO | S→C | |
| TU_TURNO, TURNO_OPONENTE | S→C | |
| DISPARO | C→S | fila, columna |
| RESULTADO_DISPARO / DISPARO_RECIBIDO | S→C | fila, columna, resultado, barco hundido |
| FIN_PARTIDA, OPONENTE_DESCONECTADO | S→C | VICTORIA / DERROTA |
| CHAT, ERROR, SALIR | | texto |

## Ejecutar sin Docker

```sh
./compilar.sh                       # necesita JDK 17+
java -jar servidor.jar 5555         # una terminal
./cliente.sh localhost 5555         # otras dos terminales
```

Coordenadas: columna A-J + fila 1-10 (`B5`). Colocación: `A1 H` (horizontal) o `D4 V` (vertical); `auto` completa al azar.

## Servidor con Docker Compose

```sh
docker compose up -d --build
docker compose logs -f
docker compose exec servidor cat /app/data/historial.txt   # historial de partidas
docker compose down
```

El puerto 5555 queda publicado en el host; el historial vive en un volumen.

## VM con Alpine (ISO `alpine-virt-3.24.4-x86_64.iso`)

1. Crear la VM (VirtualBox / VMware / QEMU): 1 CPU, 1 GB RAM, disco 4 GB, red en **modo puente** (así
   ve la IP de la PC donde corre Docker). Arrancar desde la ISO.
2. Entrar como `root` (sin contraseña) y ejecutar `setup-alpine`. Elegir teclado, hostname, DHCP, crear un
   usuario, `openssh` como servidor SSH y `sys` como modo de instalación en disco. Reiniciar y quitar la ISO.
3. Habilitar el repositorio *community* (ahí está el JDK): `vi /etc/apk/repositories`, descomentar la línea
   `community`, luego `apk update`.
4. Instalar Java (equivalente de `apt install openjdk-17-jdk` en Alpine):
   ```sh
   apk add openjdk17-jre-headless     # alcanza para el cliente; para JDK completo: apk add openjdk17
   java -version
   ```
   Si el paquete no aparece con ese nombre: `apk search openjdk`.
5. Copiar el cliente desde la PC: `scp cliente.jar cliente.sh usuario@IP_DE_LA_VM:~`
6. Dentro de la VM: `export LANG=C.UTF-8` y `sh cliente.sh IP_DE_LA_PC 5555`.
7. Probar: otro cliente en la PC (`./cliente.sh localhost 5555`) y jugar contra el de la VM.

Si no conecta: revisar que el firewall de la PC permita el puerto 5555 y que `ip a` en la VM muestre una IP de la misma red.

## Qué se probó

Se probó el servidor en Docker Compose y el cliente en una VM Alpine Linux (OpenJDK 17), jugando una partida entre la VM y la PC. Capturas en la carpeta Capturas/
