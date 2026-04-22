package tarea.com;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;


public class ej7 {
    public static void main(String[] args) throws IOException {
        BufferedReader lector = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(System.out)), true);

        out.println("Ingrese una contraseña:");
        String pass = lector.readLine();

        boolean largo = pass.length() >= 8;
        boolean numero = pass.matches(".*\\d.*");
        boolean contieneClave = pass.toLowerCase().contains("clave");

        if (largo && numero && !contieneClave) {
            out.println("Contraseña segura");
        } else {
            out.println("Contraseña vulnerable");
        }
    }
}