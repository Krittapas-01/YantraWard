package yantra.screen;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import yantra.core.GamePanel;
import yantra.core.Setting;
import yantra.entity.Enemy;
import yantra.entity.EnemyType;
import yantra.entity.Tower;
import yantra.entity.TowerType;
import yantra.ui.Button;
import yantra.ui.FieldRenderer;
import yantra.ui.Fonts;
import yantra.util.Difficulty;
import yantra.util.Level;
import yantra.util.Sfx;
import yantra.util.Vec2;

/** หน้าวิธีเล่น — ใช้ตัวป้อมและตัวผีจริงมาวาดเป็นไอคอน */
public class HowToScreen extends Screen {

    private final Level level;
    private final Difficulty difficulty;
    private final Button backBtn = new Button("กลับเมนู", Setting.WINDOW_W / 2 - 90, 548, 180, 38, Button.Style.PRIMARY);
    private final BufferedImage vignette = FieldRenderer.buildVignette(Setting.WINDOW_W, Setting.WINDOW_H);
    private final Map<TowerType, Tower> towerIcons = new EnumMap<>(TowerType.class);
    private final Map<EnemyType, Enemy> enemyIcons = new EnumMap<>(EnemyType.class);

    public HowToScreen(GamePanel host, Level level, Difficulty difficulty) {
        super(host);
        this.level = level;
        this.difficulty = difficulty;
        List<Vec2<Integer>> dummy = Arrays.asList(new Vec2<>(0, 0), new Vec2<>(1, 0));
        for (TowerType t : TowerType.values()) towerIcons.put(t, t.create(-5, -5));
        for (EnemyType e : EnemyType.values()) enemyIcons.put(e, e.create(dummy, 1));
    }

    @Override public void update(double dt) {
        for (Enemy e : enemyIcons.values()) e.tick(dt);  // เดินอายุไปเรื่อย ๆ ให้แอนิเมชันขยับ
    }

    @Override public void render(Graphics2D g) {
        g.setColor(new Color(0x12100C));
        g.fillRect(0, 0, Setting.WINDOW_W, Setting.WINDOW_H);
        g.drawImage(vignette, 0, 0, null);

        g.setFont(Fonts.bold(30));
        g.setColor(new Color(0xFFE39A));
        Fonts.drawCentered(g, "วิธีเล่น", Setting.WINDOW_W / 2, 52);

        // ----------------- คอลัมน์ซ้าย -----------------
        int x = 36, y = 96;
        heading(g, "เป้าหมาย", x, y);
        y += 22;
        g.setFont(Fonts.get(13));
        g.setColor(new Color(0xCFC6AE));
        String goal = "วิญญาณจะเดินจากป่าช้าไปยังพระอุโบสถ วางเครื่องรางข้างทางเพื่อสกัดไว้ "
                    + "ผีทุกตนที่ไปถึงจะทำให้เสียดวงวิญญาณ ถ้าดวงวิญญาณหมดคือแพ้ รอดครบ 15 คลื่นคือชนะ";
        for (String line : Fonts.wrap(goal, g.getFontMetrics(), 320)) { g.drawString(line, x, y); y += 19; }

        y += 16;
        heading(g, "การควบคุม", x, y);
        y += 24;
        String[][] keys = {
            {"คลิกซ้าย", "วางเครื่องราง / เลือกเครื่องรางที่วางแล้ว"},
            {"คลิกขวา", "ยกเลิกการเลือก"},
            {"1 – 4", "เลือกชนิดเครื่องราง"},
            {"U / X", "อัปเกรด / ขายคืน"},
            {"Space", "เรียกคลื่นถัดไปทันที (ได้โบนัส)"},
            {"F", "เร่งความเร็ว ×2 / ×4"},
            {"P / Esc", "หยุดเกมชั่วคราว"},
            {"M", "เปิด / ปิดเสียง"},
        };
        for (String[] k : keys) {
            g.setFont(Fonts.bold(12));
            g.setColor(new Color(0x2A251C));
            int kw = g.getFontMetrics().stringWidth(k[0]) + 14;
            g.fillRoundRect(x, y - 13, kw, 19, 6, 6);
            g.setColor(new Color(0xFFD98E));
            g.drawString(k[0], x + 7, y + 1);
            g.setFont(Fonts.get(13));
            g.setColor(new Color(0xCFC6AE));
            g.drawString(k[1], x + 82, y + 1);
            y += 26;
        }

        g.setFont(Fonts.get(12));
        g.setColor(new Color(0x9C8F70));
        y += 8;
        String tip = "เคล็ดลับ: ตะกรุดยิงผีกระสือที่ลอยอยู่ไม่ได้ อย่าลืมวางระฆังหรือน้ำมนต์ไว้ใกล้พระอุโบสถ";
        for (String line : Fonts.wrap(tip, g.getFontMetrics(), 320)) { g.drawString(line, x, y); y += 17; }

        // ----------------- คอลัมน์ขวา -----------------
        x = 404; y = 96;
        heading(g, "เครื่องราง", x, y);
        y += 16;
        for (TowerType t : TowerType.values()) {
            towerIcons.get(t).renderIcon(g, x + 18, y + 16);
            g.setFont(Fonts.bold(14));
            g.setColor(new Color(0xE8D9A8));
            g.drawString(t.getThaiName() + "  ·  " + t.getCost(), x + 44, y + 12);
            g.setFont(Fonts.get(12));
            g.setColor(new Color(0xA89E86));
            g.drawString(t.getDescription(), x + 44, y + 29);
            y += 42;
        }

        y += 10;
        heading(g, "วิญญาณ", x, y);
        y += 16;
        for (EnemyType e : EnemyType.values()) {
            drawEnemyIcon(g, enemyIcons.get(e), x + 18, y + 16);
            g.setFont(Fonts.bold(14));
            g.setColor(new Color(0xE8D9A8));
            g.drawString(e.getThaiName(), x + 44, y + 12);
            g.setFont(Fonts.get(12));
            g.setColor(new Color(0xA89E86));
            g.drawString(e.getDescription(), x + 44, y + 29);
            y += 42;
        }

        backBtn.render(g);
    }

