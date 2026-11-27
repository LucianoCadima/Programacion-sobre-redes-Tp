package agenda;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class GestorArchivo {
    private static final String ARCHIVO_PRINCIPAL = "agenda.dat";
    private static final String ARCHIVO_TEMPORAL = "agenda.tmp";

    
    public static List<Contacto> cargarAgenda() {
        List<Contacto> lista = new ArrayList<>();
        File archivo = new File(ARCHIVO_PRINCIPAL);
        
        if (!archivo.exists()) {
            return lista; 
        }

        try (BufferedReader lector = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = lector.readLine()) != null) {
                if (linea.trim().isEmpty()) continue;
                
                String[] campos = linea.split(";");
                if (campos.length >= 4) {
                    // Desencriptación en tiempo real al leer el archivo
                    String notaDescifrada = GestorCifrado.descifrarAES(campos[3]);
                    lista.add(new Contacto(campos[0], campos[1], campos[2], notaDescifrada));
                }
            }
        } catch (IOException e) {
            System.err.println("Error no controlado al leer archivo: " + e.getMessage());
        }
        return lista;
    }

    
    public static boolean guardarAgenda(List<Contacto> lista) {
        File archivoTmp = new File(ARCHIVO_TEMPORAL);
        File archivoOrig = new File(ARCHIVO_PRINCIPAL);

       
        try (PrintWriter escritor = new PrintWriter(new BufferedWriter(new FileWriter(archivoTmp, false)))) {
            for (Contacto c : lista) {
                String notaCifrada = GestorCifrado.cifrarAES(c.getNotaPrivada());
                escritor.println(c.getNombre() + ";" + c.getTelefono() + ";" + c.getEmail() + ";" + notaCifrada);
            }
            escritor.flush();
        } catch (IOException e) {
            return false;
        }

        
        if (archivoTmp.exists()) {
            if (archivoOrig.exists() && !archivoOrig.delete()) {
                return false;
            }
            return archivoTmp.renameTo(archivoOrig);
        }
        return false;
    }
}
