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

class EmpleadosTest {

    private Connection conn;
    private Scanner scanner;
    private PreparedStatement pstmt;

    @BeforeEach
    void setUp() throws SQLException {
        conn = mock(Connection.class);
        scanner = mock(Scanner.class);
        pstmt = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(pstmt);
    }

    @Test
    void testCrearEmpleado() throws SQLException {
        when(scanner.nextLine())
                .thenReturn("Juan Pérez")
                .thenReturn("30")
                .thenReturn("Administración");
        when(pstmt.executeUpdate()).thenReturn(1);

        Empleados.crearEmpleado(conn, scanner);

        verify(pstmt).setString(1, "Juan Pérez");
        verify(pstmt).setString(2, "30");
        verify(pstmt).setString(3, "Administración");
        verify(pstmt).executeUpdate();
    }

    @Test
    void testCrearEmpleadoFallo() throws SQLException {
        when(scanner.nextLine())
                .thenReturn("Ana García")
                .thenReturn("25")
                .thenReturn("Finanzas");

        when(pstmt.executeUpdate()).thenThrow(new SQLException("Error en la base de datos"));

        Empleados.crearEmpleado(conn, scanner);

        verify(pstmt).setString(1, "Ana García");
        verify(pstmt).setString(2, "25");
        verify(pstmt).setString(3, "Finanzas");
        verify(pstmt).executeUpdate();
    }

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
