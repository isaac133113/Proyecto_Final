import com.empresa.empleados.Empleados;
import com.empresa.reservas.Reservas;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;

public class Main {
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
                System.out.println("1. Gestionar salas");
                System.out.println("2. Gestionar empleados");
                System.out.println("3. Salir");
                System.out.print("Selecciona una opción: ");

                int opcion = -1;
                if (scanner.hasNextInt()) {
                    opcion = scanner.nextInt();
                    scanner.nextLine(); // limpiar buffer
                } else {
                    System.err.println("❌ Entrada inválida. Por favor, introduce un número.");
                    scanner.nextLine(); // limpiar entrada errónea
                    continue;
                }

                switch (opcion) {
                    case 1 -> Reservas.mostrarMenu(conn);
                    case 2 -> Empleados.mostrarMenu(conn);
           //        case 3 ->
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
}
