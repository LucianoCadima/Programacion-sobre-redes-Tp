package batallanaval.modelo;

/** Flota reglamentaria: la comparten cliente y servidor. */
public final class Flota {
    public static final String[] NOMBRES =
            {"Portaaviones", "Acorazado", "Crucero", "Submarino", "Destructor"};
    public static final int[] LONGITUDES = {5, 4, 3, 3, 2};
    public static final int CANTIDAD = NOMBRES.length;

    private Flota() { }

    public static int longitudDe(String nombre) {
        for (int i = 0; i < CANTIDAD; i++) {
            if (NOMBRES[i].equals(nombre)) return LONGITUDES[i];
        }
        return -1;
    }
}
