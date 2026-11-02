package yantra.effect;

import java.awt.*;
import java.util.List;
import yantra.entity.Enemy;

/** แอ่งน้ำมนต์ — ทำความเสียหายต่อเนื่องกับศัตรูทุกตัวที่อยู่ในแอ่ง รวมถึงผีลอย */
public class HolyWaterPool extends AreaEffect {

    private final double dps;
    private final double radius;

    public HolyWaterPool(double x, double y, double dps, double radius, double life) {
        super(x, y, life);
        this.dps = dps;
        this.radius = radius;
    }

    @Override public void affect(List<Enemy> enemies, double dt) {
        for (Enemy e : enemies) {
            if (e.isAlive() && e.distanceTo(this) <= radius) e.takeDamage(dps * dt);
        }
    }

    @Override public void render(Graphics2D g) {
        double f = fade();
        double grow = Math.min(1, age / 0.15);
        int r = (int) (radius * grow);
        g.setColor(new Color(90, 160, 230, (int) (70 * f)));
        g.fillOval((int) x - r, (int) y - r / 2 - 2, r * 2, r);
        g.setColor(new Color(170, 215, 255, (int) (150 * f)));
        g.drawOval((int) x - r, (int) y - r / 2 - 2, r * 2, r);
        double rip = (age * 1.6) % 1.0;
        int rr = (int) (r * rip);
        g.setColor(new Color(200, 230, 255, (int) (120 * f * (1 - rip))));
        g.drawOval((int) x - rr, (int) y - rr / 2 - 2, rr * 2, rr);
    }
}
