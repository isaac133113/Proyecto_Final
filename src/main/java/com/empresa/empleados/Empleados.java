package com.empresa.empleados;

import java.sql.*;
import java.util.Scanner;

/**
 * CRUD completo para gestionar empleados (Create, Read, Update, Delete).
 */
public class Empleados {
    public static void mostrarMenu(Connection conn) {
        try (Scanner scanner = new Scanner(System.in)) {
            boolean salir = false;

            while (!salir) {
                System.out.println("\n--- Menú de gestión de empleados ---");
                System.out.println("1. Crear empleado");
                System.out.println("2. Listar empleados");
                System.out.println("3. Actualizar empleado");
                System.out.println("4. Eliminar empleado");
                System.out.println("5. Salir");
                System.out.print("Selecciona una opción: ");

                int opcion = -1;
                if (scanner.hasNextInt()) {
                    opcion = scanner.nextInt();
                    scanner.nextLine(); // limpiar buffer
                } else {
                    System.err.println("❌ Entrada inválida. Por favor, introduce un número.");
                    scanner.nextLine();
                    continue;
                }

                switch (opcion) {
                    case 1 -> crearEmpleado(conn, scanner);
                    case 2 -> listarEmpleados(conn);
                    case 3 -> actualizarEmpleado(conn, scanner);
                    case 4 -> eliminarEmpleado(conn, scanner);
                    case 5 -> {
                        salir = true;
                        System.out.println("Saliendo...");
                    }
                    default -> System.err.println("❌Opción no válida.");
                }
            }
        }
    }
    /**
     * Método para crear un empleado en la base de datos.
     *
     * @param conn    Conexión abierta a la base de datos.
     * @param scanner Scanner para leer datos de entrada.
     */
    public static void crearEmpleado(Connection conn, Scanner scanner) {
        try {
            System.out.print("Nombre del empleado: ");
            String nombre = scanner.nextLine();
            System.out.print("Email: ");
            String email = scanner.nextLine();
            System.out.print("Departamento: ");
            String departamento = scanner.nextLine();

            String sql = "INSERT INTO empleados (nombre, email, departamento) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, nombre);
                ps.setString(2, email);
                ps.setString(3, departamento);
                ps.executeUpdate();
                System.out.println("✅Empleado creado correctamente.");
            }
        } catch (SQLException e) {
            System.err.println("❌Error al crear empleado.");
            e.printStackTrace();
        }
    }

    /**
     * Método para listar todos los empleados de la base de datos.
     *
     * @param conn Conexión abierta a la base de datos.
     */
    public static void listarEmpleados(Connection conn) {
        String sql = "SELECT * FROM empleados";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("\nLista de empleados:");
            while (rs.next()) {
                System.out.printf("ID: %d, Nombre: %s, Email: %s, Departamento: %s%n",
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("email"),
                        rs.getString("departamento"));
            }
        } catch (SQLException e) {
            System.err.println("❌Error al listar empleados.");
            e.printStackTrace();
        }
    }

    /**
     * Método para actualizar los datos de un empleado.
     *
     * @param conn    Conexión abierta a la base de datos.
     * @param scanner Scanner para leer datos de entrada.
     */
    public static void actualizarEmpleado(Connection conn, Scanner scanner) {
        try {
            System.out.print("Introduce el ID del empleado a actualizar: ");
            int id = Integer.parseInt(scanner.nextLine());

            System.out.print("Nuevo nombre: ");
            String nombre = scanner.nextLine();
            System.out.print("Nuevo email: ");
            String email = scanner.nextLine();
            System.out.print("Nuevo departamento: ");
            String departamento = scanner.nextLine();

            String sql = "UPDATE empleados SET nombre = ?, email = ?, departamento = ? WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, nombre);
                ps.setString(2, email);
                ps.setString(3, departamento);
                ps.setInt(4, id);
                int filas = ps.executeUpdate();
                if (filas > 0) {
                    System.out.println("✅Empleado actualizado correctamente.");
                } else {
                    System.out.println("❌No se encontró empleado con ese ID.");
                }
            }
        } catch (SQLException e) {
            System.err.println("❌Error al actualizar empleado.");
            e.printStackTrace();
        } catch (NumberFormatException e) {
            System.err.println("ID inválido.");
        }
    }

    /**
     * Método para eliminar un empleado por ID.
     *
     * @param conn    Conexión abierta a la base de datos.
     * @param scanner Scanner para leer datos de entrada.
     */
    public static void eliminarEmpleado(Connection conn, Scanner scanner) {
        try {
            System.out.print("Introduce el ID del empleado a eliminar: ");
            int id = Integer.parseInt(scanner.nextLine());

            String sql = "DELETE FROM empleados WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                int filas = ps.executeUpdate();
                if (filas > 0) {
                    System.out.println("✅Empleado eliminado correctamente.");
                } else {
                    System.out.println("❌No se encontró empleado con ese ID.");
                }
            }
        } catch (SQLException e) {
            System.err.println("❌Error al eliminar empleado.");
            e.printStackTrace();
        } catch (NumberFormatException e) {
            System.err.println("❌ID inválido.");
        }
    }


}
