package yantra.util;

/** ระดับความยาก — enum ที่มี field และ constructor ของตัวเอง */
public enum Difficulty {
    EASY  ("ง่าย", 0.64, 0.26, 0.00, 450, 30, 0.8),
    NORMAL("ปกติ", 0.80, 0.26, 0.06, 320, 20, 1.0),
    // ยาก/ฝันร้าย: จูนเลือดผีแยกรายด่าน (ด่าน 1, 2, 3) ให้บอทที่เก่งที่สุดชนะแบบเฉียดฉิว
    HARD  ("ยาก",  1.00, new double[]{1.16, 1.32, 1.04}, 0.30, 0.12, 300, 10, 1.6),
    NIGHTMARE("ฝันร้าย", 0.86, new double[]{1.32, 1.52, 1.08}, 0.30, 0.15, 400, 1, 2.5);   // ห้ามผีเข้าวัดแม้แต่ตนเดียว

    private final String thaiName;
    private final double hpScale;
    private final double[] levelTune;    // ตัวคูณเลือดเพิ่มรายด่าน (index = ลำดับด่าน)
    private final double waveGrowth;     // เลือดผีเพิ่มขึ้นกี่เท่าต่อคลื่น (ยิ่งมาก คลื่นท้าย ๆ ยิ่งโหด)
    private final double priceStep;      // สร้างเครื่องรางชนิดเดิมซ้ำ ราคาเพิ่มขึ้นกี่ % ต่อตัวที่มีอยู่
    private final int    startFaith;
    private final int    startLives;
    private final double scoreMultiplier;

    // Overloading constructor: แบบไม่จูนรายด่าน -> ทุกด่านใช้ตัวคูณ 1
    Difficulty(String thaiName, double hpScale, double waveGrowth, double priceStep,
               int startFaith, int startLives, double scoreMultiplier) {
        this(thaiName, hpScale, null, waveGrowth, priceStep, startFaith, startLives, scoreMultiplier);
    }

    Difficulty(String thaiName, double hpScale, double[] levelTune, double waveGrowth, double priceStep,
               int startFaith, int startLives, double scoreMultiplier) {
        this.thaiName = thaiName;
        this.hpScale = hpScale;
        this.levelTune = levelTune;
        this.waveGrowth = waveGrowth;
        this.priceStep = priceStep;
        this.startFaith = startFaith;
        this.startLives = startLives;
        this.scoreMultiplier = scoreMultiplier;
    }

    public String getThaiName()        { return thaiName; }
    public double getHpScale()         { return hpScale; }

    /** ตัวคูณเลือดผีจริงของด่านนี้ = ความยาก x ด่าน x ตัวจูนรายด่าน */
    public double getHpScale(Level level) {
        double tune = levelTune == null ? 1.0 : levelTune[level.ordinal()];
        return hpScale * level.getHpScale() * tune;
    }
    public double getWaveGrowth()      { return waveGrowth; }
    public double getPriceStep()       { return priceStep; }
    public int    getStartFaith()      { return startFaith; }
    public int    getStartLives()      { return startLives; }
    public double getScoreMultiplier() { return scoreMultiplier; }

    /** โหมดชีวิตเดียว — ผีเข้าถึงพระอุโบสถตนเดียวก็แพ้ */
    public boolean isOneLife() { return startLives == 1; }

    public Difficulty next() { return values()[(ordinal() + 1) % values().length]; }
    public Difficulty prev() { return values()[(ordinal() + values().length - 1) % values().length]; }
}
