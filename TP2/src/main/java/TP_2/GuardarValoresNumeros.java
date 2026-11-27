package TP_2;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class GuardarValoresNumeros {
	public static void main(String[] args) {
	//Crear un archivo de texto (en la carpeta del proyecto) que guarde TODOS los valores NUMERICOS que ingrese el usuario por consola, cada uno en un renglón (puede ingresar otros datos que no sean numero OJO).

	
	System.out.println("Por favor, escribe un valor numerico y presiona Enter: ");
     
     try {
    	 InputStreamReader isr = new InputStreamReader(System.in);
         BufferedReader buffered = new BufferedReader(isr);
         
         String dato = buffered.readLine();
         

         
         PrintWriter escritorArchivo = new PrintWriter(new FileWriter("GuardarValoresNumeros.txt"));
      
       for(int i = 0; i < dato.length();i++ )  {
    	 char caracter = dato.charAt(i);   
    	 System.out.println(caracter);
         escritorArchivo.println(caracter);
       }
       
         escritorArchivo.close();
         System.out.println("Dato guardado exitosamente en el archivo 'GuardarValoresNumeros.txt'.");

     } catch (IOException e) {
         System.err.println("Ocurrió un error de entrada/salida: " + e.getMessage());
     }
}
}
