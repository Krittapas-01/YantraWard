package yantra.entity;

import java.awt.*;
import java.util.List;
import yantra.core.Battlefield;
import yantra.effect.RingEffect;
import yantra.util.Vec2;

/** ผีกองกอย — ขาเดียว กระโดดไปมา เมื่อตายจะแตกออกเป็นเปรตน้อย 2 ตน (โอเวอร์ไรด์ onDeath) */
public class KongkoiEnemy extends Enemy {

    public KongkoiEnemy(List<Vec2<Integer>> path, double scale) {
        super(path, scale, 115, 46, 12);
    }

    @Override public void onDeath(Battlefield field) {
        for (int i = 0; i < 2; i++) {
            PretEnemy child = new PretEnemy(path, scale, true);
            child.inheritScaling(this);                         // ลูกเร็วเท่าแม่
            child.placeAt(x + (i == 0 ? -7 : 7), y + (i == 0 ? -5 : 5), waypointIndex, traveled - i * 6);
            field.spawnEnemy(child);
        }
        field.spawnEffect(new RingEffect(x, y, new Color(0xB58A5A), 22, 0.35));
    }

    @Override public String  getThaiName() { return "ผีกองกอย"; }
    @Override public double  getRadius()   { return 9; }
    @Override public boolean isFlying()    { return false; }

    @Override protected void renderBody(Graphics2D g, double cx, double cy) {
        double hop = Math.abs(Math.sin(age * 8)) * 6;
        int by = (int) (cy - hop);
        g.setColor(new Color(0x3A2A1E));
        g.setStroke(new BasicStroke(2f));
        g.drawLine((int) cx, by + 2, (int) cx, by + 10);
        g.setStroke(new BasicStroke(1f));
        g.setColor(new Color(0x6B4A30));
        g.fillOval((int) cx - 6, by - 6, 12, 11);
        g.setColor(new Color(0x201812));
        g.fillOval((int) cx - 8, by - 16, 16, 13);
        g.setColor(new Color(0x8A6A4C));
        g.fillOval((int) cx - 5, by - 13, 10, 9);
        g.setColor(new Color(0xFFD060));
        g.fillOval((int) cx - 3, by - 10, 2, 2);
        g.fillOval((int) cx + 1, by - 10, 2, 2);
    }
}
