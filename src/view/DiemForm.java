package view; // Khai báo gói chứa giao diện DiemForm

import connect.DBConnection; // Gọi lớp kết nối CSDL SQL Server qua JDBC
import java.awt.BorderLayout; // Bố cục chia vùng Bắc - Trung tâm - Nam - Đông - Tây
import java.awt.FlowLayout; // Bố cục xếp các thành phần nối tiếp theo hàng ngang
import java.awt.GridLayout; // Bố cục chia lưới các ô đều nhau theo hàng và cột
import java.sql.Connection; // Quản lý phiên kết nối đến CSDL
import java.sql.PreparedStatement; // Thực thi câu lệnh SQL có tham số (?) an toàn
import java.sql.ResultSet; // Lưu trữ tập bản ghi kết quả từ câu lệnh SELECT
import java.sql.SQLException; // Bắt và xử lý lỗi phát sinh trong quá trình thao tác SQL
import javax.swing.BorderFactory; // Tạo đường viền và khoảng đệm cho các container Swing
import javax.swing.JButton; // Nút bấm thao tác trên giao diện
import javax.swing.JFrame; // Cửa sổ ứng dụng Swing
import javax.swing.JLabel; // Nhãn hiển thị văn bản mô tả
import javax.swing.JOptionPane; // Hiển thị hộp thoại thông báo và xác nhận
import javax.swing.JPanel; // Vùng chứa gom nhóm các thành phần giao diện
import javax.swing.JScrollPane; // Thêm thanh cuộn cho bảng dữ liệu JTable
import javax.swing.JTable; // Bảng hiển thị dữ liệu dạng hàng và cột
import javax.swing.JTextField; // Ô nhập liệu hoặc hiển thị văn bản một dòng
import javax.swing.table.DefaultTableModel; // Mô hình quản lý dữ liệu hàng và cột của JTable

public class DiemForm extends JFrame { // Cửa sổ quản lý điểm sinh viên theo từng môn học

    // txtMaSV, txtMaMH: khóa định danh; txtCC: chuyên cần; txtGK: giữa kỳ; txtCK: cuối kỳ; txtTB: trung bình
    private JTextField txtMaSV, txtMaMH, txtCC, txtGK, txtCK, txtTB, txtTimKiem;
    private JTable table; // Bảng hiển thị danh sách điểm
    private DefaultTableModel model; // Mô hình dữ liệu của bảng table

    // Hàm khởi tạo: dựng giao diện và tải dữ liệu điểm ban đầu từ CSDL
    public DiemForm() {
        initUI(); // Dựng các thành phần giao diện trên form
        loadData(); // Tải danh sách điểm từ bảng Diem khi mở form
    }

