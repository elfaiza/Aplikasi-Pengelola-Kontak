import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Setupdatabase {
    public static void main(String[] args) {
        // Lokasi file database
        String dbPath = "contacts.db";
        
        // URL koneksi SQLite
        String url = "jdbc:sqlite:" + dbPath;
        
        // SQL untuk membuat tabel tanpa kolom id, dengan name sebagai PRIMARY KEY
        String createTableSQL = "CREATE TABLE IF NOT EXISTS contacts ("
                + "nama TEXT PRIMARY KEY, "  // Kolom 'name' sebagai PRIMARY KEY
                + "telepon TEXT NOT NULL UNIQUE, "
                + "kategori TEXT NOT NULL)";
        
        try (Connection connection = DriverManager.getConnection(url)) {
            Statement statement = connection.createStatement();
            statement.execute(createTableSQL);
            System.out.println("Database dan tabel berhasil dibuat!");
        } catch (SQLException e) {
            System.err.println("Terjadi kesalahan: " + e.getMessage());
        }
    }

    static Connection getConnection() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
