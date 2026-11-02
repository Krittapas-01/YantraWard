package yantra.core;

/** ค่าคงที่ทั้งหมดของเกม รวมไว้ที่เดียวเพื่อปรับแต่งได้ง่าย */
public final class Setting {
    private Setting() { }

    public static final String TITLE = "ยันต์ล้อมวัด — Yantra Ward : Temple Defense";

    public static final int TILE = 40;
    public static final int COLS = 20;
    public static final int ROWS = 12;

    public static final int HUD_H   = 60;
    public static final int BAR_H   = 64;
    public static final int FIELD_Y = HUD_H;
    public static final int PLAY_W  = COLS * TILE;               // 800
    public static final int PLAY_H  = ROWS * TILE;               // 480
    public static final int WINDOW_W = PLAY_W;                   // 800
    public static final int WINDOW_H = HUD_H + PLAY_H + BAR_H;   // 604

    public static final int MAX_FPS     = 60;
    public static final int TOTAL_WAVES = 15;
    public static final double SELL_RATE = 0.6;

    public static final String SCORE_FILE    = "highscore.txt";
    public static final String SETTINGS_FILE = "settings.txt";
}
