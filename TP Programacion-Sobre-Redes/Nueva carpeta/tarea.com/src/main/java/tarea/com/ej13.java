package tarea.com;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class ej13 {
    public static void main(String[] args) throws IOException {
        BufferedReader lector = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(System.out)), true);

        out.println("Ingrese una oración que contenga la palabra 'error':");
        String frase = lector.readLine();

        StringBuilder sb = new StringBuilder(frase);

        int inicio = sb.indexOf("error");
        if (inicio != -1) {
            int fin = inicio + "error".length() + 1;
            sb.delete(inicio, fin);
        }

        out.println(sb.toString());
    }
}