package batallanaval.cliente;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import batallanaval.modelo.Flota;
import batallanaval.modelo.Tablero;
import batallanaval.protocolo.Mensaje;
import batallanaval.protocolo.PosicionBarco;
import batallanaval.protocolo.TipoMensaje;
import batallanaval.util.Colores;

public class Cliente {

    private final String host;
    private final int puerto;
    private final BufferedReader teclado = new BufferedReader(new InputStreamReader(System.in));

    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Socket socket;

    private Tablero propio = new Tablero();
    private final Tablero enemigo = new Tablero();   // solo lo que el servidor nos informó
    private String nick;
    private String nickOponente = "Oponente";

    private volatile boolean miTurno = false;
    private final AtomicBoolean terminado = new AtomicBoolean(false);
    private boolean entradaIniciada = false;

    public Cliente(String host, int puerto) {
        this.host = host;
        this.puerto = puerto;
    }

    public void jugar() {
        try (Socket s = new Socket(host, puerto)) {
            socket = s;
            // Orden importante: primero OutputStream, luego InputStream.
            out = new ObjectOutputStream(s.getOutputStream());
            out.flush();
            in = new ObjectInputStream(s.getInputStream());

            System.out.println(Colores.TURQUESA + "Conectado a " + host + ":" + puerto + Colores.RESET);
            if (!login()) return;

            while (!terminado.get()) {
                Mensaje m = (Mensaje) in.readObject();
                manejar(m);
            }
        } catch (EOFException | SocketException e) {
            if (!terminado.get()) {
                System.out.println(Colores.CORAL + "\nSe perdió la conexión con el servidor." + Colores.RESET);
            }
        } catch (IOException e) {
            System.err.println(Colores.CORAL + "Error de conexión: " + e.getMessage() + Colores.RESET);
        } catch (ClassNotFoundException e) {
            System.err.println(Colores.CORAL + "Mensaje desconocido del servidor." + Colores.RESET);
        }
        System.out.println(Colores.ORO + "Desconectado." + Colores.RESET);
        System.exit(0);   // el hilo de teclado puede seguir bloqueado en readLine()
    }

    // ------------------------------------------------------------------ login

    private boolean login() throws IOException, ClassNotFoundException {
        while (true) {
            System.out.print(Colores.ORO + "Nickname: " + Colores.RESET);
            String n = teclado.readLine();
            if (n == null) return false;
            enviar(new Mensaje(TipoMensaje.LOGIN, n.trim()));

            Mensaje r = (Mensaje) in.readObject();
            if (r.getTipo() == TipoMensaje.LOGIN_OK) {
                nick = n.trim();
                System.out.println(Colores.LIMA + r.getTexto() + Colores.RESET);
                return true;
            }
            System.out.println(Colores.CORAL + r.getTexto() + Colores.RESET);
        }
    }

    // ------------------------------------------------------------------ mensajes del servidor

