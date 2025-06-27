package com.empresa.empleados;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.sql.*;
import java.util.Scanner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Clase de pruebas unitarias para métodos de la clase {@link Empleados}.
 * Utiliza Mockito para simular objetos de base de datos y entrada del usuario.
 */
class EmpleadosTest {

    private Connection conn;
    private Scanner scanner;
    private PreparedStatement pstmt;

    /**
     * Configura los mocks antes de cada prueba.
     * Simula una conexión de base de datos, un scanner y una sentencia preparada.
     */
    @BeforeEach
    void setUp() throws SQLException {
        conn = mock(Connection.class);
        scanner = mock(Scanner.class);
        pstmt = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(pstmt);
    }

    /**
     * Prueba el método {@code Empleados.crearEmpleado} con datos válidos.
     * Verifica que los valores ingresados por el usuario se pasen correctamente al {@code PreparedStatement}.
     */
    @Test
    void testCrearEmpleado() throws SQLException {
        // Simula la entrada del usuario
        when(scanner.nextLine())
                .thenReturn("Juan Pérez")
                .thenReturn("30")
                .thenReturn("Administración");

        // Simula una ejecución exitosa
        when(pstmt.executeUpdate()).thenReturn(1);

        // Ejecuta el método bajo prueba
        Empleados.crearEmpleado(conn, scanner);

        // Verifica que se llamaron los métodos esperados con los parámetros correctos
        verify(pstmt).setString(1, "Juan Pérez");
        verify(pstmt).setString(2, "30");
        verify(pstmt).setString(3, "Administración");
        verify(pstmt).executeUpdate();
    }

    /**
     * Prueba el método {@code Empleados.crearEmpleado} cuando ocurre una excepción en la base de datos.
     * Asegura que se maneja la excepción sin romper el flujo.
     */
    @Test
    void testCrearEmpleadoFallo() throws SQLException {
        // Simula la entrada del usuario
        when(scanner.nextLine())
                .thenReturn("Ana García")
                .thenReturn("25")
                .thenReturn("Finanzas");

        // Simula una excepción en la base de datos
        when(pstmt.executeUpdate()).thenThrow(new SQLException("Error en la base de datos"));

        // Ejecuta el método bajo prueba (debería manejar la excepción internamente)
        Empleados.crearEmpleado(conn, scanner);

        // Verifica que aún se intentó ejecutar la operación
        verify(pstmt).setString(1, "Ana García");
        verify(pstmt).setString(2, "25");
        verify(pstmt).setString(3, "Finanzas");
        verify(pstmt).executeUpdate();
    }

    /**
     * Prueba el método {@code Empleados.listarEmpleados}, simulando una consulta exitosa.
     * Verifica que se recorren correctamente los resultados del {@code ResultSet}.
     */
    @Test
    void testMostrarEmpleados() throws SQLException {
        Statement stmt = mock(Statement.class);
        ResultSet rs = mock(ResultSet.class);

        // Simula una consulta que retorna un solo resultado
        when(conn.createStatement()).thenReturn(stmt);
        when(stmt.executeQuery(anyString())).thenReturn(rs);
        when(rs.next()).thenReturn(true).thenReturn(false); // Solo un resultado
        when(rs.getInt("id")).thenReturn(1);
        when(rs.getString("nombre")).thenReturn("Sala Test");
        when(rs.getString("email")).thenReturn("juanpistacho@gmail.com");
        when(rs.getString("departamento")).thenReturn("Administración");

        // Ejecuta el método bajo prueba
        Empleados.listarEmpleados(conn);

        // Verifica que se accedió a los datos del resultado
        verify(rs, atLeastOnce()).next();
        verify(rs).getString("nombre");
    }
}
