package batallanaval.protocolo;

import java.io.Serializable;
import java.util.ArrayList;

/** Único objeto que viaja por la red (ObjectOutputStream / ObjectInputStream). */
public class Mensaje implements Serializable {
    private static final long serialVersionUID = 1L;

    private final TipoMensaje tipo;
    private String texto;
    private String remitente;
    private int fila;
    private int col;
    private String resultado;
    private ArrayList<PosicionBarco> barcos;

    public Mensaje(TipoMensaje tipo) {
        this.tipo = tipo;
    }

    public Mensaje(TipoMensaje tipo, String texto) {
        this(tipo);
        this.texto = texto;
    }

    public Mensaje coordenada(int fila, int col) { this.fila = fila; this.col = col; return this; }
    public Mensaje resultado(String resultado) { this.resultado = resultado; return this; }
    public Mensaje remitente(String remitente) { this.remitente = remitente; return this; }
    public Mensaje barcos(ArrayList<PosicionBarco> barcos) { this.barcos = barcos; return this; }
    public Mensaje texto(String texto) { this.texto = texto; return this; }

    public TipoMensaje getTipo() { return tipo; }
    public String getTexto() { return texto; }
    public String getRemitente() { return remitente; }
    public int getFila() { return fila; }
    public int getCol() { return col; }
    public String getResultado() { return resultado; }
    public ArrayList<PosicionBarco> getBarcos() { return barcos; }
}
