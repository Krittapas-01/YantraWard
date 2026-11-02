package yantra.util;

/** จุดพิกัดแบบ Generic — ใช้ได้ทั้ง Integer (ช่องตาราง) และ Double (พิกเซล) */
public class Vec2<T extends Number> {
    private T x;
    private T y;

    public Vec2(T x, T y) { this.x = x; this.y = y; }

    public T getX() { return x; }
    public T getY() { return y; }
    public void setX(T x) { this.x = x; }
    public void setY(T y) { this.y = y; }

    @Override public String toString() { return "(" + x + ", " + y + ")"; }

    @Override public boolean equals(Object o) {
        if (!(o instanceof Vec2)) return false;
        Vec2<?> v = (Vec2<?>) o;
        return x.equals(v.x) && y.equals(v.y);
    }

    @Override public int hashCode() { return x.hashCode() * 31 + y.hashCode(); }
}
