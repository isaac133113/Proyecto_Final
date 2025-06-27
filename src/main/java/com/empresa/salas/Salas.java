package com.empresa.salas;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Scanner;

/**
 * Clase que gestiona las operaciones CRUD para salas.
 * Permite crear, listar, actualizar y eliminar salas a través de una interfaz de consola.
 */
public class Salas {

    private static final Logger logger = LoggerFactory.getLogger(Salas.class);

    /**
     * Muestra el menú principal de gestión de salas.
     * Permite seleccionar acciones interactivas desde consola.
     *
     * @param conn    Conexión activa a la base de datos
     * @param scanner Scanner para capturar la entrada del usuario
     */
    public static void mostrarMenu(Connection conn, Scanner scanner) {
        boolean salir = false;

        while (!salir) {
            System.out.println("\n--- Menú de gestión de salas ---");
            System.out.println("1. Crear sala");
            System.out.println("2. Listar salas");
            System.out.println("3. Actualizar sala");
            System.out.println("4. Eliminar sala");
            System.out.println("0. Volver al menú principal");
            System.out.print("Selecciona una opción: ");

            int opcion = -1;
            if (scanner.hasNextInt()) {
                opcion = scanner.nextInt();
                scanner.nextLine(); // limpiar el buffer
            } else {
                System.out.println("❌ Entrada inválida. Por favor, introduce un número.");
                scanner.nextLine(); // descartar entrada inválida
                continue;
            }

            switch (opcion) {
                case 1 -> crearSala(conn, scanner);
                case 2 -> listarSalas(conn);
                case 3 -> actualizarSala(conn, scanner);
                case 4 -> eliminarSala(conn, scanner);
                case 0 -> {
                    salir = true;
                    System.out.println("👋 Volviendo al menú principal...");
                }
                default -> System.out.println("❌ Opción no válida.");
            }
        }
    }

    /**
     * Crea una nueva sala en la base de datos.
     *
     * @param conn    Conexión activa a la base de datos
     * @param scanner Scanner para capturar la entrada del usuario
     */
    public static void crearSala(Connection conn, Scanner scanner) {
        try {
            System.out.print("Nombre de la sala: ");
            String nombre = scanner.nextLine();

            System.out.print("Capacidad: ");
            int capacidad = Integer.parseInt(scanner.nextLine());

            System.out.print("Recursos: ");
            String recursos = scanner.nextLine();

            // Validaciones básicas
            if (nombre.trim().isEmpty() || recursos.trim().isEmpty()) {
                System.out.println("❌ Todos los campos son obligatorios");
                return;
            }

            if (capacidad <= 0) {
                System.out.println("❌ La capacidad debe ser mayor que 0");
                return;
            }

            // Inserción en la base de datos
            String sql = "INSERT INTO salas (nombre, capacidad, recursos) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, nombre);
                pstmt.setInt(2, capacidad);
                pstmt.setString(3, recursos);

                int filas = pstmt.executeUpdate();

                if (filas > 0) {
                    System.out.println("\n✅ Sala creada exitosamente:");
                    System.out.println("- Nombre: " + nombre);
                    System.out.println("- Capacidad: " + capacidad);
                    System.out.println("- Recursos: " + recursos);
                } else {
                    System.out.println("❌ No se pudo crear la sala");
                    logger.warn("Insert salas no afectó filas");
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Error al crear la sala.");
            logger.error("Error al crear la sala", e);
        } catch (NumberFormatException e) {
            System.out.println("❌ La capacidad debe ser un número válido");
            logger.error("❌ La capacidad debe ser un número válido", e);
        }
    }

    /**
     * Lista todas las salas registradas en la base de datos.
     *
     * @param conn Conexión activa a la base de datos
     */
    public static void listarSalas(Connection conn) {
        String sql = "SELECT * FROM salas";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n📋 Listado de salas:");
            boolean hayRegistros = false;

            // Recorre los resultados
            while (rs.next()) {
                hayRegistros = true;
                System.out.printf("ID: %d | Nombre: %s | Capacidad: %d | Recursos: %s%n",
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getInt("capacidad"),
                        rs.getString("recursos"));
            }

            if (!hayRegistros) {
                System.out.println("ℹ️ No hay salas registradas en el sistema.");
            }
        } catch (SQLException e) {
            logger.error("Error al obtener listado de salas", e);
            System.out.println("❌ Error al leer las salas.");
        }
    }

