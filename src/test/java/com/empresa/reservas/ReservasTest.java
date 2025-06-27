package com.empresa.reservas;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.util.Scanner;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para la clase Reservas.
 * Se simulan Scanner y JDBC para que leerEntero() funcione sin bucles infinitos.
 */
public class ReservasTest {

    private Connection conn;

    @BeforeEach
    void setUp() {
        conn = mock(Connection.class);
    }

    /**
     * Crea una reserva sin conflicto:
     * - nextLine(): "Sala A" (nombreSala)
     * - leerEntero(): hasNextInt()->true, nextInt()->1, nextLine()->"" (consumir)
     * - nextLine(): "2025-07-01", "09:00:00", "10:00:00"
     */
    @Test
    void testCrearReservaSinConflicto() throws Exception {
        Scanner scanner = mock(Scanner.class);

        // 1ª llamada nextLine() → nombreSala
        // 2ª llamada nextLine() → consumo tras nextInt()
        // 3,4,5 → fecha, inicio, fin
        when(scanner.nextLine()).thenReturn(
                "Sala A",
                "",
                "2025-07-01",
                "09:00:00",
                "10:00:00"
        );

        // leerEntero stubs
        when(scanner.hasNextInt()).thenReturn(true);
        when(scanner.nextInt()).thenReturn(1);

        // Mock para SELECT id FROM salas
        PreparedStatement psSala = mock(PreparedStatement.class);
        ResultSet rsSala = mock(ResultSet.class);
        when(conn.prepareStatement(contains("FROM salas WHERE nombre"))).thenReturn(psSala);
        when(psSala.executeQuery()).thenReturn(rsSala);
        when(rsSala.next()).thenReturn(true);
        when(rsSala.getInt("id")).thenReturn(1);
        // setString no hace nada
        doNothing().when(psSala).setString(anyInt(), anyString());

        // Mock para conflicto
        PreparedStatement conflictoStmt = mock(PreparedStatement.class);
        ResultSet rsConflicto = mock(ResultSet.class);
        when(conn.prepareStatement(contains("SELECT COUNT(*) FROM reservas"))).thenReturn(conflictoStmt);
        when(conflictoStmt.executeQuery()).thenReturn(rsConflicto);
        when(rsConflicto.next()).thenReturn(true);
        when(rsConflicto.getInt(1)).thenReturn(0);

        // Mock para insertar
        PreparedStatement insertStmt = mock(PreparedStatement.class);
        when(conn.prepareStatement(startsWith("INSERT INTO reservas"))).thenReturn(insertStmt);
        when(insertStmt.executeUpdate()).thenReturn(1);

        // Ejecuta y verifica
        Reservas.crearReserva(conn, scanner);
        verify(insertStmt).executeUpdate();
    }

    /**
     * Crea una reserva con conflicto:
     * - mismo flujo de lectura, pero conflictoStmt devuelve 1
     * - verifica que no llegue a INSERT
     */
    @Test
    void testCrearReservaConConflicto() throws Exception {
        Scanner scanner = mock(Scanner.class);

        when(scanner.nextLine()).thenReturn(
                "Sala A",
                "",
                "2025-07-01",
                "09:00:00",
                "10:00:00"
        );
        when(scanner.hasNextInt()).thenReturn(true);
        when(scanner.nextInt()).thenReturn(1);

        // Mock sala
        PreparedStatement psSala = mock(PreparedStatement.class);
        ResultSet rsSala = mock(ResultSet.class);
        when(conn.prepareStatement(contains("FROM salas WHERE nombre"))).thenReturn(psSala);
        when(psSala.executeQuery()).thenReturn(rsSala);
        when(rsSala.next()).thenReturn(true);
        when(rsSala.getInt("id")).thenReturn(1);
        doNothing().when(psSala).setString(anyInt(), anyString());

        // Mock conflicto = 1
        PreparedStatement conflictoStmt = mock(PreparedStatement.class);
        ResultSet rsConflicto = mock(ResultSet.class);
        when(conn.prepareStatement(contains("SELECT COUNT(*) FROM reservas"))).thenReturn(conflictoStmt);
        when(conflictoStmt.executeQuery()).thenReturn(rsConflicto);
        when(rsConflicto.next()).thenReturn(true);
        when(rsConflicto.getInt(1)).thenReturn(1);

        Reservas.crearReserva(conn, scanner);

        verify(conflictoStmt).executeQuery();
        // No debió llamar al INSERT
        verify(conn, never()).prepareStatement(startsWith("INSERT INTO reservas"));
    }

    /**
     * Lista reservas: simula un Statement y un ResultSet con dos filas.
     * Verifica createStatement() y executeQuery().
     */
    @Test
    void testListarReservas() throws Exception {
        Statement stmt = mock(Statement.class);
        ResultSet rs = mock(ResultSet.class);

        when(conn.createStatement()).thenReturn(stmt);
        when(stmt.executeQuery(anyString())).thenReturn(rs);

        // Dos filas
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
