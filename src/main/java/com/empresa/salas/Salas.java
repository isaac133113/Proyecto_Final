package com.empresa.salas;

import java.sql.*;
import java.util.Scanner;

/**
 * Clase que gestiona el CRUD de salas de reuniones.
 */
public class Salas {

    /**
     * Muestra el menú principal para gestionar las salas.
     * @param conn conexión a la base de datos
     */
    public static void mostrarMenu(Connection conn) {
        try (Scanner scanner = new Scanner(System.in)) {
            boolean salir = false;

            while (!salir) {
                System.out.println("\n--- Menú de gestión de salas ---");
                System.out.println("1. Crear sala");
                System.out.println("2. Listar salas");
                System.out.println("3. Actualizar sala");
                System.out.println("4. Eliminar sala");
                System.out.println("5. Salir");
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
                    case 1 -> crearSala(conn, scanner);
                    case 2 -> listarSalas(conn);
                    case 3 -> actualizarSala(conn, scanner);
                    case 4 -> eliminarSala(conn, scanner);
                    case 5 -> {
                        salir = true;
                        System.out.println("👋 Saliendo del gestor de salas...");
                    }
                    default -> System.err.println("❌ Opción no válida.");
                }
            }
        }
    }

    /**
     * Crea una nueva sala en la base de datos.
     * @param conn conexión a la base de datos
     * @param scanner para leer la entrada del usuario
     */
    static void crearSala(Connection conn, Scanner scanner) {
        try {
            System.out.print("Nombre de la sala: ");
            String nombre = scanner.nextLine();

            System.out.print("Capacidad: ");
            int capacidad = Integer.parseInt(scanner.nextLine());

            System.out.print("Recursos: ");
            String recursos = scanner.nextLine();

            String sql = "INSERT INTO salas (nombre, capacidad, recursos) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, nombre);
                pstmt.setInt(2, capacidad);
                pstmt.setString(3, recursos);
                pstmt.executeUpdate();
                System.out.println("✅ Sala creada con éxito.");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error al crear la sala:");
            e.printStackTrace();
        } catch (NumberFormatException e) {
            System.err.println("❌ Capacidad inválida. Debe ser un número entero.");
        }
    }

    /**
     * Lista todas las salas de la base de datos.
     * @param conn conexión a la base de datos
     */
    static void listarSalas(Connection conn) {
        String sql = "SELECT * FROM salas";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("\n📋 Listado de salas:");
            while (rs.next()) {
                System.out.printf("ID: %d | Nombre: %s | Capacidad: %d | Recursos: %s%n",
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getInt("capacidad"),
                        rs.getString("recursos"));
            }
        } catch (SQLException e) {
            System.err.println("❌ Error al leer las salas:");
            e.printStackTrace();
        }
    }

    /**
     * Actualiza los datos de una sala existente.
     * @param conn conexión a la base de datos
     * @param scanner para leer la entrada del usuario
     */
    private static void actualizarSala(Connection conn, Scanner scanner) {
        try {
            System.out.print("ID de la sala a actualizar: ");
            int id = Integer.parseInt(scanner.nextLine());

            System.out.print("Nuevo nombre: ");
            String nombre = scanner.nextLine();

            System.out.print("Nueva capacidad: ");
            int capacidad = Integer.parseInt(scanner.nextLine());

            System.out.print("Nuevos recursos: ");
            String recursos = scanner.nextLine();

            String sql = "UPDATE salas SET nombre = ?, capacidad = ?, recursos = ? WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, nombre);
                pstmt.setInt(2, capacidad);
                pstmt.setString(3, recursos);
                pstmt.setInt(4, id);

                int filas = pstmt.executeUpdate();
                if (filas > 0) {
                    System.out.println("✅ Sala actualizada con éxito.");
                } else {
                    System.err.println("❌ No se encontró una sala con ese ID.");
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error al actualizar la sala:");
            e.printStackTrace();
        } catch (NumberFormatException e) {
            System.err.println("❌ Entrada inválida. Debes ingresar números para ID y capacidad.");
        }
    }

    /**
     * Elimina una sala de la base de datos.
     * @param conn conexión a la base de datos
     * @param scanner para leer la entrada del usuario
     */
    private static void eliminarSala(Connection conn, Scanner scanner) {
        try {
            System.out.print("ID de la sala a eliminar: ");
            int id = Integer.parseInt(scanner.nextLine());

            String sql = "DELETE FROM salas WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                int filas = pstmt.executeUpdate();
                if (filas > 0) {
                    System.out.println("✅ Sala eliminada con éxito.");
                } else {
                    System.err.println("❌ No se encontró una sala con ese ID.");
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error al eliminar la sala:");
            e.printStackTrace();
        } catch (NumberFormatException e) {
            System.err.println("❌ ID inválido. Debe ser un número entero.");
        }
    }
}
