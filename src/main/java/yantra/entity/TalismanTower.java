package yantra.entity;

import java.awt.*;
import yantra.core.Battlefield;
import yantra.util.Sfx;

/** ตะกรุด — ยิงพลังอาคมใส่เป้าหมายเดียว แรงและเร็ว แต่ยิงผีที่ลอยอยู่ไม่ได้ */
public class TalismanTower extends Tower {

    public TalismanTower(int tileX, int tileY) {
        super(TowerType.TALISMAN, tileX, tileY);
        this.damage   = 20;
        this.fireRate = 1.5;
    }

    @Override protected void attack(Battlefield field) {
        Enemy target = findTarget(field.getEnemies());
        if (target == null) return;
        field.spawnProjectile(new Projectile(x, y - 6, target, damage, 470));
        field.playSound(Sfx.SHOOT);
        resetCooldown();
    }

    @Override public boolean canTargetFlying() { return false; }

    @Override protected void renderBody(Graphics2D g, double cx, double cy) {
        int x0 = (int) cx - 5, y0 = (int) cy - 11;
        g.setColor(new Color(0x8A6A20));
        g.fillRoundRect(x0 + 1, y0 + 1, 11, 22, 6, 6);
        g.setColor(new Color(0xD9B24A));
        g.fillRoundRect(x0, y0, 10, 21, 6, 6);
        g.setColor(new Color(0xFFE7A0));
        g.fillRect(x0 + 2, y0 + 2, 2, 17);
        g.setColor(new Color(0x7A2A1A));
        for (int i = 0; i < 3; i++) g.fillRect(x0, y0 + 5 + i * 5, 10, 2);
        g.setColor(new Color(0xC03030));
        g.drawArc((int) cx - 4, y0 - 6, 8, 8, 0, 180);
    }
}
