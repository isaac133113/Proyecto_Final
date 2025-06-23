import com.empresa.empleados.Empleados;
import com.empresa.reservas.Reservas;
import com.empresa.salas.Salas;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;

/**
 * Clase principal de la aplicación.
 * Gestiona la conexión a la base de datos y muestra el menú principal1
 *
 * para acceder a la gestión de reservas, empleados y salas.
 */
static final String DB_URL = "jdbc:mysql://localhost:3306/reservas_salas?useSSL=false&serverTimezone=UTC";
static final String USER = "root";
static final String PASS = "root";
public static void main(String[] args) {

    try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
         Scanner scanner = new Scanner(System.in)) {

        System.out.println("✅ Conectado a la base de datos");
        boolean salir = false;

        while (!salir) {
            System.out.println("\n--- Menú principal ---");
            System.out.println("1. Gestionar reservas");
            System.out.println("2. Gestionar empleados");
            System.out.println("3. Gestionar salas");
            System.out.println("4. Salir");
            System.out.print("Selecciona una opción: ");

            if (!scanner.hasNextLine()) {
                System.out.println("No hay más entrada, saliendo...");
                break; // Sale del while y termina programa
            }
            String entrada = scanner.nextLine();
            int opcion;

            try {
                opcion = Integer.parseInt(entrada);
            } catch (NumberFormatException e) {
                System.err.println("❌ Entrada inválida. Por favor, introduce un número.");
                continue;
            }

            switch (opcion) {
                case 1 -> Reservas.mostrarMenu(conn);
                case 2 -> Empleados.mostrarMenu(conn);
                case 3 -> Salas.mostrarMenu(conn);
                case 4 -> {
                    salir = true;
                    System.out.println("Saliendo...");
                }
                default -> System.err.println("Opción no válida.");
            }
        }

    } catch (SQLException e) {
        System.err.println("❌ Error al conectar con la base de datos");
        e.printStackTrace();
    }
}

