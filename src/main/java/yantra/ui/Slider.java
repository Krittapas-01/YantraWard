package yantra.ui;

import java.awt.*;

/** แถบเลื่อนปรับค่า 0–100% (ลากหรือคลิกตรงไหนก็ได้บนแถบ) */
public class Slider {

    private final String label;
    private final Rectangle track;
    private double value;          // 0..1
    private boolean dragging = false;
    private boolean hovered = false;

    public Slider(String label, int x, int y, int width, double value) {
        this.label = label;
        this.track = new Rectangle(x, y, width, 10);
        this.value = Math.max(0, Math.min(1, value));
    }

    /** พื้นที่รับคลิกกว้างกว่าตัวแถบเล็กน้อย จะได้กดง่าย */
    private Rectangle hitArea() {
        return new Rectangle(track.x - 10, track.y - 12, track.width + 20, track.height + 24);
    }

    /** @return true ถ้าค่าเปลี่ยน */
    public boolean press(int x, int y) {
        if (!hitArea().contains(x, y)) return false;
        dragging = true;
        return setFromX(x);
    }

    public boolean drag(int x, int y) {
        return dragging && setFromX(x);
    }

    public void release() { dragging = false; }

    public void hover(int x, int y) { hovered = hitArea().contains(x, y); }

    private boolean setFromX(int x) {
        double v = Math.max(0, Math.min(1, (x - track.x) / (double) track.width));
        v = Math.round(v * 20) / 20.0;          // ขยับทีละ 5%
        if (v == value) return false;
        value = v;
        return true;
    }

    public double getValue()          { return value; }
    public void   setValue(double v)  { value = Math.max(0, Math.min(1, v)); }
    public boolean isDragging()       { return dragging; }

    public void render(Graphics2D g) {
        g.setFont(Fonts.get(15));
        g.setColor(new Color(0xE8D9A8));
        g.drawString(label, track.x, track.y - 16);
        g.setColor(new Color(0xFFD98E));
        Fonts.drawRight(g, Math.round(value * 100) + "%", track.x + track.width, track.y - 16);

        g.setColor(new Color(0x2A251C));
        g.fillRoundRect(track.x, track.y, track.width, track.height, 10, 10);
        g.setColor(value == 0 ? new Color(0x6A5A4A) : new Color(0xD9B24A));
        g.fillRoundRect(track.x, track.y, (int) (track.width * value), track.height, 10, 10);
        g.setColor(new Color(0x5C5240));
        g.drawRoundRect(track.x, track.y, track.width, track.height, 10, 10);

        int kx = track.x + (int) (track.width * value);
        int r = (dragging || hovered) ? 11 : 9;
        g.setColor(new Color(0xFFF0C0));
        g.fillOval(kx - r, track.y + track.height / 2 - r, r * 2, r * 2);
        g.setColor(new Color(0x8A6A20));
        g.drawOval(kx - r, track.y + track.height / 2 - r, r * 2, r * 2);
    }
}
