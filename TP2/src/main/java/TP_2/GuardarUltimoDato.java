package TP_2;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.IOException;


public class GuardarUltimoDato {
	public static void main(String[] args) {
		//Crear un archivo de texto (en la carpeta del proyecto) que guarde solo el último dato que el usuario escribe por consola.

	       
		System.out.println("Por favor, escribe un dato y presiona Enter:");

	    try {
	            
	            InputStreamReader isr = new InputStreamReader(System.in);
	            BufferedReader buffered = new BufferedReader(isr);
	            
	            
	            String datoUsuario = buffered.readLine();

	           
	            PrintWriter escritorArchivo = new PrintWriter(new FileWriter("ultimoDato.txt"));
	            
	            
	            escritorArchivo.println(datoUsuario);
	            
	            
	            escritorArchivo.close();

	           
	            System.out.println("Dato guardado exitosamente en el archivo 'ultimoDato.txt'.");

	        } catch (IOException e) {
	            System.err.println("Ocurrió un error de entrada/salida: " + e.getMessage());
	        }
	    }
}