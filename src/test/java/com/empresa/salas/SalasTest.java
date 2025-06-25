package com.empresa.salas;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import java.sql.*;
import java.util.Scanner;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Clase de pruebas unitarias para la clase {@code Salas}.
 *
 * Utiliza Mockito para simular conexiones JDBC y entradas del usuario,
 * y Hamcrest para validar los argumentos usados en las consultas SQL.
 *
 * Se prueba la creación y listado de salas, validando el correcto
 * funcionamiento de los métodos {@code crearSala} y {@code listarSalas}.
 *
 * Requiere JUnit 5, Mockito y Hamcrest.
 */
class SalasTest {

    private Connection conn;
    private Scanner scanner;
    private PreparedStatement pstmt;

    /**
     * Configura los mocks de conexión a base de datos, scanner y prepared statement
     * antes de cada test.
     *
     * @throws SQLException si ocurre un error en la configuración del mock
     */
    @BeforeEach
    void setUp() throws SQLException {
        conn = mock(Connection.class);
        scanner = mock(Scanner.class);
        pstmt = mock(PreparedStatement.class);

        when(conn.prepareStatement(anyString())).thenReturn(pstmt);
    }

    /**
     * Test para verificar la correcta inserción de una sala usando crearSalas
     *
     * Simula la entrada de datos de usuario y captura los argumentos enviados
     * al PreparedStatement para asegurar que coinciden con los esperados.
     *
     * @throws SQLException si ocurre un error al simular la conexión o ejecución
     */
    @Test
    void testCrearSalas() throws SQLException {
        when(scanner.nextLine())
                .thenReturn("Sala A")
                .thenReturn("20")
                .thenReturn("Proyector, TV");

        Salas.crearSala(conn, scanner);

        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer> intCaptor = ArgumentCaptor.forClass(Integer.class);

        verify(pstmt, times(2)).setString(anyInt(), stringCaptor.capture());
        verify(pstmt).setInt(eq(2), intCaptor.capture());

        assertThat(stringCaptor.getAllValues().get(0), is("Sala A"));
        assertThat(stringCaptor.getAllValues().get(1), is("Proyector, TV"));
        assertThat(intCaptor.getValue(), is(20));

        verify(pstmt).executeUpdate();
    }

    /**
     * Test para verificar el listado de salas usando listarSalas}.
     *
     * Simula la ejecución de una consulta que devuelve un resultado
     * y verifica que se acceden a los campos esperados en el Resultset.
     *
     * @throws SQLException si ocurre un error al simular la consulta
     */
    @Test
    void testListarSalas() throws SQLException {
        Statement stmt = mock(Statement.class);
        ResultSet rs = mock(ResultSet.class);

        when(conn.createStatement()).thenReturn(stmt);
        when(stmt.executeQuery(anyString())).thenReturn(rs);
        when(rs.next()).thenReturn(true).thenReturn(false);
        when(rs.getInt("id")).thenReturn(1);
        when(rs.getString("nombre")).thenReturn("Sala Test");
        when(rs.getInt("capacidad")).thenReturn(10);
        when(rs.getString("recursos")).thenReturn("Proyector");

        Salas.listarSalas(conn);

        verify(rs, atLeastOnce()).next();
        verify(rs).getString("nombre");
    }
}
