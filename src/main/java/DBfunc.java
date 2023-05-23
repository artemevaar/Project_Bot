
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBfunc {
    private Connection conn;

    public Connection connect(String url, String user, String password) throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Не удалось найти драйвер");
            e.printStackTrace();
            return null;
        }
        conn = null;
        try {
            conn = DriverManager.getConnection(url, user, password);
            System.out.println("Успешное подключение к базе данных");
        } catch (SQLException e) {
            System.out.println("Ошибка подключения к базе данных");
            e.printStackTrace();
            return null;
        }
        return conn;
    }
}