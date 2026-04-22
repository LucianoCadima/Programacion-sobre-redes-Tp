package tarea.com;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class ej3 {
    public static void main(String[] args) throws IOException {
        BufferedReader lector = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(System.out)), true);

        out.println("Ingrese una oración:");
        String texto = lector.readLine().toLowerCase();

        int contador = 0;

        for (char c : texto.toCharArray()) {
            if ("aeiou".indexOf(c) != -1) {
                contador++;
            }
        }

        out.println("Cantidad de vocales: " + contador);
    }
}