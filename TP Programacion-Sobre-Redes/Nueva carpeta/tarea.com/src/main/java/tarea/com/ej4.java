package tarea.com;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class ej4 {
    public static void main(String[] args) throws IOException {
        BufferedReader lector = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(System.out)), true);

        out.println("Ingrese un párrafo:");
        String parrafo = lector.readLine();

        out.println("Palabra a buscar:");
        String buscar = lector.readLine();

        out.println("Palabra de reemplazo:");
        String reemplazo = lector.readLine();

        String resultado = parrafo.replace(buscar, reemplazo);

        out.println(resultado);
    }
}