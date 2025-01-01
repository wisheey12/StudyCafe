import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

// 회원가입 클래스
public class SignUpView extends JFrame {
    private JTextField nameField, phoneField;
    private JPasswordField passwordField, confirmPasswordField;

    public SignUpView() {
        setTitle("스터디카페 회원가입");
        setSize(450, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        
        // 폰트 설정
        Font font1 = new Font("배달의민족 주아", Font.PLAIN, 27);
        Font font2 = new Font("배달의민족 주아", Font.PLAIN, 18);
        
        // 배경 이미지
        JLabel background = new JLabel(new ImageIcon("resources/Default.jpg"));
        background.setBounds(0, 0, 450, 750);
        add(background);
        
        // 이름 필드
        JLabel nameLabel = new JLabel("이름");
        nameLabel.setBounds(50, 200, 100, 30);
        nameLabel.setFont(font1); 
        nameLabel.setForeground(new Color(128, 111, 89));
        background.add(nameLabel);

        nameField = new JTextField();
        nameField.setBounds(200, 200, 150, 30);
        nameField.setFont(font2);
        nameField.setForeground(new Color(0, 0, 0));
        background.add(nameField);

        // 핸드폰 번호 필드
        JLabel phoneLabel = new JLabel("핸드폰 번호");
        phoneLabel.setBounds(50, 250, 150, 30);
        phoneLabel.setFont(font1);
        phoneLabel.setForeground(new Color(128, 111, 89)); 
        background.add(phoneLabel);

        phoneField = new JTextField();
        phoneField.setBounds(200, 250, 150, 30);
        phoneField.setFont(font2);
        phoneField.setForeground(new Color(0, 0, 0));
        background.add(phoneField);

        // 비밀번호 필드
        JLabel passwordLabel = new JLabel("비밀번호");
        passwordLabel.setBounds(50, 300, 100, 30);
        passwordLabel.setFont(font1); 
        passwordLabel.setForeground(new Color(128, 111, 89));
        background.add(passwordLabel);

        passwordField = new JPasswordField();
        passwordField.setBounds(200, 300, 150, 30);
        background.add(passwordField);

        // 비밀번호 확인 필드
        JLabel confirmPasswordLabel = new JLabel("비밀번호 확인");
        confirmPasswordLabel.setBounds(50, 350, 150, 30);
        confirmPasswordLabel.setFont(font1); 
        confirmPasswordLabel.setForeground(new Color(128, 111, 89));
        background.add(confirmPasswordLabel);

        confirmPasswordField = new JPasswordField();
        confirmPasswordField.setBounds(200, 350, 150, 30);
        background.add(confirmPasswordField);

        // 회원가입 버튼
        JButton signUpButton = new JButton("회원가입");
        signUpButton.setBounds(150, 500, 150, 40);
        signUpButton.setBackground(new Color(143, 125, 101));
        signUpButton.setForeground(Color.WHITE);
        signUpButton.setFont(font2);
        signUpButton.addActionListener(e -> registerUser());
        background.add(signUpButton);

        setVisible(true);
    }

    private void registerUser() {
        String name = nameField.getText();
        String phone = phoneField.getText();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "비밀번호가 일치하지 않습니다.");
            return;
        }

        try (Connection conn = DBConnector.getConnection()) {
            String query = "INSERT INTO users (name, phone_number, password) VALUES (?, ?, ?)";
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setString(1, name);
            pst.setString(2, phone);
            pst.setString(3, password);

            pst.executeUpdate();
            JOptionPane.showMessageDialog(this, "회원가입이 완료되었습니다.");
            new LoginView();
            dispose();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "이미 존재하는 번호입니다.");
        }
    }
}