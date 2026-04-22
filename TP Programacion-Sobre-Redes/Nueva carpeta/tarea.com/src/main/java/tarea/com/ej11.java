package tarea.com;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class ej11 {
    public static void main(String[] args) throws IOException {
        BufferedReader lector = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(System.out)), true);

        out.println("Nombre del producto:");
        String producto = lector.readLine();

        out.println("Cantidad comprada:");
        int cantidad = Integer.parseInt(lector.readLine());

        out.println("Precio unitario:");
        double precio = Double.parseDouble(lector.readLine());

        double total = cantidad * precio;

        StringBuilder recibo = new StringBuilder();
        recibo.append("Producto: ").append(producto)
              .append(" | Cantidad: ").append(cantidad)
              .append(" | Precio: $").append(precio)
              .append(" | Total: $").append(total);

        out.println(recibo.toString());
    }
}