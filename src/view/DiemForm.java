package view;

import connect.DBConnection;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class DiemForm extends JFrame { // Cửa sổ quản lý điểm sinh viên theo từng môn học

    // txtMaSV, txtMaMH: khóa định danh; txtCC: chuyên cần; txtGK: giữa kỳ; txtCK: cuối kỳ; txtTB: trung bình
    private JTextField txtMaSV, txtMaMH, txtCC, txtGK, txtCK, txtTB, txtTimKiem;
    private JTable table;
    private DefaultTableModel model;

    public DiemForm() {
        initUI();   // Dựng giao diện
        loadData(); // Tải danh sách điểm từ bảng Diem khi mở form
    }

    private void initUI() {
        setTitle("Quan Ly Diem");
        setSize(820, 490);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Chỉ đóng form điểm, giữ MainForm mở
        setLayout(new BorderLayout(8, 8));

        // Lưới 3 hàng x 4 cột: mỗi cặp gồm 1 JLabel mô tả và 1 JTextField nhập/hiển thị giá trị
        JPanel pnlTop = new JPanel(new GridLayout(3, 4, 8, 8));
        pnlTop.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        txtMaSV = new JTextField();
        txtMaMH = new JTextField();
        txtCC = new JTextField();
        txtGK = new JTextField();
        txtCK = new JTextField();
        txtTB = new JTextField();
        txtTB.setEditable(false); // Không cho người dùng sửa tay vì điểm trung bình được tự động tính

        pnlTop.add(new JLabel("Ma SV:"));
        pnlTop.add(txtMaSV);
        pnlTop.add(new JLabel("Ma MH:"));
        pnlTop.add(txtMaMH);

        pnlTop.add(new JLabel("Diem Chuyen Can:"));
        pnlTop.add(txtCC);
        pnlTop.add(new JLabel("Diem Giua Ky:"));
        pnlTop.add(txtGK);

        pnlTop.add(new JLabel("Diem Cuoi Ky:"));
        pnlTop.add(txtCK);
        pnlTop.add(new JLabel("Diem Trung Binh:"));
        pnlTop.add(txtTB);

        // Các nút thao tác thêm, sửa, xóa và làm mới
        JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JButton btnThem = new JButton("Them");
        JButton btnSua = new JButton("Sua");
        JButton btnXoa = new JButton("Xoa");
        JButton btnLamMoi = new JButton("Lam moi");

        pnlBtn.add(btnThem);
        pnlBtn.add(btnSua);
        pnlBtn.add(btnXoa);
        pnlBtn.add(btnLamMoi);

        // Thanh tìm kiếm bản ghi điểm theo mã sinh viên hoặc mã môn học
        JPanel pnlSearch = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        txtTimKiem = new JTextField(24);
        JButton btnTimKiem = new JButton("Tim kiem");
        pnlSearch.add(new JLabel("Tim kiem (Ma SV / Ma MH):"));
        pnlSearch.add(txtTimKiem);
        pnlSearch.add(btnTimKiem);

        // Gom toàn bộ khu vực điều khiển lên phía trên (NORTH)
        JPanel pnlNorth = new JPanel(new BorderLayout());
        pnlNorth.add(pnlTop, BorderLayout.NORTH);
        pnlNorth.add(pnlBtn, BorderLayout.CENTER);
        pnlNorth.add(pnlSearch, BorderLayout.SOUTH);
        add(pnlNorth, BorderLayout.NORTH);

        // Bảng hiển thị 6 cột điểm đặt ở vùng giữa (CENTER) có thanh cuộn JScrollPane
        model = new DefaultTableModel(
                new String[]{"Ma SV", "Ma MH", "Chuyen Can", "Giua Ky", "Cuoi Ky", "Trung Binh"}, 0);
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Khi click chọn một dòng trên bảng -> tự động điền dữ liệu dòng đó lên các ô nhập
        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                txtMaSV.setText(model.getValueAt(row, 0).toString());
                txtMaMH.setText(model.getValueAt(row, 1).toString());
                txtCC.setText(model.getValueAt(row, 2).toString());
                txtGK.setText(model.getValueAt(row, 3).toString());
                txtCK.setText(model.getValueAt(row, 4).toString());
                txtTB.setText(model.getValueAt(row, 5).toString());
            }
        });

        // Gắn sự kiện cho các nút chức năng và phím Enter trong ô tìm kiếm
        btnThem.addActionListener(e -> themDiem());
        btnSua.addActionListener(e -> suaDiem());
        btnXoa.addActionListener(e -> xoaDiem());
        btnTimKiem.addActionListener(e -> timKiemDiem());
        txtTimKiem.addActionListener(e -> timKiemDiem());
        btnLamMoi.addActionListener(e -> {
            // Xóa trắng toàn bộ ô nhập/tìm kiếm và tải lại danh sách điểm từ CSDL
            txtMaSV.setText("");
            txtMaMH.setText("");
            txtCC.setText("");
            txtGK.setText("");
            txtCK.setText("");
            txtTB.setText("");
            txtTimKiem.setText("");
            loadData();
        });
    }

    // Hàm hỗ trợ làm tròn số thực double tới 2 chữ số thập phân để tránh đuôi lẻ nhị phân (vd: 9.049999999999999)
    private double lamTron2ChuSo(double val) {
        return Math.round(val * 100.0) / 100.0;
    }

    // Đọc điểm thành phần (CC, GK, CK), tính điểm TB theo trọng số 0.1 - 0.3 - 0.6 rồi làm tròn 2 chữ số
    private double tinhDiemTrungBinh() {
        double cc = Double.parseDouble(txtCC.getText().trim());
        double gk = Double.parseDouble(txtGK.getText().trim());
        double ck = Double.parseDouble(txtCK.getText().trim());
        return lamTron2ChuSo(cc * 0.1 + gk * 0.3 + ck * 0.6);
    }

    // Đổ dữ liệu điểm từ ResultSet lên bảng, đồng thời làm tròn 2 chữ số cho các cột điểm hiển thị
    private void hienThiKetQua(PreparedStatement ps) throws SQLException {
        model.setRowCount(0);
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("MaSV"),
                    rs.getString("MaMH"),
                    lamTron2ChuSo(rs.getDouble("DiemChuyenCan")),
                    lamTron2ChuSo(rs.getDouble("DiemGiuaKy")),
                    lamTron2ChuSo(rs.getDouble("DiemCuoiKy")),
                    lamTron2ChuSo(rs.getDouble("DiemTrungBinh"))
                });
            }
        }
    }

    // Tải toàn bộ bản ghi từ bảng Diem trong SQL Server
    private void loadData() {
        String sql = "SELECT MaSV, MaMH, DiemChuyenCan, DiemGiuaKy, DiemCuoiKy, DiemTrungBinh FROM Diem";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            hienThiKetQua(ps);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Loi tai du lieu Diem: " + ex.getMessage());
        }
    }

    // Tìm kiếm điểm theo MaSV hoặc MaMH với từ khóa gần đúng LIKE
    private void timKiemDiem() {
        String key = txtTimKiem.getText().trim();
        if (key.isEmpty()) {
            loadData();
            return;
        }
        String sql = "SELECT MaSV, MaMH, DiemChuyenCan, DiemGiuaKy, DiemCuoiKy, DiemTrungBinh FROM Diem "
                   + "WHERE MaSV LIKE ? OR MaMH LIKE ?";
        String like = "%" + key + "%";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            hienThiKetQua(ps);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Loi tim kiem Diem: " + ex.getMessage());
        }
    }

    // Thêm bản ghi điểm mới và lưu luôn giá trị DiemTrungBinh đã tính vào CSDL
    private void themDiem() {
        String sql = "INSERT INTO Diem(MaSV, MaMH, DiemChuyenCan, DiemGiuaKy, DiemCuoiKy, DiemTrungBinh) VALUES(?, ?, ?, ?, ?, ?)";
        try {
            double cc = Double.parseDouble(txtCC.getText().trim());
            double gk = Double.parseDouble(txtGK.getText().trim());
            double ck = Double.parseDouble(txtCK.getText().trim());
            double tb = tinhDiemTrungBinh(); // Tính điểm trung bình trước khi lưu

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, txtMaSV.getText().trim());
                ps.setString(2, txtMaMH.getText().trim());
                ps.setDouble(3, cc);
                ps.setDouble(4, gk);
                ps.setDouble(5, ck);
                ps.setDouble(6, tb);
                ps.executeUpdate();
                loadData();
                txtTB.setText(String.valueOf(tb)); // Hiển thị kết quả điểm TB vừa tính lên ô txtTB
                JOptionPane.showMessageDialog(this, "Them diem thanh cong!");
            }
        } catch (NumberFormatException ex) {
            // Báo lỗi nếu người dùng nhập điểm không phải định dạng số thực hợp lệ
            JOptionPane.showMessageDialog(this, "Cac cot diem phai nhap so hop le!");
        } catch (SQLException ex) {
            // Có thể xảy ra nếu MaSV hoặc MaMH chưa tồn tại ở bảng cha, hoặc trùng cặp khóa MaSV + MaMH
            JOptionPane.showMessageDialog(this, "Loi them Diem: " + ex.getMessage());
        }
    }

    // Sửa các cột điểm và tính lại DiemTrungBinh theo cặp khóa MaSV + MaMH
    private void suaDiem() {
        String sql = "UPDATE Diem SET DiemChuyenCan = ?, DiemGiuaKy = ?, DiemCuoiKy = ?, DiemTrungBinh = ? WHERE MaSV = ? AND MaMH = ?";
        try {
            double cc = Double.parseDouble(txtCC.getText().trim());
            double gk = Double.parseDouble(txtGK.getText().trim());
            double ck = Double.parseDouble(txtCK.getText().trim());
            double tb = tinhDiemTrungBinh();

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setDouble(1, cc);
                ps.setDouble(2, gk);
                ps.setDouble(3, ck);
                ps.setDouble(4, tb);
                ps.setString(5, txtMaSV.getText().trim());
                ps.setString(6, txtMaMH.getText().trim());
                ps.executeUpdate();
                loadData();
                txtTB.setText(String.valueOf(tb));
                JOptionPane.showMessageDialog(this, "Cap nhat diem thanh cong!");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Cac cot diem phai nhap so hop le!");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Loi sua Diem: " + ex.getMessage());
        }
    }

    // Xóa bản ghi điểm theo cặp khóa MaSV + MaMH
    private void xoaDiem() {
        String sql = "DELETE FROM Diem WHERE MaSV = ? AND MaMH = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, txtMaSV.getText().trim());
            ps.setString(2, txtMaMH.getText().trim());
            ps.executeUpdate();
            loadData();
            JOptionPane.showMessageDialog(this, "Xoa diem thanh cong!");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Loi xoa Diem: " + ex.getMessage());
        }
    }
}