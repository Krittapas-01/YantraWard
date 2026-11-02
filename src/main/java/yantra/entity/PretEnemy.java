package yantra.entity;

import java.awt.*;
import java.util.List;
import yantra.util.Vec2;

/** ผีเปรต — ศัตรูพื้นฐาน ตัวสูงผอม ปากเท่ารูเข็ม เดินตามเส้นทางปกติ */
public class PretEnemy extends Enemy {

    private final boolean small;

    public PretEnemy(List<Vec2<Integer>> path, double scale) {
        this(path, scale, false);
    }

    /** Overloading: สร้างเปรตตัวเล็ก (เกิดจากผีกองกอยแตกร่าง) */
    public PretEnemy(List<Vec2<Integer>> path, double scale, boolean small) {
        super(path, scale, small ? 22 : 60, small ? 64 : 52, small ? 2 : 6);
        this.small = small;
    }

    @Override public String  getThaiName() { return small ? "เปรตน้อย" : "ผีเปรต"; }
    @Override public double  getRadius()   { return small ? 6 : 9; }
    @Override public boolean isFlying()    { return false; }

    @Override protected void renderBody(Graphics2D g, double cx, double cy) {
        double s = small ? 0.7 : 1.0;
        double bob = Math.sin(age * 6) * 1.2;
        int top = (int) (cy - 20 * s + bob);

        g.setStroke(new BasicStroke((float) (1.6 * s)));
        g.setColor(new Color(0x8C9585));
        g.drawLine((int) (cx - 4 * s), (int) (cy - 10 * s + bob), (int) (cx - 9 * s), (int) (cy + 6 * s));
        g.drawLine((int) (cx + 4 * s), (int) (cy - 10 * s + bob), (int) (cx + 9 * s), (int) (cy + 6 * s));
        g.setStroke(new BasicStroke(1f));

        g.setColor(new Color(0xA7B09F));
        g.fillOval((int) (cx - 4.5 * s), top + (int) (7 * s), (int) (9 * s), (int) (24 * s));
        g.setColor(new Color(0xC4CCBA));
        g.fillOval((int) (cx - 4.5 * s), top, (int) (9 * s), (int) (10 * s));

        g.setColor(new Color(0x2A2A28));
        g.fillOval((int) (cx - 2.5 * s), top + (int) (3 * s), 2, 2);
        g.fillOval((int) (cx + 1 * s), top + (int) (3 * s), 2, 2);
        g.fillRect((int) cx, top + (int) (7 * s), 1, 1);
    }
}
