package agenda;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class AgendaApp {
    
    private static final PrintStream out = System.out;


    private static final String RESET = "\u001B[0m";
    private static final String ROJO = "\u001B[31m";
    private static final String VERDE = "\u001B[32m";
    private static final String AMARILLO = "\u001B[33m";
    private static final String AZUL = "\u001B[34m";
    private static final String CIAN = "\u001B[36m";


    private static List<Contacto> coleccionMemory;

    public static void main(String[] args) {
        
        coleccionMemory = GestorArchivo.cargarAgenda();
        out.println(VERDE + "[Sistema]: Base de datos cargada correctamente en memoria." + RESET);

       
        try (BufferedReader teclado = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
            boolean activo = true;
            while (activo) {
                imprimirMenu();
                String opcion = teclado.readLine();
                if (opcion == null) break;

                switch (opcion.trim()) {
                    case "1": agregar(teclado); break;
                    case "2": mostrar(); break;
                    case "3": eliminar(teclado); break;
                    case "4": editar(teclado); break;
                    case "5":
                        out.println(VERDE + "\n[Sistema]: Saliendo del ecosistema seguro." + RESET);
                        activo = false;
                        break;
                    default:
                        out.println(ROJO + "[Alerta]: Opción inválida." + RESET);
                }
            }
        } catch (IOException e) {
            out.println(ROJO + "[Error]: Falla en los flujos de lectura de consola." + RESET);
        }
    }

    private static void imprimirMenu() {
        out.println(AZUL + "\n=======================================" + RESET);
        out.println(CIAN + "       MENÚ AGENDA ENCRIPTADA AES" + RESET);
        out.println(AZUL + "=======================================" + RESET);
        out.println("1. " + AMARILLO + "Agregar" + RESET + " contacto");
        out.println("2. " + AMARILLO + "Mostrar" + RESET + " contactos (Tabulados)");
        out.println("3. " + AMARILLO + "Eliminar" + RESET + " contacto");
        out.println("4. " + AMARILLO + "Editar" + RESET + " contacto");
        out.println("5. " + ROJO + "Salir" + RESET);
        out.print(CIAN + "Selección > " + RESET);
    }

    private static void agregar(BufferedReader br) throws IOException {
        out.println(CIAN + "\n--- Registrar Contacto ---" + RESET);
        out.print("Nombre: "); String nom = br.readLine();
        out.print("Teléfono: "); String tel = br.readLine();
        out.print("Email: "); String em = br.readLine();
        out.print("Nota Privada (Se encriptará): "); String nota = br.readLine();

        if (nom.contains(";") || tel.contains(";") || em.contains(";") || nota.contains(";")) {
            out.println(ROJO + "[Error]: No se permite usar el carácter ';'." + RESET);
            return;
        }

        coleccionMemory.add(new Contacto(nom, tel, em, nota));
        persistir();
    }

    private static void mostrar() {
        out.println(CIAN + "\n--------------------------------------------------------------------------------" + RESET);
        out.printf(AMARILLO + "%-5s %-15s %-12s %-22s %-20s\n" + RESET, "ID", "Nombre", "Teléfono", "Email", "Nota Privada (AES)");
        out.println(CIAN + "--------------------------------------------------------------------------------" + RESET);

        if (coleccionMemory.isEmpty()) {
            out.println("   La memoria se encuentra vacía.");
        } else {
            for (int i = 0; i < coleccionMemory.size(); i++) {
                Contacto c = coleccionMemory.get(i);
                out.printf("%-5d %-15s %-12s %-22s %-20s\n", (i + 1), c.getNombre(), c.getTelefono(), c.getEmail(), VERDE + c.getNotaPrivada() + RESET);
            }
        }
        out.println(CIAN + "--------------------------------------------------------------------------------" + RESET);
    }

    private static void eliminar(BufferedReader br) throws IOException {
        mostrar();
        if (coleccionMemory.isEmpty()) return;

        out.print(CIAN + "ID a eliminar: " + RESET);
        try {
            int id = Integer.parseInt(br.readLine()) - 1;
            if (id >= 0 && id < coleccionMemory.size()) {
                coleccionMemory.remove(id);
                persistir();
            } else {
                out.println(ROJO + "[Alerta]: ID inválido." + RESET);
            }
        } catch (NumberFormatException e) {
            out.println(ROJO + "[Error]: Debe ingresar un número entero." + RESET);
        }
    }

    private static void editar(BufferedReader br) throws IOException {
        mostrar();
        if (coleccionMemory.isEmpty()) return;

        out.print(CIAN + "ID a modificar: " + RESET);
        try {
            int id = Integer.parseInt(br.readLine()) - 1;
            if (id >= 0 && id < coleccionMemory.size()) {
                Contacto c = coleccionMemory.get(id);

                out.print("Nuevo Nombre [" + c.getNombre() + "]: "); String nom = br.readLine();
                out.print("Nuevo Teléfono [" + c.getTelefono() + "]: "); String tel = br.readLine();
                out.print("Nuevo Email [" + c.getEmail() + "]: "); String em = br.readLine();
                out.print("Nueva Nota [" + c.getNotaPrivada() + "]: "); String nota = br.readLine();

                if (!nom.trim().isEmpty()) c.setNombre(nom);
                if (!tel.trim().isEmpty()) c.setTelefono(tel);
                if (!em.trim().isEmpty()) c.setEmail(em);
                if (!nota.trim().isEmpty()) c.setNotaPrivada(nota);

                persistir();
            } else {
                out.println(ROJO + "[Alerta]: ID fuera de rango." + RESET);
            }
        } catch (NumberFormatException e) {
            out.println(ROJO + "[Error]: Formato de ID incorrecto." + RESET);
        }
    }

    private static void persistir() {
       
        if (GestorArchivo.guardarAgenda(coleccionMemory)) {
            out.println(VERDE + "[Éxito]: Sincronizado atómicamente con agenda.dat." + RESET);
        } else {
            out.println(ROJO + "[Fallo Crítico]: Error de guardado seguro." + RESET);
        }
    }
}
