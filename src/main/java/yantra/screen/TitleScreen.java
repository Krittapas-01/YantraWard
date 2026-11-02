package yantra.screen;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import yantra.core.GamePanel;
import yantra.core.Setting;
import yantra.system.GameMap;
import yantra.ui.Button;
import yantra.ui.FieldRenderer;
import yantra.ui.Fonts;
import yantra.util.Difficulty;
import yantra.util.Level;
import yantra.util.Sfx;

/** หน้าเมนูหลัก — เลือกด่าน เลือกความยาก แล้วเริ่มเกม */
public class TitleScreen extends Screen {

    private static final int CX = Setting.WINDOW_W / 2;
    private static final int ROW_LEVEL = 326;
    private static final int ROW_DIFF  = 366;

    private final Button startBtn     = new Button("เริ่มเกม", CX - 130, 272, 260, 44, Button.Style.PRIMARY);
    private final Button levelPrev    = new Button("‹", CX - 130, ROW_LEVEL, 36, 32);
    private final Button levelNext    = new Button("›", CX + 94,  ROW_LEVEL, 36, 32);
    private final Button diffPrev     = new Button("‹", CX - 130, ROW_DIFF, 36, 32);
    private final Button diffNext     = new Button("›", CX + 94,  ROW_DIFF, 36, 32);
    private final Button howBtn       = new Button("วิธีเล่น", CX - 130, 408, 127, 34);
    private final Button settingsBtn  = new Button("ตั้งค่าเสียง", CX + 3, 408, 127, 34);
    private final Button exitBtn      = new Button("ออกจากเกม", CX - 130, 448, 260, 34);
    private final Button[] buttons    = {startBtn, levelPrev, levelNext, diffPrev, diffNext, howBtn, settingsBtn, exitBtn};

    private final Map<Level, BufferedImage> backdrops = new EnumMap<>(Level.class);
    private final BufferedImage vignette;
    private final List<double[]> embers = new ArrayList<>();
    private final Random rnd = new Random();
    private Level level;
    private Difficulty difficulty;
    private double time = 0;

    public TitleScreen(GamePanel host) {
        this(host, Level.FOREST_TEMPLE, Difficulty.NORMAL);
    }

    /** Overloading: กลับมาหน้าเมนูโดยจำด่านและระดับความยากเดิมไว้ */
    public TitleScreen(GamePanel host, Level level, Difficulty difficulty) {
        super(host);
        this.level = level;
        this.difficulty = difficulty;
        this.vignette = FieldRenderer.buildVignette(Setting.WINDOW_W, Setting.WINDOW_H);
        for (int i = 0; i < 40; i++) embers.add(newEmber(true));
    }

    /** สร้างภาพพื้นหลังของแต่ละด่านเมื่อจำเป็นเท่านั้น แล้วเก็บไว้ใช้ซ้ำ */
    private BufferedImage backdrop(Level l) {
        return backdrops.computeIfAbsent(l,
                k -> new FieldRenderer(new GameMap(k.getMapResource()), k.getGroundColor()).getBackground());
    }

    private double[] newEmber(boolean anywhere) {
        return new double[]{
            rnd.nextDouble() * Setting.WINDOW_W,
            anywhere ? rnd.nextDouble() * Setting.WINDOW_H : Setting.WINDOW_H + 5,
            10 + rnd.nextDouble() * 25,
            rnd.nextDouble() * Math.PI * 2,
            1 + rnd.nextDouble() * 2
        };
    }

    @Override public void update(double dt) {
        time += dt;
        for (int i = 0; i < embers.size(); i++) {
            double[] e = embers.get(i);
            e[1] -= e[2] * dt;
            e[0] += Math.sin(time + e[3]) * 8 * dt;
            if (e[1] < -5) embers.set(i, newEmber(false));
        }
    }

