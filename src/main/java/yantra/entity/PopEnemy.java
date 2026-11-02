package yantra.entity;

import java.awt.*;
import java.util.List;
import yantra.util.Vec2;

/** ผีปอบ — เลือดหนา เดินช้า และฟื้นเลือดเองได้ (โอเวอร์ไรด์ update) */
public class PopEnemy extends Enemy {

    private static final double REGEN_RATE = 0.025;   // ฟื้น 2.5% ของเลือดสูงสุดต่อวินาที

    public PopEnemy(List<Vec2<Integer>> path, double scale) {
        super(path, scale, 240, 30, 24);
    }

    @Override protected void update(double dt) {
        super.update(dt);
        if (alive && hp < maxHp) hp = Math.min(maxHp, hp + maxHp * REGEN_RATE * dt);
    }

    @Override public String  getThaiName() { return "ผีปอบ"; }
    @Override public double  getRadius()   { return 12; }
    @Override public boolean isFlying()    { return false; }

    @Override protected void renderBody(Graphics2D g, double cx, double cy) {
        double sway = Math.sin(age * 3) * 1.5;
        if (hp < maxHp) {
            g.setColor(new Color(120, 220, 120, 40 + (int) (30 * Math.abs(Math.sin(age * 4)))));
            g.fillOval((int) cx - 16, (int) cy - 16, 32, 32);
        }
        g.setColor(new Color(0x3F3A2E));
        g.fillOval((int) (cx - 12 + sway), (int) cy - 10, 24, 20);
        g.setColor(new Color(0x544C3B));
        g.fillOval((int) (cx - 7 + sway), (int) cy - 15, 14, 12);
        g.setColor(new Color(0x2A2620));
        g.setStroke(new BasicStroke(2f));
        g.drawLine((int) (cx - 11 + sway), (int) cy + 2, (int) (cx - 15), (int) cy + 9);
        g.drawLine((int) (cx + 11 + sway), (int) cy + 2, (int) (cx + 15), (int) cy + 9);
        g.setStroke(new BasicStroke(1f));
        g.setColor(new Color(0xFF3A3A));
        g.fillOval((int) (cx - 4 + sway), (int) cy - 11, 3, 3);
        g.fillOval((int) (cx + 2 + sway), (int) cy - 11, 3, 3);
    }
}
