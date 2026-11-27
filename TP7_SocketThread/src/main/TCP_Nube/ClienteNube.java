package main.TCP_Nube;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormat;

/**
 * CLIENTE DE NUBE
 *
 * Pide la IP del servidor, se conecta y ofrece una consola interactiva:
 *   LOGIN <usuario> <clave> | LIST | UPLOAD <archivo> | DOWNLOAD <archivo> | EXIT
 *
 * Es un cliente "lockstep": envía un comando y espera la respuesta antes
 * de pedir el siguiente, por eso no necesita hilos adicionales.
 * Los archivos descargados se guardan en la carpeta local "DESCARGAS".
 */
public class ClienteNube {

    private static final int BUFFER = 8192;
    private static final String CARPETA_DESCARGAS = "DESCARGAS";

    public static void main(String[] args) {
        BufferedReader teclado = new BufferedReader(new InputStreamReader(System.in));

        try {
            System.out.print(Colores.AMARILLO + "IP del servidor: " + Colores.RESET);
            String ip = teclado.readLine().trim();

            System.out.println("Conectando a " + ip + ":" + ServidorNube.PUERTO + " ...");

            try (Socket socket = new Socket(ip, ServidorNube.PUERTO)) {
                // Evita que la lectura quede colgada para siempre si se cae la red.
                socket.setSoTimeout(60000);

                DataInputStream dis = new DataInputStream(
                        new BufferedInputStream(socket.getInputStream()));
                DataOutputStream dos = new DataOutputStream(
                        new BufferedOutputStream(socket.getOutputStream()));

                System.out.println(Colores.VERDE + "Conectado desde "
                        + socket.getLocalAddress().getHostAddress()
                        + Colores.RESET);
                System.out.println("Comandos: LOGIN <usuario> <clave> | LIST | "
                        + "UPLOAD <archivo> | DOWNLOAD <archivo> | EXIT");

                consola(teclado, dis, dos);
            }

        } catch (IOException e) {
            System.err.println(Colores.ROJO + "Error de conexión: "
                    + e.getMessage() + Colores.RESET);
        }
        System.out.println("Sesión cerrada.");
    }

    /** Bucle de la consola interactiva. */
    private static void consola(BufferedReader teclado, DataInputStream dis,
                                DataOutputStream dos) throws IOException {
        while (true) {
            System.out.print(Colores.CYAN + "nube> " + Colores.RESET);
            String linea = teclado.readLine();
            if (linea == null) {
                linea = "EXIT";
            }
            linea = linea.trim();
            if (linea.isEmpty()) {
                continue;
            }

            String[] partes = linea.split("\\s+", 2);
            String comando = partes[0].toUpperCase();
            String argumento = partes.length > 1 ? partes[1].trim() : "";

            switch (comando) {
                case "UPLOAD":
                    subir(argumento, dis, dos);
                    break;
                case "DOWNLOAD":
                    descargar(argumento, dis, dos);
                    break;
                case "LIST":
                    enviar(dos, "LIST");
                    listar(dis);
                    break;
                case "EXIT":
                    enviar(dos, "EXIT");
                    mostrar(dis.readUTF());
                    return; // el try-with-resources cierra el socket
                default:
                    // LOGIN y cualquier otro: lo manda el cliente tal cual y el
                    // servidor decide si es válido.
                    enviar(dos, linea);
                    mostrar(dis.readUTF());
            }
        }
    }

    // ------------------------------------------------------------------- LIST
    private static void listar(DataInputStream dis) throws IOException {
        String resp = dis.readUTF();
        if (!resp.startsWith("OK|")) {
            mostrar(resp);
            return;
        }
        int cantidad = Integer.parseInt(resp.substring(3));
        System.out.println(Colores.AMARILLO + "Archivos en la nube (" + cantidad + "):"
                + Colores.RESET);
        for (int i = 0; i < cantidad; i++) {
            String nombre = dis.readUTF();
            long tamanio = dis.readLong();
            System.out.printf("  %-35s %s%n", nombre, formatear(tamanio));
        }
    }

