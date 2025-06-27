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

            String input = scanner.nextLine().trim();
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

    private static boolean existeConflictoReserva(Connection conn, int salaId, String fecha, String horaInicio, String horaFin, Integer excluirId) {
        String sql = """
            SELECT COUNT(*) FROM reservas
            WHERE sala_id = ? AND fecha = ?
            AND (hora_inicio < ? AND hora_fin > ?)""";

        if (excluirId != null) {
            sql += " AND id <> ?";
        }

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, salaId);
            stmt.setDate(2, Date.valueOf(fecha));
            stmt.setTime(3, Time.valueOf(horaFin));
            stmt.setTime(4, Time.valueOf(horaInicio));

            if (excluirId != null) {
                stmt.setInt(5, excluirId);
            }

            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            logger.error("Error verificando conflicto de reserva", e);
            return true;
        }
    }

    public static void crearReserva(Connection conn, Scanner scanner) {
        try {
            System.out.print("ID de la sala: ");
            int idSala = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("ID del empleado: ");
            int idEmpleado = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Fecha (YYYY-MM-DD): ");
            String fecha = scanner.nextLine().trim();
            System.out.print("Hora de inicio (HH:MM:SS): ");
            String horaInicio = scanner.nextLine().trim();
            System.out.print("Hora de fin (HH:MM:SS): ");
            String horaFin = scanner.nextLine().trim();

            if (existeConflictoReserva(conn, idSala, fecha, horaInicio, horaFin, null)) {
                System.out.println("❌ Conflicto: ya existe una reserva en ese horario.");
                return;
            }

            String sql = "INSERT INTO reservas (sala_id, empleado_id, fecha, hora_inicio, hora_fin) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, idSala);
                pstmt.setInt(2, idEmpleado);
                pstmt.setDate(3, Date.valueOf(fecha));
                pstmt.setTime(4, Time.valueOf(horaInicio));
                pstmt.setTime(5, Time.valueOf(horaFin));

                int filas = pstmt.executeUpdate();
                if (filas > 0) {
                    System.out.println("\n✅ Reserva creada exitosamente:");
                    System.out.println("- ID Sala: " + idSala);
                    System.out.println("- ID Empleado: " + idEmpleado);
                    System.out.println("- Fecha: " + fecha);
                    System.out.println("- Hora inicio: " + horaInicio + " | Hora fin: " + horaFin);
                } else {
                    System.out.println("❌ No se pudo crear la reserva.");
                    logger.warn("Insert reservas no afectó filas");
                }
            }
        } catch (SQLException | IllegalArgumentException e) {
            logger.error("Error al crear la reserva", e);
            System.out.println("❌ Error al crear la reserva.");
        }
    }

    public static void listarReservas(Connection conn) {
        String sql = """
            SELECT r.id, r.fecha, r.hora_inicio, r.hora_fin, s.nombre AS sala_nombre, e.nombre AS empleado_nombre
            FROM reservas r
            JOIN salas s ON r.sala_id = s.id
            JOIN empleados e ON r.empleado_id = e.id
        """;

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n📋 Listado de reservas:");
            boolean hayRegistros = false;

            while (rs.next()) {
                hayRegistros = true;
                System.out.printf("ID: %d | Sala: %s | Empleado: %s | Fecha: %s | %s-%s%n",
                        rs.getInt("id"),
                        rs.getString("sala_nombre"),
                        rs.getString("empleado_nombre"),
                        rs.getDate("fecha"),
                        rs.getTime("hora_inicio"),
                        rs.getTime("hora_fin"));
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
            int id = Integer.parseInt(scanner.nextLine().trim());

            String checkSql = "SELECT COUNT(*) FROM reservas WHERE id = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, id);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) == 0) {
                    System.out.println("❌ No existe una reserva con el ID: " + id);
                    return;
                }
            }

            System.out.print("Nuevo ID de sala: ");
            int salaId = Integer.parseInt(scanner.nextLine().trim());

            String checkSala = "SELECT COUNT(*) FROM salas WHERE id = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSala)) {
                checkStmt.setInt(1, salaId);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) == 0) {
                    System.out.println("❌ La sala con ID " + salaId + " no existe.");
                    return;
                }
            }

            System.out.print("Nuevo ID del empleado: ");
            int empleadoId = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Nueva fecha (YYYY-MM-DD): ");
            String fecha = scanner.nextLine().trim();
            System.out.print("Nueva hora inicio (HH:MM:SS): ");
            String horaInicio = scanner.nextLine().trim();
            System.out.print("Nueva hora fin (HH:MM:SS): ");
            String horaFin = scanner.nextLine().trim();

            if (existeConflictoReserva(conn, salaId, fecha, horaInicio, horaFin, id)) {
                System.out.println("❌ Conflicto de horario. No se puede actualizar.");
                return;
            }

            String updateSql = "UPDATE reservas SET sala_id = ?, empleado_id = ?, fecha = ?, hora_inicio = ?, hora_fin = ? WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                pstmt.setInt(1, salaId);
                pstmt.setInt(2, empleadoId);
                pstmt.setDate(3, Date.valueOf(fecha));
                pstmt.setTime(4, Time.valueOf(horaInicio));
                pstmt.setTime(5, Time.valueOf(horaFin));
                pstmt.setInt(6, id);

                int filas = pstmt.executeUpdate();
                if (filas > 0) {
                    System.out.println("\n✅ Reserva actualizada con éxito:");
                    System.out.println("- ID Sala: " + salaId);
                    System.out.println("- ID Empleado: " + empleadoId);
                    System.out.println("- Fecha: " + fecha);
                    System.out.println("- Hora inicio: " + horaInicio + " | Hora fin: " + horaFin);
                } else {
                    System.out.println("❌ No se pudo actualizar la reserva.");
                }
            }
        } catch (SQLException | IllegalArgumentException e) {
            logger.error("Error al actualizar la reserva", e);
            System.out.println("❌ Error al actualizar la reserva.");
        }
    }

    public static void eliminarReserva(Connection conn, Scanner scanner) {
        try {
            System.out.print("ID de la reserva a eliminar: ");
            int id = Integer.parseInt(scanner.nextLine().trim());

            String checkSql = "SELECT COUNT(*) FROM reservas WHERE id = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, id);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) == 0) {
                    System.out.println("❌ No existe una reserva con el ID: " + id);
                    return;
                }
            }

            System.out.print("¿Está seguro de eliminar la reserva? (S/N): ");
            String confirmacion = scanner.nextLine().trim();
            if (!confirmacion.equalsIgnoreCase("S")) {
                System.out.println("ℹ️ Operación cancelada.");
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
                }
            }
        } catch (SQLException e) {
            logger.error("Error al eliminar reserva", e);
            System.out.println("❌ Error al eliminar la reserva.");
        }
    }
}
