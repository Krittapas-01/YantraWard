package yantra.entity;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import yantra.core.Battlefield;
import yantra.core.Setting;
import yantra.util.Upgradable;

/** คลาสนามธรรมของเครื่องรางทุกชนิด */
public abstract class Tower extends Entity implements Upgradable {

    protected final TowerType type;
    protected final int tileX;
    protected final int tileY;

    protected double range;
    protected double damage;
    protected double fireRate;           // ครั้ง/วินาที
    protected double cooldown = 0;
    protected double flash = 0;
    protected int    level = 1;
    protected int    totalInvested;
    private   double disabledTimer = 0;

    protected Tower(TowerType type, int tileX, int tileY) {
        super(tileX * Setting.TILE + Setting.TILE / 2.0, tileY * Setting.TILE + Setting.TILE / 2.0);
        this.type = type;
        this.tileX = tileX;
        this.tileY = tileY;
        this.range = type.getBaseRange();
        this.totalInvested = type.getCost();
    }

    // ---------------- สิ่งที่คลาสลูกต้องเขียนเอง ----------------

    /** ทุกป้อมต้องโจมตีได้ แต่ "โจมตีอย่างไร" เป็นเรื่องของคลาสลูก */
    protected abstract void attack(Battlefield field);

    /** ป้อมนี้โจมตีวิญญาณที่ล่องลอยได้หรือไม่ */
    public abstract boolean canTargetFlying();

    /** วาดตัวเครื่องราง (ไม่รวมฐาน) */
    protected abstract void renderBody(Graphics2D g, double cx, double cy);

    // ---------------- ส่วนที่ใช้ร่วมกัน ----------------

    @Override protected void update(double dt) {
        if (cooldown > 0) cooldown -= dt;
        if (flash > 0) flash -= dt;
        if (disabledTimer > 0) disabledTimer -= dt;
    }

    /** เรียกจาก GameManager ทุกเฟรม — ตรวจเงื่อนไขร่วมก่อน แล้วค่อยให้คลาสลูกโจมตี */
    public final void tryAttack(Battlefield field) {
        if (isDisabled() || cooldown > 0) return;
        attack(field);
    }

    /** เลือกศัตรูที่เดินไปไกลที่สุด (ใกล้พระอุโบสถที่สุด) ภายในรัศมี */
    protected Enemy findTarget(List<Enemy> enemies) {
        Enemy best = null;
        for (Enemy e : enemies) {
            if (!e.isAlive()) continue;
            if (e.isFlying() && !canTargetFlying()) continue;
            if (distanceTo(e) > range) continue;
            if (best == null || e.getProgress() > best.getProgress()) best = e;
        }
        return best;
    }

    protected void resetCooldown() {
        cooldown = 1.0 / fireRate;
        flash = 0.12;
    }

    /** บันทึกราคาที่จ่ายจริง (อาจแพงกว่าราคาตั้งต้น) เพื่อคิดราคาขายคืนให้ถูก */
    public void setPurchasePrice(int price) { totalInvested = price; }

    public void    disable(double seconds) { disabledTimer = Math.max(disabledTimer, seconds); }
    public boolean isDisabled()           { return disabledTimer > 0; }

    // ---------------- Upgradable ----------------
    @Override public int getLevel()       { return level; }
    @Override public int getMaxLevel()    { return 3; }
    @Override public int getUpgradeCost() { return (int) Math.round(type.getCost() * 0.85 * level / 5.0) * 5; }
    @Override public int getSellValue()   { return (int) (totalInvested * Setting.SELL_RATE); }

    /** Template Method: ส่วนที่เหมือนกันทำที่นี่ ส่วนที่ต่างกันอยู่ใน applyUpgrade() */
    @Override public final void upgrade() {
        if (!canUpgrade()) return;
        totalInvested += getUpgradeCost();
        level++;
        applyUpgrade();
    }

    /** ค่าเริ่มต้นของการอัปเกรด — คลาสลูกโอเวอร์ไรด์ได้ */
    protected void applyUpgrade() {
        damage   *= 1.5;
        range    *= 1.08;
        fireRate *= 1.12;
    }

    /** ข้อมูลที่แสดงในแผงป้อม — คลาสลูกโอเวอร์ไรด์เพื่อแสดงค่าที่ต่างกัน */
    public List<String[]> getStatLines() {
        List<String[]> lines = new ArrayList<>();
        lines.add(new String[]{"ความเสียหาย", String.format("%.0f", damage)});
        lines.add(new String[]{"รัศมี", String.format("%.1f ช่อง", range / Setting.TILE)});
        lines.add(new String[]{"อัตรายิง", String.format("%.1f / วิ", fireRate)});
        return lines;
    }

    public TowerType getType()   { return type; }
    public double getRange()     { return range; }
    public int    getTileX()     { return tileX; }
    public int    getTileY()     { return tileY; }

    // ---------------- การวาด ----------------

    @Override public void render(Graphics2D g) {
        renderPlate(g, x, y);
        renderBody(g, x, y);

        g.setColor(new Color(0xFFE39A));
        for (int i = 0; i < level; i++) g.fillOval((int) x - 9 + i * 7, (int) y + 12, 4, 4);

        if (flash > 0) {
            g.setColor(new Color(255, 240, 200, 60));
            g.fillOval((int) x - 16, (int) y - 16, 32, 32);
        }
        if (isDisabled()) {
            g.setColor(new Color(40, 10, 60, 150));
            g.fillRoundRect((int) x - 16, (int) y - 16, 32, 32, 8, 8);
            g.setColor(new Color(0xC090F0));
            g.setStroke(new BasicStroke(2f));
            double a = age * 4;
            g.drawArc((int) x - 10, (int) y - 10, 20, 20, (int) Math.toDegrees(a), 250);
            g.setStroke(new BasicStroke(1f));
        }
    }

    public void renderIcon(Graphics2D g, double cx, double cy) {
        renderPlate(g, cx, cy);
        renderBody(g, cx, cy);
    }

    private void renderPlate(Graphics2D g, double cx, double cy) {
        g.setColor(new Color(0, 0, 0, 90));
        g.fillRoundRect((int) cx - 14, (int) cy - 11, 30, 30, 8, 8);
        g.setColor(new Color(0x4A4438));
        g.fillRoundRect((int) cx - 15, (int) cy - 15, 30, 30, 8, 8);
        g.setColor(new Color(0x6A6252));
        g.drawRoundRect((int) cx - 15, (int) cy - 15, 30, 30, 8, 8);
    }

    public void renderRange(Graphics2D g) {
        Color c = type.getColor();
        int r = (int) range;
        g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 28));
        g.fillOval((int) x - r, (int) y - r, r * 2, r * 2);
        g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 130));
        g.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{6f, 5f}, 0f));
        g.drawOval((int) x - r, (int) y - r, r * 2, r * 2);
        g.setStroke(new BasicStroke(1f));
    }
}