    private void manejar(Mensaje m) throws IOException {
        switch (m.getTipo()) {
            case ESPERA:
                System.out.println(Colores.ORO + "~ Sala de espera: " + m.getTexto() + Colores.RESET);
                break;
            case PARTIDA_ENCONTRADA:
                nickOponente = m.getTexto();
                System.out.println(Colores.LIMA + "Rival asignado: " + nickOponente + Colores.RESET);
                break;
            case COLOCAR_BARCOS:
                colocarBarcos();
                break;
            case ESPERAR_OPONENTE:
                System.out.println(Colores.ORO + "Flota lista. Esperando que el oponente termine..." + Colores.RESET);
                break;
            case INICIO_JUEGO:
                System.out.println(Colores.LIMA + "\n╔════════════════════════════╗\n║  ¡ZAFARRANCHO DE COMBATE!  ║\n╚════════════════════════════╝" + Colores.RESET);
                System.out.println("Disparo: coordenada (ej: B5). Chat: /chat mensaje. Salir: /salir");
                mostrarTableros();
                iniciarEntrada();
                break;
            case TU_TURNO:
                miTurno = true;
                System.out.println(Colores.LIMA + "» Tu turno, capitán. Elegí una coordenada (ej: C7):" + Colores.RESET);
                break;
            case TURNO_OPONENTE:
                miTurno = false;
                System.out.println(Colores.GRIS + "... esperando la jugada de " + nickOponente + Colores.RESET);
                break;
            case RESULTADO_DISPARO:
                miTurno = false;
                enemigo.registrarResultado(m.getFila(), m.getCol(), m.getResultado(), m.getTexto());
                System.out.println("Tu salva sobre " + coord(m) + ": " + colorear(m.getResultado())
                        + (m.getTexto() != null ? " (" + m.getTexto() + ")" : ""));
                mostrarTableros();
                break;
            case DISPARO_RECIBIDO:
                propio.disparar(m.getFila(), m.getCol());   // mantiene sincronizada nuestra flota
                System.out.println(nickOponente + " apuntó a " + coord(m) + ": " + colorear(m.getResultado())
                        + (m.getTexto() != null ? " (tu " + m.getTexto() + ")" : ""));
                mostrarTableros();
                break;
            case FIN_PARTIDA:
                if ("VICTORIA".equals(m.getResultado())) {
                    enemigo.verificarVictoria();
                } else {
                    propio.verificarDerrota();
                }
                System.out.println("\nTu flota:");
                propio.mostrarBarcos();
                terminado.set(true);
                break;
            case OPONENTE_DESCONECTADO:
                System.out.println(Colores.ORO + m.getTexto() + " ¡Ganás por abandono!" + Colores.RESET);
                terminado.set(true);
                break;
            case CHAT:
                System.out.println(Colores.LILA + "<chat> " + m.getRemitente() + ": " + Colores.RESET + m.getTexto());
                break;
            case ERROR:
                System.out.println(Colores.CORAL + "! " + m.getTexto() + Colores.RESET);
                break;
            default:
                System.out.println("Mensaje del servidor: " + m.getTipo());
        }
    }

    // ------------------------------------------------------------------ colocación

    private void colocarBarcos() throws IOException {
        propio = new Tablero();
        ArrayList<PosicionBarco> posiciones = new ArrayList<>();
        Random azar = new Random();
        boolean auto = false;

        System.out.println(Colores.TURQUESA + "\n░░ DESPLIEGUE DE LA FLOTA ░░" + Colores.RESET);
        System.out.println("Formato: coordenada + orientación (H horizontal / V vertical). Ej: A1 H");
        System.out.println("Escribí 'auto' para colocar lo que falta al azar.");

        for (int i = 0; i < Flota.CANTIDAD; i++) {
            String nombre = Flota.NOMBRES[i];
            int largo = Flota.LONGITUDES[i];
            boolean puesto = false;

            while (!puesto) {
                if (auto) {
                    int f = azar.nextInt(Tablero.TAMANIO), c = azar.nextInt(Tablero.TAMANIO);
                    boolean h = azar.nextBoolean();
                    if (propio.colocarBarco(nombre, f, c, largo, h)) {
                        posiciones.add(new PosicionBarco(nombre, f, c, largo, h));
                        puesto = true;
                    }
                    continue;
                }
                propio.mostrarTablero(false);
                System.out.print(Colores.ORO + nombre + " (" + largo + " casillas) > " + Colores.RESET);
                String linea = teclado.readLine();
                if (linea == null) throw new EOFException();
                linea = linea.trim();

                if (linea.equalsIgnoreCase("auto")) { auto = true; continue; }

                String[] partes = linea.split("\\s+");
                int[] pos = partes.length == 2 ? parsearCoordenada(partes[0]) : null;
                boolean horizontal = partes.length == 2 && partes[1].equalsIgnoreCase("H");
                boolean vertical = partes.length == 2 && partes[1].equalsIgnoreCase("V");

                if (pos == null || !(horizontal || vertical)) {
                    System.out.println(Colores.CORAL + "Formato inválido. Ej: A1 H" + Colores.RESET);
                } else if (!propio.colocarBarco(nombre, pos[0], pos[1], largo, horizontal)) {
                    System.out.println(Colores.CORAL + "No entra o se superpone con otro barco." + Colores.RESET);
                } else {
                    posiciones.add(new PosicionBarco(nombre, pos[0], pos[1], largo, horizontal));
                    puesto = true;
                }
            }
        }
        System.out.println(Colores.LIMA + "\nTu flota:" + Colores.RESET);
        propio.mostrarTablero(false);
        enviar(new Mensaje(TipoMensaje.BARCOS_LISTOS).barcos(posiciones));
    }

