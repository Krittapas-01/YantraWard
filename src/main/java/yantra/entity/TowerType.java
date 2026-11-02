package yantra.entity;

import java.awt.Color;

/** ชนิดของเครื่องราง — แต่ละค่ารู้วิธีสร้างป้อมของตัวเอง (Factory ในรูปแบบ enum) */
public enum TowerType {
    TALISMAN("ตะกรุด", 50, 120, 0xD9B24A, "ยิงเป้าหมายเดียว แรงและเร็ว แต่ยิงผีที่ลอยอยู่ไม่ได้") {
        @Override public Tower create(int col, int row) { return new TalismanTower(col, row); }
    },
    BELL("ระฆัง", 150, 100, 0xE0955C, "สั่นสะเทือนเป็นวงกว้าง โดนหลายตัวพร้อมกัน รวมถึงผีลอย") {
        @Override public Tower create(int col, int row) { return new BellTower(col, row); }
    },
    THREAD("สายสิญจน์", 75, 110, 0xE8E2CF, "ไม่ทำความเสียหาย แต่ผูกผีในรัศมีให้เดินช้าลง") {
        @Override public Tower create(int col, int row) { return new ThreadTower(col, row); }
    },
    HOLY_WATER("น้ำมนต์", 140, 135, 0x6FB0E8, "สาดเป็นแอ่ง ทำความเสียหายต่อเนื่อง โดนผีลอยได้") {
        @Override public Tower create(int col, int row) { return new HolyWaterTower(col, row); }
    };

    private final String thaiName;
    private final int    cost;
    private final double baseRange;
    private final Color  color;
    private final String description;

    TowerType(String thaiName, int cost, double baseRange, int rgb, String description) {
        this.thaiName = thaiName;
        this.cost = cost;
        this.baseRange = baseRange;
        this.color = new Color(rgb);
        this.description = description;
    }

    public String getThaiName()    { return thaiName; }
    public int    getCost()        { return cost; }
    public double getBaseRange()   { return baseRange; }
    public Color  getColor()       { return color; }
    public String getDescription() { return description; }

    public abstract Tower create(int col, int row);
}
