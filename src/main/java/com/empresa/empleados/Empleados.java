package com.empresa.empleados;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Scanner;

public class Empleados {

    private static final Logger logger = LoggerFactory.getLogger(Empleados.class);

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
            System.out.println("❌ Error al crear empleado. Consulta el log para más detalles.");
            logger.error("Error al crear empleado", e);
        }
    }

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

    public static void actualizarEmpleado(Connection conn, Scanner scanner) {
        try {
            System.out.print("ID del empleado a actualizar: ");
            String idInput = scanner.nextLine().trim();

            if (!idInput.matches("\\d+")) {
                System.out.println("❌ El ID debe ser un número entero positivo");
                logger.warn("Intento de actualizar empleado con ID inválido: {}", idInput);
                return;
            }
            int id = Integer.parseInt(idInput);

            // Verificar existencia
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

    public static void eliminarEmpleado(Connection conn, Scanner scanner) {
        try {
            System.out.print("ID del empleado a eliminar: ");
            String idInput = scanner.nextLine().trim();

            if (!idInput.matches("\\d+")) {
                System.out.println("❌ El ID debe ser un número entero positivo");
                logger.warn("Intento de eliminar empleado con ID inválido: {}", idInput);
                return;
            }
            int id = Integer.parseInt(idInput);

            // Verificar existencia
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

            System.out.print("¿Está seguro de eliminar el empleado? (S/N): ");
            String confirmacion = scanner.nextLine().trim();
            if (!confirmacion.equalsIgnoreCase("S")) {
                System.out.println("ℹ️ Operación cancelada");
                return;
            }

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
