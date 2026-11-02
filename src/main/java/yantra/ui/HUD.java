package yantra.ui;

import java.awt.*;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import yantra.core.GameManager;
import yantra.core.Setting;
import yantra.entity.Enemy;
import yantra.entity.Tower;
import yantra.entity.TowerType;

/** ส่วนแสดงข้อมูลของหน้าเล่นเกม: แถบบน แถบเลือกเครื่องราง แผงป้อม ป้ายประกาศ */
public class HUD {

    /** PANEL = คลิกโดนพื้นที่แผงป้อม (กินคลิกไว้ ไม่ให้ทะลุไปวางป้อม) */
    public enum Action { NONE, NEXT_WAVE, SPEED, MUTE, PAUSE, UPGRADE, SELL, PANEL }

    private static final Color BAR_BG  = new Color(0x100E0B);
    private static final Color GOLD    = new Color(0xFFD98E);
    private static final Color CREAM   = new Color(0xE8D9A8);
    private static final Color DIM     = new Color(0x8A8068);

    private static final int BAR_Y  = Setting.FIELD_Y + Setting.PLAY_H;
    private static final int SLOT_W = 138;
    private static final int SLOT_H = 48;
    private static final int PANEL_W = 224;
    private static final int PANEL_H = 214;

    private final Button nextWaveBtn = new Button("เรียกคลื่น", 496, 14, 118, 32, Button.Style.PRIMARY);
    private final Button speedBtn    = new Button("×1", 620, 14, 52, 32);
    private final Button muteBtn     = new Button("เสียง", 678, 14, 56, 32);
    private final Button pauseBtn    = new Button("หยุด", 740, 14, 52, 32);
    private final Button upgradeBtn  = new Button("อัปเกรด", 0, 0, PANEL_W - 24, 30, Button.Style.PRIMARY);
    private final Button sellBtn     = new Button("ขาย", 0, 0, PANEL_W - 24, 26, Button.Style.DANGER);

    private final Map<TowerType, Tower> previews = new EnumMap<>(TowerType.class);
    private int mouseX = -1, mouseY = -1;

    public HUD() {
        for (TowerType t : TowerType.values()) previews.put(t, t.create(-5, -5));
    }

    public void onMouseMoved(int x, int y) {
        mouseX = x; mouseY = y;
        for (Button b : new Button[]{nextWaveBtn, speedBtn, muteBtn, pauseBtn, upgradeBtn, sellBtn}) b.updateHover(x, y);
    }

    // =====================================================================
    //                               Hit test
    // =====================================================================

    public Action actionAt(int x, int y, GameManager gm) {
        if (nextWaveBtn.contains(x, y)) return Action.NEXT_WAVE;
        if (speedBtn.contains(x, y))    return Action.SPEED;
        if (muteBtn.contains(x, y))     return Action.MUTE;
        if (pauseBtn.contains(x, y))    return Action.PAUSE;
        if (gm.getSelectedTower() != null) {
            if (upgradeBtn.contains(x, y)) return Action.UPGRADE;
            if (sellBtn.contains(x, y))    return Action.SELL;
            if (panelRect(gm).contains(x, y)) return Action.PANEL;
        }
        if (gm.getInfoType() != null && infoRect().contains(x, y)) return Action.PANEL;
        return Action.NONE;
    }

    public boolean panelContains(int x, int y, GameManager gm) {
        if (gm.getSelectedTower() != null && panelRect(gm).contains(x, y)) return true;
        return gm.getInfoType() != null && infoRect().contains(x, y);
    }

    /** แผงรายละเอียดของเครื่องรางในร้าน อยู่มุมบนซ้ายเสมอ */
    private Rectangle infoRect() {
        return new Rectangle(12, Setting.FIELD_Y + 12, PANEL_W, 206);
    }

    public TowerType slotAt(int x, int y) {
        if (y < BAR_Y + 8 || y > BAR_Y + 8 + SLOT_H) return null;
        for (TowerType t : TowerType.values()) {
            int sx = 10 + t.ordinal() * (SLOT_W + 8);
            if (x >= sx && x <= sx + SLOT_W) return t;
        }
        return null;
    }

