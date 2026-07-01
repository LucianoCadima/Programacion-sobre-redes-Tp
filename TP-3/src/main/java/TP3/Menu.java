package TP3;

import TP3.Consultas;
import TP3.Utils;

public class Menu {

    public static void main(String[] args) {
    	Consultas servicio = new Consultas();
        boolean ejecutar = true;

        while (ejecutar) {
            System.out.println(Utils.TITULO + "=====================================" + Utils.RESET);
            System.out.println(Utils.TITULO + "       SISTEMA DE INVENTARIO         " + Utils.RESET);
            System.out.println(Utils.TITULO + "=====================================" + Utils.RESET);
            System.out.println("\t1. Agregar producto");
            System.out.println("\t2. Mostrar inventario");
            System.out.println("\t3. Editar producto");
            System.out.println("\t4. Eliminar producto");
            System.out.println("\t5. Salir");
            System.out.println(Utils.TITULO + "-------------------------------------" + Utils.RESET);

            int opcion = Utils.leerEntero("Seleccione una opción: ");

            switch (opcion) {
                case 1 -> servicio.agregarProducto();
                case 2 -> servicio.mostrarInventario();
                case 3 -> servicio.editarProducto();
                case 4 -> servicio.eliminarProducto();
                case 5 -> {
                    System.out.println(Utils.EXITO + "\nSaliendo del sistema de forma segura..." + Utils.RESET);
                    ejecutar = false;
                }
                default -> System.out.println(Utils.ERROR + "Opción inválida. Intente de nuevo." + Utils.RESET);
            }
            System.out.println(); // Salto de línea estético
        }
    }
}