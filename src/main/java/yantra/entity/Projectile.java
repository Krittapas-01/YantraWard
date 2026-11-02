package yantra.entity;

import java.awt.*;
import java.util.ArrayDeque;
import java.util.Deque;
import yantra.core.Battlefield;
import yantra.effect.Particle;

/** พลังอาคมที่พุ่งเข้าหาเป้าหมาย เมื่อถึงเป้าจะสร้างความเสียหาย */
public class Projectile extends Entity {

    protected final Enemy  target;      // null = ไปที่จุดคงที่
    protected final double damage;
    protected final double speed;
    protected double tx;
    protected double ty;
    protected boolean impacted = false;
    private final Deque<double[]> trail = new ArrayDeque<>();

    /** ยิงแบบติดตามเป้าหมาย */
    public Projectile(double x, double y, Enemy target, double damage, double speed) {
        super(x, y);
        this.target = target;
        this.damage = damage;
        this.speed  = speed;
        this.tx = target.getX();
        this.ty = target.getY();
    }

    /** Overloading: ยิงไปยังจุดคงที่ (ใช้โดยคลาสลูก) */
    protected Projectile(double x, double y, double tx, double ty, double damage, double speed) {
        super(x, y);
        this.target = null;
        this.damage = damage;
        this.speed  = speed;
        this.tx = tx;
        this.ty = ty;
    }

    @Override protected void update(double dt) {
        if (target != null && target.isAlive()) {
            tx = target.getX();
            ty = target.getY() - (target.isFlying() ? 12 : 4);
        }
        trail.addFirst(new double[]{x, y});
        if (trail.size() > 6) trail.removeLast();

        double dx = tx - x, dy = ty - y;
        double dist = Math.hypot(dx, dy);
        double step = speed * dt;
        if (dist <= step) { x = tx; y = ty; impacted = true; return; }
        x += dx / dist * step;
        y += dy / dist * step;
    }

    public boolean hasImpacted() { return impacted; }

    /** สิ่งที่เกิดเมื่อถึงเป้า — คลาสลูกโอเวอร์ไรด์ได้ */
    public void onImpact(Battlefield field) {
        if (target != null && target.isAlive()) target.takeDamage(damage);
        for (int i = 0; i < 4; i++) {
            double a = Math.random() * Math.PI * 2;
            field.spawnEffect(new Particle(x, y, Math.cos(a) * 60, Math.sin(a) * 60,
                                           new Color(0xFFE39A), 2.5, 0.25));
        }
        kill();
    }

    @Override public void render(Graphics2D g) {
        int i = 0;
        for (double[] p : trail) {
            int a = 110 - i * 18;
            if (a <= 0) break;
            g.setColor(new Color(255, 220, 130, a));
            int s = 5 - i / 2;
            g.fillOval((int) p[0] - s / 2, (int) p[1] - s / 2, s, s);
            i++;
        }
        g.setColor(new Color(255, 230, 150, 90));
        g.fillOval((int) x - 5, (int) y - 5, 10, 10);
        g.setColor(new Color(0xFFF4D0));
        g.fillOval((int) x - 2, (int) y - 2, 5, 5);
    }
}
