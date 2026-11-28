package batallanaval.servidor;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Historial de partidas en un archivo de texto (punto extra). */
public final class Historial {

    private static final String ARCHIVO = System.getenv("HISTORIAL") != null
            ? System.getenv("HISTORIAL") : "historial.txt";
    private static final Object CANDADO = new Object();

    private Historial() { }

    public static void registrar(String linea) {
        String fecha = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        synchronized (CANDADO) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(ARCHIVO, true))) {
                pw.println(fecha + " | " + linea);
            } catch (IOException e) {
                System.err.println("No se pudo escribir el historial: " + e.getMessage());
            }
        }
    }
}