    private Rectangle panelRect(GameManager gm) {
        Tower t = gm.getSelectedTower();
        int px = (t != null && t.getX() > Setting.PLAY_W / 2.0) ? 12 : Setting.PLAY_W - PANEL_W - 12;
        return new Rectangle(px, Setting.FIELD_Y + 12, PANEL_W, PANEL_H);
    }

    // =====================================================================
    //                               แถบบน
    // =====================================================================

    public void renderTop(Graphics2D g, GameManager gm, int fps) {
        g.setPaint(new GradientPaint(0, 0, new Color(0x1A1712), 0, Setting.HUD_H, BAR_BG));
        g.fillRect(0, 0, Setting.WINDOW_W, Setting.HUD_H);
        g.setColor(new Color(0x3A3226));
        g.drawLine(0, Setting.HUD_H - 1, Setting.WINDOW_W, Setting.HUD_H - 1);

        // ศรัทธา
        drawLotus(g, 22, 30);
        g.setFont(Fonts.get(11));
        g.setColor(DIM);
        g.drawString("ศรัทธา", 40, 22);
        g.setFont(Fonts.bold(20));
        g.setColor(GOLD);
        g.drawString(String.valueOf(gm.getEconomy().getFaith()), 40, 45);

        // ดวงวิญญาณ
        drawFlame(g, 142, 30);
        g.setFont(Fonts.get(11));
        g.setColor(DIM);
        g.drawString("ดวงวิญญาณ", 158, 22);
        g.setFont(Fonts.bold(20));
        if (gm.getDifficulty().isOneLife()) {
            int pulse = (int) (150 + 105 * Math.abs(Math.sin(gm.getElapsed() * 3)));
            g.setColor(new Color(255, 70, 70, pulse));
            g.drawString("ห้ามพลาด", 158, 45);
        } else {
            g.setColor(gm.getLives() > 5 ? new Color(0xFFB0A0) : new Color(0xFF5A5A));
            g.drawString(gm.getLives() + " / " + gm.getDifficulty().getStartLives(), 158, 45);
        }

        // คลื่น
        g.setFont(Fonts.get(11));
        g.setColor(DIM);
        g.drawString("คลื่น", 272, 22);
        g.setFont(Fonts.bold(20));
        g.setColor(new Color(0xCFE3B5));
        g.drawString(gm.getWaves().getCurrentWave() + " / " + gm.getWaves().getTotalWaves(), 272, 45);

        g.setFont(Fonts.get(12));
        String status;
        if (gm.getWaves().isRunning()) {
            status = "กำลังบุก · เหลือ " + (gm.getEnemies().size() + gm.getWaves().getQueued()) + " ตน";
            g.setColor(new Color(0xE0A0A0));
        } else if (gm.getWaves().isFinished()) {
            status = "ครบทุกคลื่นแล้ว";
            g.setColor(DIM);
        } else {
            status = String.format("คลื่นถัดไปใน %.0f วิ", Math.ceil(gm.getWaves().getRestTimer()));
            g.setColor(CREAM);
        }
        g.drawString(status, 345, 36);
        g.setFont(Fonts.get(10));
        g.setColor(new Color(0x5A5446));
        int faster = (int) Math.round((gm.getWaves().getSpeedMultiplier() - 1) * 100);
        g.drawString("ด่าน " + gm.getLevel().getNumber() + " · " + gm.getDifficulty().getThaiName()
                     + (faster > 0 ? " · ผีเร็ว +" + faster + "%" : "")
                     + (fps > 0 ? " · " + fps + " FPS" : ""), 345, 50);

        nextWaveBtn.setEnabled(gm.getWaves().canCallEarly());
        speedBtn.setLabel("×" + gm.getSpeed());
        speedBtn.setStyle(gm.getSpeed() > 1 ? Button.Style.PRIMARY : Button.Style.SECONDARY);
        muteBtn.setLabel(gm.isMuted() ? "เงียบ" : "เสียง");
        nextWaveBtn.render(g);
        speedBtn.render(g);
        muteBtn.render(g);
        pauseBtn.render(g);
    }

