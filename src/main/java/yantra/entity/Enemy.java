package yantra.entity;

import java.awt.*;
import java.util.List;
import yantra.core.Battlefield;
import yantra.core.Setting;
import yantra.util.Damageable;
import yantra.util.Vec2;

/** คลาสนามธรรมของวิญญาณทุกชนิด */
public abstract class Enemy extends Entity implements Damageable {

    protected final List<Vec2<Integer>> path;
    protected final double scale;       // ตัวคูณความแข็งแกร่งตามคลื่น/ความยาก
    protected double hp;
    protected double maxHp;
    protected double baseSpeed;
    protected int    reward;

    protected int     waypointIndex = 1;
    protected double  traveled = 0;
    protected boolean reachedGoal = false;

    private double speedMultiplier = 1.0;   // ยิ่งคลื่นหลัง ๆ ผียิ่งเดินเร็ว
    private double rewardMultiplier = 1.0;  // ยิ่งคลื่นหลัง ๆ ศรัทธาต่อตัวยิ่งน้อย (กันเงินเฟ้อ)
    private double slowFactor = 1.0;
    private double slowTimer  = 0.0;
    private double hitFlash   = 0.0;

    protected Enemy(List<Vec2<Integer>> path, double scale, double baseHp, double speed, int reward) {
        super(tileCenterX(path.get(0)), tileCenterY(path.get(0)));
        this.path      = path;
        this.scale     = scale;
        this.maxHp     = baseHp * scale;
        this.hp        = maxHp;
        this.baseSpeed = speed;
        this.reward    = reward;
    }

    protected static double tileCenterX(Vec2<Integer> t) { return t.getX() * Setting.TILE + Setting.TILE / 2.0; }
    protected static double tileCenterY(Vec2<Integer> t) { return t.getY() * Setting.TILE + Setting.TILE / 2.0; }

    /** ใช้ตอนเกิดกลางทาง เช่น ผีที่แตกร่างออกมา */
    public void placeAt(double px, double py, int waypoint, double traveledSoFar) {
        this.x = px;
        this.y = py;
        this.waypointIndex = waypoint;
        this.traveled = traveledSoFar;
    }

    @Override protected void update(double dt) {
        if (slowTimer > 0) { slowTimer -= dt; if (slowTimer <= 0) slowFactor = 1.0; }
        if (hitFlash > 0) hitFlash -= dt;
        move(dt);
    }

    /** การเคลื่อนที่พื้นฐาน: เดินตามเส้นทางที่ Pathfinder หามาให้ — คลาสลูกโอเวอร์ไรด์ได้ */
    protected void move(double dt) {
        if (waypointIndex >= path.size()) { reachedGoal = true; return; }
        Vec2<Integer> target = path.get(waypointIndex);
        double tx = tileCenterX(target), ty = tileCenterY(target);
        double dx = tx - x, dy = ty - y;
        double dist = Math.hypot(dx, dy);
        double step = getSpeed() * dt;
        if (dist <= step) {
            x = tx; y = ty;
            traveled += dist;
            waypointIndex++;
        } else {
            x += dx / dist * step;
            y += dy / dist * step;
            traveled += step;
        }
    }

    /**
     * ตำแหน่งโดยประมาณในอีก t วินาทีข้างหน้า (ป้อมใช้เล็งดักหน้า)
     * ค่าเริ่มต้นคือเดินตามเส้นทาง — คลาสลูกที่เคลื่อนที่ต่างออกไปโอเวอร์ไรด์ได้
     */
    public double[] predictPosition(double t) {
        double px = x, py = y, remain = getSpeed() * t;
        int wi = waypointIndex;
        while (remain > 0 && wi < path.size()) {
            double tx = tileCenterX(path.get(wi)), ty = tileCenterY(path.get(wi));
            double d = Math.hypot(tx - px, ty - py);
            if (d >= remain) {
                px += (tx - px) / d * remain;
                py += (ty - py) / d * remain;
                remain = 0;
            } else {
                px = tx; py = ty;
                remain -= d;
                wi++;
            }
        }
        return new double[]{px, py};
    }

    /** Hook: ความสามารถพิเศษที่ต้องยุ่งกับสนาม (เช่น บอสสาปป้อม) ค่าเริ่มต้นคือไม่ทำอะไร */
    public void interact(Battlefield field, double dt) { }

