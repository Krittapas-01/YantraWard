package yantra.effect;

import java.util.List;
import yantra.entity.Enemy;

/** เอฟเฟกต์ที่ส่งผลต่อศัตรูในพื้นที่ตลอดช่วงเวลาที่ยังอยู่ */
public abstract class AreaEffect extends Effect {

    protected AreaEffect(double x, double y, double life) {
        super(x, y, life);
    }

    public abstract void affect(List<Enemy> enemies, double dt);
}
