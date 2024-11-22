/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */

/**
 *
 * @author Disporapar HST
 */
import javax.swing.*;
import java.io.BufferedReader;
import java.awt.event.ActionListener;
import java.io.FileReader;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.io.IOException;
import javax.swing.JTable;
import javax.swing.table.TableModel;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import javax.swing.table.DefaultTableModel;
import java.sql.*;

public class Apk extends javax.swing.JFrame {
 
 
    /**
     * Creates new form Apk
     */
    public Apk() {
        initComponents();
        updateTable();
        
        hapusbutton.addActionListener(new java.awt.event.ActionListener() {
    public void actionPerformed(java.awt.event.ActionEvent evt) {
        hapusbuttonActionPerformed(evt);  // Panggil method yang akan dijalankan ketika tombol hapus ditekan
    }
});
        
    editbutton.addActionListener(new ActionListener() {
    @Override
    public void actionPerformed(ActionEvent evt) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(null, "Pilih kontak yang ingin diedit.");
            return;
        }

        // Ambil data dari form input
        String namaKontak = nama.getText().trim();
        String teleponKontak = telepon.getText().trim();
        String kategoriKontak = (String) kategori.getSelectedItem();

        if (namaKontak.isEmpty() || teleponKontak.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Nama dan Telepon harus diisi!");
            return;  // Jika nama atau telepon kosong
        }

        // Validasi nomor telepon
        if (!isValidTelepon(teleponKontak)) {
            JOptionPane.showMessageDialog(null, "Nomor telepon tidak valid!");
            return;
        }

        // Ambil telepon yang lama dari baris yang dipilih
        String oldTeleponKontak = table.getValueAt(selectedRow, 1).toString();

        // Konfirmasi pengeditan
        int confirm = JOptionPane.showConfirmDialog(null, "Apakah Anda yakin ingin memperbarui kontak ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            // Lakukan pembaruan data ke database
            try (Connection conn = Koneksi.getConnection()) {
                String query = "UPDATE contacts SET nama = ?, telepon = ?, kategori = ? WHERE telepon = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, namaKontak);
                stmt.setString(2, teleponKontak);
                stmt.setString(3, kategoriKontak);
                stmt.setString(4, oldTeleponKontak);  // Update berdasarkan nomor telepon lama

                int result = stmt.executeUpdate();
                if (result > 0) {
                    JOptionPane.showMessageDialog(null, "Kontak berhasil diperbarui!");
                    updateTable();  // Memperbarui tabel setelah pengeditan
                    clearFields();  // Mengosongkan form setelah update
                } else {
                    JOptionPane.showMessageDialog(null, "Kontak gagal diperbarui.");
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
            }
        }
    }

           
        });
    
    
    caributton.addActionListener(new ActionListener() {
    @Override
    public void actionPerformed(ActionEvent e) {
        String searchTerm = cari.getText().trim();  // Ambil teks pencarian dari JTextField
        if (searchTerm.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Masukkan kata kunci untuk mencari!");
            return;
        }

        // Proses pencarian di database
        try (Connection conn = Koneksi.getConnection()) {
            String query = "SELECT * FROM contacts WHERE nama LIKE ? OR telepon LIKE ? OR kategori LIKE ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            
            // Menggunakan wildcard untuk pencocokan substring
            stmt.setString(1, "%" + searchTerm + "%");  // Mencari di kolom nama
            stmt.setString(2, "%" + searchTerm + "%");  // Mencari di kolom telepon
            stmt.setString(3, "%" + searchTerm + "%");  // Mencari di kolom kategori

            ResultSet rs = stmt.executeQuery();
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            model.setRowCount(0);  // Clear existing rows

            while (rs.next()) {
                model.addRow(new Object[] {
                    rs.getString("nama"),
                    rs.getString("telepon"),
                    rs.getString("kategori")
                });
            }

            if (model.getRowCount() == 0) {
                JOptionPane.showMessageDialog(null, "Kontak tidak ditemukan.");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
        }
    }
});
    
    exportbutton.addActionListener(new ActionListener() {
    @Override
    public void actionPerformed(ActionEvent evt) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save as CSV");
        int userSelection = fileChooser.showSaveDialog(null);
        
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            java.io.File fileToSave = fileChooser.getSelectedFile();
            String filePath = fileToSave.getAbsolutePath();
            
            // Pastikan ekstensi file adalah .csv
            if (!filePath.endsWith(".csv")) {
                filePath += ".csv";
            }
            
            try (FileWriter fw = new FileWriter(filePath);
                 BufferedWriter bw = new BufferedWriter(fw)) {
                DefaultTableModel model = (DefaultTableModel) table.getModel();
                int columnCount = model.getColumnCount();
                int rowCount = model.getRowCount();

                // Tulis header kolom
                for (int i = 0; i < columnCount; i++) {
                    bw.write(model.getColumnName(i));
                    if (i < columnCount - 1) {
                        bw.write(",");
                    }
                }
                bw.newLine();  // Pindah ke baris baru

                // Tulis data tabel
                for (int i = 0; i < rowCount; i++) {
                    for (int j = 0; j < columnCount; j++) {
                        bw.write(model.getValueAt(i, j).toString());
                        if (j < columnCount - 1) {
                            bw.write(",");
                        }
                    }
                    bw.newLine();  // Pindah ke baris baru
                }

                JOptionPane.showMessageDialog(null, "Data berhasil diekspor ke CSV!");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Error saat mengekspor data: " + e.getMessage());
            }
        }
    }
});

    importbutton.addActionListener(new ActionListener() {
    @Override
    public void actionPerformed(ActionEvent evt) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Open CSV File");
        int userSelection = fileChooser.showOpenDialog(null);
        
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            java.io.File fileToOpen = fileChooser.getSelectedFile();
            String filePath = fileToOpen.getAbsolutePath();
            
            try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
                String line;
                DefaultTableModel model = (DefaultTableModel) table.getModel();
                model.setRowCount(0);  // Bersihkan tabel sebelum mengimpor data

                while ((line = br.readLine()) != null) {
                    String[] data = line.split(",");
                    if (data.length == 3) {  // Pastikan ada 3 kolom (nama, telepon, kategori)
                        model.addRow(data);
                    }
                }
                JOptionPane.showMessageDialog(null, "Data berhasil diimpor!");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Error saat mengimpor data: " + e.getMessage());
            }
        }
    }
});
    }
    private void updateTable() {
    try (Connection conn = Koneksi.getConnection()) {
        String query = "SELECT * FROM contacts";
        PreparedStatement stmt = conn.prepareStatement(query);
        ResultSet rs = stmt.executeQuery();

        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);  // Clear existing rows

        while (rs.next()) {
            model.addRow(new Object[]{
                rs.getString("nama"),
                rs.getString("telepon"),
                rs.getString("kategori")
            });
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
    }
}
   


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        nama = new javax.swing.JTextField();
        telepon = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        caributton = new javax.swing.JButton();
        tambahbutton = new javax.swing.JButton();
        hapusbutton = new javax.swing.JButton();
        kategori = new javax.swing.JComboBox<>();
        jLabel5 = new javax.swing.JLabel();
        editbutton = new javax.swing.JButton();
        cari = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        list = new javax.swing.JList<>();
        exportbutton = new javax.swing.JButton();
        importbutton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        jLabel1.setText("Pengelola Kontak");

        jLabel2.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel2.setText("Nama");

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel3.setText("Telepon");

        jLabel4.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel4.setText("Kategori");

        caributton.setText("Cari");

        tambahbutton.setText("Tambah");
        tambahbutton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tambahbuttonActionPerformed(evt);
            }
        });

        hapusbutton.setText("Hapus");
        hapusbutton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hapusbuttonActionPerformed(evt);
            }
        });

        kategori.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Keluarga", "Teman", "Kerja" }));
        kategori.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                kategoriActionPerformed(evt);
            }
        });

        jLabel5.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel5.setText("Cari");

        editbutton.setText("Edit");

        table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Nama", "Telepon", "Kategori"
            }
        ));
        jScrollPane1.setViewportView(table);

        list.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "Keluarga", "Teman", "Kerja", "Semua" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        jScrollPane2.setViewportView(list);

        exportbutton.setText("Export CSV");

        importbutton.setText("Import CSV");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(22, 22, 22)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(kategori, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(telepon)
                                    .addComponent(nama)))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(cari)))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(tambahbutton, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(hapusbutton, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(caributton, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(editbutton, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(198, 198, 198)
                                .addComponent(jLabel1))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(40, 40, 40)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jScrollPane2)
                                    .addComponent(exportbutton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(importbutton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(18, 18, 18)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 505, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 68, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jLabel1)
                .addGap(40, 40, 40)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(nama, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tambahbutton, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(telepon, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(hapusbutton, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(kategori, javax.swing.GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE)
                    .addComponent(editbutton, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(caributton, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cari, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(exportbutton, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(importbutton, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(44, 44, 44))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void tambahbuttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tambahbuttonActionPerformed
 String namaKontak = nama.getText().trim();  // Ambil nama dan hapus spasi di awal/akhir
    String teleponKontak = telepon.getText().trim();  // Ambil telepon dan hapus spasi di awal/akhir
    String kategoriKontak = (String) kategori.getSelectedItem();  // Ambil kategori dari JComboBox

    // Periksa apakah nama atau telepon kosong hanya ketika tombol tambah ditekan
    if (namaKontak.isEmpty() || teleponKontak.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Nama dan Telepon harus diisi!");
        return;  // Jika nama atau telepon kosong, jangan lanjutkan
    }

    // Validasi nomor telepon (hanya angka, panjang 10-13 digit)
    if (!isValidTelepon(teleponKontak)) {
        JOptionPane.showMessageDialog(this, "Nomor telepon tidak valid! Harus berupa angka dan panjang antara 10-13 digit.");
        return;  // Hentikan jika nomor telepon tidak valid
    }

    // Jika tidak ada yang kosong dan validasi nomor telepon sukses, lanjutkan ke proses database
    try (Connection conn = Koneksi.getConnection()) {
        String query = "INSERT INTO contacts (nama, telepon, kategori) VALUES (?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(query);

        // Set nilai parameter untuk prepared statement
        stmt.setString(1, namaKontak);
        stmt.setString(2, teleponKontak);
        stmt.setString(3, kategoriKontak);

        int result = stmt.executeUpdate();  // Eksekusi query untuk memasukkan data
        if (result > 0) {
            JOptionPane.showMessageDialog(this, "Kontak berhasil ditambahkan!");
            clearFields();  // Kosongkan field setelah data berhasil ditambahkan
            updateTable();  // Perbarui tabel untuk menampilkan data terbaru
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
    }
    }//GEN-LAST:event_tambahbuttonActionPerformed

    private void kategoriActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_kategoriActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_kategoriActionPerformed

    private void hapusbuttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hapusbuttonActionPerformed
 // Ambil baris yang dipilih dari tabel
    int selectedRow = table.getSelectedRow();  // Mendapatkan baris yang dipilih

    if (selectedRow == -1) {  // Jika tidak ada baris yang dipilih
        JOptionPane.showMessageDialog(this, "Pilih kontak yang ingin dihapus.");
        return;  // Jangan lanjutkan jika tidak ada baris yang dipilih
    }

    // Ambil ID atau telepon dari baris yang dipilih
    String teleponKontak = table.getValueAt(selectedRow, 1).toString();  // Ambil telepon dari kolom 2

    // Konfirmasi penghapusan
    int confirm = JOptionPane.showConfirmDialog(this, "Apakah Anda yakin ingin menghapus kontak ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
    if (confirm == JOptionPane.YES_OPTION) {
        // Lakukan penghapusan dari database
        try (Connection conn = Koneksi.getConnection()) {
            String query = "DELETE FROM contacts WHERE telepon = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, teleponKontak);  // Menghapus berdasarkan nomor telepon

            int result = stmt.executeUpdate();  // Eksekusi query untuk menghapus data
            if (result > 0) {
                JOptionPane.showMessageDialog(this, "Kontak berhasil dihapus!");
                updateTable();  // Perbarui tabel setelah penghapusan
                
                // Reset baris yang dipilih setelah update
                table.clearSelection();  // Menghapus seleksi dari tabel setelah penghapusan
            } else {
                JOptionPane.showMessageDialog(this, "Kontak tidak ditemukan atau gagal dihapus.");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    } // TODO add your handling code here:
    }//GEN-LAST:event_hapusbuttonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Apk.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Apk.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Apk.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Apk.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Apk().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField cari;
    private javax.swing.JButton caributton;
    private javax.swing.JButton editbutton;
    private javax.swing.JButton exportbutton;
    private javax.swing.JButton hapusbutton;
    private javax.swing.JButton importbutton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JComboBox<String> kategori;
    private javax.swing.JList<String> list;
    private javax.swing.JTextField nama;
    private javax.swing.JTable table;
    private javax.swing.JButton tambahbutton;
    private javax.swing.JTextField telepon;
    // End of variables declaration//GEN-END:variables
String[] categories = { "Semua", "Keluarga", "Teman", "Kerja" };
JList<String> kategoriJList = new JList<>(categories);

  private void clearFields() {
    nama.setText("");  // Kosongkan JTextField nama
    telepon.setText("");  // Kosongkan JTextField telepon
    kategori.setSelectedIndex(0);  // Set JComboBox kategori ke pilihan pertama
}
  private boolean isValidTelepon(String telepon) {
    // Periksa apakah telepon hanya berisi angka dan memiliki panjang antara 10 sampai 13 digit
    return telepon.matches("\\d{10,13}");
}

    private static class File {

        public File() {
        }
    }

}
