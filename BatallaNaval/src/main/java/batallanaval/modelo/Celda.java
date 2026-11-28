package batallanaval.modelo;

public class Celda {
    private TipoCelda tipo;
    private boolean disparada;
    private String nombreBarco;

    public Celda() {
        this.tipo = TipoCelda.AGUA;
        this.disparada = false;
        this.nombreBarco = null;
    }

    public TipoCelda getTipo() { return tipo; }
    public void setTipo(TipoCelda tipo) { this.tipo = tipo; }
    public boolean isDisparada() { return disparada; }
    public void setDisparada(boolean disparada) { this.disparada = disparada; }
    public String getNombreBarco() { return nombreBarco; }
    public void setNombreBarco(String nombreBarco) { this.nombreBarco = nombreBarco; }
}
