package yantra.entity;

import java.awt.*;
import java.util.List;
import yantra.core.Battlefield;
import yantra.effect.FloatingText;
import yantra.effect.RingEffect;
import yantra.util.Sfx;
import yantra.util.Vec2;

/**
 * แม่นาค — บอสคลื่นสุดท้าย
 * - มีเกราะ (โอเวอร์ไรด์ damageTakenMultiplier)
 * - สาปป้อมที่อยู่ใกล้ที่สุดให้หยุดทำงานชั่วคราว (โอเวอร์ไรด์ interact)
 * - เลือดเหลือ 2/3 และ 1/3 จะเรียกเปรตมาช่วยครั้งละ 4 ตน
 * - เลือดต่ำกว่า 30% จะคลั่ง: เดินเร็วขึ้น 50% และสาปถี่ขึ้น
 */
public class NakBoss extends Enemy {

    private static final double CURSE_INTERVAL = 5.0;
    private static final double CURSE_RANGE    = 210;
    private static final double CURSE_DURATION = 5.0;

    private double curseTimer = 2.5;
    private int summons = 0;            // เรียกลูกสมุนไปแล้วกี่รอบ (สูงสุด 2)
    private boolean enraged = false;

    public NakBoss(List<Vec2<Integer>> path, double scale) {
        super(path, scale, 1400, 21, 100);
    }

    @Override public double getSpeed() {
        return super.getSpeed() * (enraged ? 1.5 : 1.0);
    }

    @Override protected double damageTakenMultiplier() { return 0.8; }

    @Override public void interact(Battlefield field, double dt) {
        curseTimer -= dt;
        if (curseTimer <= 0) {
            curseTimer = enraged ? CURSE_INTERVAL * 0.6 : CURSE_INTERVAL;
            Tower nearest = null;
            for (Tower t : field.getTowers()) {
                if (t.isDisabled() || distanceTo(t) > CURSE_RANGE) continue;
                if (nearest == null || distanceTo(t) < distanceTo(nearest)) nearest = t;
            }
            if (nearest != null) {
                nearest.disable(CURSE_DURATION);
                field.spawnEffect(new RingEffect(nearest.getX(), nearest.getY(), new Color(0xB070E0), 34, 0.6));
                field.spawnEffect(new FloatingText(nearest.getX(), nearest.getY() - 20, "ถูกสาป!", new Color(0xD0A0FF)));
                field.playSound(Sfx.CURSE);
            }
        }
        double ratio = hp / maxHp;
        if ((summons == 0 && ratio < 2.0 / 3) || (summons == 1 && ratio < 1.0 / 3)) {
            summons++;
            for (int i = 0; i < 4; i++) {
                PretEnemy p = new PretEnemy(path, scale);
                p.inheritScaling(this);
                p.placeAt(x + (i - 1.5) * 9, y + (i - 1.5) * 5, waypointIndex, traveled);
                field.spawnEnemy(p);
            }
            field.spawnEffect(new RingEffect(x, y, new Color(0x9050C0), 50, 0.7));
            field.playSound(Sfx.CURSE);
        }
        if (!enraged && ratio < 0.3) {
            enraged = true;
            field.spawnEffect(new RingEffect(x, y, new Color(0xFF4060), 70, 0.9));
            field.spawnEffect(new FloatingText(x, y - 30, "แม่นาคคลั่ง!", new Color(0xFF7080)));
            field.playSound(Sfx.BOSS);
        }
    }

    @Override public int     getLeakDamage() { return 10; }
    @Override public boolean isBoss()        { return true; }
    @Override public String  getThaiName()   { return "แม่นาค"; }
    @Override public double  getRadius()     { return 16; }
    @Override public boolean isFlying()      { return false; }

    @Override protected void renderBody(Graphics2D g, double cx, double cy) {
        double pulse = 0.5 + 0.5 * Math.sin(age * (enraged ? 9 : 3));
        for (int i = 3; i >= 1; i--) {
            g.setColor(enraged ? new Color(220, 50, 70, (int) ((16 + 12 * pulse) * i))
                               : new Color(150, 80, 200, (int) ((14 + 10 * pulse) * i)));
            int rr = 16 + i * 6;
            g.fillOval((int) cx - rr, (int) cy - rr - 6, rr * 2, rr * 2);
        }
        // ผมยาว
        g.setColor(new Color(0x0E0B10));
        g.fillOval((int) cx - 13, (int) cy - 30, 26, 44);
        // ชุด
        int[] px = {(int) cx - 9, (int) cx + 9, (int) cx + 14, (int) cx - 14};
        int[] py = {(int) cy - 8, (int) cy - 8, (int) cy + 16, (int) cy + 16};
        g.setColor(new Color(0x6E8C6A));
        g.fillPolygon(px, py, 4);
        g.setColor(new Color(0xC9A13B));
        g.drawLine((int) cx - 9, (int) cy - 4, (int) cx + 9, (int) cy + 4);
        // หน้า
        g.setColor(new Color(0xE8DCCB));
        g.fillOval((int) cx - 7, (int) cy - 24, 14, 17);
        g.setColor(new Color(0xB01828));
        g.fillOval((int) cx - 4, (int) cy - 18, 3, 3);
        g.fillOval((int) cx + 1, (int) cy - 18, 3, 3);
        // แขนยาวที่ยื่นออกมา
        g.setColor(new Color(0xE8DCCB));
        g.setStroke(new BasicStroke(2.2f));
        double reach = 14 + 6 * pulse;
        g.drawLine((int) cx + 9, (int) cy - 4, (int) (cx + 9 + reach), (int) (cy - 10 - reach * 0.3));
        g.setStroke(new BasicStroke(1f));
    }
}
