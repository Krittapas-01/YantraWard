package yantra.entity;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.List;
import yantra.core.Battlefield;
import yantra.util.Sfx;

/** ระฆัง — เสียงระฆังสั่นเป็นวงกว้าง ทำความเสียหายทุกตัวในรัศมี รวมถึงผีที่ลอยอยู่ */
public class BellTower extends Tower {

    private static final double WAVE_TIME = 0.45;
    private double waveTimer = 0;

    public BellTower(int tileX, int tileY) {
        super(TowerType.BELL, tileX, tileY);
        this.damage   = 13;
        this.fireRate = 0.75;
    }

    @Override protected void update(double dt) {
        super.update(dt);
        if (waveTimer > 0) waveTimer -= dt;
    }

    /** เสียงระฆังส่งผลได้จำกัดจำนวน: 5 / 7 / 9 ตน ตามระดับ */
    public int getMaxTargets() { return 3 + level * 2; }

    @Override protected void attack(Battlefield field) {
        List<Enemy> inRange = new ArrayList<>();
        for (Enemy e : field.getEnemies()) {
            if (e.isAlive() && distanceTo(e) <= range) inRange.add(e);
        }
        // ตีตัวที่ใกล้พระอุโบสถที่สุดก่อน
        inRange.sort((a, b) -> Double.compare(b.getProgress(), a.getProgress()));
        int hits = Math.min(getMaxTargets(), inRange.size());
        for (int i = 0; i < hits; i++) inRange.get(i).takeDamage(damage);
        if (hits > 0) {
            resetCooldown();
            waveTimer = WAVE_TIME;
            field.playSound(Sfx.BELL);
        }
    }

    @Override public boolean canTargetFlying() { return true; }

    @Override public java.util.List<String[]> getStatLines() {
        java.util.List<String[]> lines = super.getStatLines();
        lines.add(new String[]{"ตีได้พร้อมกัน", "สูงสุด " + getMaxTargets() + " ตน"});
        return lines;
    }

    @Override public void render(Graphics2D g) {
        if (waveTimer > 0) {
            double p = 1 - waveTimer / WAVE_TIME;
            int r = (int) (range * p);
            g.setColor(new Color(230, 160, 110, (int) (170 * (1 - p))));
            g.setStroke(new BasicStroke(2f));
            g.drawOval((int) x - r, (int) y - r, r * 2, r * 2);
            g.setStroke(new BasicStroke(1f));
        }
        super.render(g);
    }

    @Override protected void renderBody(Graphics2D g, double cx, double cy) {
        double swing = waveTimer > 0 ? Math.sin(age * 40) * 1.5 : 0;
        GeneralPath bell = new GeneralPath();
        bell.moveTo(cx - 3 + swing, cy - 10);
        bell.quadTo(cx - 9 + swing, cy - 9, cx - 9 + swing, cy + 2);
        bell.lineTo(cx - 12 + swing, cy + 8);
        bell.lineTo(cx + 12 + swing, cy + 8);
        bell.lineTo(cx + 9 + swing, cy + 2);
        bell.quadTo(cx + 9 + swing, cy - 9, cx + 3 + swing, cy - 10);
        bell.closePath();
        g.setColor(new Color(0x9A5A30));
        g.fill(bell);
        g.setColor(new Color(0xE0955C));
        g.fillRect((int) (cx - 6 + swing), (int) cy - 6, 3, 10);
        g.setColor(new Color(0x5A3018));
        g.draw(bell);
        g.setColor(new Color(0xD9B24A));
        g.fillOval((int) (cx - 2 + swing), (int) cy - 14, 4, 5);
        g.fillOval((int) (cx - 2 + swing), (int) cy + 7, 4, 4);
    }
}