    @Override public void render(Graphics2D g) {
        g.drawImage(backdrop(level), 0, 0, Setting.WINDOW_W, Setting.WINDOW_H, null);
        g.setColor(new Color(6, 6, 10, 185));
        g.fillRect(0, 0, Setting.WINDOW_W, Setting.WINDOW_H);
        g.drawImage(vignette, 0, 0, null);

        for (double[] e : embers) {
            int a = (int) (120 + 100 * Math.sin(time * 3 + e[3]));
            g.setColor(new Color(255, 200, 110, Math.max(0, Math.min(255, a))));
            int s = (int) e[4];
            g.fillOval((int) e[0], (int) e[1], s + 1, s + 1);
        }

        drawYantra(g, CX, 150, 112);

        g.setFont(Fonts.bold(58));
        String title = "ยันต์ล้อมวัด";
        int tw = g.getFontMetrics().stringWidth(title);
        g.setColor(new Color(0, 0, 0, 170));
        g.drawString(title, CX - tw / 2 + 3, 176);
        g.setPaint(new GradientPaint(0, 120, new Color(0xFFF0C0), 0, 180, new Color(0xD9A640)));
        g.drawString(title, CX - tw / 2, 173);

        g.setFont(Fonts.get(14));
        g.setColor(new Color(0xB8A478));
        Fonts.drawCentered(g, "Y A N T R A   W A R D   ·   T E M P L E   D E F E N S E", CX, 206);
        g.setFont(Fonts.get(13));
        g.setColor(new Color(0x8A7E62));
        Fonts.drawCentered(g, "ปกป้องพระอุโบสถจากวิญญาณ 15 คลื่น ด้วยเครื่องรางของหลวงพ่อ", CX, 234);

        for (Button b : buttons) b.render(g);
        drawSelector(g, ROW_LEVEL, "ด่าน " + level.getNumber() + ": " + level.getThaiName());
        drawSelector(g, ROW_DIFF, "ความยาก: " + difficulty.getThaiName());

        g.setFont(Fonts.get(12));
        if (difficulty.isOneLife()) {
            g.setColor(new Color(0xFF6A6A));
            Fonts.drawCentered(g, "โหมดฝันร้าย: ผีเข้าวัดแม้แต่ตนเดียว = แพ้ทันที  ·  คะแนน ×"
                                  + difficulty.getScoreMultiplier(), CX, 504);
        } else {
            g.setColor(new Color(0x9C9078));
            Fonts.drawCentered(g, level.getDescription(), CX, 504);
        }

        int best = host.getScores().getBest(level, difficulty);
        g.setFont(Fonts.get(13));
        g.setColor(new Color(0xFFD98E));
        Fonts.drawCentered(g, best > 0 ? String.format("คะแนนสูงสุด (ด่าน %d · %s): %,d",
                                                       level.getNumber(), difficulty.getThaiName(), best)
                                       : "ยังไม่มีคะแนนในด่านและระดับนี้", CX, 530);

        g.setFont(Fonts.get(11));
        g.setColor(new Color(0x6A624E));
        Fonts.drawCentered(g, "Enter = เริ่ม  ·  บน/ล่าง = ด่าน  ·  ซ้าย/ขวา = ความยาก  ·  H = วิธีเล่น  ·  S = ตั้งค่าเสียง", CX, 560);
        g.setColor(new Color(0x5A5344));
        Fonts.drawCentered(g, "040613204 Object-Oriented Programming  ·  มจพ.  ·  กฤตภาส ชูภักดี", CX, 590);
    }

    private void drawSelector(Graphics2D g, int y, String text) {
        boolean danger = y == ROW_DIFF && difficulty.isOneLife();
        g.setColor(danger ? new Color(0x2A1216) : new Color(0x1C1A16));
        g.fillRoundRect(CX - 88, y, 176, 32, 8, 8);
        g.setColor(danger ? new Color(0xB04050) : new Color(0x5C5240));
        g.drawRoundRect(CX - 88, y, 176, 32, 8, 8);
        g.setFont(Fonts.get(14));
        g.setColor(danger ? new Color(0xFF9A9A) : new Color(0xDCCFAE));
        Fonts.drawCentered(g, text, CX, y + 21);
    }

