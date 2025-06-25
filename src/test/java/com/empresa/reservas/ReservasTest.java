package com.empresa.reservas;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.sql.*;
import java.util.Scanner;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.*;

public class ReservasTest {

    private Connection conn;
    private PreparedStatement checkStmt;
    private PreparedStatement insertStmt;
    private ResultSet resultSet;

    @BeforeEach
    void setUp() throws Exception {
        conn = mock(Connection.class);
        checkStmt = mock(PreparedStatement.class);
        insertStmt = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        when(conn.prepareStatement(startsWith("SELECT"))).thenReturn(checkStmt);
        when(conn.prepareStatement(startsWith("INSERT"))).thenReturn(insertStmt);
        when(checkStmt.executeQuery()).thenReturn(resultSet);
    }

    /**
     * Prueba la creación exitosa de una reserva cuando no hay conflictos.
     *
     * Este test verifica que:
     * 1. Se realiza la consulta SELECT para verificar conflictos
     * 2. Se ejecuta el INSERT para crear la nueva reserva
     * 3. Los datos de entrada son procesados correctamente
     *
     * @throws Exception Si ocurre un error durante la ejecución de la prueba
     */
    @Test
    void testCrearReservaSinConflicto() throws Exception {
        Scanner scanner = mock(Scanner.class);
        when(scanner.nextLine()).thenReturn("1", "1", "2025-07-01", "09:00", "10:00");
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(0);

        Reservas.crearReserva(conn, scanner);

        verify(conn).prepareStatement(startsWith("SELECT"));
        verify(checkStmt).executeQuery();
        verify(conn).prepareStatement(startsWith("INSERT"));
        verify(insertStmt).executeUpdate();
    }

    /**
     * Prueba el manejo de conflictos al intentar crear una reserva.
     *
     * Este test verifica que:
     * 1. Se realiza la consulta SELECT para verificar conflictos
     * 2. Se detecta el conflicto correctamente
     * 3. NO se ejecuta el INSERT cuando hay conflicto
     *
     * El sistema debe detectar que ya existe una reserva que se solapa
     * con el horario solicitado.
     *
     * @throws Exception Si ocurre un error durante la ejecución de la prueba
     */
    @Test
    void testCrearReservaConConflicto() throws Exception {
        Scanner scanner = mock(Scanner.class);
        when(scanner.nextLine()).thenReturn("1", "1", "2025-07-01", "09:30", "10:30");
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(1);

        Reservas.crearReserva(conn, scanner);

        verify(checkStmt).executeQuery();
        verify(insertStmt, never()).executeUpdate();
    }

    /**
     * Prueba el listado de reservas existentes.
     *
     * Este test verifica que:
     * 1. Se crea la consulta SQL correctamente
     * 2. Se recuperan todos los campos necesarios de cada reserva
     * 3. Se procesan múltiples registros (2 reservas en este caso)
     *
     * @throws Exception Si ocurre un error durante la ejecución de la prueba
     */
    @Test
    void testListarReservas() throws Exception {
        Statement stmt = mock(Statement.class);
        when(conn.createStatement()).thenReturn(stmt);
        when(stmt.executeQuery(anyString())).thenReturn(resultSet);

        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getInt("id")).thenReturn(101, 102);
        when(resultSet.getString("sala")).thenReturn("Sala A", "Sala B");
        when(resultSet.getString("empleado")).thenReturn("Juan Pérez", "María Gómez");
        when(resultSet.getDate("fecha")).thenReturn(Date.valueOf("2025-07-01"), Date.valueOf("2025-07-02"));
        when(resultSet.getTime("hora_inicio")).thenReturn(Time.valueOf("09:00:00"), Time.valueOf("10:00:00"));
        when(resultSet.getTime("hora_fin")).thenReturn(Time.valueOf("10:00:00"), Time.valueOf("11:00:00"));

        Reservas.listarReservas(conn);

        verify(conn).createStatement();
        verify(stmt).executeQuery(anyString());
        verify(resultSet, times(3)).next();
        verify(resultSet, times(2)).getInt("id");
        verify(resultSet, times(2)).getString("sala");
        verify(resultSet, times(2)).getString("empleado");
        verify(resultSet, times(2)).getDate("fecha");
        verify(resultSet, times(2)).getTime("hora_inicio");
        verify(resultSet, times(2)).getTime("hora_fin");
    }

}
