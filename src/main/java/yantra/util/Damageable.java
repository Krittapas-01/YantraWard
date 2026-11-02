package yantra.util;

/** สิ่งที่รับความเสียหายได้ */
public interface Damageable {
    void   takeDamage(double amount);
    double getHp();
    double getMaxHp();
}
