package TP_2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Numeros_Primos {
    public static void main(String[] args) {  
    	//Utilizar el archivo creado anteriormente “números.txt” (con los números impares eliminados) y colocar en otro archivos de texto “primos.dat” (fuera de la carpeta del proyecto y de la carpeta que tiene el archivo “números.txt”) todos los números de ”números.txt” que sean primos.
        File numerosOriginal = new File("numeros.txt");
        String rutaUsuario = System.getProperty("user.home");
        File archivoPrimos = new File(rutaUsuario + File.separator + "primos.dat");
        
        try (BufferedReader lector = new BufferedReader(new FileReader(numerosOriginal));
             BufferedWriter escritor = new BufferedWriter(new FileWriter(archivoPrimos))) {
            
            String renglon;
            
            while ((renglon = lector.readLine()) != null) {
                renglon = renglon.replace("[", "").replace("]", "").trim();
                
                if (!renglon.isEmpty()) {
                    String[] partes = renglon.split(",");
                    String listaPrimos = "";
                    
                    for (int i = 0; i < partes.length; i++) {
                        String textoNumero = partes[i].trim();
                        
                        if (!textoNumero.isEmpty()) {
                            int numero = Integer.parseInt(textoNumero);
                            
                            if (esPrimo(numero)) {
                                if (listaPrimos.isEmpty()) {
                                    listaPrimos = String.valueOf(numero);
                                } else {
                                    listaPrimos = listaPrimos + ", " + numero;
                                }
                            }
                        }
                    }
                    
                    if (!listaPrimos.isEmpty()) {
                        escritor.write("[" + listaPrimos + "]");
                        escritor.newLine();
                    }
                }
            }
            
            System.out.println("Archivo 'primos.dat' creado con éxito en: " + archivoPrimos.getAbsolutePath());
            
        } catch (IOException e) {
            System.out.println("Error al procesar los archivos: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Error en el formato de los números: " + e.getMessage());
        }
    }

    public static boolean esPrimo(int num) {
        if (num <= 1) {
            return false;
        }
        for (int i = 2; i * i <= num; i++) {
            if (num % i == 0) {
                return false;
            }
        }
        return true;
    }
}
