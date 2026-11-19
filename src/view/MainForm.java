package view;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

public class MainForm extends JFrame { // Kế thừa JFrame để tạo cửa sổ chính của ứng dụng

    public MainForm() {
        initUI(); // Khởi tạo giao diện chính
    }

    private void initUI() {
        setTitle("Quan Ly Sinh Vien");
        setSize(540, 240);
        setLocationRelativeTo(null); // Căn giữa màn hình
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Đóng cửa sổ chính sẽ thoát toàn bộ chương trình
        setLayout(new BorderLayout(10, 10));

        // Nhãn tiêu đề phía trên cửa sổ chính
        JLabel lblTitle = new JLabel("QUAN LY SINH VIEN - MON HOC - DIEM", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(15, 10, 5, 10));
        add(lblTitle, BorderLayout.NORTH);

        // Panel dạng lưới 1 hàng 3 cột chứa 3 nút điều hướng sang các form quản lý
        JPanel panelBtn = new JPanel(new GridLayout(1, 3, 12, 12));
        panelBtn.setBorder(BorderFactory.createEmptyBorder(20, 25, 30, 25));

        JButton btnSinhVien = new JButton("Quan Ly Sinh Vien");
        JButton btnMonHoc = new JButton("Quan Ly Mon Hoc");
        JButton btnDiem = new JButton("Quan Ly Diem");

        // Mở từng form tương ứng khi người dùng bấm nút
        btnSinhVien.addActionListener(e -> new SinhVienForm().setVisible(true));
        btnMonHoc.addActionListener(e -> new MonHocForm().setVisible(true));
        btnDiem.addActionListener(e -> new DiemForm().setVisible(true));

        panelBtn.add(btnSinhVien);
        panelBtn.add(btnMonHoc);
        panelBtn.add(btnDiem);
        add(panelBtn, BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        // Đưa việc khởi tạo giao diện lên luồng sự kiện Swing (EDT)
        SwingUtilities.invokeLater(() -> new MainForm().setVisible(true));
    }
}