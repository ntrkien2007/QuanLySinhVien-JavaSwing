package connect;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    // Chuỗi kết nối JDBC tới SQL Server Express trên máy local
    private static final String URL =
            "jdbc:sqlserver://localhost\\SQLEXPRESS;" // Địa chỉ máy chủ và tên instance SQL Server Express
            + "databaseName=QuanLySinhVien;" // Tên cơ sở dữ liệu cần kết nối
            + "integratedSecurity=true;" // Xác thực qua tài khoản Windows hiện tại (Windows Authentication)
            + "encrypt=true;" // Bật mã hóa kết nối
            + "trustServerCertificate=true;"; // Tin cậy chứng chỉ tự ký của server local để tránh lỗi SSL

    public static Connection getConnection() throws SQLException { // Hàm static trả về kết nối cho các Form sử dụng
        try {
            // Nạp tường minh lớp Driver JDBC của SQL Server
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException ex) {
            // Báo lỗi rõ ràng nếu thiếu file thư viện (.jar) SQL Server JDBC trong project
            throw new SQLException("Khong tim thay driver MSSQL JDBC", ex);
        }
        return DriverManager.getConnection(URL); // Tạo và trả về đối tượng Connection tới CSDL
    }

    public static void main(String[] args) { // Hàm main dùng để test nhanh kết nối CSDL độc lập
        try (Connection conn = getConnection()) { // Tự động đóng kết nối sau khi chạy xong khối try
            System.out.println("Ket noi thanh cong toi QuanLySinhVien!");
        } catch (SQLException ex) {
            System.out.println("Loi ket noi: " + ex.getMessage());
        }
    }
}