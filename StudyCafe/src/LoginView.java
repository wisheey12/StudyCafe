import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class LoginView extends JFrame {
    private JTextField phoneField;
    private JPasswordField passwordField;

    // 로그인 뷰 (기본 실행시 메인 화면)
    public LoginView() {
        setTitle("스터디카페에 어서오세요");
        setSize(450, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        
        // 폰트 설정
        Font font1 = new Font("배달의민족 주아", Font.PLAIN, 27);
        Font font2 = new Font("배달의민족 주아", Font.PLAIN, 18);

        // 배경 이미지
        JLabel background = new JLabel(new ImageIcon("resources/Main.jpg"));
        background.setBounds(0, 0, 450, 750);
        add(background);

        // 핸드폰 번호 필드
        JLabel phoneLabel = new JLabel("핸드폰번호");
        phoneLabel.setBounds(70, 410, 200, 30);
        phoneLabel.setFont(font1); 
        phoneLabel.setForeground(new Color(128, 111, 89)); 
        background.add(phoneLabel);

        phoneField = new JTextField();
        phoneField.setBounds(200, 410, 150, 30); 
        phoneField.setFont(font2);
        phoneField.setForeground(new Color(0, 0, 0));
        background.add(phoneField);

        // 비밀번호 필드
        JLabel passwordLabel = new JLabel("비밀번호");
        passwordLabel.setBounds(93, 450, 200, 30); 
        passwordLabel.setFont(font1); 
        passwordLabel.setForeground(new Color(128, 111, 89)); 
        background.add(passwordLabel);
        
        passwordField = new JPasswordField();
        passwordField.setBounds(200, 450, 150, 30); 
        background.add(passwordField);

        // 로그인 버튼
        JButton loginButton = new JButton("로그인");
        loginButton.setBounds(120, 550, 100, 50); 
        loginButton.setBackground(new Color(143, 125, 101));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("배달의민족 주아", Font.BOLD, 18));
        background.add(loginButton);

        // 회원가입 버튼
        JButton signUpButton = new JButton("회원가입");
        signUpButton.setBounds(240, 550, 100, 50);
        signUpButton.setBackground(new Color(143, 125, 101));
        signUpButton.setForeground(Color.WHITE);
        signUpButton.setFont(new Font("배달의민족 주아", Font.BOLD, 18));
        background.add(signUpButton);

        // 버튼 이벤트
        loginButton.addActionListener(e -> login());
        signUpButton.addActionListener(e -> openSignUpView());
        
        setVisible(true);
    }

    private void login() {
        String phone = phoneField.getText();
        String password = new String(passwordField.getPassword());

        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/studycafedb", "root", "1234");
             PreparedStatement ps = con.prepareStatement("SELECT name, role FROM users WHERE phone_number = ? AND password = ?")) {

            ps.setString(1, phone);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String userName = rs.getString("name");
                String role = rs.getString("role");

                // 사용자에게 환영 메시지 출력
                JOptionPane.showMessageDialog(this, userName + "님, 어서오세요!");

                if ("admin".equals(role)) {
                    // 운영자 화면으로 이동
                    new AdminView().setVisible(true);
                } else {
                    // 사용자 RoomView로 이동
                    new RoomView(userName).setVisible(true);
                }
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "로그인 실패. 정보를 확인해주세요.");
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "데이터베이스 오류 발생!");
        }
    }


    private void openSignUpView() {
        new SignUpView();
    }

    public static void main(String[] args) {
        new LoginView();
    }
}