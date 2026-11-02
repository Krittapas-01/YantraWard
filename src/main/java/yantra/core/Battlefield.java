package yantra.core;

import java.util.List;
import yantra.effect.AreaEffect;
import yantra.effect.Effect;
import yantra.entity.Enemy;
import yantra.entity.Projectile;
import yantra.entity.Tower;
import yantra.util.Sfx;

/**
 * "สนามรบ" ในมุมมองของวัตถุในเกม
 * ป้อมและศัตรูรู้จักแค่ interface นี้ ไม่ต้องรู้จัก GameManager ตัวจริง
 * (ลดการผูกมัดระหว่างคลาส — Dependency Inversion)
 */
public interface Battlefield {
    List<Enemy> getEnemies();
    List<Tower> getTowers();

    void spawnEnemy(Enemy e);
    void spawnProjectile(Projectile p);
    void spawnArea(AreaEffect a);
    void spawnEffect(Effect e);

    void playSound(Sfx s);

    /** จำนวนศัตรูทั้งหมด รวมตัวที่เพิ่งเกิดในเฟรมนี้ */
    int getAliveEnemyCount();
}
