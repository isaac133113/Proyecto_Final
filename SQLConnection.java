package SQLConnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
public class SQLConnection {
    public static void main(String[] args) {
        // Parámetros de conexión
        String url = "jdbc:mysql://localhost:3306/reservas_salas?useSSL=false&serverTimezone=UTC";
        String user = "root";
        String password = "root";
       
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            System.out.println("Conexión establecida correctamente a la base de datos reservas_salas");
        } catch (SQLException e) {
            System.out.println("Error al conectar con la base de datos:");
            e.printStackTrace();
        }
    }
}