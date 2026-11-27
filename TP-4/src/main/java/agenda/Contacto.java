package agenda;

public class Contacto {
    private String nombre;
    private String telefono;
    private String email;
    private String notaPrivada;

    public Contacto(String nombre, String telefono, String email, String notaPrivada) {
        this.nombre = nombre;
        this.telefono = telefono;
        this.email = email;
        this.notaPrivada = notaPrivada;
    }

    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNotaPrivada() { return notaPrivada; }
    public void setNotaPrivada(String notaPrivada) { this.notaPrivada = notaPrivada; }
}
