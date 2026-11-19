package view;

import connect.DBConnection;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class SinhVienForm extends JFrame {

    private JTextField txtMaSV, txtHoTen, txtNgaySinh, txtLop, txtTimKiem;
    private JComboBox<String> cboGioiTinh;
    private JTable table;
    private DefaultTableModel model;

    public SinhVienForm() {
        initUI();   // Dựng giao diện
        loadData(); // Tải danh sách sinh viên từ SQL Server khi mở form
    }

    private void initUI() {
        setTitle("Quan Ly Sinh Vien");
        setSize(780, 480);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Chỉ đóng form con, không tắt MainForm
        setLayout(new BorderLayout(8, 8));

        // Khu vực nhập thông tin sinh viên
        JPanel pnlTop = new JPanel(new GridLayout(3, 4, 8, 8));
        pnlTop.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        txtMaSV = new JTextField();
        txtHoTen = new JTextField();
        txtNgaySinh = new JTextField();
        cboGioiTinh = new JComboBox<>(new String[]{"Nam", "Nu"});
        txtLop = new JTextField();

        pnlTop.add(new JLabel("Ma SV:"));
        pnlTop.add(txtMaSV);
        pnlTop.add(new JLabel("Ho Ten:"));
        pnlTop.add(txtHoTen);

        pnlTop.add(new JLabel("Ngay Sinh (yyyy-MM-dd):"));
        pnlTop.add(txtNgaySinh);
        pnlTop.add(new JLabel("Gioi Tinh:"));
        pnlTop.add(cboGioiTinh);

        pnlTop.add(new JLabel("Lop:"));
        pnlTop.add(txtLop);
        pnlTop.add(new JLabel(""));
        pnlTop.add(new JLabel(""));

        // Khu vực các nút thao tác CRUD và làm mới
        JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JButton btnThem = new JButton("Them");
        JButton btnSua = new JButton("Sua");
        JButton btnXoa = new JButton("Xoa");
        JButton btnLamMoi = new JButton("Lam moi");

        pnlBtn.add(btnThem);
        pnlBtn.add(btnSua);
        pnlBtn.add(btnXoa);
        pnlBtn.add(btnLamMoi);

        // Khu vực tìm kiếm theo Ma SV, Ho Ten hoặc Lop
        JPanel pnlSearch = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        txtTimKiem = new JTextField(24);
        JButton btnTimKiem = new JButton("Tim kiem");
        pnlSearch.add(new JLabel("Tim kiem (Ma SV / Ho Ten / Lop):"));
        pnlSearch.add(txtTimKiem);
        pnlSearch.add(btnTimKiem);

        JPanel pnlNorth = new JPanel(new BorderLayout());
        pnlNorth.add(pnlTop, BorderLayout.NORTH);
        pnlNorth.add(pnlBtn, BorderLayout.CENTER);
        pnlNorth.add(pnlSearch, BorderLayout.SOUTH);
        add(pnlNorth, BorderLayout.NORTH);

        // Bảng hiển thị danh sách sinh viên
        model = new DefaultTableModel(new String[]{"Ma SV", "Ho Ten", "Ngay Sinh", "Gioi Tinh", "Lop"}, 0);
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Click chọn dòng trên bảng -> đổ dữ liệu lên các ô nhập
        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                txtMaSV.setText(model.getValueAt(row, 0).toString());
                txtHoTen.setText(model.getValueAt(row, 1).toString());
                Object ns = model.getValueAt(row, 2);
                txtNgaySinh.setText(ns != null ? ns.toString() : "");
                cboGioiTinh.setSelectedItem(model.getValueAt(row, 3).toString());
                txtLop.setText(model.getValueAt(row, 4).toString());
            }
        });

        // Gắn sự kiện cho các nút và phím Enter ở ô tìm kiếm
        btnThem.addActionListener(e -> themSV());
        btnSua.addActionListener(e -> suaSV());
        btnXoa.addActionListener(e -> xoaSV());
        btnTimKiem.addActionListener(e -> timKiemSV());
        txtTimKiem.addActionListener(e -> timKiemSV());
        btnLamMoi.addActionListener(e -> {
            txtMaSV.setText("");
            txtHoTen.setText("");
            txtNgaySinh.setText("");
            txtLop.setText("");
            txtTimKiem.setText("");
            cboGioiTinh.setSelectedIndex(0);
            loadData();
        });
    }

    // Đổ dữ liệu từ ResultSet vào bảng
    private void hienThiKetQua(PreparedStatement ps) throws SQLException {
        model.setRowCount(0);
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("MaSV"),
                    rs.getString("HoTen"),
                    rs.getDate("NgaySinh"),
                    rs.getString("GioiTinh"),
                    rs.getString("Lop")
                });
            }
        }
    }

    // Tải toàn bộ bản ghi từ bảng SinhVien
    private void loadData() {
        String sql = "SELECT MaSV, HoTen, NgaySinh, GioiTinh, Lop FROM SinhVien";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            hienThiKetQua(ps);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Loi tai du lieu SV: " + ex.getMessage());
        }
    }

    // Tìm kiếm gần đúng theo MaSV, HoTen hoặc Lop
    private void timKiemSV() {
        String key = txtTimKiem.getText().trim();
        if (key.isEmpty()) {
            loadData();
            return;
        }
        String sql = "SELECT MaSV, HoTen, NgaySinh, GioiTinh, Lop FROM SinhVien "
                   + "WHERE MaSV LIKE ? OR HoTen LIKE ? OR Lop LIKE ?";
        String like = "%" + key + "%";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            hienThiKetQua(ps);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Loi tim kiem SV: " + ex.getMessage());
        }
    }

    // Thêm sinh viên mới vào CSDL
    private void themSV() {
        String sql = "INSERT INTO SinhVien(MaSV, HoTen, NgaySinh, GioiTinh, Lop) VALUES(?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, txtMaSV.getText().trim());
            ps.setString(2, txtHoTen.getText().trim());
            String nsText = txtNgaySinh.getText().trim();
            ps.setDate(3, nsText.isEmpty() ? null : Date.valueOf(nsText));
            ps.setString(4, cboGioiTinh.getSelectedItem().toString());
            ps.setString(5, txtLop.getText().trim());
            ps.executeUpdate();
            loadData();
            JOptionPane.showMessageDialog(this, "Them sinh vien thanh cong!");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "Ngay sinh phai theo dinh dang yyyy-MM-dd (vd: 2007-05-15)!");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Loi them SV: " + ex.getMessage());
        }
    }

    // Cập nhật thông tin sinh viên theo MaSV
    private void suaSV() {
        String sql = "UPDATE SinhVien SET HoTen = ?, NgaySinh = ?, GioiTinh = ?, Lop = ? WHERE MaSV = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, txtHoTen.getText().trim());
            String nsText = txtNgaySinh.getText().trim();
            ps.setDate(2, nsText.isEmpty() ? null : Date.valueOf(nsText));
            ps.setString(3, cboGioiTinh.getSelectedItem().toString());
            ps.setString(4, txtLop.getText().trim());
            ps.setString(5, txtMaSV.getText().trim());
            ps.executeUpdate();
            loadData();
            JOptionPane.showMessageDialog(this, "Cap nhat sinh vien thanh cong!");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "Ngay sinh phai theo dinh dang yyyy-MM-dd (vd: 2007-05-15)!");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Loi sua SV: " + ex.getMessage());
        }
    }

    // Xóa sinh viên theo MaSV
    private void xoaSV() {
        String sql = "DELETE FROM SinhVien WHERE MaSV = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, txtMaSV.getText().trim());
            ps.executeUpdate();
            loadData();
            JOptionPane.showMessageDialog(this, "Xoa sinh vien thanh cong!");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Loi xoa SV: " + ex.getMessage());
        }
    }
}