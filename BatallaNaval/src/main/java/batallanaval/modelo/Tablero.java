package batallanaval.modelo;

import java.util.ArrayList;

import batallanaval.util.Colores;

public class Tablero {

    public static final int TAMANIO = 10;

    public static final String RES_AGUA = "Agua";
    public static final String RES_IMPACTO = "¡IMPACTO!";
    public static final String RES_HUNDIDO = "¡HUNDIDO!";
    public static final String RES_INVALIDA = "Posición inválida";
    public static final String RES_REPETIDA = "Ya disparaste en esa posición";

    private ArrayList<ArrayList<Celda>> tablero;
    private ArrayList<Barco> barcos;
    private String ultimoBarcoHundido;

    public Tablero() {
        inicializarTablero();
    }

    public void inicializarTablero() {
        tablero = new ArrayList<>();
        barcos = new ArrayList<>();
        ultimoBarcoHundido = null;
        for (int f = 0; f < TAMANIO; f++) {
            ArrayList<Celda> fila = new ArrayList<>();
            for (int c = 0; c < TAMANIO; c++) {
                fila.add(new Celda());
            }
            tablero.add(fila);
        }
    }

    private boolean posicionValida(int fila, int col) {
        return fila >= 0 && fila < TAMANIO && col >= 0 && col < TAMANIO;
    }

    public boolean colocarBarco(String nombre, int fila, int col, int longitud, boolean horizontal) {
        if (nombre == null || longitud <= 0) return false;
        if (buscarBarco(nombre) != null) return false;              // ya colocado

        int filaFin = horizontal ? fila : fila + longitud - 1;
        int colFin = horizontal ? col + longitud - 1 : col;
        if (!posicionValida(fila, col) || !posicionValida(filaFin, colFin)) return false;

        for (int i = 0; i < longitud; i++) {                        // colisiones
            int f = horizontal ? fila : fila + i;
            int c = horizontal ? col + i : col;
            if (tablero.get(f).get(c).getTipo() != TipoCelda.AGUA) return false;
        }
        for (int i = 0; i < longitud; i++) {
            int f = horizontal ? fila : fila + i;
            int c = horizontal ? col + i : col;
            Celda celda = tablero.get(f).get(c);
            celda.setTipo(TipoCelda.BARCO);
            celda.setNombreBarco(nombre);
        }
        barcos.add(new Barco(nombre, longitud));
        return true;
    }

    public String disparar(int fila, int col) {
        if (!posicionValida(fila, col)) return RES_INVALIDA;
        Celda celda = tablero.get(fila).get(col);
        if (celda.isDisparada()) return RES_REPETIDA;

        celda.setDisparada(true);
        if (celda.getTipo() == TipoCelda.BARCO) {
            celda.setTipo(TipoCelda.IMPACTO);
            Barco barco = buscarBarco(celda.getNombreBarco());
            barco.recibirImpacto();
            if (barco.estaHundido()) {
                ultimoBarcoHundido = barco.getNombre();
                return RES_HUNDIDO;
            }
            return RES_IMPACTO;
        }
        celda.setTipo(TipoCelda.FALLO);
        return RES_AGUA;
    }

    /**
     * Uso del cliente sobre el tablero "enemigo": no conoce los barcos, solo anota
     * lo que el servidor le informó.
     */
    public void registrarResultado(int fila, int col, String resultado, String barcoHundido) {
        if (!posicionValida(fila, col)) return;
        Celda celda = tablero.get(fila).get(col);
        celda.setDisparada(true);
        boolean toco = RES_IMPACTO.equals(resultado) || RES_HUNDIDO.equals(resultado);
        celda.setTipo(toco ? TipoCelda.IMPACTO : TipoCelda.FALLO);
        if (RES_HUNDIDO.equals(resultado) && barcoHundido != null) {
            Barco b = new Barco(barcoHundido, Flota.longitudDe(barcoHundido));
            for (int i = 0; i < b.getLongitud(); i++) b.recibirImpacto();
            barcos.add(b);
            ultimoBarcoHundido = barcoHundido;
        }
    }