    // Thiết lập bố cục, các ô nhập liệu, nút chức năng, bảng và sự kiện tương tác
    private void initUI() {
        setTitle("Quan Ly Diem"); // Đặt tiêu đề cửa sổ
        setSize(820, 490); // Thiết lập kích thước cửa sổ (rộng x cao)
        setLocationRelativeTo(null); // Căn giữa cửa sổ trên màn hình
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Chỉ đóng form điểm, giữ MainForm mở
        setLayout(new BorderLayout(8, 8)); // Dùng BorderLayout với khoảng cách 8px

        // Lưới 3 hàng x 4 cột: mỗi cặp gồm 1 JLabel mô tả và 1 JTextField nhập/hiển thị giá trị
        JPanel pnlTop = new JPanel(new GridLayout(3, 4, 8, 8));
        pnlTop.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10)); // Tạo lề xung quanh lưới nhập liệu

        txtMaSV = new JTextField(); // Ô nhập mã sinh viên
        txtMaMH = new JTextField(); // Ô nhập mã môn học
        txtCC = new JTextField(); // Ô nhập điểm chuyên cần
        txtGK = new JTextField(); // Ô nhập điểm giữa kỳ
        txtCK = new JTextField(); // Ô nhập điểm cuối kỳ
        txtTB = new JTextField(); // Ô hiển thị điểm trung bình
        txtTB.setEditable(false); // Khóa sửa tay vì DiemTrungBinh do SQL Server tự động tính

        // Thêm từng cặp nhãn và ô nhập liệu theo đúng thứ tự lưới 3x4
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
        JButton btnThem = new JButton("Them"); // Nút thêm bản ghi điểm mới
        JButton btnSua = new JButton("Sua"); // Nút cập nhật điểm thành phần
        JButton btnXoa = new JButton("Xoa"); // Nút xóa bản ghi điểm
        JButton btnLamMoi = new JButton("Lam moi"); // Nút xóa trắng ô nhập và tải lại dữ liệu

        pnlBtn.add(btnThem);
        pnlBtn.add(btnSua);
        pnlBtn.add(btnXoa);
        pnlBtn.add(btnLamMoi);

        // Thanh tìm kiếm bản ghi điểm theo mã sinh viên hoặc mã môn học
        JPanel pnlSearch = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        txtTimKiem = new JTextField(24); // Ô nhập từ khóa tìm kiếm
        JButton btnTimKiem = new JButton("Tim kiem"); // Nút thực hiện tìm kiếm
        pnlSearch.add(new JLabel("Tim kiem (Ma SV / Ma MH):"));
        pnlSearch.add(txtTimKiem);
        pnlSearch.add(btnTimKiem);

        // Gom toàn bộ khu vực điều khiển lên phía trên (NORTH)
        JPanel pnlNorth = new JPanel(new BorderLayout());
        pnlNorth.add(pnlTop, BorderLayout.NORTH); // Khu vực nhập liệu ở trên cùng
        pnlNorth.add(pnlBtn, BorderLayout.CENTER); // Khu vực nút chức năng ở giữa
        pnlNorth.add(pnlSearch, BorderLayout.SOUTH); // Khu vực tìm kiếm ở dưới cùng của pnlNorth
        add(pnlNorth, BorderLayout.NORTH);

        // Bảng hiển thị 6 cột điểm đặt ở vùng giữa (CENTER) có thanh cuộn JScrollPane
        model = new DefaultTableModel(
                new String[]{"Ma SV", "Ma MH", "Chuyen Can", "Giua Ky", "Cuoi Ky", "Trung Binh"}, 0);
        table = new JTable(model); // Khởi tạo bảng với model đã cấu hình cột
        add(new JScrollPane(table), BorderLayout.CENTER); // Bọc bảng trong JScrollPane để cuộn khi dữ liệu dài

        // Khi chọn một dòng trên bảng -> tự động điền dữ liệu dòng đó lên các ô nhập
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) { // Chỉ xử lý khi trạng thái chọn dòng đã ổn định
                int row = table.getSelectedRow(); // Lấy chỉ số dòng đang được chọn
                if (row >= 0) {
                    // Đổ giá trị từng cột của dòng được chọn vào các ô tương ứng
                    txtMaSV.setText(model.getValueAt(row, 0).toString());
                    txtMaMH.setText(model.getValueAt(row, 1).toString());
                    txtCC.setText(model.getValueAt(row, 2).toString());
                    txtGK.setText(model.getValueAt(row, 3).toString());
                    txtCK.setText(model.getValueAt(row, 4).toString());
                    txtTB.setText(model.getValueAt(row, 5).toString());
                }
            }
        });

        // Gắn sự kiện cho các nút chức năng và phím Enter trong ô tìm kiếm
        btnThem.addActionListener(e -> themDiem()); // Bấm "Them" -> gọi themDiem()
        btnSua.addActionListener(e -> suaDiem()); // Bấm "Sua" -> gọi suaDiem()
        btnXoa.addActionListener(e -> xoaDiem()); // Bấm "Xoa" -> gọi xoaDiem()
        btnTimKiem.addActionListener(e -> timKiemDiem()); // Bấm "Tim kiem" -> gọi timKiemDiem()
        txtTimKiem.addActionListener(e -> timKiemDiem()); // Nhấn Enter trong ô tìm kiếm -> gọi timKiemDiem()
        btnLamMoi.addActionListener(e -> {
            // Xóa trắng toàn bộ ô nhập/tìm kiếm và tải lại danh sách điểm từ CSDL
            txtMaSV.setText("");
            txtMaMH.setText("");
            txtCC.setText("");
            txtGK.setText("");
            txtCK.setText("");
            txtTB.setText("");
            txtTimKiem.setText("");
            loadData(); // Làm mới bảng hiển thị
        });
    }

    // Làm tròn số thực double tới 2 chữ số thập phân để tránh sai số dấu phẩy động
    private double lamTron2ChuSo(double val) {
        return Math.round(val * 100.0) / 100.0;
    }

    // Kiểm tra và lấy 3 đầu điểm hợp lệ (không rỗng, đúng kiểu số thực, nằm trong đoạn [0, 10])
    private double[] layVaKiemTraDiem() {
        String sCC = txtCC.getText().trim(); // Đọc chuỗi điểm chuyên cần và cắt khoảng trắng thừa
        String sGK = txtGK.getText().trim(); // Đọc chuỗi điểm giữa kỳ và cắt khoảng trắng thừa
        String sCK = txtCK.getText().trim(); // Đọc chuỗi điểm cuối kỳ và cắt khoảng trắng thừa

        // Báo lỗi nếu bỏ trống bất kỳ đầu điểm nào
        if (sCC.isEmpty() || sGK.isEmpty() || sCK.isEmpty()) {
            throw new IllegalArgumentException("Vui long nhap day du diem Chuyen can, Giua ky va Cuoi ky!");
        }

        // Chuyển chuỗi sang số thực double (ném NumberFormatException nếu nhập chữ sai định dạng)
        double cc = Double.parseDouble(sCC);
        double gk = Double.parseDouble(sGK);
        double ck = Double.parseDouble(sCK);

        // Kiểm tra điểm thành phần phải thuộc thang điểm [0, 10]
        if (cc < 0 || cc > 10 || gk < 0 || gk > 10 || ck < 0 || ck > 10) {
            throw new IllegalArgumentException("Diem thanh phan phai nam trong khoang tu 0 den 10!");
        }

        return new double[]{cc, gk, ck}; // Trả về mảng 3 điểm hợp lệ
    }

    // Đổ dữ liệu điểm từ ResultSet lên bảng, đồng thời làm tròn 2 chữ số cho các cột điểm hiển thị
    private void hienThiKetQua(PreparedStatement ps) throws SQLException {
        model.setRowCount(0); // Xóa các dòng cũ trên bảng trước khi nạp dữ liệu mới
        try (ResultSet rs = ps.executeQuery()) { // Thực thi truy vấn và tự động đóng ResultSet
            while (rs.next()) { // Duyệt từng dòng kết quả trả về từ SQL Server
                model.addRow(new Object[]{
                    rs.getString("MaSV"), // Cột mã sinh viên
                    rs.getString("MaMH"), // Cột mã môn học
                    lamTron2ChuSo(rs.getDouble("DiemChuyenCan")), // Điểm chuyên cần
                    lamTron2ChuSo(rs.getDouble("DiemGiuaKy")), // Điểm giữa kỳ
                    lamTron2ChuSo(rs.getDouble("DiemCuoiKy")), // Điểm cuối kỳ
                    lamTron2ChuSo(rs.getDouble("DiemTrungBinh")) // Điểm trung bình đã tính từ CSDL
                });
            }
        }
    }

    // Truy vấn và hiển thị lại điểm trung bình đã tính từ SQL Server lên ô txtTB
    private void capNhatOTrungBinhTuCSDL(String maSV, String maMH) {
        String sql = "SELECT ROUND(DiemTrungBinh, 2) AS DiemTB FROM Diem WHERE MaSV = ? AND MaMH = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSV); // Gán mã sinh viên vào tham số thứ 1
            ps.setString(2, maMH); // Gán mã môn học vào tham số thứ 2
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Đưa giá trị DiemTrungBinh thực tế từ CSDL lên ô hiển thị txtTB
                    txtTB.setText(String.valueOf(rs.getDouble("DiemTB")));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace(); // In lỗi ra console nếu truy vấn đọc lại điểm TB gặp sự cố
        }
    }

    // Tải toàn bộ bản ghi từ bảng Diem trong SQL Server
    private void loadData() {
        String sql = "SELECT MaSV, MaMH, DiemChuyenCan, DiemGiuaKy, DiemCuoiKy, DiemTrungBinh FROM Diem";
        try (Connection conn = DBConnection.getConnection(); // Mở kết nối tới SQL Server
             PreparedStatement ps = conn.prepareStatement(sql)) { // Chuẩn bị câu lệnh SELECT
            hienThiKetQua(ps); // Đổ kết quả truy vấn lên bảng
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Loi tai du lieu Diem: " + ex.getMessage());
        }
    }

    // Tìm kiếm điểm theo MaSV hoặc MaMH với từ khóa gần đúng LIKE
    private void timKiemDiem() {
        String key = txtTimKiem.getText().trim(); // Lấy từ khóa tìm kiếm
        if (key.isEmpty()) { // Nếu ô tìm kiếm trống thì tải lại toàn bộ danh sách
            loadData();
            return;
        }
        // Truy vấn các bản ghi có MaSV hoặc MaMH chứa từ khóa tìm kiếm
        String sql = "SELECT MaSV, MaMH, DiemChuyenCan, DiemGiuaKy, DiemCuoiKy, DiemTrungBinh FROM Diem "
                   + "WHERE MaSV LIKE ? OR MaMH LIKE ?";
        String like = "%" + key + "%"; // Tạo mẫu tìm kiếm tương đối
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, like); // Gán mẫu tìm kiếm cho điều kiện MaSV
            ps.setString(2, like); // Gán mẫu tìm kiếm cho điều kiện MaMH
            hienThiKetQua(ps); // Hiển thị các dòng tìm được lên bảng
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Loi tim kiem Diem: " + ex.getMessage());
        }
    }

    // Thêm bản ghi điểm mới (không ghi vào DiemTrungBinh vì cột này do SQL Server tự động tính)
    private void themDiem() {
        String maSV = txtMaSV.getText().trim(); // Lấy mã sinh viên
        String maMH = txtMaMH.getText().trim(); // Lấy mã môn học

        // Kiểm tra mã sinh viên và mã môn học không được để trống
        if (maSV.isEmpty() || maMH.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui long nhap Ma SV va Ma MH!");
            return;
        }

        double[] diem;
        try {
            diem = layVaKiemTraDiem(); // Kiểm tra và lấy mảng [CC, GK, CK]
        } catch (NumberFormatException ex) {
            // Báo lỗi nếu người dùng nhập điểm không phải định dạng số thực hợp lệ
            JOptionPane.showMessageDialog(this, "Cac cot diem phai nhap so hop le!");
            return;
        } catch (IllegalArgumentException ex) {
            // Báo lỗi nếu thiếu đầu điểm hoặc điểm ngoài khoảng [0, 10]
            JOptionPane.showMessageDialog(this, ex.getMessage());
            return;
        }

        // Chỉ chèn 5 cột nhập liệu; DiemTrungBinh do SQL Server tự tính toán
        String sql = "INSERT INTO Diem(MaSV, MaMH, DiemChuyenCan, DiemGiuaKy, DiemCuoiKy) VALUES(?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSV); // Gán MaSV
            ps.setString(2, maMH); // Gán MaMH
            ps.setDouble(3, diem[0]); // Gán điểm chuyên cần
            ps.setDouble(4, diem[1]); // Gán điểm giữa kỳ
            ps.setDouble(5, diem[2]); // Gán điểm cuối kỳ
            ps.executeUpdate(); // Thực thi câu lệnh INSERT vào bảng Diem
            loadData(); // Làm mới bảng hiển thị
            capNhatOTrungBinhTuCSDL(maSV, maMH); // Đọc điểm TB vừa được SQL Server tính để hiện lên txtTB
            JOptionPane.showMessageDialog(this, "Them diem thanh cong!");
        } catch (SQLException ex) {
            // Xảy ra nếu trùng cặp khóa chính (MaSV, MaMH) hoặc MaSV/MaMH chưa tồn tại ở bảng cha
            JOptionPane.showMessageDialog(this, "Loi them Diem: " + ex.getMessage());
        }
    }

    // Sửa các cột điểm thành phần theo cặp khóa MaSV + MaMH (SQL Server tự tính lại DiemTrungBinh)
    private void suaDiem() {
        String maSV = txtMaSV.getText().trim(); // Lấy mã sinh viên cần sửa điểm
        String maMH = txtMaMH.getText().trim(); // Lấy mã môn học cần sửa điểm

        // Kiểm tra đã có mã sinh viên và mã môn học để xác định bản ghi cần cập nhật chưa
        if (maSV.isEmpty() || maMH.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui long chon hoac nhap Ma SV va Ma MH can sua!");
            return;
        }

        double[] diem;
        try {
            diem = layVaKiemTraDiem(); // Kiểm tra và lấy mảng [CC, GK, CK] mới
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Cac cot diem phai nhap so hop le!");
            return;
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
            return;
        }

        // Chỉ cập nhật 3 cột điểm thành phần; cột DiemTrungBinh tự cập nhật theo công thức trong CSDL
        String sql = "UPDATE Diem SET DiemChuyenCan = ?, DiemGiuaKy = ?, DiemCuoiKy = ? WHERE MaSV = ? AND MaMH = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, diem[0]); // Điểm chuyên cần mới
            ps.setDouble(2, diem[1]); // Điểm giữa kỳ mới
            ps.setDouble(3, diem[2]); // Điểm cuối kỳ mới
            ps.setString(4, maSV); // Điều kiện lọc theo MaSV
            ps.setString(5, maMH); // Điều kiện lọc theo MaMH
            int rows = ps.executeUpdate(); // Thực thi UPDATE và lấy số dòng bị ảnh hưởng
            if (rows > 0) {
                loadData(); // Làm mới bảng hiển thị
                capNhatOTrungBinhTuCSDL(maSV, maMH); // Cập nhật lại điểm TB mới lên ô txtTB
                JOptionPane.showMessageDialog(this, "Cap nhat diem thanh cong!");
            } else {
                JOptionPane.showMessageDialog(this, "Khong tim thay ban ghi diem de cap nhat!");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Loi sua Diem: " + ex.getMessage());
        }
    }

    // Xóa bản ghi điểm theo cặp khóa MaSV + MaMH
    private void xoaDiem() {
        String maSV = txtMaSV.getText().trim(); // Lấy mã sinh viên cần xóa điểm
        String maMH = txtMaMH.getText().trim(); // Lấy mã môn học cần xóa điểm

        // Kiểm tra đã chọn hoặc nhập đủ cặp khóa MaSV và MaMH chưa
        if (maSV.isEmpty() || maMH.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui long chon hoac nhap Ma SV va Ma MH can xoa!");
            return;
        }

        // Hỏi xác nhận trước khi xóa dữ liệu khỏi bảng Diem
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Ban co chac chan muon xoa diem cua SV [" + maSV + "] mon [" + maMH + "]?",
                "Xac nhan xoa",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) {
            return; // Hủy thao tác nếu người dùng không xác nhận
        }

        String sql = "DELETE FROM Diem WHERE MaSV = ? AND MaMH = ?"; // Câu lệnh xóa bản ghi điểm
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSV); // Gán MaSV vào điều kiện xóa
            ps.setString(2, maMH); // Gán MaMH vào điều kiện xóa
            int rows = ps.executeUpdate(); // Thực thi DELETE
            if (rows > 0) {
                loadData(); // Tải lại danh sách điểm sau khi xóa
                JOptionPane.showMessageDialog(this, "Xoa diem thanh cong!");
            } else {
                JOptionPane.showMessageDialog(this, "Khong tim thay ban ghi diem de xoa!");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Loi xoa Diem: " + ex.getMessage());
        }
    }
}