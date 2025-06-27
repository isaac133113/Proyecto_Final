package com.empresa.reservas;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.sql.*;
import java.util.Scanner;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class ReservasTest {

    private Connection conn;
    private PreparedStatement pstmt;
    private ResultSet resultSet;

    @BeforeEach
    void setUp() throws Exception {
        conn = mock(Connection.class);
        pstmt = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);
    }

    @Test
    void testCrearReservaSinConflicto() throws Exception {
        Scanner scanner = mock(Scanner.class);
        when(scanner.nextLine()).thenReturn("1", "1", "2025-07-01", "09:00:00", "10:00:00");

        PreparedStatement conflictoStmt = mock(PreparedStatement.class);
        ResultSet rsConflicto = mock(ResultSet.class);

        when(conn.prepareStatement(contains("SELECT COUNT(*) FROM reservas"))).thenReturn(conflictoStmt);
        when(conflictoStmt.executeQuery()).thenReturn(rsConflicto);
        when(rsConflicto.next()).thenReturn(true);
        when(rsConflicto.getInt(1)).thenReturn(0);

        PreparedStatement insertStmt = mock(PreparedStatement.class);
        when(conn.prepareStatement(startsWith("INSERT INTO reservas"))).thenReturn(insertStmt);
        when(insertStmt.executeUpdate()).thenReturn(1);

        Reservas.crearReserva(conn, scanner);

        verify(insertStmt).executeUpdate();
    }

    @Test
    void testCrearReservaConConflicto() throws Exception {
        Scanner scanner = mock(Scanner.class);
        when(scanner.nextLine()).thenReturn("1", "1", "2025-07-01", "09:00:00", "10:00:00");

        PreparedStatement conflictoStmt = mock(PreparedStatement.class);
        ResultSet rsConflicto = mock(ResultSet.class);

        when(conn.prepareStatement(contains("SELECT COUNT(*) FROM reservas"))).thenReturn(conflictoStmt);
        when(conflictoStmt.executeQuery()).thenReturn(rsConflicto);
        when(rsConflicto.next()).thenReturn(true);
        when(rsConflicto.getInt(1)).thenReturn(1);

        Reservas.crearReserva(conn, scanner);

        verify(conflictoStmt).executeQuery();
        verify(conn, never()).prepareStatement(startsWith("INSERT INTO reservas"));
    }

    @Test
    void testListarReservas() throws Exception {
        Statement stmt = mock(Statement.class);
        ResultSet rs = mock(ResultSet.class);

        when(conn.createStatement()).thenReturn(stmt);
        when(stmt.executeQuery(anyString())).thenReturn(rs);

        when(rs.next()).thenReturn(true, true, false);
        when(rs.getInt("id")).thenReturn(101, 102);
        when(rs.getString("sala_nombre")).thenReturn("Sala A", "Sala B");
        when(rs.getString("empleado_nombre")).thenReturn("Ana", "Luis");
        when(rs.getDate("fecha")).thenReturn(Date.valueOf("2025-07-01"), Date.valueOf("2025-07-02"));
        when(rs.getTime("hora_inicio")).thenReturn(Time.valueOf("09:00:00"), Time.valueOf("10:00:00"));
        when(rs.getTime("hora_fin")).thenReturn(Time.valueOf("10:00:00"), Time.valueOf("11:00:00"));

        Reservas.listarReservas(conn);

        verify(conn).createStatement();
        verify(stmt).executeQuery(anyString());
    }
}