    /** วาดผีลงในกรอบสี่เหลี่ยมขนาดเท่าไอคอนป้อม (ย่อบอสให้พอดีกรอบ) */
    private void drawEnemyIcon(Graphics2D g, Enemy e, int cx, int cy) {
        g.setColor(new Color(0x221E18));
        g.fillRoundRect(cx - 15, cy - 15, 30, 30, 8, 8);
        g.setColor(new Color(0x4A4234));
        g.drawRoundRect(cx - 15, cy - 15, 30, 30, 8, 8);
        Graphics2D gi = (Graphics2D) g.create();
        gi.clip(new java.awt.geom.RoundRectangle2D.Double(cx - 15, cy - 15, 30, 30, 8, 8));
        gi.translate(cx, cy);
        double s = e.isBoss() ? 0.55 : e.isFlying() ? 0.9 : 0.85;
        gi.scale(s, s);
        e.renderIcon(gi, 0, e.isBoss() ? 10 : 5);
        gi.dispose();
    }

    private void heading(Graphics2D g, String s, int x, int y) {
        g.setFont(Fonts.bold(16));
        g.setColor(new Color(0xD9B24A));
        g.drawString(s, x, y);
        g.setColor(new Color(0x4A4030));
        g.drawLine(x, y + 6, x + 340, y + 6);
    }

    @Override public void onMouseMoved(int x, int y) {
        super.onMouseMoved(x, y);
        backBtn.updateHover(x, y);
    }

    @Override public void onMousePressed(int x, int y, int button) {
        if (backBtn.contains(x, y)) back();
    }

    @Override public void onKeyPressed(int key) {
        if (key == KeyEvent.VK_ESCAPE || key == KeyEvent.VK_BACK_SPACE || key == KeyEvent.VK_ENTER) back();
    }

    private void back() {
        host.getSound().play(Sfx.CLICK);
        host.setScreen(new TitleScreen(host, level, difficulty));
    }
}
