package com.empresa.reservas;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.sql.*;
import java.util.Scanner;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.*;

public class ReservasTest {

    private Connection conn;
    private PreparedStatement checkEmpleadoStmt;
    private PreparedStatement checkSalaStmt;
    private PreparedStatement checkConflictoStmt;
    private PreparedStatement insertStmt;
    private ResultSet resultSetEmpleado;
    private ResultSet resultSetSala;
    private ResultSet resultSetConflicto;

    @BeforeEach
    void setUp() throws Exception {
        conn = mock(Connection.class);

        // Preparar los mocks para cada PreparedStatement y ResultSet
        checkEmpleadoStmt = mock(PreparedStatement.class);
        checkSalaStmt = mock(PreparedStatement.class);
        checkConflictoStmt = mock(PreparedStatement.class);
        insertStmt = mock(PreparedStatement.class);

        resultSetEmpleado = mock(ResultSet.class);
        resultSetSala = mock(ResultSet.class);
        resultSetConflicto = mock(ResultSet.class);

        // Cuando prepareStatement con sql para empleado devuelve checkEmpleadoStmt, similar para sala y conflicto
        when(conn.prepareStatement(startsWith("SELECT COUNT(*) FROM empleados"))).thenReturn(checkEmpleadoStmt);
        when(conn.prepareStatement(startsWith("SELECT COUNT(*) FROM salas"))).thenReturn(checkSalaStmt);
        when(conn.prepareStatement(startsWith("SELECT COUNT(*) FROM reservas WHERE sala_id ="))).thenReturn(checkConflictoStmt);
        when(conn.prepareStatement(startsWith("INSERT INTO reservas"))).thenReturn(insertStmt);

        // Vincular executeQuery con ResultSet correcto para cada consulta
        when(checkEmpleadoStmt.executeQuery()).thenReturn(resultSetEmpleado);
        when(checkSalaStmt.executeQuery()).thenReturn(resultSetSala);
        when(checkConflictoStmt.executeQuery()).thenReturn(resultSetConflicto);
    }

    @Test
    void testCrearReservaSinConflicto() throws Exception {
        Scanner scanner = mock(Scanner.class);
        when(scanner.nextLine()).thenReturn("1", "1", "2025-07-01", "09:00", "10:00");

        // Simular que empleado existe
        when(resultSetEmpleado.next()).thenReturn(true);
        when(resultSetEmpleado.getInt(1)).thenReturn(1);

        // Simular que sala existe
        when(resultSetSala.next()).thenReturn(true);
        when(resultSetSala.getInt(1)).thenReturn(1);

        // Simular que NO hay conflicto (0 reservas en ese rango)
        when(resultSetConflicto.next()).thenReturn(true);
        when(resultSetConflicto.getInt(1)).thenReturn(0);

        when(insertStmt.executeUpdate()).thenReturn(1);

        Reservas.crearReserva(conn, scanner);

        verify(checkEmpleadoStmt).executeQuery();
        verify(checkSalaStmt).executeQuery();
        verify(checkConflictoStmt).executeQuery();
        verify(insertStmt).executeUpdate();
    }

    @Test
    void testCrearReservaConConflicto() throws Exception {
        Scanner scanner = mock(Scanner.class);
        when(scanner.nextLine()).thenReturn("1", "1", "2025-07-01", "09:30", "10:30");

        // Empleado existe
        when(resultSetEmpleado.next()).thenReturn(true);
        when(resultSetEmpleado.getInt(1)).thenReturn(1);

        // Sala existe
        when(resultSetSala.next()).thenReturn(true);
        when(resultSetSala.getInt(1)).thenReturn(1);

        // Hay conflicto (1 reserva solapada)
        when(resultSetConflicto.next()).thenReturn(true);
        when(resultSetConflicto.getInt(1)).thenReturn(1);

        Reservas.crearReserva(conn, scanner);

        verify(checkConflictoStmt).executeQuery();
        verify(insertStmt, never()).executeUpdate();
    }
    @Test
    void testListarReservas() throws Exception {
        Statement stmt = mock(Statement.class);
        ResultSet rs = mock(ResultSet.class);

        when(conn.createStatement()).thenReturn(stmt);
        when(stmt.executeQuery(anyString())).thenReturn(rs);

        // Simulamos 2 filas y luego false
        when(rs.next()).thenReturn(true, true, false);

        when(rs.getInt("id")).thenReturn(101, 102);
        when(rs.getString("empleado_id")).thenReturn("1", "2");
        when(rs.getString("sala_nombre")).thenReturn("Sala A", "Sala B");
        when(rs.getDate("fecha")).thenReturn(Date.valueOf("2025-07-01"), Date.valueOf("2025-07-02"));
        when(rs.getTime("hora_inicio")).thenReturn(Time.valueOf("09:00:00"), Time.valueOf("10:00:00"));
        when(rs.getTime("hora_fin")).thenReturn(Time.valueOf("10:00:00"), Time.valueOf("11:00:00"));

        Reservas.listarReservas(conn);

        verify(conn).createStatement();
        verify(stmt).executeQuery(anyString());
        verify(rs, times(3)).next();
        verify(rs, times(2)).getInt("id");
        verify(rs, times(2)).getString("empleado_id");
        verify(rs, times(2)).getString("sala_nombre");
        verify(rs, times(2)).getDate("fecha");
        verify(rs, times(2)).getTime("hora_inicio");
        verify(rs, times(2)).getTime("hora_fin");
    }

}
