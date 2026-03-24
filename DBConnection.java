import java.sql.*;

public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/railwaydb";
    private static final String USER = "root";  // change if needed
    private static final String PASS = "SH19@shrau"; // change to your MySQL password

    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASS);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Test connection
    public static void main(String[] args) {
        try (Connection con = getConnection()) {
            if (con != null) {
                System.out.println("✅ Connected to MySQL Database!");
            } else {
                System.out.println("❌ Connection Failed!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
