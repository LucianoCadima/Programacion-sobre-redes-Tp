package TP_2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class BorrarNumerosMulti3 {
    public static void main(String[] args) {    
        File numerosOriginal = new File("numeros.txt");
        File archivoTemporal = new File("numero_temp.txt");
        
        try (BufferedReader lector = new BufferedReader(new FileReader(numerosOriginal));
             BufferedWriter escritor = new BufferedWriter(new FileWriter(archivoTemporal))) {
            
            String renglon;
            
            while ((renglon = lector.readLine()) != null) {
                
              renglon = renglon.replace("[", "").replace("]", "").trim();
                
              if (!renglon.isEmpty()) {
                    
               String[] partes = renglon.split(",");
               String nuevoRenglon = "";
                    
               for (int i = 0; i < partes.length; i++) {
                 String textoNumero = partes[i].trim();
                        
                 if (!textoNumero.isEmpty()) {
                  int valorNumero = Integer.parseInt(textoNumero);
                            
                            
                  if (valorNumero % 3 != 0) {
                    if (nuevoRenglon.isEmpty()) {
                        nuevoRenglon = String.valueOf(valorNumero);
                        } else {
                          nuevoRenglon = nuevoRenglon + ", " + valorNumero;
                                }
                            }
                        }
                    }
                    
                    
                    if (!nuevoRenglon.isEmpty()) {
                        escritor.write("[" + nuevoRenglon + "]");
                        escritor.newLine();
                    }
                }
            }
            
        } catch (IOException e) {
            System.out.println("Ocurrió un error al procesar el archivo: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Error de formato numérico: " + e.getMessage());
        }
        
        if (numerosOriginal.delete()) {
            archivoTemporal.renameTo(numerosOriginal);
            System.out.println("Múltiplos de 3 eliminados de la lista.");
        } else {
            System.out.println("No se pudo actualizar el archivo original.");
        }
    }
}