    private Barco buscarBarco(String nombre) {
        for (Barco b : barcos) {
            if (b.getNombre().equals(nombre)) return b;
        }
        return null;
    }

    // ------------------------------------------------------------ visualización

    public static final int ANCHO_VISIBLE = 25;

    /** Cada línea mide exactamente ANCHO_VISIBLE caracteres visibles (sin contar los colores). */
    public ArrayList<String> renderizar(boolean ocultarBarcos) {
        ArrayList<String> lineas = new ArrayList<>();
        String borde = "─".repeat(21);

        StringBuilder cab = new StringBuilder("    ");
        for (int c = 0; c < TAMANIO; c++) cab.append((char) ('A' + c)).append(' ');
        lineas.add(Colores.NARANJA + Colores.NEGRITA + cab + " " + Colores.RESET);
        lineas.add(Colores.TURQUESA + "  ┌" + borde + "┐" + Colores.RESET);

        for (int f = 0; f < TAMANIO; f++) {
            StringBuilder sb = new StringBuilder();
            sb.append(Colores.NARANJA).append(String.format("%2d", f + 1))
              .append(Colores.TURQUESA).append("│ ").append(Colores.RESET);
            for (int c = 0; c < TAMANIO; c++) {
                sb.append(simbolo(tablero.get(f).get(c), ocultarBarcos)).append(' ');
            }
            sb.append(Colores.TURQUESA).append("│").append(Colores.RESET);
            lineas.add(sb.toString());
        }
        lineas.add(Colores.TURQUESA + "  └" + borde + "┘" + Colores.RESET);
        return lineas;
    }

    private String simbolo(Celda celda, boolean ocultarBarcos) {
        switch (celda.getTipo()) {
            case BARCO:
                return ocultarBarcos ? Colores.AGUA_PROFUNDA + "~" + Colores.RESET
                                     : Colores.NARANJA + Colores.NEGRITA + "B" + Colores.RESET;
            case IMPACTO:
                return Colores.CORAL + Colores.NEGRITA + "X" + Colores.RESET;
            case FALLO:
                return Colores.GRIS_CLARO + "O" + Colores.RESET;
            default:
                return Colores.AGUA_PROFUNDA + "~" + Colores.RESET;
        }
    }

    public void mostrarTablero(boolean ocultarBarcos) {
        for (String linea : renderizar(ocultarBarcos)) System.out.println(linea);
    }

    public void mostrarBarcos() {
        for (Barco b : barcos) {
            boolean hundido = b.estaHundido();
            System.out.printf("%-14s long: %d  impactos: %d  %s%n",
                    b.getNombre(), b.getLongitud(), b.getImpactos(),
                    hundido ? Colores.CORAL + "HUNDIDO" + Colores.RESET
                            : Colores.LIMA + "ACTIVO" + Colores.RESET);
        }
    }

    // ------------------------------------------------------------ victoria / derrota

    public boolean todosLosBarcosCaidos() {
        if (barcos.size() < Flota.CANTIDAD) return false;
        for (Barco b : barcos) {
            if (!b.estaHundido()) return false;
        }
        return true;
    }

    /** Se invoca sobre el tablero del enemigo. */
    public boolean verificarVictoria() {
        if (todosLosBarcosCaidos()) {
            System.out.println(Colores.LIMA + "*** ¡VICTORIA! Hundiste todos los barcos enemigos ***" + Colores.RESET);
            return true;
        }
        return false;
    }

    /** Se invoca sobre el tablero propio. */
    public boolean verificarDerrota() {
        if (todosLosBarcosCaidos()) {
            System.out.println(Colores.CORAL + "*** DERROTA: hundieron toda tu flota ***" + Colores.RESET);
            return true;
        }
        return false;
    }

    public ArrayList<Barco> getBarcos() { return barcos; }
    public String getUltimoBarcoHundido() { return ultimoBarcoHundido; }
}
