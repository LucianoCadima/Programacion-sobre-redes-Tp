package main.TCP_Nube;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

/**
 * MANEJADOR DE CLIENTE (un hilo por cliente)
 *
 * Protocolo (todo por un único socket TCP):
 *  - Comandos y respuestas de texto: writeUTF / readUTF.
 *    Las respuestas empiezan con "OK|" o "ERROR|".
 *  - Datos de archivos: primero un long con el tamaño (writeLong) y luego
 *    exactamente esa cantidad de bytes (write). Así el receptor sabe cuándo
 *    termina el archivo sin cerrar la conexión.
 *
 * Reglas:
 *  - Hasta que no se haga LOGIN correcto, cualquier comando (menos EXIT)
 *    recibe un mensaje de error.
 *  - Cada sesión usa sus propios streams y variables locales, por eso
 *    los hilos no interfieren entre sí. Los archivos se suben a un
 *    temporal y se renombran al final, para que otro cliente nunca
 *    vea (ni descargue) un archivo a medio escribir.
 */
public class ManejadorCliente implements Runnable {

    private static final int BUFFER = 8192;

    private final Socket socket;
    private final ServidorNube servidor;
    private final String ip;

    private DataInputStream dis;
    private DataOutputStream dos;
    private String usuario = null; // null = todavía no autenticado

    public ManejadorCliente(Socket socket, ServidorNube servidor) {
        this.socket = socket;
        this.servidor = servidor;
        this.ip = socket.getInetAddress().getHostAddress();
    }

    @Override
    public void run() {
        try {
            // Streams de datos sobre el socket (entrada = comandos, salida = respuestas).
            dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

            boolean seguir = true;
            while (seguir) {
                // Bloquea hasta que el cliente envíe un comando.
                String linea = dis.readUTF().trim();
                if (linea.isEmpty()) {
                    continue;
                }
                seguir = procesar(linea);
            }

        } catch (EOFException e) {
            ServidorNube.log(Colores.AMARILLO, "AVISO", ip
                    + " cortó la conexión sin enviar EXIT");
        } catch (IOException e) {
            ServidorNube.log(Colores.ROJO, "ERROR", ip + ": " + e.getMessage());
        } finally {
            cerrar();
        }
    }

    /** Interpreta un comando. Devuelve false cuando hay que terminar la sesión. */
    private boolean procesar(String linea) throws IOException {
        String[] partes = linea.split("\\s+", 2);
        String comando = partes[0].toUpperCase();
        String argumento = partes.length > 1 ? partes[1].trim() : "";

        ServidorNube.log(Colores.CYAN, "COMANDO", ip
                + (usuario != null ? " (" + usuario + ")" : "") + " -> "
                + (comando.equals("LOGIN") ? "LOGIN ****" : linea));

        if (comando.equals("EXIT")) {
            responder("OK|Hasta luego");
            return false;
        }

        if (comando.equals("LOGIN")) {
            login(argumento);
            return true;
        }

        // Exigir autenticación antes de cualquier comando de archivos.
        if (usuario == null) {
            ServidorNube.log(Colores.ROJO, "DENEGADO", ip
                    + " intentó " + comando + " sin autenticarse");
            responder("ERROR|Debe autenticarse primero: LOGIN <usuario> <clave>");
            return true;
        }

        switch (comando) {
            case "LIST":
                listar();
                break;
            case "UPLOAD":
                subir(argumento);
                break;
            case "DOWNLOAD":
                descargar(argumento);
                break;
            default:
                responder("ERROR|Comando desconocido: " + comando);
        }
        return true;
    }

    // ------------------------------------------------------------------ LOGIN
    private void login(String argumento) throws IOException {
        String[] datos = argumento.split("\\s+");
        if (datos.length != 2) {
            responder("ERROR|Uso: LOGIN <usuario> <clave>");
            return;
        }
        if (servidor.credencialesValidas(datos[0], datos[1])) {
            usuario = datos[0];
            ServidorNube.log(Colores.VERDE, "LOGIN", ip + " inició sesión como " + usuario);
            responder("OK|Bienvenido " + usuario);
        } else {
            ServidorNube.log(Colores.ROJO, "LOGIN", ip + " credenciales incorrectas");
            responder("ERROR|Usuario o clave incorrectos");
        }
    }

