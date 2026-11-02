package yantra.util;

import java.awt.Color;

/** ด่านทั้งหมดในเกม — แต่ละด่านมีแผนที่ ความยากพื้นฐาน และสีพื้นของตัวเอง */
public enum Level {
    FOREST_TEMPLE(1, "วัดร้างกลางป่า",
            "ทางเดียวคดเคี้ยว เหมาะสำหรับเริ่มต้น",
            "เคล็ดลับ: วางตะกรุดตรงหัวโค้ง จะยิงโดนผีได้นานกว่าวางข้างทางตรง",
            "/maps/level1.txt", 1.00, new Color(28, 36, 24)),
    CANAL(2, "ท่าน้ำริมคลอง",
            "ทางยาวอ้อมข้ามคลอง ผีกระสือบินลัดข้ามน้ำมาได้",
            "เคล็ดลับ: ผีกระสือจะบินลัดเลียบขอบล่างของแผนที่ วางระฆังหรือน้ำมนต์ดักไว้ใกล้พระอุโบสถ",
            "/maps/level2.txt", 0.96, new Color(24, 34, 32)),
    TWIN_GRAVES(3, "ลานเจดีย์สองป่าช้า",
            "ผีมาจากสองป่าช้าแล้วมารวมกันกลางลาน",
            "เคล็ดลับ: ผีมาสองทางแล้วรวมกันตรงกลาง วางเครื่องรางที่แรงที่สุดไว้ที่จุดรวม",
            "/maps/level3.txt", 1.06, new Color(34, 33, 27));

    private final int    number;
    private final String thaiName;
    private final String description;
    private final String tip;
    private final String mapResource;
    private final double hpScale;
    private final Color  groundColor;

    Level(int number, String thaiName, String description, String tip, String mapResource,
          double hpScale, Color groundColor) {
        this.number = number;
        this.thaiName = thaiName;
        this.description = description;
        this.tip = tip;
        this.mapResource = mapResource;
        this.hpScale = hpScale;
        this.groundColor = groundColor;
    }

    public int    getNumber()      { return number; }
    public String getThaiName()    { return thaiName; }
    public String getDescription() { return description; }
    public String getTip()         { return tip; }
    public String getMapResource() { return mapResource; }
    public double getHpScale()     { return hpScale; }
    public Color  getGroundColor() { return groundColor; }

    /** ด่านถัดไป หรือ null ถ้าเป็นด่านสุดท้าย */
    public Level next() {
        return ordinal() + 1 < values().length ? values()[ordinal() + 1] : null;
    }

    /** เลื่อนไปมาแบบวนรอบ (ใช้ในหน้าเมนู) */
    public Level cycle(int dir) {
        int n = values().length;
        return values()[(ordinal() + dir % n + n) % n];
    }
}
