package view; // Thuộc gói view chứa các màn hình giao diện

import connect.DBConnection; // Lớp kết nối JDBC tới cơ sở dữ liệu SQL Server
import java.awt.BorderLayout; // Bố cục chia vùng Bắc - Trung tâm - Nam - Đông - Tây
import java.awt.FlowLayout; // Bố cục xếp component nối tiếp theo chiều ngang
import java.awt.GridLayout; // Bố cục chia ô lưới đều nhau
import java.sql.Connection; // Đối tượng kết nối tới CSDL
import java.sql.PreparedStatement; // Câu lệnh SQL có tham số (?) giúp tránh lỗi chuỗi và SQL Injection
import java.sql.ResultSet; // Tập bản ghi trả về từ câu truy vấn SELECT
import java.sql.SQLException; // Ngoại lệ khi thao tác với CSDL thất bại
import javax.swing.BorderFactory; // Tạo khoảng cách lề trống xung quanh panel
import javax.swing.JButton; // Nút bấm
import javax.swing.JFrame; // Cửa sổ giao diện
import javax.swing.JLabel; // Nhãn chữ
import javax.swing.JOptionPane; // Hộp thoại thông báo lỗi hoặc thành công
import javax.swing.JPanel; // Khung chứa các thành phần giao diện
import javax.swing.JScrollPane; // Thanh cuộn cho bảng dữ liệu
import javax.swing.JTable; // Bảng hiển thị dữ liệu dạng hàng - cột
import javax.swing.JTextField; // Ô nhập văn bản 1 dòng
import javax.swing.table.DefaultTableModel; // Mô hình quản lý dữ liệu hàng/cột của JTable

public class MonHocForm extends JFrame { // Kế thừa JFrame để tạo cửa sổ quản lý môn học

    // Các ô nhập liệu, ô tìm kiếm, bảng và model dữ liệu
    private JTextField txtMaMH, txtTenMH, txtSoTinChi, txtTimKiem;
    private JTable table;
    private DefaultTableModel model;

    public MonHocForm() {
        initUI();   // Khởi tạo giao diện form
        loadData(); // Tải danh sách môn học từ SQL Server ngay khi mở form
    }

