package batallanaval.servidor;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import batallanaval.modelo.Flota;
import batallanaval.modelo.Tablero;
import batallanaval.protocolo.Mensaje;
import batallanaval.protocolo.PosicionBarco;
import batallanaval.protocolo.TipoMensaje;
import batallanaval.util.Colores;

/**
 * Sala de juego de dos jugadores. Los dos hilos de los clientes llaman a procesar():
 * todos los métodos públicos son synchronized para mantener el estado consistente.
 */
public class Partida {

    private enum Estado { COLOCANDO, JUGANDO, FINALIZADA }

    private static final AtomicInteger CONTADOR = new AtomicInteger();

    private final int id = CONTADOR.incrementAndGet();
    private final Jugador j1;
    private final Jugador j2;
    private Estado estado = Estado.COLOCANDO;
    private Jugador turno;

    public Partida(Jugador j1, Jugador j2) {
        this.j1 = j1;
        this.j2 = j2;
    }

    public synchronized void iniciar() {
        j1.setPartida(this);
        j2.setPartida(this);
        System.out.println(Colores.CYAN + "[PARTIDA " + id + "] " + j1.getNick()
                + " vs " + j2.getNick() + Colores.RESET);

        if (!j1.isConectado()) { jugadorDesconectado(j1); return; }
        if (!j2.isConectado()) { jugadorDesconectado(j2); return; }

        j1.enviar(new Mensaje(TipoMensaje.PARTIDA_ENCONTRADA, j2.getNick()));
        j2.enviar(new Mensaje(TipoMensaje.PARTIDA_ENCONTRADA, j1.getNick()));
        j1.enviar(new Mensaje(TipoMensaje.COLOCAR_BARCOS));
        j2.enviar(new Mensaje(TipoMensaje.COLOCAR_BARCOS));
    }

    public synchronized void procesar(Jugador j, Mensaje m) {
        switch (m.getTipo()) {
            case CHAT:
                String texto = m.getTexto() == null ? "" : m.getTexto().trim();
                if (texto.length() > 200) texto = texto.substring(0, 200);
                if (!texto.isEmpty()) {
                    oponente(j).enviar(new Mensaje(TipoMensaje.CHAT, texto).remitente(j.getNick()));
                }
                break;
            case BARCOS_LISTOS:
                recibirFlota(j, m);
                break;
            case DISPARO:
                recibirDisparo(j, m);
                break;
            default:
                j.enviar(new Mensaje(TipoMensaje.ERROR, "Mensaje no esperado: " + m.getTipo()));
        }
    }

    // ------------------------------------------------------------------ flota

    private void recibirFlota(Jugador j, Mensaje m) {
        if (estado != Estado.COLOCANDO || j.isListo()) {
            j.enviar(new Mensaje(TipoMensaje.ERROR, "Ahora no podés colocar barcos."));
            return;
        }
        Tablero t = validarFlota(m.getBarcos());
        if (t == null) {
            j.enviar(new Mensaje(TipoMensaje.ERROR, "Flota inválida: se vuelve a pedir."));
            j.enviar(new Mensaje(TipoMensaje.COLOCAR_BARCOS));
            return;
        }
        j.setTablero(t);
        j.enviar(new Mensaje(TipoMensaje.ESPERAR_OPONENTE));

        if (j1.isListo() && j2.isListo()) {
            estado = Estado.JUGANDO;
            turno = new Random().nextBoolean() ? j1 : j2;
            j1.enviar(new Mensaje(TipoMensaje.INICIO_JUEGO));
            j2.enviar(new Mensaje(TipoMensaje.INICIO_JUEGO));
            notificarTurno();
        }
    }

