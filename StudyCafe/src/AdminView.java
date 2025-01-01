import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Timer;
import java.util.TimerTask;

public class AdminView extends JFrame {
    private JTable userTable, roomTable;
    private DefaultTableModel userTableModel, roomTableModel;
    private Timer timer;
    private DefaultTableModel salesTableModel;

    //스터디카페 운영 클래스
    public AdminView() {
        setTitle("운영자 시스템");
        setSize(450, 750);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                new LoginView().setVisible(true); // 창을 닫으면 LoginView로 이동
            }
        });

        JTabbedPane tabbedPane = new JTabbedPane();

        // 회원 목록 탭
        JPanel userPanel = new JPanel(new BorderLayout());
        userTableModel = new DefaultTableModel(new String[]{"ID", "Name", "Phone Number", "Role"}, 0);
        userTable = new JTable(userTableModel);
        loadUserTable();
        userPanel.add(new JScrollPane(userTable), BorderLayout.CENTER);
        tabbedPane.addTab("회원 목록", userPanel);

        // 좌석 목록 탭
        JPanel roomPanel = new JPanel(new BorderLayout());
        roomTableModel = new DefaultTableModel(new String[]{"Seat Number", "Status", "Remaining Time", "User Name"}, 0);
        roomTable = new JTable(roomTableModel);
        loadRoomTable();
        roomPanel.add(new JScrollPane(roomTable), BorderLayout.CENTER);
        tabbedPane.addTab("좌석 목록", roomPanel);

        add(tabbedPane);

        startRoomStatusUpdater();
        
        // 건의사항 탭
        JPanel feedbackPanel = new JPanel(new BorderLayout());
        DefaultTableModel feedbackTableModel = new DefaultTableModel(new String[]{"날짜", "메시지"}, 0);
        JTable feedbackTable = new JTable(feedbackTableModel);
        loadFeedbackTable(feedbackTableModel);

        feedbackPanel.add(new JScrollPane(feedbackTable), BorderLayout.CENTER);
        tabbedPane.addTab("건의사항", feedbackPanel);
        
        // 이용 기록 탭
        JPanel historyPanel = new JPanel(new BorderLayout());
        DefaultTableModel historyTableModel = new DefaultTableModel(new String[]{"좌석번호", "사용자 이름", "시작 시간", "종료 시간"}, 0);
        JTable historyTable = new JTable(historyTableModel);
        loadHistoryTable(historyTableModel);
        historyPanel.add(new JScrollPane(historyTable), BorderLayout.CENTER);
        tabbedPane.addTab("이용 기록", historyPanel);
        
        // 일매출 확인 탭
        JPanel salesPanel = new JPanel(new BorderLayout());
        salesTableModel = new DefaultTableModel(new String[]{"날짜", "총 매출"}, 0);
        JTable salesTable = new JTable(salesTableModel);
        salesPanel.add(new JScrollPane(salesTable), BorderLayout.CENTER);

        tabbedPane.addTab("일매출", salesPanel);

        add(tabbedPane);
        loadSalesData();
    }

    // 유저 데이터 불러오기
    private void loadUserTable() {
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/studycafedb", "root", "1234");
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, name, phone_number, role FROM users")) {

            userTableModel.setRowCount(0);
            while (rs.next()) {
                userTableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("phone_number"),
                        rs.getString("role")
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "회원 목록을 불러오는 중 오류가 발생했습니다.");
        }
    }

    // 좌석 데이터 불러오기
    private void loadRoomTable() {
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/studycafedb", "root", "1234");
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT seat_number, status, remaining_time, user_name, end_time FROM room")) {

            roomTableModel.setRowCount(0);
            while (rs.next()) {
                String remainingTime = calculateRemainingTime(rs.getTimestamp("end_time"));
                roomTableModel.addRow(new Object[]{
                        rs.getInt("seat_number"),
                        rs.getString("status"),
                        remainingTime,
                        rs.getString("user_name") != null ? rs.getString("user_name") : ""
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "좌석 목록을 불러오는 중 오류가 발생했습니다.");
        }
    }

    // 좌석 종료시간 계산
    private String calculateRemainingTime(Timestamp endTime) {
        if (endTime == null) return "";
        long currentTime = System.currentTimeMillis();
        long diff = endTime.getTime() - currentTime;

        if (diff <= 0) {
            // 시간이 만료되면 상태를 이용가능으로 변경
            updateSeatToAvailable(endTime);
            return "";
        }

        long minutes = diff / (60 * 1000);
        return minutes + "분 남음";
    }

    // 종료 시간이 되면 좌석을 이용 상태로 변경
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
    
    // 1분마다 좌석 이용 상태 업데이트
    private void startRoomStatusUpdater() {
        timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> loadRoomTable());
            }
        }, 0, 60 * 1000);
    }
    
    //피드백 데이터 로드
    private void loadFeedbackTable(DefaultTableModel model) {
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/studycafedb", "root", "1234");
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT date, message FROM feedback")) {

            model.setRowCount(0); // 테이블 초기화
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getDate("date"),
                        rs.getString("message")
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "건의사항 데이터를 불러오는 중 오류가 발생했습니다.");
        }
    }
    
    // 이용 기록 데이터 로드
    private void loadHistoryTable(DefaultTableModel model) {
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/studycafedb", "root", "1234");
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT seat_number, user_name, start_time, end_time FROM history")) {

            model.setRowCount(0); // 기존 데이터 초기화
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("seat_number"),
                        rs.getString("user_name"),
                        rs.getTimestamp("start_time"),
                        rs.getTimestamp("end_time")
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "이용 기록 데이터를 불러오는 중 오류가 발생했습니다.");
        }
    }
    
    // 일매출 데이터 로드
    private void loadSalesData() {
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/studycafedb", "root", "1234");
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT date, SUM(amount) AS total_sales FROM sales GROUP BY date")) {

            salesTableModel.setRowCount(0); // 테이블 초기화
            while (rs.next()) {
                salesTableModel.addRow(new Object[]{
                        rs.getDate("date"),
                        rs.getInt("total_sales")
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "매출 데이터를 불러오는 중 오류가 발생했습니다.");
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AdminView().setVisible(true));
    }
}