    private void drawLotus(Graphics2D g, int cx, int cy) {
        g.setColor(new Color(0xE8A0B0));
        g.fillOval(cx - 3, cy - 10, 6, 14);
        g.setColor(new Color(0xD08098));
        g.fillOval(cx - 9, cy - 6, 8, 11);
        g.fillOval(cx + 1, cy - 6, 8, 11);
        g.setColor(new Color(0xD9B24A));
        g.fillRect(cx - 7, cy + 4, 14, 3);
    }

    private void drawFlame(Graphics2D g, int cx, int cy) {
        g.setColor(new Color(0xE8DCC0));
        g.fillRect(cx - 3, cy, 6, 10);
        g.setColor(new Color(0xFFB050));
        g.fillOval(cx - 4, cy - 11, 8, 12);
        g.setColor(new Color(0xFFF0B0));
        g.fillOval(cx - 2, cy - 6, 4, 6);
    }

    // =====================================================================
    //                            แถบเลือกเครื่องราง
    // =====================================================================

    public void renderBottom(Graphics2D g, GameManager gm) {
        g.setColor(BAR_BG);
        g.fillRect(0, BAR_Y, Setting.WINDOW_W, Setting.BAR_H);
        g.setColor(new Color(0x3A3226));
        g.drawLine(0, BAR_Y, Setting.WINDOW_W, BAR_Y);

        for (TowerType t : TowerType.values()) {
            int sx = 10 + t.ordinal() * (SLOT_W + 8), sy = BAR_Y + 8;
            boolean sel   = gm.getSelectedType() == t;
            int price     = gm.getPrice(t);
            boolean poor  = !gm.getEconomy().canAfford(price);
            boolean hover = mouseX >= sx && mouseX <= sx + SLOT_W && mouseY >= sy && mouseY <= sy + SLOT_H;

            g.setColor(sel ? new Color(0x2E2616) : hover ? new Color(0x221F19) : new Color(0x18160F));
            g.fillRoundRect(sx, sy, SLOT_W, SLOT_H, 8, 8);
            g.setColor(sel ? t.getColor() : new Color(0x4A4234));
            g.setStroke(new BasicStroke(sel ? 2f : 1f));
            g.drawRoundRect(sx, sy, SLOT_W, SLOT_H, 8, 8);
            g.setStroke(new BasicStroke(1f));

            Composite old = g.getComposite();
            if (poor) g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
            previews.get(t).renderIcon(g, sx + 25, sy + 24);
            g.setFont(Fonts.bold(15));
            g.setColor(CREAM);
            g.drawString(t.getThaiName(), sx + 48, sy + 22);
            g.setFont(Fonts.get(12));
            g.setColor(price > t.getCost() ? new Color(0xFF8A50) : GOLD);   // สีส้ม = แพงขึ้นกว่าราคาตั้งต้น
            g.drawString(price + " ศรัทธา", sx + 48, sy + 39);
            g.setComposite(old);

            g.setFont(Fonts.get(10));
            g.setColor(DIM);
            g.drawString(String.valueOf(t.ordinal() + 1), sx + SLOT_W - 12, sy + 13);
        }

        int hx = 10 + 4 * (SLOT_W + 8) + 4;
        g.setFont(Fonts.get(11));
        g.setColor(DIM);
        g.drawString("คลิกซ้ำ = ดูรายละเอียด", hx, BAR_Y + 22);
        g.drawString("คลิกขวา = ยกเลิก", hx, BAR_Y + 37);
        g.drawString("U อัปเกรด · X ขาย · F เร่ง", hx, BAR_Y + 52);
    }

    // =====================================================================
    //                              แผงป้อม
    // =====================================================================

