package batallanaval.protocolo;

import java.io.Serializable;

public class PosicionBarco implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String nombre;
    private final int fila;
    private final int col;
    private final int longitud;
    private final boolean horizontal;

    public PosicionBarco(String nombre, int fila, int col, int longitud, boolean horizontal) {
        this.nombre = nombre;
        this.fila = fila;
        this.col = col;
        this.longitud = longitud;
        this.horizontal = horizontal;
    }

    public String getNombre() { return nombre; }
    public int getFila() { return fila; }
    public int getCol() { return col; }
    public int getLongitud() { return longitud; }
    public boolean isHorizontal() { return horizontal; }
}
