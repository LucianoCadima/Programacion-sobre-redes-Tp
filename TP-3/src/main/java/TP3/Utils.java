package TP3;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class Utils {
    // Reemplazo de Scanner por BufferedReader
    private static final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    // Códigos ANSI para estética en consola
    public static final String RESET = "\u001B[0m";
    public static final String SELECCION = "\u001B[36m"; // Cian
    public static final String EXITO = "\u001B[32m";    // Verde
    public static final String ERROR = "\u001B[31m";    // Rojo
    public static final String TITULO = "\u001B[35m";   // Púrpura

    // Método específico de lectura que retorna un String usando BufferedReader
    public static String leerString(String mensaje) {
        System.out.print(SELECCION + mensaje + RESET);
        try {
            String linea = reader.readLine();
            return (linea != null) ? linea.trim() : "";
        } catch (IOException e) {
            System.out.println(ERROR + "\t[Error] Error al leer la entrada de consola." + RESET);
            return "";
        }
    }

    public static String leerStringObligatorio(String mensaje) {
        while (true) {
            String entrada = leerString(mensaje);
            if (!entrada.isEmpty() && !entrada.contains(";")) {
                return entrada;
            }
            System.out.println(ERROR + "\t[Error] El campo no puede estar vacío ni contener ';'" + RESET);
        }
    }

    public static int leerEntero(String mensaje) {
        while (true) {
            String entrada = leerString(mensaje);
            if (esEntero(entrada)) {
                return Integer.parseInt(entrada);
            }
            System.out.println(ERROR + "\t[Error] Debe ingresar un número entero válido." + RESET);
        }
    }

    public static float leerDecimal(String mensaje) {
        while (true) {
            String entrada = leerString(mensaje);
            if (esDecimal(entrada) || esEntero(entrada)) {
                return Float.parseFloat(entrada);
            }
            System.out.println(ERROR + "\t[Error] Debe ingresar un número decimal válido (ej: 50.6)." + RESET);
        }
    }

    // Métodos de validación de tipo de dato
    public static boolean esEntero(String str) {
        if (str == null || str.isEmpty()) return false;
        return str.matches("-?\\d+");
    }

    public static boolean esDecimal(String str) {
        if (str == null || str.isEmpty()) return false;
        return str.matches("-?\\d+\\.\\d+");
    }
}