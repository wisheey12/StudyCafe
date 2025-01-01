import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// DB 연결 클래스

public class DBConnector {
    private static final String URL = "jdbc:mysql://localhost:3306/StudyCafeDB";
    private static final String USER = "root";
    private static final String PASSWORD = "1234";

    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}