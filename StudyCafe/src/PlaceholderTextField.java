import javax.swing.*;
import java.awt.*;

// 플레이스 홀더 클래스

public class PlaceholderTextField extends JTextField {
    private String placeholder;

    public PlaceholderTextField(String placeholder) { // 문자열 생성
        this.placeholder = placeholder;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (getText().isEmpty() && placeholder != null) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(Color.GRAY); // 플레이스홀더 텍스트 색상
            g2.setFont(getFont());

            // 텍스트 좌표 계산
            int x = getInsets().left; // 왼쪽 여백
            int y = (getHeight() - g.getFontMetrics().getHeight()) / 2 + g.getFontMetrics().getAscent(); // 세로 중앙 정렬

            // 텍스트 그리기
            g2.drawString(placeholder, x, y);
            g2.dispose();
        }
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
        repaint();
    }

    public String getPlaceholder() {
        return placeholder;
    }
}