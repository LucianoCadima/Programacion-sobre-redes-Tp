package TP3;

import TP3.Producto;
import TP3.Utils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Consultas {
    private static final String FILE_NAME = "Inventario.dat";

    public Consultas() {
        crearArchivoSiNoExiste();
    }

    private void crearArchivoSiNoExiste() {
        File file = new File(FILE_NAME);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            System.out.println(Utils.ERROR + "Error al inicializar el archivo." + Utils.RESET);
        }
    }

    // Cargar todos los productos desde el archivo a memoria
    private List<Producto> cargarProductos() {
        List<Producto> lista = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_NAME))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                if (linea.trim().isEmpty()) continue;
                String[] campos = linea.split(";");
                if (campos.length == 4) {
                    String nombre = campos[0];
                    float pCompra = Float.parseFloat(campos[1]);
                    float pVenta = Float.parseFloat(campos[2]);
                    int stock = Integer.parseInt(campos[3]);
                    lista.add(new Producto(nombre, pCompra, pVenta, stock));
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.out.println(Utils.ERROR + "Error al leer los datos." + Utils.RESET);
        }
        return lista;
    }

    // Guardar la lista completa de regreso al archivo
    private void guardarTodosLosProductos(List<Producto> lista) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (Producto p : lista) {
                bw.write(p.toFileFormat());
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println(Utils.ERROR + "Error al escribir en el archivo." + Utils.RESET);
        }
    }

    public void agregarProducto() {
        System.out.println("\n" + Utils.TITULO + "--- REGISTRAR NUEVO ARTÍCULO ---" + Utils.RESET);
        String nombre = Utils.leerStringObligatorio("\tNombre: ");
        float pCompra = Utils.leerDecimal("\tPrecio de Compra: ");
        float pVenta = Utils.leerDecimal("\tPrecio de Venta: ");
        int stock = Utils.leerEntero("\tStock: ");

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_NAME, true))) {
            Producto nuevo = new Producto(nombre, pCompra, pVenta, stock);
            bw.write(nuevo.toFileFormat());
            bw.newLine();
            System.out.println(Utils.EXITO + "\n\t¡Producto agregado con éxito!" + Utils.RESET);
        } catch (IOException e) {
            System.out.println(Utils.ERROR + "\tNo se pudo guardar el producto." + Utils.RESET);
        }
    }

    public void mostrarInventario() {
        List<Producto> lista = cargarProductos();
        System.out.println("\n" + Utils.TITULO + "-------------------------------------------------------------");
        System.out.printf("%-20s %-15s %-15s %-10s\n", "NOMBRE", "P. COMPRA", "P. VENTA", "STOCK");
        System.out.println("-------------------------------------------------------------" + Utils.RESET);

        if (lista.isEmpty()) {
            System.out.println("\t[El inventario está vacío]");
        } else {
            for (Producto p : lista) {
                System.out.printf("%-20s $%-14.2f $%-14.2f %-10d\n", 
                        p.getNombre(), p.getPrecioCompra(), p.getPrecioVenta(), p.getStock());
            }
        }
        System.out.println(Utils.TITULO + "-------------------------------------------------------------\n" + Utils.RESET);
    }

    public void editarProducto() {
        System.out.println("\n" + Utils.TITULO + "--- EDITAR PRODUCTO ---" + Utils.RESET);
        String nombreBuscar = Utils.leerString("Ingrese el nombre del producto a editar: ");
        List<Producto> lista = cargarProductos();
        boolean encontrado = false;

        for (Producto p : lista) {
            if (p.getNombre().equalsIgnoreCase(nombreBuscar)) {
                encontrado = true;
                System.out.println(Utils.EXITO + "\tProducto encontrado. Ingrese los nuevos datos:" + Utils.RESET);
                p.setPrecioCompra(Utils.leerDecimal("\tNuevo Precio de Compra: "));
                p.setPrecioVenta(Utils.leerDecimal("\tNuevo Precio de Venta: "));
                p.setStock(Utils.leerEntero("\tNuevo Stock: "));
                break;
            }
        }

        if (encontrado) {
            guardarTodosLosProductos(lista);
            System.out.println(Utils.EXITO + "\t¡Producto modificado correctamente!" + Utils.RESET);
        } else {
            System.out.println(Utils.ERROR + "\tProducto no encontrado." + Utils.RESET);
        }
    }

    public void eliminarProducto() {
        System.out.println("\n" + Utils.TITULO + "--- ELIMINAR PRODUCTO ---" + Utils.RESET);
        String nombreBuscar = Utils.leerString("Ingrese el nombre del producto a eliminar: ");
        List<Producto> lista = cargarProductos();
        boolean eliminado = lista.removeIf(p -> p.getNombre().equalsIgnoreCase(nombreBuscar));

        if (eliminado) {
            guardarTodosLosProductos(lista);
            System.out.println(Utils.EXITO + "\t¡Producto eliminado correctamente del archivo!" + Utils.RESET);
        } else {
            System.out.println(Utils.ERROR + "\tProducto no encontrado." + Utils.RESET);
        }
    }
}