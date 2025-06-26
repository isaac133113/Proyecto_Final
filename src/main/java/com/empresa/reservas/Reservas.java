package com.empresa.reservas;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Scanner;

public class Reservas {

    private static final Logger logger = LoggerFactory.getLogger(Reservas.class);

    public static void mostrarMenu(Connection conn, Scanner scanner) {
        boolean salir = false;

        while (!salir) {
            System.out.println("\n--- Menú de gestión de reservas ---");
            System.out.println("1. Crear reserva");
            System.out.println("2. Listar reservas");
            System.out.println("3. Actualizar reserva");
            System.out.println("4. Eliminar reserva");
            System.out.println("0. Volver al menú principal");
            System.out.print("Selecciona una opción: ");

            String input = scanner.nextLine();
            int opcion;
            try {
                opcion = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("❌ Entrada inválida. Por favor, introduce un número.");
                logger.warn("Entrada inválida para opción menú reservas: {}", input);
                continue;
            }

            switch (opcion) {
                case 1 -> crearReserva(conn, scanner);
                case 2 -> listarReservas(conn);
                case 3 -> actualizarReserva(conn, scanner);
                case 4 -> eliminarReserva(conn, scanner);
                case 0 -> {
                    salir = true;
                    System.out.println("👋 Volviendo al menú principal...");
                }
                default -> {
                    System.out.println("❌ Opción no válida.");
                    logger.warn("Opción inválida en menú reservas: {}", opcion);
                }
            }
        }
    }

    public static void crearReserva(Connection conn, Scanner scanner) {
        try {
            System.out.print("ID de la sala: ");
            String idSalaStr = scanner.nextLine().trim();
            if (!idSalaStr.matches("\\d+")) {
                System.out.println("❌ El ID de la sala debe ser un número entero positivo");
                return;
            }
            int idSala = Integer.parseInt(idSalaStr);

            System.out.print("ID del empleado: ");
            String idEmpleadoStr = scanner.nextLine().trim();
            if (!idEmpleadoStr.matches("\\d+")) {
                System.out.println("❌ El ID del empleado debe ser un número entero positivo");
                return;
            }
            int idEmpleado = Integer.parseInt(idEmpleadoStr);

            System.out.print("Fecha y hora (YYYY-MM-DD HH:MM:SS): ");
            String fechaHora = scanner.nextLine().trim();

            if (fechaHora.isEmpty()) {
                System.out.println("❌ La fecha y hora no pueden estar vacías");
                return;
            }

            String sql = "INSERT INTO reservas (id_sala, id_empleado, fecha_hora) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, idSala);
                pstmt.setInt(2, idEmpleado);
                pstmt.setString(3, fechaHora);

                int filas = pstmt.executeUpdate();

                if (filas > 0) {
                    System.out.println("\n✅ Reserva creada exitosamente:");
                    System.out.println("- ID Sala: " + idSala);
                    System.out.println("- ID Empleado: " + idEmpleado);
                    System.out.println("- Fecha y hora: " + fechaHora);
                } else {
                    System.out.println("❌ No se pudo crear la reserva");
                    logger.warn("Insert reservas no afectó filas");
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Error al crear la reserva.");
            logger.error("Error al crear la reserva", e);
        }
    }

    public static void listarReservas(Connection conn) {
        String sql = "SELECT r.id, r.fecha_hora, s.nombre AS sala_nombre, e.nombre AS empleado_nombre " +
                "FROM reservas r " +
                "JOIN salas s ON r.id_sala = s.id " +
                "JOIN empleados e ON r.id_empleado = e.id";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n📋 Listado de reservas:");
            boolean hayRegistros = false;
            while (rs.next()) {
                hayRegistros = true;
                System.out.printf("ID: %d | Sala: %s | Empleado: %s | Fecha y hora: %s%n",
                        rs.getInt("id"),
                        rs.getString("sala_nombre"),
                        rs.getString("empleado_nombre"),
                        rs.getString("fecha_hora"));
            }
            if (!hayRegistros) {
                System.out.println("ℹ️ No hay reservas registradas en el sistema.");
            }
        } catch (SQLException e) {
            logger.error("Error al obtener listado de reservas", e);
            System.out.println("❌ Error al leer las reservas.");
        }
    }

