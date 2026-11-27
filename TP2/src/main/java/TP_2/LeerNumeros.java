package TP_2;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class LeerNumeros {
	public static void main(String[] args) throws FileNotFoundException {
	
		InputStreamReader isr = new InputStreamReader(System.in);
		BufferedReader buffered = new BufferedReader(isr);
		
		try (BufferedReader br = new BufferedReader(new FileReader("GuardarNumerosPares.txt"))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                System.out.println(linea);
            }
        } catch (IOException e) {
            System.out.println("Error al leer el archivo: " + e.getMessage());
        }
	}
}
    
