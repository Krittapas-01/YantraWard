package yantra.entity;

import java.awt.*;
import java.util.List;
import yantra.util.Vec2;

/**
 * ผีกระสือ — ตัวอย่าง Polymorphism ที่ชัดที่สุดในเกม
 * โอเวอร์ไรด์ move() ให้ "บินตรง" เข้าพระอุโบสถแทนการเดินตามเส้นทาง
 */
public class KrasueEnemy extends Enemy {

    private final double goalX;
    private final double goalY;

    public KrasueEnemy(List<Vec2<Integer>> path, double scale) {
        super(path, scale, 32, 64, 11);
        Vec2<Integer> goal = path.get(path.size() - 1);
        this.goalX = tileCenterX(goal);
        this.goalY = tileCenterY(goal);
    }

    @Override protected void move(double dt) {
        double dx = goalX - x, dy = goalY - y;
        double dist = Math.hypot(dx, dy);
        double step = getSpeed() * dt;
        if (dist <= step) { reachedGoal = true; return; }
        x += dx / dist * step;
        y += dy / dist * step;
        traveled += step;
    }

    /** บินเป็นเส้นตรง จึงทำนายตำแหน่งตามแนวตรงไปยังพระอุโบสถ */
    @Override public double[] predictPosition(double t) {
        double dx = goalX - x, dy = goalY - y;
        double dist = Math.hypot(dx, dy);
        double step = Math.min(dist, getSpeed() * t);
        if (dist < 1e-6) return new double[]{x, y};
        return new double[]{x + dx / dist * step, y + dy / dist * step};
    }

    @Override public String  getThaiName() { return "ผีกระสือ"; }
    @Override public double  getRadius()   { return 7; }
    @Override public boolean isFlying()    { return true; }

    @Override protected void renderBody(Graphics2D g, double cx, double cy) {
        // แสงเรือง
        for (int i = 3; i >= 1; i--) {
            g.setColor(new Color(150, 230, 120, 18 * i));
            int rr = 8 + i * 4;
            g.fillOval((int) cx - rr, (int) cy - rr + 4, rr * 2, rr * 2);
        }
        // ไส้ห้อย
        g.setStroke(new BasicStroke(1.6f));
        for (int i = -1; i <= 1; i++) {
            double sway = Math.sin(age * 5 + i) * 2.5;
            g.setColor(i == 0 ? new Color(0xB8324A) : new Color(0x8FD97A));
            g.drawLine((int) (cx + i * 2), (int) cy + 5, (int) (cx + i * 3 + sway), (int) cy + 17);
        }
        g.setStroke(new BasicStroke(1f));
        // หัว
        g.setColor(new Color(0x1A1414));
        g.fillOval((int) cx - 8, (int) cy - 9, 16, 15);
        g.setColor(new Color(0xE3C29E));
        g.fillOval((int) cx - 5, (int) cy - 5, 10, 11);
        g.setColor(new Color(0x1A1414));
        g.fillRect((int) cx - 6, (int) cy - 7, 12, 3);
        g.setColor(new Color(0xC02030));
        g.fillOval((int) cx - 3, (int) cy - 1, 2, 2);
        g.fillOval((int) cx + 1, (int) cy - 1, 2, 2);
    }
}