    public static void actualizarReserva(Connection conn, Scanner scanner) {
        try {
            System.out.print("ID de la reserva a actualizar: ");
            String idStr = scanner.nextLine().trim();

            if (!idStr.matches("\\d+")) {
                System.out.println("❌ El ID debe ser un número entero positivo");
                logger.warn("Intento de actualizar reserva con ID inválido: {}", idStr);
                return;
            }
            int id = Integer.parseInt(idStr);

            String checkSql = "SELECT COUNT(*) FROM reservas WHERE id = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, id);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) == 0) {
                    System.out.println("❌ No existe una reserva con el ID: " + id);
                    logger.warn("Intento de actualizar reserva inexistente. ID: {}", id);
                    return;
                }
            }

            System.out.print("Nuevo ID de la sala: ");
            String idSalaStr = scanner.nextLine().trim();
            if (!idSalaStr.matches("\\d+")) {
                System.out.println("❌ El ID de la sala debe ser un número entero positivo");
                return;
            }
            int idSala = Integer.parseInt(idSalaStr);

            System.out.print("Nuevo ID del empleado: ");
            String idEmpleadoStr = scanner.nextLine().trim();
            if (!idEmpleadoStr.matches("\\d+")) {
                System.out.println("❌ El ID del empleado debe ser un número entero positivo");
                return;
            }
            int idEmpleado = Integer.parseInt(idEmpleadoStr);

            System.out.print("Nueva fecha y hora (YYYY-MM-DD HH:MM:SS): ");
            String fechaHora = scanner.nextLine().trim();
            if (fechaHora.isEmpty()) {
                System.out.println("❌ La fecha y hora no pueden estar vacías");
                return;
            }

            String updateSql = "UPDATE reservas SET id_sala = ?, id_empleado = ?, fecha_hora = ? WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                pstmt.setInt(1, idSala);
                pstmt.setInt(2, idEmpleado);
                pstmt.setString(3, fechaHora);
                pstmt.setInt(4, id);

                int filas = pstmt.executeUpdate();
                if (filas > 0) {
                    System.out.println("\n✅ Reserva actualizada con éxito:");
                    System.out.println("- ID Sala: " + idSala);
                    System.out.println("- ID Empleado: " + idEmpleado);
                    System.out.println("- Fecha y hora: " + fechaHora);
                } else {
                    System.out.println("❌ No se pudo actualizar la reserva");
                    logger.warn("Update reservas no afectó filas para ID: {}", id);
                }
            }
        } catch (SQLException e) {
            logger.error("Error al actualizar la reserva", e);
            System.out.println("❌ Error al actualizar la reserva.");
        }
    }

    public static void eliminarReserva(Connection conn, Scanner scanner) {
        try {
            System.out.print("ID de la reserva a eliminar: ");
            String idInput = scanner.nextLine().trim();

            if (!idInput.matches("\\d+")) {
                System.out.println("❌ El ID debe ser un número entero positivo");
                logger.warn("Intento de eliminar reserva con ID inválido: {}", idInput);
                return;
            }
            int id = Integer.parseInt(idInput);

            String checkSql = "SELECT COUNT(*) FROM reservas WHERE id = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, id);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) == 0) {
                    System.out.println("❌ No existe una reserva con el ID: " + id);
                    logger.warn("Intento de eliminar reserva inexistente. ID: {}", id);
                    return;
                }
            }

            System.out.print("¿Está seguro de eliminar la reserva? (S/N): ");
            String confirmacion = scanner.nextLine().trim();
            if (!confirmacion.equalsIgnoreCase("S")) {
                System.out.println("ℹ️ Operación cancelada");
                return;
            }

            String sql = "DELETE FROM reservas WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                int filas = pstmt.executeUpdate();
                if (filas > 0) {
                    System.out.println("✅ Reserva eliminada con éxito.");
                } else {
                    System.out.println("❌ No se pudo eliminar la reserva.");
                    logger.warn("Delete reserva no afectó filas para ID: {}", id);
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Error al eliminar la reserva.");
            logger.error("Error al eliminar reserva", e);
        }
    }
}
