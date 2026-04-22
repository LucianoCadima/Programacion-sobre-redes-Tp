package tarea.com;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class ej14 {
    public static void main(String[] args) throws IOException {
        BufferedReader lector = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(System.out)), true);

        out.println("Ingrese una palabra:");
        String palabra = lector.readLine();

        StringBuilder sb = new StringBuilder(palabra);
        sb.insert(0, "<b>");
        sb.append("</b>");

        out.println(sb.toString());
    }
}