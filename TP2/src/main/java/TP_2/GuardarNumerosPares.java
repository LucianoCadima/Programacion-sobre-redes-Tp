package TP_2;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class GuardarNumerosPares {
	public static void main(String[] args) {
		// Crear un archivo de texto (fuera de la carpeta del proyecto) que se llame
		// “números.txt” que guarde los números pares desde el 0 al 1000.

		try {
			InputStreamReader isr = new InputStreamReader(System.in);
			BufferedReader buffered = new BufferedReader(isr);
			int numero = -1;
			List<Integer> lista = new ArrayList<>();
			while (numero <= 1000) {
				numero = numero + 1;
				if (numero % 2 == 0) {
					lista.add(numero);
				}
				if (numero > 1000) {
					break;
				}
			}

			PrintWriter escritorArchivo = new PrintWriter(new FileWriter("numeros.txt"),true);
			System.out.println(lista);
	        escritorArchivo.println(lista);

			escritorArchivo.close();
			System.out.println("Lista guardada exitosamente en el archivo 'numeros.txt'.");

		} catch (IOException e) {
			System.err.println("Ocurrió un error de entrada/salida: " + e.getMessage());
		}
	}
}
