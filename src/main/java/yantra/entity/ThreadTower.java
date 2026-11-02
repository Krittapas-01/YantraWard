package yantra.entity;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import yantra.core.Battlefield;
import yantra.core.Setting;

/** สายสิญจน์ — ไม่ทำความเสียหาย แต่โยงด้ายผูกผีในรัศมีให้เดินช้าลง */
public class ThreadTower extends Tower {

    private double slowFactor = 0.62;             // เหลือความเร็ว 62% (ช้าลง 38%)
    private final List<double[]> links = new ArrayList<>();

    public ThreadTower(int tileX, int tileY) {
        super(TowerType.THREAD, tileX, tileY);
        this.damage   = 0;
        this.fireRate = 4;
    }

    @Override protected void attack(Battlefield field) {
        links.clear();
        for (Enemy e : field.getEnemies()) {
            if (!e.isAlive() || distanceTo(e) > range) continue;
            e.applySlow(slowFactor, 0.4);
            links.add(new double[]{e.getX(), e.getY() - (e.isFlying() ? 12 : 4)});
        }
        cooldown = 1.0 / fireRate;
    }

    @Override public boolean canTargetFlying() { return true; }

    /** ป้อมนี้ไม่มีความเสียหาย การอัปเกรดจึงทำให้ช้าลงมากขึ้นและรัศมีกว้างขึ้นแทน */
    @Override protected void applyUpgrade() {
        slowFactor -= 0.05;      // อัปเกรดแต่ละครั้งช้าลงเพิ่มอีก 5% (38% → 43% → 48%)
        range *= 1.06;
    }

    @Override public List<String[]> getStatLines() {
        List<String[]> lines = new ArrayList<>();
        lines.add(new String[]{"ลดความเร็ว", String.format("%.0f%%", (1 - slowFactor) * 100)});
        lines.add(new String[]{"รัศมี", String.format("%.1f ช่อง", range / Setting.TILE)});
        lines.add(new String[]{"โจมตีผีลอย", "ได้"});
        return lines;
    }

    @Override public void render(Graphics2D g) {
        if (!isDisabled()) {
            g.setStroke(new BasicStroke(1f));
            for (double[] l : links) {
                g.setColor(new Color(240, 234, 214, 120));
                g.drawLine((int) x, (int) y - 8, (int) l[0], (int) l[1]);
            }
        }
        super.render(g);
    }

    @Override protected void renderBody(Graphics2D g, double cx, double cy) {
        g.setColor(new Color(0x7A5A3A));
        g.fillRect((int) cx - 1, (int) cy - 12, 3, 22);
        g.setColor(new Color(0xF2ECDA));
        g.fillOval((int) cx - 7, (int) cy - 5, 14, 10);
        g.setColor(new Color(0xBDB5A0));
        for (int i = -5; i <= 5; i += 3) g.drawLine((int) cx + i, (int) cy - 4, (int) cx + i, (int) cy + 4);
        g.setColor(new Color(0xF2ECDA));
        double w = Math.sin(age * 2) * 2;
        g.drawArc((int) cx - 12, (int) (cy - 14 + w), 24, 10, 20, 140);
    }
}