    /**
     * Actualiza la información de una sala existente.
     *
     * @param conn    Conexión activa a la base de datos
     * @param scanner Scanner para capturar la entrada del usuario
     */
    private static void actualizarSala(Connection conn, Scanner scanner) {
        try {
            System.out.print("ID de la sala a actualizar: ");
            String idInput = scanner.nextLine();

            if (!idInput.matches("\\d+")) {
                System.out.println("❌ El ID debe ser un número entero positivo");
                logger.warn("Intento de actualizar sala con ID inválido: {}", idInput);
                return;
            }
            int id = Integer.parseInt(idInput);

            // Verifica si la sala existe
            String sql = "SELECT COUNT(*) FROM salas WHERE id = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(sql)) {
                checkStmt.setInt(1, id);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) == 0) {
                    System.out.println("❌ No existe una sala con el ID: " + id);
                    logger.warn("Intento de actualizar sala inexistente. ID: {}", id);
                    return;
                }
            }

            // Solicita nuevos datos
            System.out.print("Nuevo nombre: ");
            String nombre = scanner.nextLine();
            if (nombre.trim().isEmpty()) {
                System.out.println("❌ El nombre no puede estar vacío");
                return;
            }

            System.out.print("Nueva capacidad: ");
            String capacidadInput = scanner.nextLine();
            if (!capacidadInput.matches("\\d+")) {
                System.out.println("❌ La capacidad debe ser un número entero positivo");
                return;
            }
            int capacidad = Integer.parseInt(capacidadInput);
            if (capacidad <= 0) {
                System.out.println("❌ La capacidad debe ser mayor que 0");
                return;
            }

            System.out.print("Nuevos recursos: ");
            String recursos = scanner.nextLine();
            if (recursos.trim().isEmpty()) {
                System.out.println("❌ Los recursos no pueden estar vacíos");
                return;
            }

            // Ejecuta la actualización
            String updateSql = "UPDATE salas SET nombre = ?, capacidad = ?, recursos = ? WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                pstmt.setString(1, nombre);
                pstmt.setInt(2, capacidad);
                pstmt.setString(3, recursos);
                pstmt.setInt(4, id);

                int filas = pstmt.executeUpdate();
                if (filas > 0) {
                    System.out.println("\n✅ Sala actualizada con éxito:");
                    System.out.println("- Nombre: " + nombre);
                    System.out.println("- Capacidad: " + capacidad);
                    System.out.println("- Recursos: " + recursos);
                }
            }
        } catch (SQLException e) {
            logger.error("Error al actualizar la sala", e);
            System.out.println("❌ Error al actualizar la sala.");
        }
    }

    /**
     * Elimina una sala de la base de datos tras confirmación del usuario.
     *
     * @param conn    Conexión activa a la base de datos
     * @param scanner Scanner para capturar la entrada del usuario
     */
    private static void eliminarSala(Connection conn, Scanner scanner) {
        try {
            System.out.print("ID de la sala a eliminar: ");
            String idInput = scanner.nextLine();

            if (!idInput.matches("\\d+")) {
                System.out.println("❌ El ID debe ser un número entero positivo");
                logger.warn("Intento de eliminar sala con ID inválido: {}", idInput);
                return;
            }
            int id = Integer.parseInt(idInput);

            // Verifica si la sala existe
            String checkSql = "SELECT COUNT(*) FROM salas WHERE id = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, id);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) == 0) {
                    System.out.println("❌ No existe una sala con el ID: " + id);
                    logger.warn("Intento de eliminar sala inexistente. ID: {}", id);
                    return;
                }
            }

            System.out.print("¿Está seguro de eliminar la sala? (S/N): ");
            String confirmacion = scanner.nextLine();
            if (!confirmacion.equalsIgnoreCase("S")) {
                System.out.println("ℹ️ Operación cancelada");
                return;
            }

            // Ejecuta eliminación
            String sql = "DELETE FROM salas WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                int filas = pstmt.executeUpdate();
                if (filas > 0) {
                    System.out.println("✅ Sala eliminada con éxito.");
                }
            }
        } catch (SQLException e) {
            logger.error("Error al eliminar sala", e);
            System.out.println("❌ Error al eliminar la sala.");
        } catch (NumberFormatException e) {
            System.out.println("❌ ID inválido. Debe ser un número entero.");
            logger.warn("Error de formato en ID al eliminar sala", e);
        }
    }
}
