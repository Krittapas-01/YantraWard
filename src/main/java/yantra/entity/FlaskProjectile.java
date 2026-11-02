package yantra.entity;

import java.awt.*;
import yantra.core.Battlefield;
import yantra.effect.HolyWaterPool;
import yantra.effect.Particle;
import yantra.util.Sfx;

/** ขวดน้ำมนต์ — ลอยเป็นวิถีโค้งไปตกที่จุดหมาย แล้วกลายเป็นแอ่งน้ำมนต์ (โอเวอร์ไรด์ onImpact) */
public class FlaskProjectile extends Projectile {

    public static final double SPEED = 230;

    private final double startX;
    private final double startY;
    private final double totalDist;
    private final double poolRadius;
    private final double poolDuration;

    public FlaskProjectile(double x, double y, double tx, double ty,
                           double dps, double poolRadius, double poolDuration) {
        super(x, y, tx, ty, dps, SPEED);
        this.startX = x;
        this.startY = y;
        this.totalDist = Math.max(1, Math.hypot(tx - x, ty - y));
        this.poolRadius = poolRadius;
        this.poolDuration = poolDuration;
    }

    @Override public void onImpact(Battlefield field) {
        field.spawnArea(new HolyWaterPool(tx, ty, damage, poolRadius, poolDuration));
        for (int i = 0; i < 10; i++) {
            double a = Math.random() * Math.PI * 2;
            double v = 40 + Math.random() * 60;
            field.spawnEffect(new Particle(tx, ty, Math.cos(a) * v, Math.sin(a) * v - 30,
                                           new Color(0x9CD2FF), 3, 0.4));
        }
        field.playSound(Sfx.SPLASH);
        kill();
    }

    @Override public void render(Graphics2D g) {
        double t = 1 - Math.hypot(tx - x, ty - y) / totalDist;
        double arc = Math.sin(Math.PI * Math.max(0, Math.min(1, t))) * 34;
        int dx = (int) x, dy = (int) (y - arc);
        g.setColor(new Color(0, 0, 0, 60));
        g.fillOval((int) x - 3, (int) y - 1, 6, 3);
        g.setColor(new Color(0xCFE8FF));
        g.fillOval(dx - 4, dy - 4, 8, 9);
        g.setColor(new Color(0x6FB0E8));
        g.fillOval(dx - 3, dy - 1, 6, 5);
        g.setColor(new Color(0x7A5A3A));
        g.fillRect(dx - 1, dy - 7, 3, 3);
    }
}