    public void renderTowerPanel(Graphics2D g, GameManager gm) {
        Tower t = gm.getSelectedTower();
        upgradeBtn.setVisible(t != null);
        sellBtn.setVisible(t != null);
        if (t == null) {
            if (gm.getInfoType() != null) renderShopInfo(g, gm, gm.getInfoType());
            return;
        }

        Rectangle r = panelRect(gm);
        g.setColor(new Color(16, 14, 11, 238));
        g.fillRoundRect(r.x, r.y, r.width, r.height, 10, 10);
        g.setColor(t.getType().getColor());
        g.drawRoundRect(r.x, r.y, r.width, r.height, 10, 10);

        t.renderIcon(g, r.x + 26, r.y + 28);
        g.setFont(Fonts.bold(17));
        g.setColor(CREAM);
        g.drawString(t.getType().getThaiName(), r.x + 50, r.y + 26);
        for (int i = 0; i < t.getMaxLevel(); i++) {
            g.setColor(i < t.getLevel() ? GOLD : new Color(0x3A342A));
            g.fillOval(r.x + 51 + i * 13, r.y + 34, 9, 9);
        }
        if (t.isDisabled()) {
            g.setFont(Fonts.get(11));
            g.setColor(new Color(0xD0A0FF));
            g.drawString("ถูกสาป!", r.x + 96, r.y + 43);
        }

        g.setFont(Fonts.get(11));
        g.setColor(DIM);
        int y = r.y + 62;
        for (String line : Fonts.wrap(t.getType().getDescription(), g.getFontMetrics(), r.width - 24)) {
            g.drawString(line, r.x + 12, y);
            y += 14;
        }
        y += 6;

        g.setFont(Fonts.get(13));
        List<String[]> stats = t.getStatLines();
        for (String[] s : stats) {
            g.setColor(new Color(0xCFC6AE));
            g.drawString(s[0], r.x + 12, y);
            g.setColor(Color.WHITE);
            Fonts.drawRight(g, s[1], r.x + r.width - 12, y);
            y += 18;
        }

        int by = r.y + r.height - 66;
        upgradeBtn.setLocation(r.x + 12, by);
        sellBtn.setLocation(r.x + 12, by + 36);
        if (t.canUpgrade()) {
            upgradeBtn.setLabel("อัปเกรด  -" + t.getUpgradeCost());
            upgradeBtn.setEnabled(gm.getEconomy().canAfford(t.getUpgradeCost()));
        } else {
            upgradeBtn.setLabel("เต็มระดับแล้ว");
            upgradeBtn.setEnabled(false);
        }
        sellBtn.setLabel("ขายคืน  +" + t.getSellValue());
        upgradeBtn.render(g);
        sellBtn.render(g);
    }

    /** แผงรายละเอียดของเครื่องรางที่ยังไม่ได้ซื้อ — ใช้ตัวอย่างระดับ 1 มาแสดงค่าจริง */
    private void renderShopInfo(Graphics2D g, GameManager gm, TowerType type) {
        Tower preview = previews.get(type);
        Rectangle r = infoRect();
        g.setColor(new Color(16, 14, 11, 238));
        g.fillRoundRect(r.x, r.y, r.width, r.height, 10, 10);
        g.setColor(type.getColor());
        g.drawRoundRect(r.x, r.y, r.width, r.height, 10, 10);

        preview.renderIcon(g, r.x + 26, r.y + 28);
        g.setFont(Fonts.bold(17));
        g.setColor(CREAM);
        g.drawString(type.getThaiName(), r.x + 50, r.y + 26);
        g.setFont(Fonts.get(12));
        int price = gm.getPrice(type);
        boolean afford = gm.getEconomy().canAfford(price);
        g.setColor(afford ? GOLD : new Color(0xE07070));
        g.drawString(price + " ศรัทธา" + (afford ? "" : "  (ยังไม่พอ)"), r.x + 51, r.y + 43);

        g.setFont(Fonts.get(11));
        g.setColor(DIM);
        int y = r.y + 62;
        for (String line : Fonts.wrap(type.getDescription(), g.getFontMetrics(), r.width - 24)) {
            g.drawString(line, r.x + 12, y);
            y += 14;
        }
        y += 6;

        g.setFont(Fonts.get(13));
        boolean hasFlyLine = false;
        for (String[] s : preview.getStatLines()) {
            if (s[0].contains("ผีลอย")) hasFlyLine = true;
            drawStat(g, r, s[0], s[1], y, Color.WHITE);
            y += 18;
        }
        if (!hasFlyLine) {
            boolean fly = preview.canTargetFlying();
            drawStat(g, r, "โจมตีผีลอย", fly ? "ได้" : "ไม่ได้", y, fly ? new Color(0x9FE0A0) : new Color(0xE08080));
            y += 18;
        }

        g.setFont(Fonts.get(11));
        g.setColor(new Color(0x7A7058));
        int owned = gm.countOf(type);
        long step = Math.round(gm.getDifficulty().getPriceStep() * 100);
        String note;
        if (step == 0)       note = "อัปเกรดได้ " + preview.getMaxLevel() + " ระดับ · คลิกช่องว่างเพื่อวาง";
        else if (owned > 0)  note = "มีอยู่ " + owned + " ตัว · สร้างเพิ่มแพงขึ้นทีละ " + step + "%";
        else                 note = "สร้างซ้ำชนิดเดิม ราคาจะแพงขึ้นทีละ " + step + "%";
        Fonts.drawCentered(g, note, r.x + r.width / 2, r.y + r.height - 12);
    }