    // ----------------------------------------------------------------- UPLOAD
    private static void subir(String ruta, DataInputStream dis, DataOutputStream dos)
            throws IOException {
        if (ruta.isEmpty()) {
            System.out.println(Colores.ROJO + "Uso: UPLOAD <nombre_archivo>" + Colores.RESET);
            return;
        }
        File archivo = new File(ruta);
        if (!archivo.isFile()) {
            System.out.println(Colores.ROJO + "No existe el archivo local: " + ruta
                    + Colores.RESET);
            return;
        }

        enviar(dos, "UPLOAD " + archivo.getName());
        String resp = dis.readUTF();
        if (!resp.startsWith("OK|")) {
            mostrar(resp); // el servidor lo rechazó (ej. no autenticado)
            return;
        }

        long tamanio = archivo.length();
        dos.writeLong(tamanio); // primero el tamaño...

        // ...y después los bytes del archivo, bloque a bloque.
        try (FileInputStream fis = new FileInputStream(archivo);
             BufferedInputStream in = new BufferedInputStream(fis)) {
            byte[] buffer = new byte[BUFFER];
            int leidos;
            while ((leidos = in.read(buffer)) != -1) {
                dos.write(buffer, 0, leidos);
            }
        }
        dos.flush();

        System.out.println("Enviados " + formatear(tamanio));
        mostrar(dis.readUTF()); // confirmación final del servidor
    }

    // --------------------------------------------------------------- DOWNLOAD
    private static void descargar(String nombre, DataInputStream dis,
                                  DataOutputStream dos) throws IOException {
        if (nombre.isEmpty()) {
            System.out.println(Colores.ROJO + "Uso: DOWNLOAD <nombre_archivo>" + Colores.RESET);
            return;
        }

        enviar(dos, "DOWNLOAD " + nombre);
        String resp = dis.readUTF();
        if (!resp.startsWith("OK|")) {
            mostrar(resp);
            return;
        }

        long esperado = dis.readLong(); // tamaño que va a llegar

        File carpeta = new File(CARPETA_DESCARGAS);
        carpeta.mkdirs();
        File destino = new File(carpeta, nombre);
        File temporal = new File(carpeta, nombre + ".part");

        long recibido = 0;
        try (FileOutputStream fos = new FileOutputStream(temporal);
             BufferedOutputStream out = new BufferedOutputStream(fos)) {
            byte[] buffer = new byte[BUFFER];
            // Reconstruimos el archivo leyendo exactamente 'esperado' bytes.
            while (recibido < esperado) {
                int pedir = (int) Math.min(buffer.length, esperado - recibido);
                int leidos = dis.read(buffer, 0, pedir);
                if (leidos == -1) {
                    break;
                }
                out.write(buffer, 0, leidos);
                recibido += leidos;
            }
        }

        if (recibido == esperado) {
            Files.move(temporal.toPath(), destino.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
            System.out.println(Colores.VERDE + "OK: descargado en "
                    + destino.getAbsolutePath() + " (" + recibido + " bytes)"
                    + Colores.RESET);
        } else {
            temporal.delete();
            throw new IOException("Descarga incompleta: " + recibido + "/" + esperado);
        }
    }

    // ---------------------------------------------------------------- Helpers
    private static void enviar(DataOutputStream dos, String msg) throws IOException {
        dos.writeUTF(msg);
        dos.flush();
    }

    /** Muestra una respuesta "OK|..." en verde o "ERROR|..." en rojo. */
    private static void mostrar(String resp) {
        if (resp.startsWith("OK|")) {
            System.out.println(Colores.VERDE + resp.substring(3) + Colores.RESET);
        } else if (resp.startsWith("ERROR|")) {
            System.out.println(Colores.ROJO + "[ERROR] " + resp.substring(6) + Colores.RESET);
        } else {
            System.out.println(resp);
        }
    }

    private static String formatear(long bytes) {
        DecimalFormat df = new DecimalFormat("#0.00");
        return bytes + " bytes (" + df.format(bytes / 1024.0) + " KB)";
    }
}