    private void initUI() {
        setTitle("Quan Ly Mon Hoc"); // Tiêu đề cửa sổ
        setSize(720, 440); // Kích thước rộng 720px, cao 440px
        setLocationRelativeTo(null); // Căn cửa sổ ra giữa màn hình
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Đóng form môn học nhưng không tắt MainForm
        setLayout(new BorderLayout(8, 8)); // Khoảng cách hở giữa các vùng BorderLayout là 8px

        // Panel nhập liệu dạng lưới 2 hàng x 3 cột (hàng 1 là nhãn, hàng 2 là ô nhập tương ứng)
        JPanel pnlTop = new JPanel(new GridLayout(2, 3, 8, 8));
        pnlTop.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        txtMaMH = new JTextField();
        txtTenMH = new JTextField();
        txtSoTinChi = new JTextField();

        pnlTop.add(new JLabel("Ma MH:"));
        pnlTop.add(new JLabel("Ten MH:"));
        pnlTop.add(new JLabel("So Tin Chi:"));
        pnlTop.add(txtMaMH);
        pnlTop.add(txtTenMH);
        pnlTop.add(txtSoTinChi);

        // Panel chứa các nút thao tác CRUD và nút làm mới form
        JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JButton btnThem = new JButton("Them");
        JButton btnSua = new JButton("Sua");
        JButton btnXoa = new JButton("Xoa");
        JButton btnLamMoi = new JButton("Lam moi");

        pnlBtn.add(btnThem);
        pnlBtn.add(btnSua);
        pnlBtn.add(btnXoa);
        pnlBtn.add(btnLamMoi);

        // Panel tìm kiếm môn học theo mã môn hoặc tên môn
        JPanel pnlSearch = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        txtTimKiem = new JTextField(24);
        JButton btnTimKiem = new JButton("Tim kiem");
        pnlSearch.add(new JLabel("Tim kiem (Ma MH / Ten MH):"));
        pnlSearch.add(txtTimKiem);
        pnlSearch.add(btnTimKiem);

        // Gom cả 3 khu vực (nhập liệu + nút bấm + tìm kiếm) vào vùng NORTH của form
        JPanel pnlNorth = new JPanel(new BorderLayout());
        pnlNorth.add(pnlTop, BorderLayout.NORTH);
        pnlNorth.add(pnlBtn, BorderLayout.CENTER);
        pnlNorth.add(pnlSearch, BorderLayout.SOUTH);
        add(pnlNorth, BorderLayout.NORTH);

        // Khởi tạo bảng với 3 cột đúng cấu trúc bảng MonHoc, đặt ở vùng CENTER
        model = new DefaultTableModel(new String[]{"Ma MH", "Ten MH", "So Tin Chi"}, 0);
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Sự kiện khi chọn 1 dòng trên bảng: tự động đẩy giá trị từng cột lên các ô nhập
        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) { // Kiểm tra có dòng hợp lệ được chọn (tránh row = -1 khi reset bảng)
                txtMaMH.setText(model.getValueAt(row, 0).toString());
                txtTenMH.setText(model.getValueAt(row, 1).toString());
                txtSoTinChi.setText(model.getValueAt(row, 2).toString());
            }
        });

        // Gắn hành động cho các nút CRUD, tìm kiếm (cả click nút và nhấn Enter) và làm mới
        btnThem.addActionListener(e -> themMH());
        btnSua.addActionListener(e -> suaMH());
        btnXoa.addActionListener(e -> xoaMH());
        btnTimKiem.addActionListener(e -> timKiemMH());
        txtTimKiem.addActionListener(e -> timKiemMH());
        btnLamMoi.addActionListener(e -> {
            // Xóa sạch nội dung các ô nhập và tải lại toàn bộ dữ liệu gốc từ CSDL
            txtMaMH.setText("");
            txtTenMH.setText("");
            txtSoTinChi.setText("");
            txtTimKiem.setText("");
            loadData();
        });
    }

    // Hàm dùng chung để nạp kết quả truy vấn SELECT từ PreparedStatement lên JTable
    private void hienThiKetQua(PreparedStatement ps) throws SQLException {
        model.setRowCount(0); // Xóa các dòng cũ trên bảng trước khi đổ dữ liệu mới
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) { // Duyệt từng bản ghi trả về từ SQL Server
                model.addRow(new Object[]{
                    rs.getString("MaMH"),
                    rs.getString("TenMH"),
                    rs.getInt("SoTinChi")
                });
            }
        }
    }

    // Lấy toàn bộ danh sách môn học từ bảng MonHoc
    private void loadData() {
        String sql = "SELECT MaMH, TenMH, SoTinChi FROM MonHoc";
        // try-with-resources tự động đóng Connection và PreparedStatement sau khi thực thi xong
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            hienThiKetQua(ps);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Loi tai du lieu MH: " + ex.getMessage());
        }
    }

    // Tìm kiếm gần đúng môn học theo MaMH hoặc TenMH dùng toán tử LIKE
    private void timKiemMH() {
        String key = txtTimKiem.getText().trim();
        if (key.isEmpty()) { // Nếu không nhập từ khóa thì hiển thị lại toàn bộ danh sách
            loadData();
            return;
        }
        String sql = "SELECT MaMH, TenMH, SoTinChi FROM MonHoc WHERE MaMH LIKE ? OR TenMH LIKE ?";
        String like = "%" + key + "%"; // Thêm ký tự đại diện % ở hai đầu để tìm chuỗi con bất kỳ
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, like); // Gán tham số ? thứ nhất cho MaMH LIKE ?
            ps.setString(2, like); // Gán tham số ? thứ hai cho TenMH LIKE ?
            hienThiKetQua(ps);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Loi tim kiem MH: " + ex.getMessage());
        }
    }

    // Thêm một môn học mới vào bảng MonHoc
    private void themMH() {
        String sql = "INSERT INTO MonHoc(MaMH, TenMH, SoTinChi) VALUES(?, ?, ?)";
        try {
            // Chuyển chuỗi số tín chỉ sang số nguyên int; nếu nhập chữ sẽ ném NumberFormatException
            int stc = Integer.parseInt(txtSoTinChi.getText().trim());
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, txtMaMH.getText().trim());
                ps.setString(2, txtTenMH.getText().trim());
                ps.setInt(3, stc);
                ps.executeUpdate(); // Thực thi câu lệnh INSERT ghi dữ liệu xuống CSDL
                loadData(); // Cập nhật lại bảng hiển thị ngay sau khi thêm
                JOptionPane.showMessageDialog(this, "Them mon hoc thanh cong!");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "So tin chi phai la so nguyen!");
        } catch (SQLException ex) {
            // Bắt lỗi SQL như trùng khóa chính MaMH hoặc vi phạm ràng buộc kiểm tra
            JOptionPane.showMessageDialog(this, "Loi them MH: " + ex.getMessage());
        }
    }

    // Cập nhật tên môn và số tín chỉ dựa theo khóa chính MaMH
    private void suaMH() {
        String sql = "UPDATE MonHoc SET TenMH = ?, SoTinChi = ? WHERE MaMH = ?";
        try {
            int stc = Integer.parseInt(txtSoTinChi.getText().trim());
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, txtTenMH.getText().trim());
                ps.setInt(2, stc);
                ps.setString(3, txtMaMH.getText().trim()); // Điều kiện WHERE xác định đúng môn cần sửa
                ps.executeUpdate(); // Thực thi câu lệnh UPDATE
                loadData();
                JOptionPane.showMessageDialog(this, "Cap nhat mon hoc thanh cong!");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "So tin chi phai la so nguyen!");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Loi sua MH: " + ex.getMessage());
        }
    }

    // Xóa môn học theo MaMH
    private void xoaMH() {
        String sql = "DELETE FROM MonHoc WHERE MaMH = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, txtMaMH.getText().trim());
            ps.executeUpdate(); // Thực thi câu lệnh DELETE
            loadData();
            JOptionPane.showMessageDialog(this, "Xoa mon hoc thanh cong!");
        } catch (SQLException ex) {
            // Nếu môn học đã có điểm trong bảng Diem, SQL Server có thể báo lỗi ràng buộc khóa ngoại
            JOptionPane.showMessageDialog(this, "Loi xoa MH: " + ex.getMessage());
        }
    }
}