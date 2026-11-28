package batallanaval.servidor;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicBoolean;

import batallanaval.modelo.Tablero;
import batallanaval.protocolo.Mensaje;
import batallanaval.protocolo.TipoMensaje;
import batallanaval.util.Colores;

/** Atiende a UN cliente en su propio hilo. */
public class Jugador implements Runnable {

    private final Socket socket;
    private final Servidor servidor;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private final AtomicBoolean conectado = new AtomicBoolean(true);

    private volatile String nick;
    private volatile Partida partida;

    // Estado de juego: solo se toca dentro de bloques synchronized de Partida.
    private Tablero tablero;
    private boolean listo;
    private int disparos;

    public Jugador(Socket socket, Servidor servidor) {
        this.socket = socket;
        this.servidor = servidor;
    }

    @Override
    public void run() {
        try {
            // Orden importante: primero OutputStream, luego InputStream (evita bloqueo cruzado).
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());

            if (!autenticar()) return;
            servidor.emparejar(this);

            while (conectado.get()) {
                Mensaje m = (Mensaje) in.readObject();
                if (m.getTipo() == TipoMensaje.SALIR) break;

                Partida p = partida;
                if (p == null) {
                    enviar(new Mensaje(TipoMensaje.ERROR, "Todavía no tenés oponente."));
                } else {
                    p.procesar(this, m);
                }
            }
        } catch (EOFException | SocketException e) {
            // El cliente cerró la conexión: caso normal.
        } catch (IOException | ClassNotFoundException e) {
            System.err.println(Colores.AMARILLO + "[AVISO] " + describir() + ": " + e + Colores.RESET);
        } finally {
            cerrar();
        }
    }

    private boolean autenticar() throws IOException, ClassNotFoundException {
        for (int intento = 0; intento < 5; intento++) {
            Mensaje m = (Mensaje) in.readObject();
            if (m.getTipo() != TipoMensaje.LOGIN) {
                enviar(new Mensaje(TipoMensaje.LOGIN_ERROR, "Primero tenés que identificarte."));
                continue;
            }
            String candidato = m.getTexto() == null ? "" : m.getTexto().trim();
            if (!Servidor.nickValido(candidato)) {
                enviar(new Mensaje(TipoMensaje.LOGIN_ERROR,
                        "Nickname inválido (1 a 15 letras, números, _ o -)."));
            } else if (!servidor.registrar(candidato, this)) {
                enviar(new Mensaje(TipoMensaje.LOGIN_ERROR, "Ese nickname ya está en uso."));
            } else {
                nick = candidato;
                enviar(new Mensaje(TipoMensaje.LOGIN_OK, "Bienvenido " + candidato));
                System.out.println(Colores.VERDE + "[CONECTADO] " + candidato + Colores.RESET);
                return true;
            }
        }
        return false;
    }

    /** Un solo hilo escribe a la vez en el mismo stream. */
    public boolean enviar(Mensaje m) {
        if (!conectado.get()) return false;
        synchronized (this) {
            try {
                out.writeObject(m);
                out.flush();
                out.reset();
                return true;
            } catch (IOException e) {
                cerrar();
                return false;
            }
        }
    }

    public void cerrar() {
        if (!conectado.compareAndSet(true, false)) return;
        try {
            socket.close();
        } catch (IOException e) {
            System.err.println("Error cerrando el socket de " + describir());
        }
        servidor.desconectar(this);
        Partida p = partida;
        if (p != null) p.jugadorDesconectado(this);
    }

    private String describir() {
        return nick != null ? nick : socket.getInetAddress().getHostAddress();
    }

    public String getNick() { return nick; }
    public boolean isConectado() { return conectado.get(); }
    public void setPartida(Partida p) { this.partida = p; }

    Tablero getTablero() { return tablero; }
    void setTablero(Tablero t) { this.tablero = t; this.listo = true; }
    boolean isListo() { return listo; }
    int getDisparos() { return disparos; }
    void sumarDisparo() { disparos++; }
}
