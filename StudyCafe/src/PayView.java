import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Date;

public class PayView extends JFrame {
    private JButton[] timeButtons;
    private JLabel priceLabel;
    private JButton payButton;
    private int selectedPrice = 0;
    private int seatNumber;
    private String userName;

    public PayView(String userName, int seatNumber) {
        this.userName = userName;
        this.seatNumber = seatNumber;
        
        // 폰트 설정
        Font font = new Font("배달의민족 주아", Font.PLAIN, 27);
        
        // 배경색 설정
        getContentPane().setBackground(new Color(202, 189, 173));

        setTitle("결제");
        setSize(450, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null); // 레이아웃 사용하지 않음

        // 시간 선택 버튼
        String[] timeOptions = {"1시간", "2시간", "4시간", "6시간", "8시간", "12시간"};
        int[] hoursOptions = {1, 2, 4, 6, 8, 12}; 
        int[] prices = {2000, 3000, 6000, 8000, 9000, 12000};
        timeButtons = new JButton[timeOptions.length];

        for (int i = 0; i < timeOptions.length; i++) {
            int price = prices[i];
            int hours = hoursOptions[i];

            timeButtons[i] = new JButton(timeOptions[i]);
            timeButtons[i].setBackground(new Color(143, 125, 101));
            timeButtons[i].setForeground(Color.WHITE);
            timeButtons[i].setBounds(50 + (i % 2) * 210, 150 + (i / 2) * 70, 120, 50); 
            timeButtons[i].addActionListener(e -> {
                selectedPrice = price;
                priceLabel.setText("결제 금액은 " + selectedPrice + "원 입니다.");
                payButton.putClientProperty("hours", hours); 
            });
            add(timeButtons[i]);
        }

        // 결제 금액 라벨
        priceLabel = new JLabel("시간을 선택해주세요.", JLabel.CENTER);
        priceLabel.setBounds(20, 400, 410, 50);
        priceLabel.setFont(new Font("배달의민족 주아", Font.PLAIN, 20));
        add(priceLabel);

        // 결제 버튼
        payButton = new JButton("결제");
        payButton.setFont(font);
        payButton.setBackground(new Color(143, 125, 101));
        payButton.setForeground(Color.WHITE);
        payButton.setBounds(150, 500, 150, 50);
        payButton.addActionListener(e -> makePayment());
        add(payButton);
    }

    private void makePayment() {
        if (selectedPrice == 0) {
            JOptionPane.showMessageDialog(this, "이용 시간을 선택하세요.");
            return;
        }

        int hours = (int) payButton.getClientProperty("hours");
        Timestamp startTime = new Timestamp(System.currentTimeMillis());
        Timestamp endTime = new Timestamp(System.currentTimeMillis() + hours * 3600 * 1000);

        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/studycafedb", "root", "1234")) {
            // 좌석 상태 업데이트
            PreparedStatement psRoom = con.prepareStatement(
                "UPDATE room SET status = '이용중', remaining_time = ?, end_time = ?, user_name = ? WHERE seat_number = ?");
            psRoom.setString(1, hours * 60 + "분 남음");
            psRoom.setTimestamp(2, endTime);
            psRoom.setString(3, userName);
            psRoom.setInt(4, seatNumber);
            psRoom.executeUpdate();

            // 이용 기록 추가
            PreparedStatement psHistory = con.prepareStatement(
                "INSERT INTO history (seat_number, user_name, start_time, end_time) VALUES (?, ?, ?, ?)");
            psHistory.setInt(1, seatNumber);
            psHistory.setString(2, userName);
            psHistory.setTimestamp(3, startTime);
            psHistory.setTimestamp(4, endTime);
            psHistory.executeUpdate();

            // 매출 기록 추가
            PreparedStatement psSales = con.prepareStatement(
                "INSERT INTO sales (date, amount) VALUES (CURDATE(), ?)");
            psSales.setInt(1, selectedPrice);
            psSales.executeUpdate();

            JOptionPane.showMessageDialog(this, "결제가 완료되었습니다!");
            new RoomView(userName).setVisible(true);
            dispose();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "결제 처리 중 오류가 발생했습니다.");
        }
    }
}