package com.empresa.empleados;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Scanner;

/**
 * Clase para gestionar operaciones CRUD de empleados.
 */
public class Empleados {

    private static final Logger logger = LoggerFactory.getLogger(Empleados.class);

    /**
     * Muestra el menú principal para gestión de empleados y gestiona las opciones seleccionadas.
     *
     * @param conn    Conexión a la base de datos.
     * @param scanner Scanner para leer entrada del usuario.
     */
    public static void mostrarMenu(Connection conn, Scanner scanner) {
        boolean salir = false;

        while (!salir) {
            System.out.println("\n--- Menú de gestión de empleados ---");
            System.out.println("1. Crear empleado");
            System.out.println("2. Listar empleados");
            System.out.println("3. Actualizar empleado");
            System.out.println("4. Eliminar empleado");
            System.out.println("0. Volver al menú principal");
            System.out.print("Selecciona una opción: ");

            String input = scanner.nextLine();
            int opcion;
            try {
                opcion = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("❌ Entrada inválida. Por favor, introduce un número.");
                logger.warn("Entrada inválida para opción menú: {}", input);
                continue;
            }

            switch (opcion) {
                case 1 -> crearEmpleado(conn, scanner);
                case 2 -> listarEmpleados(conn);
                case 3 -> actualizarEmpleado(conn, scanner);
                case 4 -> eliminarEmpleado(conn, scanner);
                case 0 -> {
                    salir = true;
                    System.out.println("👋 Volviendo al menú principal...");
                }
                default -> {
                    System.out.println("❌ Opción no válida.");
                    logger.warn("Opción inválida en menú empleados: {}", opcion);
                }
            }
        }
    }

