package yantra.entity;

import yantra.util.Renderable;

/** คลาสนามธรรมของทุกสิ่งที่มีตัวตนอยู่บนสนาม */
public abstract class Entity implements Renderable {
    protected double x;
    protected double y;
    protected boolean alive = true;
    protected double age = 0;          // เวลาที่มีชีวิตอยู่ (ใช้ทำแอนิเมชัน)

    protected Entity(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /** Template Method: นับอายุให้ก่อน แล้วค่อยให้คลาสลูกทำงานของตัวเอง */
    public final void tick(double dt) {
        age += dt;
        update(dt);
    }

    /** ทุกวัตถุต้องบอกได้ว่าเมื่อเวลาผ่านไป dt วินาที ตัวเองเปลี่ยนไปอย่างไร */
    protected abstract void update(double dt);

    public double  getX()    { return x; }
    public double  getY()    { return y; }
    public boolean isAlive() { return alive; }
    public void    kill()    { alive = false; }

    // Overloading: วัดระยะได้ทั้งจากพิกัดและจาก Entity อื่น
    public double distanceTo(double px, double py) { return Math.hypot(x - px, y - py); }
    public double distanceTo(Entity other)         { return distanceTo(other.x, other.y); }
}
