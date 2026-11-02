package yantra.system;

import yantra.util.InsufficientFaithException;

/**
 * ระบบแรงศรัทธา (สกุลเงินในเกม)
 * ตัวอย่าง Encapsulation: ยอดเงินเป็น private แก้ได้ผ่านเมธอดที่ตรวจสอบแล้วเท่านั้น
 */
public class EconomySystem {

    private int faith;
    private int totalEarned;

    public EconomySystem(int startFaith) {
        this.faith = startFaith;
    }

    public void spend(int amount) throws InsufficientFaithException {
        if (amount > faith) throw new InsufficientFaithException(amount, faith);
        faith -= amount;
    }

    public void earn(int amount) {
        if (amount <= 0) return;
        faith += amount;
        totalEarned += amount;
    }

    public boolean canAfford(int amount) { return faith >= amount; }
    public int getFaith()                { return faith; }
    public int getTotalEarned()          { return totalEarned; }
}