    // ------------------------------------------------------------------ entrada durante la partida

    private void iniciarEntrada() {
        if (entradaIniciada) return;
        entradaIniciada = true;
        Thread t = new Thread(this::leerTeclado, "ENTRADA-TECLADO");
        t.setDaemon(true);
        t.start();
    }

    private void leerTeclado() {
        try {
            String linea;
            while (!terminado.get() && (linea = teclado.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty()) continue;

                if (linea.equalsIgnoreCase("/salir")) {
                    enviar(new Mensaje(TipoMensaje.SALIR));
                    terminado.set(true);
                    socket.close();
                    return;
                }
                if (linea.toLowerCase().startsWith("/chat ")) {
                    enviar(new Mensaje(TipoMensaje.CHAT, linea.substring(6)));
                    continue;
                }
                int[] pos = parsearCoordenada(linea);
                if (pos == null) {
                    System.out.println(Colores.CORAL + "Coordenada inválida (A-J + 1-10). Chat: /chat texto" + Colores.RESET);
                } else if (!miTurno) {
                    System.out.println(Colores.GRIS + "Todavía no es tu turno." + Colores.RESET);
                } else {
                    enviar(new Mensaje(TipoMensaje.DISPARO).coordenada(pos[0], pos[1]));
                }
            }
        } catch (IOException e) {
            // socket cerrado: el hilo principal se encarga del mensaje
        }
    }

    // ------------------------------------------------------------------ utilidades

    private synchronized void enviar(Mensaje m) throws IOException {
        out.writeObject(m);
        out.flush();
        out.reset();
    }

    /** "B5" -> {fila 4, columna 1}; null si es inválida. */
    private int[] parsearCoordenada(String s) {
        if (s == null || !s.matches("(?i)[A-J](10|[1-9])")) return null;
        int col = Character.toUpperCase(s.charAt(0)) - 'A';
        int fila = Integer.parseInt(s.substring(1)) - 1;
        return new int[] {fila, col};
    }

    private String coord(Mensaje m) {
        return "" + (char) ('A' + m.getCol()) + (m.getFila() + 1);
    }

    private String colorear(String resultado) {
        if (Tablero.RES_AGUA.equals(resultado)) return Colores.AGUA_PROFUNDA + resultado + Colores.RESET;
        if (Tablero.RES_IMPACTO.equals(resultado)) return Colores.ORO + resultado + Colores.RESET;
        return Colores.CORAL + resultado + Colores.RESET;
    }

    private void mostrarTableros() {
        ArrayList<String> izq = propio.renderizar(false);
        ArrayList<String> der = enemigo.renderizar(true);
        System.out.println();
        System.out.printf("%-" + Tablero.ANCHO_VISIBLE + "s      %-" + Tablero.ANCHO_VISIBLE + "s%n",
                "  FLOTA DE " + nick.toUpperCase(), "  RADAR · " + nickOponente.toUpperCase());
        for (int i = 0; i < izq.size(); i++) {
            System.out.println(izq.get(i) + "      " + der.get(i));
        }
        System.out.println();
    }
}
