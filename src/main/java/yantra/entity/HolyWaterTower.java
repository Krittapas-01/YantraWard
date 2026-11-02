package yantra.entity;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import yantra.core.Battlefield;
import yantra.core.Setting;

/** น้ำมนต์ — สาดขวดน้ำมนต์ไปตกเป็นแอ่ง ทำความเสียหายต่อเนื่องกับทุกตัวในแอ่ง รวมถึงผีลอย */
public class HolyWaterTower extends Tower {

    private double poolRadius   = 36;
    private double poolDuration = 2.6;

    public HolyWaterTower(int tileX, int tileY) {
        super(TowerType.HOLY_WATER, tileX, tileY);
        this.damage   = 15;       // ความเสียหายต่อวินาทีของแอ่ง
        this.fireRate = 0.45;
    }

    @Override protected void attack(Battlefield field) {
        Enemy target = findTarget(field.getEnemies());
        if (target == null) return;
        // ขวดลอยไปช้า จึงต้องเล็งดักหน้า: ประมาณเวลาบิน แล้วถามผีว่าตอนนั้นจะอยู่ตรงไหน (ทำ 2 รอบให้แม่นขึ้น)
        double t = distanceTo(target) / FlaskProjectile.SPEED;
        double[] aim = target.predictPosition(t);
        t = Math.hypot(aim[0] - x, aim[1] - y) / FlaskProjectile.SPEED;
        aim = target.predictPosition(t);
        field.spawnProjectile(new FlaskProjectile(x, y - 6, aim[0], aim[1],
                                                  damage, poolRadius, poolDuration));
        resetCooldown();
    }

    @Override public boolean canTargetFlying() { return true; }

    @Override protected void applyUpgrade() {
        damage     *= 1.35;
        poolRadius += 4;
        range      *= 1.06;
    }

    @Override public List<String[]> getStatLines() {
        List<String[]> lines = new ArrayList<>();
        lines.add(new String[]{"ความเสียหาย", String.format("%.0f / วิ", damage)});
        lines.add(new String[]{"ขนาดแอ่ง", String.format("%.1f ช่อง", poolRadius * 2 / Setting.TILE)});
        lines.add(new String[]{"รัศมีสาด", String.format("%.1f ช่อง", range / Setting.TILE)});
        return lines;
    }

    @Override protected void renderBody(Graphics2D g, double cx, double cy) {
        g.setColor(new Color(0x8C8F96));
        g.fillArc((int) cx - 12, (int) cy - 10, 24, 20, 180, 180);
        g.setColor(new Color(0xCDD2DA));
        g.fillOval((int) cx - 12, (int) cy - 3, 24, 7);
        g.setColor(new Color(0x6FB0E8));
        g.fillOval((int) cx - 10, (int) cy - 2, 20, 5);
        g.setColor(new Color(0x4F8F3A));
        g.fillOval((int) cx - 1, (int) cy - 12, 6, 9);
        g.fillOval((int) cx - 6, (int) cy - 10, 6, 8);
        g.setColor(new Color(255, 255, 255, 140 + (int) (80 * Math.sin(age * 5))));
        g.fillOval((int) cx + 4, (int) cy - 1, 2, 2);
    }
}