    /**
     * Solicita y valida la entrada de un número entero positivo desde el Scanner.
     * Repite la petición hasta que se introduce un valor válido.
     *
     * @param scanner Scanner para leer la entrada.
     * @param mensaje Mensaje para mostrar al usuario.
     * @return Número entero positivo ingresado.
     */
    public static int leerEntero(Scanner scanner, String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String input = scanner.nextLine().trim();
            if (input.matches("\\d+")) {
                return Integer.parseInt(input);
            }
            System.out.println("❌ Entrada inválida. Por favor, introduce un número entero positivo.");
        }
    }

    /**
     * Crea un nuevo empleado solicitando datos por consola y guardándolos en la base de datos.
     *
     * @param conn    Conexión a la base de datos.
     * @param scanner Scanner para leer entrada del usuario.
     */
    public static void crearEmpleado(Connection conn, Scanner scanner) {
        try {
            System.out.print("Nombre del empleado: ");
            String nombre = scanner.nextLine().trim();

            System.out.print("Email: ");
            String email = scanner.nextLine().trim();

            System.out.print("Departamento: ");
            String departamento = scanner.nextLine().trim();

            if (nombre.isEmpty() || email.isEmpty() || departamento.isEmpty()) {
                System.out.println("❌ No se permiten campos vacíos al crear empleado.");
                logger.warn("Intento de crear empleado con campos vacíos");
                return;
            }

            String sql = "INSERT INTO empleados (nombre, email, departamento) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, nombre);
                pstmt.setString(2, email);
                pstmt.setString(3, departamento);
                int filas = pstmt.executeUpdate();
                if (filas > 0) {
                    System.out.println("\n✅ Empleado creado correctamente:");
                    System.out.println("- Nombre: " + nombre);
                    System.out.println("- Email: " + email);
                    System.out.println("- Departamento: " + departamento);
                } else {
                    System.out.println("❌ No se pudo crear el empleado.");
                    logger.warn("Insert empleado no afectó filas");
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Error al crear empleado.");
            logger.error("Error al crear empleado", e);
        }
    }

    /**
     * Lista todos los empleados guardados en la base de datos mostrando sus datos por consola.
     *
     * @param conn Conexión a la base de datos.
     */
    public static void listarEmpleados(Connection conn) {
        String sql = "SELECT * FROM empleados";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("\n📋 Lista de empleados:");
            boolean hay = false;
            while (rs.next()) {
                hay = true;
                System.out.printf("ID: %d | Nombre: %s | Email: %s | Departamento: %s%n",
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("email"),
                        rs.getString("departamento"));
            }
            if (!hay) {
                System.out.println("ℹ️ No hay empleados registrados.");
            }
        } catch (SQLException e) {
            System.out.println("❌ Error al listar empleados.");
            logger.error("Error al listar empleados", e);
        }
    }

    /**
     * Actualiza los datos de un empleado identificado por su ID.
     * Solicita los nuevos valores y realiza las validaciones pertinentes.
     *
     * @param conn    Conexión a la base de datos.
     * @param scanner Scanner para leer entrada del usuario.
     */
    public static void actualizarEmpleado(Connection conn, Scanner scanner) {
        try {
            // Leer ID con validación
            int id = leerEntero(scanner, "ID del empleado a actualizar: ");

            // Verificar que el empleado exista
            String checkSql = "SELECT COUNT(*) FROM empleados WHERE id = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, id);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) == 0) {
                    System.out.println("❌ No existe un empleado con el ID: " + id);
                    logger.warn("Intento de actualizar empleado inexistente. ID: {}", id);
                    return;
                }
            }

            // Leer nuevos datos
            System.out.print("Nuevo nombre: ");
            String nombre = scanner.nextLine().trim();
            if (nombre.isEmpty()) {
                System.out.println("❌ El nombre no puede estar vacío");
                return;
            }

            System.out.print("Nuevo email: ");
            String email = scanner.nextLine().trim();
            if (email.isEmpty()) {
                System.out.println("❌ El email no puede estar vacío");
                return;
            }

            System.out.print("Nuevo departamento: ");
            String departamento = scanner.nextLine().trim();
            if (departamento.isEmpty()) {
                System.out.println("❌ El departamento no puede estar vacío");
                return;
            }

            // Ejecutar update
            String updateSql = "UPDATE empleados SET nombre = ?, email = ?, departamento = ? WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                pstmt.setString(1, nombre);
                pstmt.setString(2, email);
                pstmt.setString(3, departamento);
                pstmt.setInt(4, id);

                int filas = pstmt.executeUpdate();
                if (filas > 0) {
                    System.out.println("\n✅ Empleado actualizado con éxito:");
                    System.out.println("- Nombre: " + nombre);
                    System.out.println("- Email: " + email);
                    System.out.println("- Departamento: " + departamento);
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Error al actualizar el empleado.");
            logger.error("Error al actualizar empleado", e);
        }
    }

    /**
     * Elimina un empleado de la base de datos identificado por su ID tras confirmación del usuario.
     *
     * @param conn    Conexión a la base de datos.
     * @param scanner Scanner para leer entrada del usuario.
     */
    public static void eliminarEmpleado(Connection conn, Scanner scanner) {
        try {
            // Leer ID con validación
            int id = leerEntero(scanner, "ID del empleado a eliminar: ");

            // Verificar que el empleado exista
            String checkSql = "SELECT COUNT(*) FROM empleados WHERE id = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, id);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) == 0) {
                    System.out.println("❌ No existe un empleado con el ID: " + id);
                    logger.warn("Intento de eliminar empleado inexistente. ID: {}", id);
                    return;
                }
            }

            // Confirmación
            System.out.print("¿Está seguro de eliminar el empleado? (S/N): ");
            String confirmacion = scanner.nextLine().trim();
            if (!confirmacion.equalsIgnoreCase("S")) {
                System.out.println("ℹ️ Operación cancelada");
                return;
            }

            // Ejecutar eliminación
            String sql = "DELETE FROM empleados WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                int filas = pstmt.executeUpdate();
                if (filas > 0) {
                    System.out.println("✅ Empleado eliminado con éxito.");
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Error al eliminar empleado.");
            logger.error("Error al eliminar empleado", e);
        }
    }
}
