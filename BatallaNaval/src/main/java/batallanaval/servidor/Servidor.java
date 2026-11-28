package batallanaval.servidor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import batallanaval.protocolo.Mensaje;
import batallanaval.protocolo.TipoMensaje;
import batallanaval.util.Colores;

public class Servidor implements AutoCloseable {

    private final ServerSocket server;
    private volatile boolean ejecutando = true;

    private final ConcurrentMap<String, Jugador> jugadores = new ConcurrentHashMap<>();

    // Sala de espera: un jugador esperando pareja. Se protege con su propio candado.
    private final Object salaEspera = new Object();
    private Jugador enEspera;

    public Servidor(int puerto) throws IOException {
        server = new ServerSocket(puerto);
    }

    public void iniciar() {
        System.out.println(Colores.GREEN + "Servidor Batalla Naval escuchando en el puerto "
                + server.getLocalPort() + Colores.RESET);

        while (ejecutando) {
            try {
                Socket socket = server.accept();
                System.out.println(Colores.AZUL + "[CONEXIÓN] "
                        + socket.getInetAddress().getHostAddress() + Colores.RESET);
                // Un hilo por cliente.
                new Thread(new Jugador(socket, this), "JUGADOR-" + socket.getPort()).start();
            } catch (IOException e) {
                if (ejecutando) {
                    System.err.println(Colores.ROJO + "[ERROR] " + e.getMessage() + Colores.RESET);
                }
                break;
            }
        }
    }

    public static boolean nickValido(String nick) {
        return nick != null && nick.matches("[\\p{L}\\p{N}_-]{1,15}");
    }

    public boolean registrar(String nick, Jugador j) {
        return jugadores.putIfAbsent(nick.toLowerCase(), j) == null;
    }

    public void desconectar(Jugador j) {
        if (j.getNick() != null) {
            jugadores.remove(j.getNick().toLowerCase(), j);
            System.out.println(Colores.AMARILLO + "[DESCONECTADO] " + j.getNick() + Colores.RESET);
        }
        synchronized (salaEspera) {
            if (enEspera == j) enEspera = null;
        }
    }

    /** Sala de espera: empareja de a dos; cada par obtiene su propia Partida. */
    public void emparejar(Jugador j) {
        Jugador rival = null;
        synchronized (salaEspera) {
            if (enEspera != null && enEspera.isConectado()) {
                rival = enEspera;
                enEspera = null;
            } else {
                enEspera = j;
            }
        }
        if (rival == null) {
            j.enviar(new Mensaje(TipoMensaje.ESPERA, "Esperando a otro jugador..."));
        } else {
            new Partida(rival, j).iniciar();          // fuera del candado de la sala
        }
    }

    @Override
    public synchronized void close() {
        if (!ejecutando) return;           // el hook de cierre y try-with-resources pueden llamarlo dos veces
        ejecutando = false;
        try {
            if (!server.isClosed()) server.close();
        } catch (IOException e) {
            System.err.println("Error cerrando el servidor.");
        }
        for (Jugador j : jugadores.values()) j.cerrar();
        System.out.println(Colores.ROJO + "Servidor detenido." + Colores.RESET);
    }
}
