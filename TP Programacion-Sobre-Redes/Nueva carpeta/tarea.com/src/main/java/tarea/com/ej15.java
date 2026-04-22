package tarea.com;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class ej15 {
    public static void main(String[] args) throws IOException {
        BufferedReader lector = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(System.out)), true);

        out.println("Ingrese la primera palabra:");
        String p1 = lector.readLine();

        out.println("Ingrese la segunda palabra:");
        String p2 = lector.readLine();

        out.println("Ingrese la tercera palabra:");
        String p3 = lector.readLine();

        StringBuilder sb = new StringBuilder();
        sb.append(p1).append("-").append(p2).append("-").append(p3);

        int inicio = sb.indexOf(p2) - 1;
        int fin = inicio + p2.length() + 2;

        sb.delete(inicio, fin);

        out.println(sb.toString());
    }
}