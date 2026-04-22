package tarea.com;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class ej1 {
	
	public static void main(String[] args) {

	
		BufferedReader lector = new BufferedReader(  new InputStreamReader(System.in) );
		PrintWriter out = new PrintWriter( new BufferedWriter( new OutputStreamWriter(System.out) ) ,true );
	
      out.println("ingrese el nombre de la organizacion: ");	
      
      try {
		String aux = lector.readLine();
		String PalabrasMayus = aux.toUpperCase();
		char letra1 = PalabrasMayus.charAt(0);
		int espacio = PalabrasMayus.indexOf(" ");
		String palabras = PalabrasMayus.substring(espacio +1);
		char letra2 = palabras.charAt(0);
		int espacio2 = palabras.indexOf(" ");
		String palabras2 = palabras.substring(espacio2 +1);
		char letra3 = palabras2.charAt(0);
		
		
		out.println(letra1);
		out.println(letra2);
		out.println(letra3);
		
	
		
		
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} 
	
	
	
	

}
}