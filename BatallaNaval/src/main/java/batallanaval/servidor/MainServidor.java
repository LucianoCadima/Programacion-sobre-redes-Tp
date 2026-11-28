package batallanaval.servidor;

import java.io.IOException;

public class MainServidor {

    public static final int PUERTO_DEFECTO = 5555;

    public static void main(String[] args) {
        int puerto = PUERTO_DEFECTO;
        try {
            if (args.length > 0) puerto = Integer.parseInt(args[0]);
            else if (System.getenv("PUERTO") != null) puerto = Integer.parseInt(System.getenv("PUERTO"));
        } catch (NumberFormatException e) {
            System.err.println("Puerto inválido, uso " + PUERTO_DEFECTO);
        }

        try (Servidor servidor = new Servidor(puerto)) {
            Runtime.getRuntime().addShutdownHook(new Thread(servidor::close, "CIERRE-SERVIDOR"));
            servidor.iniciar();
        } catch (IOException e) {
            System.err.println("No se pudo iniciar el servidor: " + e.getMessage());
        }
    }
}
