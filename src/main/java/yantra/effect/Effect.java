package yantra.effect;

import yantra.entity.Entity;

/** เอฟเฟกต์ภาพที่มีอายุจำกัด เมื่อหมดอายุจะหายไปเอง */
public abstract class Effect extends Entity {

    protected final double life;

    protected Effect(double x, double y, double life) {
        super(x, y);
        this.life = life;
    }

    @Override protected void update(double dt) {
        if (age >= life) kill();
    }

    /** ค่าความทึบที่ลดลงตามอายุ 1 → 0 */
    protected double fade() {
        return Math.max(0, 1 - age / life);
    }
}
