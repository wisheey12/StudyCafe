import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;

// 기기 대여 화면
public class DeviceView extends JFrame {
    private DefaultListModel<String> deviceListModel;
    private JList<String> deviceList;
    private JButton rentButton, returnButton;
    private Set<String> selectedDevices;
    private String loggedInUser;

    public DeviceView(String userName) {
        this.loggedInUser = userName;
        
        // 컨텐트팬 설정
        getContentPane().setBackground(new Color(202, 189, 173));
        setTitle("기기 대여 목록");
        setSize(480, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);

        selectedDevices = new HashSet<>();

        // 제목 설정
        JLabel titleLabel = new JLabel("기기 대여 및 반납", JLabel.CENTER);
        titleLabel.setBounds(10, 10, 480, 30);
        titleLabel.setFont(new Font("배달의민족 주아", Font.BOLD, 23));
        add(titleLabel);

        // 기기 목록
        deviceListModel = new DefaultListModel<>();
        deviceList = new JList<>(deviceListModel);
        deviceList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        deviceList.setFont(new Font("배달의민족 주아", Font.PLAIN, 23));
        deviceList.setBounds(0, 50, 480, 400);
        add(deviceList);

        loadDeviceData();

        // 대여 버튼
        rentButton = new JButton("대여");
        rentButton.setFont(new Font("배달의민족 주아", Font.BOLD, 23));
        rentButton.setBackground(new Color(143, 125, 101));
        rentButton.setForeground(Color.WHITE);
        rentButton.setBounds(100, 470, 100, 40);
        rentButton.addActionListener(e -> rentDevices());
        add(rentButton);

        // 반납 버튼
        returnButton = new JButton("반납");
        returnButton.setFont(new Font("배달의민족 주아", Font.BOLD, 23));
        returnButton.setBackground(new Color(143, 125, 101));
        returnButton.setForeground(Color.WHITE);
        returnButton.setBounds(300, 470, 100, 40);
        returnButton.addActionListener(e -> returnDevices());
        add(returnButton);

        setVisible(true);
    }

    
    // 데이터베이스에서 기기 정보 불러오기    
    private void loadDeviceData() {
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/studycafedb", "root", "1234");
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT device_name, status, user_name FROM device")) {

            deviceListModel.clear();
            while (rs.next()) {
                String deviceName = rs.getString("device_name");
                String status = rs.getString("status");
                String userName = rs.getString("user_name");

                if (userName != null && userName.length() > 1) {
                    userName = userName.charAt(0) + "*" + userName.substring(2); // 이름의 두 번째 글자를 *로 변경
                }

                // 대여 상태 표시
                String displayStatus = status;
                if ("대여 중".equals(status) && userName != null) {
                    displayStatus += " (" + userName + ")";
                }
                deviceListModel.addElement(deviceName + " - " + displayStatus);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "기기 데이터를 불러오는 중 오류가 발생했습니다.");
        }
    }

    // 기기 대여 기능
    private void rentDevices() {
        int[] selectedIndices = deviceList.getSelectedIndices();
        if (selectedIndices.length == 0) {
            JOptionPane.showMessageDialog(this, "대여할 기기를 선택해주세요.");
            return;
        }

        boolean anyRented = false;

        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/studycafedb", "root", "1234")) {
            for (int index : selectedIndices) {
                String selectedDevice = deviceListModel.get(index);
                String deviceName = selectedDevice.split(" - ")[0];
                String status = selectedDevice.split(" - ")[1];

                if (status.startsWith("대여 중")) {
                    JOptionPane.showMessageDialog(this, deviceName + "은(는) 대여 중입니다.");
                    continue;
                }

                try (PreparedStatement ps = con.prepareStatement("UPDATE device SET status = '대여 중', user_name = ? WHERE device_name = ?")) {
                    ps.setString(1, loggedInUser);
                    ps.setString(2, deviceName);
                    ps.executeUpdate();
                    anyRented = true;
                }
            }

            if (anyRented) {
                JOptionPane.showMessageDialog(this, "선택한 기기를 대여했습니다. 빠른 반납 부탁드립니다.");
            }
            loadDeviceData();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "대여 처리 중 오류가 발생했습니다.");
        }
    }

    // 기기 반납 기능
    private void returnDevices() {
        int[] selectedIndices = deviceList.getSelectedIndices();
        if (selectedIndices.length == 0) {
            JOptionPane.showMessageDialog(this, "반납할 기기를 선택해주세요.");
            return;
        }

        boolean anyReturned = false;

        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/studycafedb", "root", "1234")) {
            for (int index : selectedIndices) {
                String selectedDevice = deviceListModel.get(index);
                String deviceName = selectedDevice.split(" - ")[0];

                // 현재 사용자가 대여한 사용자인지 확인
                try (PreparedStatement checkPs = con.prepareStatement(
                        "SELECT user_name FROM device WHERE device_name = ? AND status = '대여 중'")) {
                    checkPs.setString(1, deviceName);
                    ResultSet rs = checkPs.executeQuery();

                    if (rs.next()) {
                        String renter = rs.getString("user_name");
                        if (!loggedInUser.equals(renter)) {
                            JOptionPane.showMessageDialog(this, deviceName + "은(는) 다른 사용자에 의해 대여 중입니다.");
                            continue;
                        }
                    } else {
                        JOptionPane.showMessageDialog(this, deviceName + "은(는) 대여 중 상태가 아닙니다.");
                        continue;
                    }
                }

                // 반납한 기기의 상태 변경
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE device SET status = '대여 가능', user_name = NULL WHERE device_name = ?")) {
                    ps.setString(1, deviceName);
                    ps.executeUpdate();
                    anyReturned = true;
                }
            }

            if (anyReturned) {
                JOptionPane.showMessageDialog(this, "선택한 기기를 반납했습니다.");
            }
            loadDeviceData();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "반납 처리 중 오류가 발생했습니다.");
        }
    }
}