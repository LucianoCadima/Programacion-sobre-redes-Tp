package batallanaval.modelo;

public class Barco {
    private String nombre;
    private int longitud;
    private int impactos;

    public Barco(String nombre, int longitud) {
        this.nombre = nombre;
        this.longitud = longitud;
        this.impactos = 0;
    }

    public void recibirImpacto() { impactos++; }
    public boolean estaHundido() { return impactos >= longitud; }

    public String getNombre() { return nombre; }
    public int getLongitud() { return longitud; }
    public int getImpactos() { return impactos; }
}
