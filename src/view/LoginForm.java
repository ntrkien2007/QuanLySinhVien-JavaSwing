package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * Form đăng nhập hệ thống cho ứng dụng Quản Lý Sinh Viên (Java Swing).
 * Khi xác thực thành công sẽ mở màn hình chính MainForm và đóng form đăng nhập.
 */
public class LoginForm extends JFrame {

    // Khai báo các thành phần giao diện chính trên form
    private JTextField txtUsername;      // Ô nhập tên đăng nhập
    private JPasswordField txtPassword;  // Ô nhập mật khẩu (ẩn ký tự)
    private JButton btnLogin;            // Nút đăng nhập
    private JButton btnExit;             // Nút thoát chương trình

    // Hàm khởi tạo cửa sổ đăng nhập
    public LoginForm() {
        setTitle("Đăng Nhập Hệ Thống - Quản Lý Sinh Viên");
        setSize(420, 260);                     // Kích thước cửa sổ (rộng x cao)
        setLocationRelativeTo(null);           // Căn cửa sổ ra giữa màn hình
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Tắt hẳn ứng dụng khi bấm nút đóng cửa sổ
        initComponents();                      // Dựng giao diện và gán sự kiện
    }

    // Thiết lập bố cục, màu sắc, phông chữ và sự kiện cho các thành phần
    private void initComponents() {
        // Panel tổng chia theo BorderLayout (tiêu đề ở trên, khu vực nhập liệu ở giữa)
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(new Color(235, 238, 242));

        // Tiêu đề form đăng nhập
        JLabel lblTitle = new JLabel("ĐĂNG NHẬP HỆ THỐNG", JLabel.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(new Color(27, 54, 93));
        mainPanel.add(lblTitle, BorderLayout.NORTH);

        // Panel chứa các nhãn, ô nhập và nút bấm theo lưới GridBagLayout
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(235, 238, 242));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10); // Khoảng cách lề xung quanh mỗi ô
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Nhãn và ô nhập Tên đăng nhập (hàng 0)
        JLabel lblUser = new JLabel("Tên đăng nhập:");
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 13));
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(lblUser, gbc);

        txtUsername = new JTextField("admin", 18); // Điền sẵn "admin" để thao tác nhanh
        txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridx = 1;
        gbc.gridy = 0;
        formPanel.add(txtUsername, gbc);

        // Nhãn và ô nhập Mật khẩu (hàng 1)
        JLabel lblPass = new JLabel("Mật khẩu:");
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 13));
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(lblPass, gbc);

        txtPassword = new JPasswordField("123456", 18); // Điền sẵn mật khẩu mặc định
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridx = 1;
        gbc.gridy = 1;
        formPanel.add(txtPassword, gbc);

        // Panel chứa 2 nút Đăng nhập và Thoát (hàng 2, gộp 2 cột)
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(235, 238, 242));

        btnLogin = new JButton("Đăng nhập");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnLogin.setBackground(new Color(27, 54, 93));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);

        btnExit = new JButton("Thoát");
        btnExit.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnExit.setFocusPainted(false);

        buttonPanel.add(btnLogin);
        buttonPanel.add(btnExit);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        formPanel.add(buttonPanel, gbc);

        // Gắn formPanel vào giữa mainPanel và thêm mainPanel vào JFrame
        mainPanel.add(formPanel, BorderLayout.CENTER);
        add(mainPanel);

        // Bắt sự kiện click nút Đăng nhập, nút Thoát và phím Enter mặc định
        btnLogin.addActionListener(e -> handleLogin());
        btnExit.addActionListener(e -> System.exit(0));
        getRootPane().setDefaultButton(btnLogin);
    }

    // Xử lý kiểm tra thông tin đăng nhập khi người dùng bấm nút "Đăng nhập" hoặc nhấn Enter
    private void handleLogin() {
        // Lấy dữ liệu người dùng nhập và loại bỏ khoảng trắng thừa hai đầu
        String user = txtUsername.getText().trim();
        String pass = new String(txtPassword.getPassword()).trim();

        // Kiểm tra trường hợp để trống tên đăng nhập hoặc mật khẩu
        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu!");
            return;
        }

        // Đối chiếu tài khoản mặc định (admin / 123456)
        if ("admin".equals(user) && "123456".equals(pass)) {
            JOptionPane.showMessageDialog(this, "Đăng nhập thành công!");
            new MainForm().setVisible(true); // Mở giao diện chính của phần mềm
            this.dispose();                  // Đóng cửa sổ đăng nhập hiện tại
        } else {
            // Báo lỗi nếu sai thông tin đăng nhập
            JOptionPane.showMessageDialog(
                this,
                "Sai tên đăng nhập hoặc mật khẩu! (Mặc định: admin / 123456)",
                "Lỗi đăng nhập",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // Hàm main để chạy thử trực tiếp LoginForm trên luồng giao diện Swing (EDT)
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
    }
}