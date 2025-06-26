import com.empresa.empleados.Empleados;
import com.empresa.reservas.Reservas;
import com.empresa.salas.Salas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;

public class Principal {

    static final String DB_URL = "jdbc:mysql://localhost:3306/reservas_salas";
    static final String USER = "root";
    static final String PASS = "root";
    private static final Logger logger = LoggerFactory.getLogger(Principal.class);

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
                System.out.println("0. Salir");
                System.out.print("Selecciona una opción: ");

                String entrada = scanner.nextLine();
                int opcion;

                try {
                    opcion = Integer.parseInt(entrada);
                } catch (NumberFormatException e) {
                    System.err.println("❌ Entrada inválida. Por favor, introduce un número.");
                    continue;
                }

                switch (opcion) {
                    case 1 -> Reservas.mostrarMenu(conn, scanner);
                    case 2 -> Empleados.mostrarMenu(conn, scanner);
                    case 3 -> Salas.mostrarMenu(conn, scanner);
                    case 0 -> { salir = true;
                        System.out.println("👋 Saliendo...");
                    }
                    default -> System.err.println("Opción no válida.");
                }
            }

        } catch (SQLException e) {
            logger.error("❌ Error al conectar con la base de datos: {}", e);
        }
    }
}
