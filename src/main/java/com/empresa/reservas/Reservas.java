package com.empresa.reservas;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Scanner;

/**
 * Clase para gestionar operaciones CRUD de reservas en salas mediante consola.
 * Interactúa con base de datos usando JDBC y realiza validaciones de conflictos.
 */
public class Reservas {

    private static final Logger logger = LoggerFactory.getLogger(Reservas.class);

    /**
     * Muestra el menú principal de gestión de reservas e interactúa con el usuario.
     *
     * @param conn    Conexión a la base de datos.
     * @param scanner Scanner para entrada de datos por consola.
     */
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

            // Validar entrada numérica del usuario
            try {
                opcion = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("❌ Entrada inválida. Por favor, introduce un número.");
                logger.warn("Entrada inválida para opción menú reservas: {}", input);
                continue;
            }

            // Evaluar opción seleccionada
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

    /**
     * Verifica si existe un conflicto de reserva en la misma sala, fecha y horario dado.
     *
     * @param conn       Conexión a la base de datos.
     * @param salaId     ID de la sala.
     * @param fecha      Fecha de la reserva (formato YYYY-MM-DD).
     * @param horaInicio Hora de inicio (formato HH:MM:SS).
     * @param horaFin    Hora de fin (formato HH:MM:SS).
     * @param excluirId  ID de reserva a excluir de la comprobación (útil para actualizar), puede ser null.
     * @return true si existe conflicto; false en caso contrario.
     */
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
            // En caso de error asumimos conflicto para evitar duplicados
            return true;
        }
    }

    /**
     * Método para crear una nueva reserva, solicitando datos y validando conflictos.
     *
     * @param conn    Conexión a la base de datos.
     * @param scanner Scanner para entrada por consola.
     */
    public static void crearReserva(Connection conn, Scanner scanner) {
        try {
            System.out.print("Nombre de la sala: ");
            String nombreSala = scanner.nextLine().trim();

            // Buscar ID de sala por nombre
            String sqlSala = "SELECT id FROM salas WHERE nombre = ?";
            Integer idSala = null;
            try (PreparedStatement psSala = conn.prepareStatement(sqlSala)) {
                psSala.setString(1, nombreSala);
                try (ResultSet rs = psSala.executeQuery()) {
                    if (rs.next()) {
                        idSala = rs.getInt("id");
                    } else {
                        System.out.println("❌ La sala con nombre '" + nombreSala + "' no existe.");
                        return;
                    }
                }
            }

            int idEmpleado = leerEntero(scanner, "ID del empleado: ");

            System.out.print("Fecha (YYYY-MM-DD): ");
            String fecha = scanner.nextLine().trim();
            System.out.print("Hora de inicio (HH:MM:SS): ");
            String horaInicio = scanner.nextLine().trim();
            System.out.print("Hora de fin (HH:MM:SS): ");
            String horaFin = scanner.nextLine().trim();

            // Verificar conflictos antes de insertar
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
                    System.out.println("- Sala: " + nombreSala);
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

    /**
     * Lista todas las reservas existentes mostrando datos relacionados.
     *
     * @param conn Conexión a la base de datos.
     */
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

    /**
     * Actualiza una reserva existente solicitando los nuevos datos y validando conflictos.
     *
     * @param conn    Conexión a la base de datos.
     * @param scanner Scanner para entrada por consola.
     */
    public static void actualizarReserva(Connection conn, Scanner scanner) {
        try {
            int id = leerEntero(scanner, "ID de la reserva a actualizar: ");

            // Verificar que la reserva exista
            String checkSql = "SELECT COUNT(*) FROM reservas WHERE id = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, id);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) == 0) {
                    System.out.println("❌ No existe una reserva con el ID: " + id);
                    return;
                }
            }

            // Solicitar y validar nombre de sala
            System.out.print("Nombre de la sala: ");
            String nombreSala = scanner.nextLine().trim();
            String sqlSala = "SELECT id FROM salas WHERE nombre = ?";
            Integer salaId = null;
            try (PreparedStatement psSala = conn.prepareStatement(sqlSala)) {
                psSala.setString(1, nombreSala);
                try (ResultSet rs = psSala.executeQuery()) {
                    if (rs.next()) {
                        salaId = rs.getInt("id");
                    } else {
                        System.out.println("❌ La sala con nombre '" + nombreSala + "' no existe.");
                        return;
                    }
                }
            }

            int empleadoId = leerEntero(scanner, "Nuevo ID del empleado: ");

            System.out.print("Nueva fecha (YYYY-MM-DD): ");
            String fecha = scanner.nextLine().trim();
            System.out.print("Nueva hora inicio (HH:MM:SS): ");
            String horaInicio = scanner.nextLine().trim();
            System.out.print("Nueva hora fin (HH:MM:SS): ");
            String horaFin = scanner.nextLine().trim();

            // Verificar conflictos excluyendo la reserva actual
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
                    System.out.println("- Sala: " + nombreSala);
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

    /**
     * Elimina una reserva existente solicitando confirmación al usuario.
     *
     * @param conn    Conexión a la base de datos.
     * @param scanner Scanner para entrada por consola.
     */
    public static void eliminarReserva(Connection conn, Scanner scanner) {
        try {
            int id = leerEntero(scanner, "ID de la reserva a eliminar: ");

            // Verificar que la reserva exista
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

    /**
     * Método auxiliar para leer un número entero de manera segura desde consola.
     * Continúa solicitando hasta obtener una entrada válida.
     *
     * @param scanner Scanner para entrada por consola.
     * @param mensaje Mensaje a mostrar al usuario.
     * @return Entero leído.
     */
    private static int leerEntero(Scanner scanner, String mensaje) {
        while (true) {
            System.out.print(mensaje);
            if (scanner.hasNextInt()) {
                int valor = scanner.nextInt();
                scanner.nextLine(); // Consumir salto de línea
                return valor;
            } else {
                System.out.println("❌ Entrada inválida. Debe ingresar un número entero.");
                scanner.nextLine(); // Limpiar entrada inválida
            }
        }
    }
}