    /** Hook: สิ่งที่เกิดขึ้นเมื่อตาย (เช่น ผีกองกอยแตกร่าง) ค่าเริ่มต้นคือไม่ทำอะไร */
    public void onDeath(Battlefield field) { }

    /** ตัวคูณความเสียหายที่ได้รับ — บอสโอเวอร์ไรด์เพื่อให้มีเกราะ */
    protected double damageTakenMultiplier() { return 1.0; }

    @Override public void takeDamage(double amount) {
        if (!alive) return;
        hp -= amount * damageTakenMultiplier();
        if (amount > 2) hitFlash = 0.08;
        if (hp <= 0) { hp = 0; kill(); }
    }

    public void applySlow(double factor, double duration) {
        slowFactor = Math.min(slowFactor, factor);
        slowTimer  = Math.max(slowTimer, duration);
    }

    /** เพิ่มความเร็วตามคลื่น (WaveManager เป็นคนเรียก) */
    public void   setSpeedMultiplier(double m) { speedMultiplier = m; }
    public double getSpeedMultiplier()         { return speedMultiplier; }
    public void   setRewardMultiplier(double m){ rewardMultiplier = m; }

    /** ผีที่เกิดจากผีตัวอื่น (แตกร่าง/ถูกเรียก) ได้ความเร็วและค่าหัวตามคลื่นเดียวกับตัวแม่ */
    public void inheritScaling(Enemy parent) {
        this.speedMultiplier  = parent.speedMultiplier;
        this.rewardMultiplier = parent.rewardMultiplier;
    }

    public double  getSpeed()       { return baseSpeed * speedMultiplier * slowFactor; }
    public boolean isSlowed()       { return slowTimer > 0; }
    public boolean hasReachedGoal() { return reachedGoal; }
    public int     getReward()      { return Math.max(1, (int) Math.round(reward * rewardMultiplier)); }
    public double  getProgress()    { return traveled; }
    public int     getLeakDamage()  { return 1; }
    public boolean isBoss()         { return false; }
    @Override public double getHp()    { return hp; }
    @Override public double getMaxHp() { return maxHp; }

    // ---------------- สิ่งที่คลาสลูกทุกตัวต้องบอก ----------------
    public abstract String  getThaiName();
    public abstract double  getRadius();
    public abstract boolean isFlying();

    /** วาดตัวผีที่ตำแหน่ง (cx, cy) — แต่ละชนิดวาดไม่เหมือนกัน */
    protected abstract void renderBody(Graphics2D g, double cx, double cy);

    /** Template Method: เงา + ตัว + เอฟเฟกต์ + หลอดเลือด ลำดับเดียวกันทุกชนิด */
    @Override public final void render(Graphics2D g) {
        double r = getRadius();
        double lift = isFlying() ? 12 + Math.sin(age * 3) * 2 : 0;

        g.setColor(new Color(0, 0, 0, isFlying() ? 60 : 95));
        g.fillOval((int) (x - r), (int) (y + r * 0.35), (int) (r * 2), (int) (r * 0.8));

        renderBody(g, x, y - lift);

        if (hitFlash > 0) {
            g.setColor(new Color(255, 255, 255, 110));
            g.fillOval((int) (x - r), (int) (y - lift - r), (int) (r * 2), (int) (r * 2));
        }
        if (isSlowed()) {
            g.setColor(new Color(232, 226, 207, 150));
            g.setStroke(new BasicStroke(1.2f));
            g.drawOval((int) (x - r - 3), (int) (y - lift - r - 3), (int) (r * 2 + 6), (int) (r * 2 + 6));
            g.setStroke(new BasicStroke(1f));
        }
        if (hp < maxHp && !isBoss()) {
            int bw = (int) (r * 2 + 8);
            int bx = (int) x - bw / 2, by = (int) (y - lift - r - 10);
            g.setColor(new Color(20, 10, 10, 200));
            g.fillRect(bx - 1, by - 1, bw + 2, 5);
            g.setColor(hp / maxHp > 0.4 ? new Color(0x7FD07F) : new Color(0xE0505A));
            g.fillRect(bx, by, (int) (bw * hp / maxHp), 3);
        }
    }

    /** วาดเป็นไอคอน (ใช้ในหน้าวิธีเล่น) */
    public void renderIcon(Graphics2D g, double cx, double cy) {
        renderBody(g, cx, cy);
    }
}
