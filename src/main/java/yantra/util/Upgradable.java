package yantra.util;

/** สิ่งที่อัปเกรดและขายคืนได้ */
public interface Upgradable {
    int  getLevel();
    int  getMaxLevel();
    int  getUpgradeCost();
    void upgrade();
    int  getSellValue();

    /** default method: คลาสที่ implement ไม่ต้องเขียนเองก็ได้ */
    default boolean canUpgrade() {
        return getLevel() < getMaxLevel();
    }
}