    /** Reconstruye la flota en un Tablero propio del servidor; null si no cumple las reglas. */
    private Tablero validarFlota(ArrayList<PosicionBarco> lista) {
        if (lista == null || lista.size() != Flota.CANTIDAD) return null;
        Tablero t = new Tablero();
        boolean[] usado = new boolean[Flota.CANTIDAD];
        for (PosicionBarco p : lista) {
            int idx = -1;
            for (int i = 0; i < Flota.CANTIDAD; i++) {
                if (!usado[i] && Flota.NOMBRES[i].equals(p.getNombre())
                        && Flota.LONGITUDES[i] == p.getLongitud()) {
                    idx = i;
                    break;
                }
            }
            if (idx < 0) return null;
            usado[idx] = true;
            if (!t.colocarBarco(p.getNombre(), p.getFila(), p.getCol(), p.getLongitud(), p.isHorizontal())) {
                return null;
            }
        }
        return t;
    }

    // ------------------------------------------------------------------ juego

    private void notificarTurno() {
        turno.enviar(new Mensaje(TipoMensaje.TU_TURNO));
        oponente(turno).enviar(new Mensaje(TipoMensaje.TURNO_OPONENTE));
    }

    private void recibirDisparo(Jugador j, Mensaje m) {
        if (estado != Estado.JUGANDO) {
            j.enviar(new Mensaje(TipoMensaje.ERROR, "La partida no está en juego."));
            return;
        }
        if (j != turno) {
            j.enviar(new Mensaje(TipoMensaje.ERROR, "No es tu turno."));
            return;
        }
        Jugador rival = oponente(j);
        Tablero tableroRival = rival.getTablero();
        String res = tableroRival.disparar(m.getFila(), m.getCol());

        if (Tablero.RES_INVALIDA.equals(res) || Tablero.RES_REPETIDA.equals(res)) {
            j.enviar(new Mensaje(TipoMensaje.ERROR, res + ". Probá de nuevo."));
            return;                                    // conserva el turno
        }
        j.sumarDisparo();
        String hundido = Tablero.RES_HUNDIDO.equals(res) ? tableroRival.getUltimoBarcoHundido() : null;

        j.enviar(new Mensaje(TipoMensaje.RESULTADO_DISPARO, hundido)
                .coordenada(m.getFila(), m.getCol()).resultado(res));
        rival.enviar(new Mensaje(TipoMensaje.DISPARO_RECIBIDO, hundido)
                .coordenada(m.getFila(), m.getCol()).resultado(res));

        if (tableroRival.todosLosBarcosCaidos()) {      // todos los barcos del rival caídos
            finalizar(j, rival);
        } else {
            turno = rival;
            notificarTurno();
        }
    }

    private void finalizar(Jugador ganador, Jugador perdedor) {
        estado = Estado.FINALIZADA;
        ganador.enviar(new Mensaje(TipoMensaje.FIN_PARTIDA, "¡Ganaste!").resultado("VICTORIA"));
        perdedor.enviar(new Mensaje(TipoMensaje.FIN_PARTIDA, "Perdiste.").resultado("DERROTA"));
        String linea = "Partida " + id + ": ganó " + ganador.getNick() + " (" + ganador.getDisparos()
                + " disparos) contra " + perdedor.getNick() + " (" + perdedor.getDisparos() + " disparos)";
        System.out.println(Colores.VERDE + "[FIN] " + linea + Colores.RESET);
        Historial.registrar(linea);
    }

    public synchronized void jugadorDesconectado(Jugador j) {
        if (estado == Estado.FINALIZADA) return;
        estado = Estado.FINALIZADA;
        Jugador otro = oponente(j);
        otro.enviar(new Mensaje(TipoMensaje.OPONENTE_DESCONECTADO, j.getNick() + " abandonó la partida."));
        String linea = "Partida " + id + ": " + j.getNick() + " abandonó; gana por abandono " + otro.getNick();
        System.out.println(Colores.AMARILLO + "[FIN] " + linea + Colores.RESET);
        Historial.registrar(linea);
    }

    private Jugador oponente(Jugador j) {
        return j == j1 ? j2 : j1;
    }
}
