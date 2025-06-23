package com.empresa.reservas;

import java.sql.*;
import java.util.Scanner;

/**
 * Clase que gestiona las operaciones relacionadas con las reservas de salas.
 * Permite crear reservas, consultar las existentes y controlar conflictos de horario.
 */
public class Reservas {

    /**
     * Muestra el menú de gestión de reservas e invoca las operaciones correspondientes.
     * @param conn conexión activa a la base de datos
     */
    public static void mostrarMenu(Connection conn) {
        try (Scanner scanner = new Scanner(System.in)) {
            boolean salir = false;

            while (!salir) {
                // Menú principal
                System.out.println("\n--- Menú de gestión de reservas ---");
                System.out.println("1. Crear reserva");
                System.out.println("2. Listar reservas");
                System.out.println("3. Salir");
                System.out.print("Selecciona una opción: ");

                int opcion = -1;
                if (scanner.hasNextInt()) {
                    opcion = scanner.nextInt();
                    scanner.nextLine(); // Limpiar buffer tras lectura de int
                } else {
                    System.err.println("❌ Entrada inválida.");
                    scanner.nextLine(); // Limpiar entrada errónea
                    continue;
                }

                // Llamada a la opción seleccionada
                switch (opcion) {
                    case 1 -> crearReserva(conn, scanner);
                    case 2 -> listarReservas(conn);
                    case 3 -> salir = true;
                    default -> System.err.println("❌ Opción no válida.");
                }
            }
        }
    }

    /**
     * Permite crear una nueva reserva si no hay conflicto horario.
     * @param conn conexión a la base de datos
     * @param scanner objeto para capturar entradas del usuario
     */
    private static void crearReserva(Connection conn, Scanner scanner) {
        try {
            // Solicitar datos al usuario
            System.out.print("ID de la sala: ");
            int salaId = Integer.parseInt(scanner.nextLine());

            System.out.print("ID del empleado: ");
            int empleadoId = Integer.parseInt(scanner.nextLine());

            System.out.print("Fecha (YYYY-MM-DD): ");
            String fecha = scanner.nextLine();

            System.out.print("Hora de inicio (HH:MM): ");
            String horaInicio = scanner.nextLine() + ":00";

            System.out.print("Hora de fin (HH:MM): ");
            String horaFin = scanner.nextLine() + ":00";

            // Verificación de conflicto de horarios
            String checkSql = "SELECT COUNT(*) FROM reservas WHERE sala_id = ? AND fecha = ? AND (hora_inicio < ? AND hora_fin > ?) ";

            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, salaId);
                checkStmt.setDate(2, Date.valueOf(fecha));
                checkStmt.setTime(3, Time.valueOf(horaFin));
                checkStmt.setTime(4, Time.valueOf(horaInicio));

                ResultSet rs = checkStmt.executeQuery();
                rs.next();
                if (rs.getInt(1) > 0) {
                    System.err.println("❌ Conflicto: Ya existe una reserva para esa sala y horario.");
                    return;
                }
            }

            // Insertar reserva si no hay conflicto
            String insertSql = "INSERT INTO reservas (sala_id, empleado_id, fecha, hora_inicio, hora_fin) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                pstmt.setInt(1, salaId);
                pstmt.setInt(2, empleadoId);
                pstmt.setDate(3, Date.valueOf(fecha));
                pstmt.setTime(4, Time.valueOf(horaInicio));
                pstmt.setTime(5, Time.valueOf(horaFin));
                pstmt.executeUpdate();
                System.out.println("✅ Reserva creada con éxito.");
            }

        } catch (NumberFormatException e) {
            // Captura errores al convertir texto a número
            System.err.println("❌ Error: ID o formato de hora inválido.");
        } catch (IllegalArgumentException e) {
            // Captura errores de formato de fecha u hora
            System.err.println("❌ Formato de fecha u hora incorrecto.");
        } catch (SQLException e) {
            // Captura errores SQL
            System.err.println("❌ Error al insertar reserva:");
            e.printStackTrace();
        }
    }

    /**
     * Lista todas las reservas realizadas, mostrando los nombres de la sala y el empleado.
     * @param conn conexión activa a la base de datos
     */
    private static void listarReservas(Connection conn) {
        String sql = "SELECT r.id, s.nombre AS sala, e.nombre AS empleado, r.fecha, r.hora_inicio, r.hora_fin FROM reservas r JOIN salas s ON r.sala_id = s.id JOIN empleados e ON r.empleado_id = e.id ORDER BY r.fecha, r.hora_inicio";

        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("\n📋 Reservas registradas:");
            while (rs.next()) {
                System.out.printf("ID: %d | Sala: %s | Empleado: %s | Fecha: %s | %s - %s%n",
                        rs.getInt("id"),
                        rs.getString("sala"),
                        rs.getString("empleado"),
                        rs.getDate("fecha"),
                        rs.getTime("hora_inicio"),
                        rs.getTime("hora_fin"));
            }
        } catch (SQLException e) {
            System.err.println("❌ Error al leer las reservas:");
            e.printStackTrace();
        }
    }
}