    private void drawStat(Graphics2D g, Rectangle r, String label, String value, int y, Color valueColor) {
        g.setColor(new Color(0xCFC6AE));
        g.drawString(label, r.x + 12, y);
        g.setColor(valueColor);
        Fonts.drawRight(g, value, r.x + r.width - 12, y);
    }

    // =====================================================================
    //                        ข้อความแจ้งเตือน / ป้ายประกาศ
    // =====================================================================

    public void renderMessages(Graphics2D g, GameManager gm) {
        if (gm.getWaves().getCurrentWave() == 0) {
            int w = 560, h = 58, x = (Setting.WINDOW_W - w) / 2, y = Setting.FIELD_Y + Setting.PLAY_H - h - 14;
            g.setColor(new Color(16, 14, 11, 225));
            g.fillRoundRect(x, y, w, h, 12, 12);
            g.setColor(new Color(0x6A5A3A));
            g.drawRoundRect(x, y, w, h, 12, 12);
            g.setFont(Fonts.bold(15));
            g.setColor(GOLD);
            Fonts.drawCentered(g, "ด่าน " + gm.getLevel().getNumber() + " : " + gm.getLevel().getThaiName(),
                               Setting.WINDOW_W / 2, y + 22);
            g.setFont(Fonts.get(12));
            g.setColor(CREAM);
            Fonts.drawCentered(g, gm.getLevel().getTip(), Setting.WINDOW_W / 2, y + 44);
        }

        String toast = gm.getToast();
        if (!toast.isEmpty()) {
            g.setFont(Fonts.get(14));
            int w = g.getFontMetrics().stringWidth(toast) + 30;
            int x = (Setting.WINDOW_W - w) / 2, y = Setting.FIELD_Y + Setting.PLAY_H - 44;
            int a = (int) (230 * gm.getToastAlpha());
            g.setColor(new Color(16, 14, 11, a));
            g.fillRoundRect(x, y, w, 28, 14, 14);
            g.setColor(new Color(232, 217, 168, a));
            Fonts.drawCentered(g, toast, Setting.WINDOW_W / 2, y + 19);
        }

        String banner = gm.getBanner();
        if (!banner.isEmpty()) {
            double a = Math.max(0, gm.getBannerAlpha());
            int cy = Setting.FIELD_Y + 150;
            g.setColor(new Color(0, 0, 0, (int) (150 * a)));
            g.fillRect(0, cy - 34, Setting.WINDOW_W, 54);
            Color c = gm.getBannerColor();
            g.setFont(Fonts.bold(30));
            g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (255 * a)));
            Fonts.drawCentered(g, banner, Setting.WINDOW_W / 2, cy + 4);
        }

        Enemy boss = gm.getBoss();
        if (boss != null) {
            int w = 360, x = (Setting.WINDOW_W - w) / 2, y = Setting.FIELD_Y + 10;
            g.setColor(new Color(16, 10, 20, 220));
            g.fillRoundRect(x - 10, y - 4, w + 20, 34, 10, 10);
            g.setFont(Fonts.bold(12));
            g.setColor(new Color(0xE0B0F0));
            g.drawString("แม่นาค", x, y + 11);
            g.setColor(new Color(0x2A1830));
            g.fillRect(x, y + 16, w, 8);
            g.setColor(new Color(0xB04090));
            g.fillRect(x, y + 16, (int) (w * boss.getHp() / boss.getMaxHp()), 8);
            g.setColor(new Color(0x70406A));
            g.drawRect(x, y + 16, w, 8);
        }
    }
}
