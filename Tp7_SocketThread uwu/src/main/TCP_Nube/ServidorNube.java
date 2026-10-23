package main.TCP_Nube;

import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * SERVIDOR DE NUBE
 *
 * Lógica de hilos:
 *  - El hilo principal (main) SOLO hace accept() en un bucle infinito.
 *  - Cada conexión entrante se entrega a un hilo nuevo (ManejadorCliente),
 *    de modo que el accept() queda libre enseguida y se pueden atender
 *    muchos usuarios a la vez sin bloquearse entre sí.
 *
 * Autenticación: las credenciales válidas están en el mapa USUARIOS.
 * Almacenamiento: todos los archivos viven en la carpeta local "NUBE".
 */
public class ServidorNube implements AutoCloseable {

    public static final int PUERTO = 5555;
    public static final String CARPETA = "NUBE";

    // Credenciales del servidor (usuario -> clave).
    private static final Map<String, String> USUARIOS = Map.of(
            "admin", "1234",
            "alumno", "clave123",
            "invitado", "0000"
    );

    private static final DateTimeFormatter HORA =
            DateTimeFormatter.ofPattern("HH:mm:ss");

    private final ServerSocket server;
    private final File carpeta;
    private final AtomicInteger conectados = new AtomicInteger();
    private volatile boolean ejecutando = true;

    public ServidorNube(int puerto) throws IOException {
        // Si no existe, creamos la carpeta donde se guardan los recursos.
        carpeta = new File(CARPETA);
        if (!carpeta.exists() && !carpeta.mkdirs()) {
            throw new IOException("No se pudo crear la carpeta " + CARPETA);
        }
        server = new ServerSocket(puerto);
    }

    public void iniciar() {
        log(Colores.VERDE, "SERVIDOR", "Escuchando en el puerto "
                + server.getLocalPort());
        log(Colores.VERDE, "SERVIDOR", "Carpeta de la nube: "
                + carpeta.getAbsolutePath());
        mostrarIPs();

        while (ejecutando) {
            try {
                // accept() bloquea hasta que llega un cliente.
                Socket socket = server.accept();

                int total = conectados.incrementAndGet();
                log(Colores.AZUL, "CONEXIÓN", "Cliente "
                        + socket.getInetAddress().getHostAddress() + ":"
                        + socket.getPort() + " (conectados: " + total + ")");

                // Un hilo independiente por cliente: el bucle vuelve
                // inmediatamente al accept() para esperar al siguiente.
                Thread hilo = new Thread(
                        new ManejadorCliente(socket, this),
                        "CLIENTE-" + socket.getInetAddress().getHostAddress()
                                + ":" + socket.getPort()
                );
                hilo.start();

            } catch (IOException e) {
                if (ejecutando) {
                    log(Colores.ROJO, "ERROR", e.getMessage());
                }
                break;
            }
        }
    }

    /** Valida usuario y clave contra las credenciales del servidor. */
    public boolean credencialesValidas(String usuario, String clave) {
        String esperada = USUARIOS.get(usuario);
        return esperada != null && esperada.equals(clave);
    }

    public File getCarpeta() {
        return carpeta;
    }

    /** Lo llama cada ManejadorCliente al terminar su sesión. */
    public void clienteDesconectado() {
        conectados.decrementAndGet();
    }

    /**
     * Log de consola. Es synchronized para que dos hilos no mezclen
     * sus líneas al imprimir a la vez.
     */
    public static synchronized void log(String color, String tipo, String msg) {
        System.out.println(color + "[" + LocalTime.now().format(HORA) + "]["
                + tipo + "][" + Thread.currentThread().getName() + "] "
                + msg + Colores.RESET);
    }

    /** Muestra las IPs de esta PC (sirve para que el cliente sepa a dónde conectar). */
    private void mostrarIPs() {
        try {
            for (NetworkInterface ni : Collections.list(
                    NetworkInterface.getNetworkInterfaces())) {
                if (!ni.isUp() || ni.isLoopback()) {
                    continue;
                }
                for (InetAddress ia : Collections.list(ni.getInetAddresses())) {
                    if (ia instanceof Inet4Address) {
                        log(Colores.CYAN, "SERVIDOR", "IP disponible: "
                                + ia.getHostAddress() + " (" + ni.getDisplayName() + ")");
                    }
                }
            }
        } catch (IOException e) {
            // Solo informativo; si falla no afecta al servidor.
        }
    }

    @Override
    public void close() {
        ejecutando = false;
        try {
            server.close();
        } catch (IOException e) {
            // Ya estaba cerrado.
        }
    }

    public static void main(String[] args) {
        int puerto = PUERTO;
        if (args.length > 0) {
            puerto = Integer.parseInt(args[0]);
        }

        try (ServidorNube servidor = new ServidorNube(puerto)) {
            servidor.iniciar();
        } catch (IOException e) {
            System.err.println("No se pudo iniciar el servidor: " + e.getMessage());
        }
    }
}
