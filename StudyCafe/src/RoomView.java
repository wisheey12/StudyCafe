import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import java.awt.*;
import java.sql.*;
import java.util.Timer;
import java.util.TimerTask;


// 좌석 대여 클래스
public class RoomView extends JFrame {
    private JTable seatTable;
    private DefaultTableModel tableModel;
    private String loggedInUser;

    public RoomView(String userName) {
        this.loggedInUser = userName;

        setTitle("좌석 현황");
        setSize(450, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 배경 설정
        getContentPane().setBackground(new Color(202, 189, 173));
        setLayout(null);
        
        // 폰트 설정
        Font font = new Font("배달의민족 주아", Font.PLAIN, 18);

        // 좌석 테이블 설정
        String[] columnNames = {"좌석번호", "상태", "남은 시간"};
        tableModel = new DefaultTableModel(null, columnNames);
        seatTable = new JTable(tableModel);
        
        // 테이블 헤더 생성 및 추가
        JTableHeader tableHeader = seatTable.getTableHeader();
        tableHeader.setBounds(0, 0, 450, 30); 
        tableHeader.setBackground(new Color(143, 125, 101));
        tableHeader.setForeground(Color.WHITE); 
        tableHeader.setFont(font);
        add(tableHeader);

        // 테이블 크기 설정
        seatTable.setBounds(0, 30, 450, 450); 
        seatTable.setRowHeight(30);
        seatTable.setFont(font);

        // 좌석의 이용 가능 여부 상태 표시
        DefaultTableCellRenderer statusRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value != null) {
                    String status = value.toString();
                    if ("이용가능".equals(status)) {
                        cell.setForeground(Color.BLUE);
                    } else if ("이용중".equals(status)) {
                        cell.setForeground(Color.RED);
                    } else {
                        cell.setForeground(Color.BLACK);
                    }
                }
                return cell;
            }
        };

        seatTable.getColumnModel().getColumn(1).setCellRenderer(statusRenderer);
        add(seatTable);

        loadSeatData();
        startSeatStatusUpdater();

        // 좌석 선택 이벤트
        seatTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = seatTable.getSelectedRow();
                String status = (String) tableModel.getValueAt(row, 1);
                int seatNumber = (int) tableModel.getValueAt(row, 0);

                if ("이용가능".equals(status)) {
                    new PayView(loggedInUser, seatNumber).setVisible(true);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(null, "이미 이용중인 좌석입니다.");
                }
            }
        });


        // 건의사항 버튼
        JButton feedbackButton = new JButton("건의사항");
        feedbackButton.setFont(font);
        feedbackButton.setBackground(new Color(143, 125, 101));
        feedbackButton.setForeground(Color.WHITE);
        feedbackButton.setBounds(10, 650, 100, 40); 
        feedbackButton.addActionListener(e -> openFeedbackDialog()); //
        add(feedbackButton);
        
        // 로그아웃 버튼
        JButton logoutButton = new JButton("로그아웃");
        logoutButton.setFont(font);
        logoutButton.setBackground(new Color(143, 125, 101));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setBounds(170, 650, 100, 40);
        logoutButton.addActionListener(e -> logout());
        add(logoutButton);
        
        // 기기 대여 버튼
        JButton deviceButton = new JButton("기기 대여");
        deviceButton.setFont(font);
        deviceButton.setBackground(new Color(143, 125, 101));
        deviceButton.setForeground(Color.WHITE);
        deviceButton.setBounds(330, 650, 100, 40);
        deviceButton.addActionListener(e -> new DeviceView(loggedInUser)); // DeviceView로 이동
        add(deviceButton);
    }
    
    
    // 건의사항 버튼 클릭 시
    private void openFeedbackDialog() {
        JDialog dialog = new JDialog(this, "건의사항 전송", true);
        dialog.setSize(300, 200);
        dialog.setLayout(null);

        PlaceholderTextField messageField = new PlaceholderTextField("메시지는 익명으로 전송됩니다"); // 플레이스 홀더 클래스
        messageField.setBounds(20, 20, 260, 40);
        dialog.add(messageField);

        JButton sendButton = new JButton("전송");
        sendButton.setFont(new Font("배달의민족 주아", Font.PLAIN, 15));
        sendButton.setBackground(new Color(143, 125, 101));
        sendButton.setForeground(Color.WHITE);
        sendButton.setBounds(100, 100, 100, 40); 
        sendButton.addActionListener(e -> {
            String message = messageField.getText();
            if (message.isEmpty() || message.equals("메시지는 익명으로 전송됩니다")) {
                JOptionPane.showMessageDialog(dialog, "메시지를 입력해주세요.");
                return;
            }

            // 입력 메시지를 데이터베이스에 저장
            try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/studycafedb", "root", "1234");
                 PreparedStatement ps = con.prepareStatement("INSERT INTO feedback (date, message) VALUES (?, ?)")) {

                ps.setDate(1, new java.sql.Date(System.currentTimeMillis()));
                ps.setString(2, message);
                ps.executeUpdate();

                JOptionPane.showMessageDialog(dialog, "전송이 완료되었습니다!");
                dialog.dispose();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "전송 중 오류가 발생했습니다.");
            }
        });

        dialog.add(sendButton);
        dialog.setVisible(true);
    }
    
    // 로그아웃 버튼 클릭 시
    private void logout() {
        new LoginView(); // LoginView로 이동
        dispose();
        JOptionPane.showMessageDialog(this, "로그아웃 되었습니다.");
    }

    
    // 좌석 데이터를 가져와 테이블 업데이트
    private void loadSeatData() {
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/studycafedb", "root", "1234");
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM room")) {

            tableModel.setRowCount(0); // 테이블 초기화
            while (rs.next()) {
                String remainingTime = calculateRemainingTime(rs.getTimestamp("end_time"));
                tableModel.addRow(new Object[]{
                        rs.getInt("seat_number"),
                        rs.getString("status"),
                        remainingTime
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private String calculateRemainingTime(Timestamp endTime) {
        if (endTime == null) return "";
        long currentTime = System.currentTimeMillis();
        long diff = endTime.getTime() - currentTime;

        if (diff <= 0) {
            updateSeatToAvailable(endTime);
            return "";
        }

        long minutes = diff / (60 * 1000);
        return minutes + "분 남음";
    }

    private void updateSeatToAvailable(Timestamp endTime) {
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/studycafedb", "root", "1234");
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE room SET status = '이용가능', remaining_time = NULL, end_time = NULL, user_name = NULL WHERE end_time = ?")) {

            ps.setTimestamp(1, endTime);
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void startSeatStatusUpdater() {
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> loadSeatData());
            }
        }, 0, 60 * 1000); // 1분마다 실행
    }
}
