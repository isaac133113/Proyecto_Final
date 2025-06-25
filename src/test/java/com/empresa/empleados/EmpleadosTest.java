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
 * Simula conexiones JDBC y entradas de usuario para verificar la correcta
 * inserción y listado de empleados en la base de datos.
 */
class EmpleadosTest {

    private Connection conn;
    private Scanner scanner;
    private PreparedStatement pstmt;

    /**
     * Configura los mocks necesarios antes de cada test.
     *
     * @throws SQLException si ocurre un error en la configuración
     */
    @BeforeEach
    void setUp() throws SQLException {
        conn = mock(Connection.class);
        scanner = mock(Scanner.class);
        pstmt = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(pstmt);
    }

    /**
     * Test para verificar la correcta inserción de un empleado.
     *
     * Simula la entrada del usuario con nombre, cargo y salario,
     * y verifica que los datos se pasan correctamente al PreparedStatement.
     *
     * @throws SQLException si ocurre un error en la simulación
     */
    @Test
    void testCrearEmpleado() throws SQLException {
        when(scanner.nextLine())
                .thenReturn("Juan Pérez")
                .thenReturn("30")
                .thenReturn("Administración");

        Empleados.crearEmpleado(conn, scanner);

        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);

        verify(pstmt).setString(eq(1), stringCaptor.capture());
        verify(pstmt).setString(eq(2), stringCaptor.capture());
        verify(pstmt).setString(eq(3), stringCaptor.capture());
        verify(pstmt).executeUpdate();

        assertThat(stringCaptor.getAllValues().get(0), is("Juan Pérez"));
        assertThat(stringCaptor.getAllValues().get(1), is("30"));
        assertThat(stringCaptor.getAllValues().get(2), is("Administración"));
    }
    @Test
    void testCrearEmpleadoFallo() throws SQLException {
        when(scanner.nextLine())
                .thenReturn("Ana García")
                .thenReturn("25")
                .thenReturn("Finanzas");
        when(pstmt.executeUpdate()).thenThrow(new SQLException("Error en la base de datos"));

        try {
            Empleados.crearEmpleado(conn, scanner);
        } catch (Exception e) {
            assertThat(e.getMessage(), is("Error en la base de datos"));
        }
        verify(pstmt).setString(1, "Ana García");
        verify(pstmt).setString(2, "25");
        verify(pstmt).setString(3, "Finanzas");
        verify(pstmt).executeUpdate();
    }

    /**
     * Test para verificar el listado de empleados.
     *
     * Simula la consulta y verifica que se leen correctamente los campos del ResultSet.
     *
     * @throws SQLException si ocurre un error en la simulación
     */
    @Test
    void testMostrarEmpleados() throws SQLException {
        Statement stmt = mock(Statement.class);
        ResultSet rs = mock(ResultSet.class);

        when(conn.createStatement()).thenReturn(stmt);
        when(stmt.executeQuery(anyString())).thenReturn(rs);
        when(rs.next()).thenReturn(true).thenReturn(false);
        when(rs.getInt("id")).thenReturn(1);
        when(rs.getString("nombre")).thenReturn("Sala Test");
        when(rs.getString("email")).thenReturn("juanpistacho@gmail.com");
        when(rs.getString("departamento")).thenReturn("Administración");

        Empleados.listarEmpleados(conn);

        verify(rs, atLeastOnce()).next();
        verify(rs).getString("nombre");
    }
}