    // ------------------------------------------------------------------- LIST
    private void listar() throws IOException {
        File[] archivos = servidor.getCarpeta().listFiles(
                f -> f.isFile() && !f.getName().endsWith(".part"));
        if (archivos == null) {
            archivos = new File[0];
        }
        Arrays.sort(archivos);

        // Primero la cantidad, luego (nombre, tamaño) de cada archivo.
        responder("OK|" + archivos.length);
        for (File f : archivos) {
            dos.writeUTF(f.getName());
            dos.writeLong(f.length());
        }
        dos.flush();
    }

    // ----------------------------------------------------------------- UPLOAD
    private void subir(String nombre) throws IOException {
        if (!nombreValido(nombre)) {
            responder("ERROR|Nombre de archivo inválido");
            return;
        }

        // Le avisamos al cliente que puede empezar a enviar los bytes.
        responder("OK|Listo para recibir");

        long tamanio = dis.readLong();
        File destino = new File(servidor.getCarpeta(), nombre);
        // Temporal único por hilo: dos clientes subiendo el mismo nombre no se pisan.
        File temporal = new File(servidor.getCarpeta(),
                nombre + "." + java.util.UUID.randomUUID() + ".part");

        long recibido = 0;
        try (FileOutputStream fos = new FileOutputStream(temporal);
             BufferedOutputStream out = new BufferedOutputStream(fos)) {

            byte[] buffer = new byte[BUFFER];
            // Leemos EXACTAMENTE 'tamanio' bytes (ni uno más) del socket.
            while (recibido < tamanio) {
                int pedir = (int) Math.min(buffer.length, tamanio - recibido);
                int leidos = dis.read(buffer, 0, pedir);
                if (leidos == -1) {
                    break; // el cliente se cayó a mitad de la subida
                }
                out.write(buffer, 0, leidos);
                recibido += leidos;
            }
        }

        if (recibido == tamanio) {
            Files.move(temporal.toPath(), destino.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
            ServidorNube.log(Colores.VERDE, "UPLOAD", usuario + " subió " + nombre
                    + " (" + recibido + " bytes)");
            responder("OK|Archivo recibido (" + recibido + " bytes)");
        } else {
            temporal.delete();
            ServidorNube.log(Colores.ROJO, "UPLOAD", "Subida incompleta de " + nombre
                    + " (" + recibido + "/" + tamanio + ")");
            throw new EOFException("Subida interrumpida");
        }
    }

    // --------------------------------------------------------------- DOWNLOAD
    private void descargar(String nombre) throws IOException {
        if (!nombreValido(nombre)) {
            responder("ERROR|Nombre de archivo inválido");
            return;
        }
        File archivo = new File(servidor.getCarpeta(), nombre);
        if (!archivo.isFile()) {
            responder("ERROR|El archivo no existe en la nube");
            return;
        }

        long tamanio = archivo.length();
        responder("OK|Enviando " + nombre);
        dos.writeLong(tamanio);

        try (FileInputStream fis = new FileInputStream(archivo);
             BufferedInputStream in = new BufferedInputStream(fis)) {

            byte[] buffer = new byte[BUFFER];
            int leidos;
            long enviado = 0;
            // Leemos del disco y escribimos al socket bloque a bloque.
            while (enviado < tamanio && (leidos = in.read(buffer)) != -1) {
                dos.write(buffer, 0, leidos);
                enviado += leidos;
            }
        }
        dos.flush();

        ServidorNube.log(Colores.VERDE, "DOWNLOAD", usuario + " descargó " + nombre
                + " (" + tamanio + " bytes)");
    }

    // ---------------------------------------------------------------- Helpers
    /** Rechaza rutas (../, /, \) para que nadie salga de la carpeta de la nube. */
    private boolean nombreValido(String nombre) {
        return !nombre.isEmpty()
                && !nombre.contains("/")
                && !nombre.contains("\\")
                && !nombre.contains("..")
                && !nombre.endsWith(".part");
    }

    private void responder(String msg) throws IOException {
        dos.writeUTF(msg);
        dos.flush();
    }

    /** Cierre limpio: se ejecuta siempre al terminar el hilo. */
    private void cerrar() {
        try {
            socket.close();
        } catch (IOException e) {
            // Ya estaba cerrado.
        }
        servidor.clienteDesconectado();
        ServidorNube.log(Colores.AMARILLO, "DESCONECTADO", ip
                + (usuario != null ? " (" + usuario + ")" : ""));
    }
}
