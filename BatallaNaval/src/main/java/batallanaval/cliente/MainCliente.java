package batallanaval.cliente;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import batallanaval.util.Colores;

public class MainCliente {

    public static void main(String[] args) throws IOException {
        String host = "localhost";
        int puerto = 5555;

        if (args.length >= 2) {            // java MainCliente <ip> <puerto>
            host = args[0];
            puerto = Integer.parseInt(args[1]);
        } else {                           // o se piden por consola
            BufferedReader teclado = new BufferedReader(new InputStreamReader(System.in));
            System.out.print(Colores.ORO + "IP del servidor [localhost]: " + Colores.RESET);
            String ip = teclado.readLine();
            if (ip != null && !ip.trim().isEmpty()) host = ip.trim();
            System.out.print(Colores.ORO + "Puerto [5555]: " + Colores.RESET);
            String p = teclado.readLine();
            if (p != null && !p.trim().isEmpty()) {
                try {
                    puerto = Integer.parseInt(p.trim());
                } catch (NumberFormatException e) {
                    System.out.println("Puerto inválido, uso 5555.");
                }
            }
        }

        System.out.println(Colores.TURQUESA
                + "╔" + "═".repeat(34) + "╗\n"
                + "║    " + Colores.NARANJA + Colores.NEGRITA + "B A T A L L A   N A V A L" + Colores.RESET + Colores.TURQUESA + "     ║\n"
                + "║    " + Colores.GRIS_CLARO + "duelo en red · 2 jugadores" + Colores.TURQUESA + "    ║\n"
                + "╚" + "═".repeat(34) + "╝" + Colores.RESET);
        new Cliente(host, puerto).jugar();
    }
}
