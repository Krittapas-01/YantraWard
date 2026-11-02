package yantra.entity;

import java.util.List;
import yantra.util.Vec2;

/**
 * ชนิดของวิญญาณ — enum ที่แต่ละค่ามี method ของตัวเอง (constant-specific body)
 * ใช้เป็น Factory สร้างศัตรูโดยไม่ต้องเขียน switch
 */
public enum EnemyType {
    PRET("ผีเปรต", "ศัตรูพื้นฐาน เดินตามเส้นทางปกติ") {
        @Override public Enemy create(List<Vec2<Integer>> p, double s) { return new PretEnemy(p, s); }
    },
    KRASUE("ผีกระสือ", "ล่องลอย บินตรงเข้าหาพระอุโบสถ ตะกรุดยิงไม่โดน") {
        @Override public Enemy create(List<Vec2<Integer>> p, double s) { return new KrasueEnemy(p, s); }
    },
    POP("ผีปอบ", "เลือดหนา เดินช้า และฟื้นเลือดเองได้") {
        @Override public Enemy create(List<Vec2<Integer>> p, double s) { return new PopEnemy(p, s); }
    },
    KONGKOI("ผีกองกอย", "เมื่อตายจะแตกออกเป็นเปรตน้อย 2 ตน") {
        @Override public Enemy create(List<Vec2<Integer>> p, double s) { return new KongkoiEnemy(p, s); }
    },
    NAK("แม่นาค", "บอส มีเกราะ สาปป้อม เรียกลูกสมุน และคลั่งเมื่อใกล้ตาย") {
        @Override public Enemy create(List<Vec2<Integer>> p, double s) { return new NakBoss(p, s); }
    };

    private final String thaiName;
    private final String description;

    EnemyType(String thaiName, String description) {
        this.thaiName = thaiName;
        this.description = description;
    }

    public String getThaiName()    { return thaiName; }
    public String getDescription() { return description; }

    public abstract Enemy create(List<Vec2<Integer>> path, double scale);
}