    private void drawYantra(Graphics2D g, int cx, int cy, int r) {
        Stroke old = g.getStroke();
        g.setStroke(new BasicStroke(1.4f));
        int glow = (int) (40 + 20 * Math.sin(time * 1.5));
        g.setColor(new Color(217, 178, 74, glow));
        g.drawOval(cx - r, cy - r, r * 2, r * 2);
        g.drawOval(cx - r + 12, cy - r + 12, (r - 12) * 2, (r - 12) * 2);
        for (int k = 0; k < 2; k++) {
            double rot = (k == 0 ? 1 : -1) * time * 0.15 + (k == 0 ? -Math.PI / 2 : Math.PI / 2);
            int[] xs = new int[3], ys = new int[3];
            for (int i = 0; i < 3; i++) {
                double a = rot + i * Math.PI * 2 / 3;
                xs[i] = cx + (int) (Math.cos(a) * (r - 14));
                ys[i] = cy + (int) (Math.sin(a) * (r - 14));
            }
            g.drawPolygon(xs, ys, 3);
        }
        for (int i = 0; i < 16; i++) {
            double a = i * Math.PI / 8 + time * 0.05;
            int x1 = cx + (int) (Math.cos(a) * r), y1 = cy + (int) (Math.sin(a) * r);
            int x2 = cx + (int) (Math.cos(a) * (r + 10)), y2 = cy + (int) (Math.sin(a) * (r + 10));
            g.drawLine(x1, y1, x2, y2);
        }
        g.setStroke(old);
    }

    @Override public void onMouseMoved(int x, int y) {
        super.onMouseMoved(x, y);
        for (Button b : buttons) b.updateHover(x, y);
    }

    @Override public void onMousePressed(int x, int y, int button) {
        if (startBtn.contains(x, y))       startGame();
        else if (levelPrev.contains(x, y)) changeLevel(-1);
        else if (levelNext.contains(x, y)) changeLevel(1);
        else if (diffPrev.contains(x, y))  changeDifficulty(-1);
        else if (diffNext.contains(x, y))  changeDifficulty(1);
        else if (howBtn.contains(x, y))    openHowTo();
        else if (settingsBtn.contains(x, y)) openSettings();
        else if (exitBtn.contains(x, y))   host.exitGame();
    }

    @Override public void onKeyPressed(int key) {
        switch (key) {
            case KeyEvent.VK_ENTER: case KeyEvent.VK_SPACE: startGame(); break;
            case KeyEvent.VK_UP:    changeLevel(-1); break;
            case KeyEvent.VK_DOWN:  changeLevel(1);  break;
            case KeyEvent.VK_LEFT:  changeDifficulty(-1); break;
            case KeyEvent.VK_RIGHT: changeDifficulty(1);  break;
            case KeyEvent.VK_H:     openHowTo(); break;
            case KeyEvent.VK_S:     openSettings(); break;
            default: break;
        }
    }

    private void changeLevel(int dir) {
        level = level.cycle(dir);
        host.getSound().play(Sfx.CLICK);
    }

    private void changeDifficulty(int dir) {
        difficulty = dir > 0 ? difficulty.next() : difficulty.prev();
        host.getSound().play(Sfx.CLICK);
    }

    private void startGame() {
        host.getSound().play(Sfx.WAVE);
        host.setScreen(new PlayScreen(host, level, difficulty));
    }

    private void openSettings() {
        host.getSound().play(Sfx.CLICK);
        host.setScreen(new SettingsScreen(host, this));
    }

    private void openHowTo() {
        host.getSound().play(Sfx.CLICK);
        host.setScreen(new HowToScreen(host, level, difficulty));
    }
}
