/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Disporapar HST
 */
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Koneksi {
    
    // Method untuk mendapatkan koneksi ke database SQLite
    public static Connection getConnection() throws SQLException {
        // Path untuk file database SQLite
        String dbPath = "contacts.db";  // Nama file database
        // URL koneksi SQLite
        String url = "jdbc:sqlite:" + dbPath;
        
        // Mengembalikan koneksi
        return DriverManager.getConnection(url);
    }
}
