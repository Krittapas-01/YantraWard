package yantra.screen;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import yantra.core.GameManager;
import yantra.core.GamePanel;
import yantra.core.Setting;
import yantra.effect.AreaEffect;
import yantra.effect.Effect;
import yantra.entity.Enemy;
import yantra.entity.Projectile;
import yantra.entity.Tower;
import yantra.entity.TowerType;
import yantra.ui.Button;
import yantra.ui.FieldRenderer;
import yantra.ui.Fonts;
import yantra.ui.HUD;
import yantra.util.Difficulty;
import yantra.util.GameState;
import yantra.util.Level;
import yantra.util.Sfx;

/** หน้าเล่นเกม — แปลง input เป็นคำสั่งให้ GameManager และวาดสนามทั้งหมด */
public class PlayScreen extends Screen {

    private static final int CX = Setting.WINDOW_W / 2;

    private final Level level;
    private final Difficulty difficulty;
    private final GameManager game;
    private final FieldRenderer field;
    private final HUD hud = new HUD();

    private final Button resumeBtn   = new Button("เล่นต่อ",      CX - 110, 280, 220, 40, Button.Style.PRIMARY);
    private final Button settingsBtn = new Button("ตั้งค่าเสียง",  CX - 110, 330, 220, 36);
    private final Button restartBtn  = new Button("เริ่มใหม่",     CX - 110, 376, 220, 36);
    private final Button menuBtn     = new Button("กลับเมนูหลัก", CX - 110, 422, 220, 36);
    private final Button againBtn   = new Button("เล่นอีกครั้ง",  CX - 290, 486, 180, 42);
    private final Button nextBtn    = new Button("ด่านถัดไป",    CX - 90,  486, 180, 42, Button.Style.PRIMARY);
    private final Button endMenuBtn = new Button("กลับเมนูหลัก", CX + 110, 486, 180, 42);

    private int hoverCol = -1;
    private int hoverRow = -1;

    public PlayScreen(GamePanel host, Level level, Difficulty difficulty) {
        super(host);
        this.level = level;
        this.difficulty = difficulty;
        this.game  = new GameManager(level, difficulty, host.getSound(), host.getScores());
        this.field = new FieldRenderer(game.getMap(), level.getGroundColor());
    }

    public GameManager getGame() { return game; }

    @Override public void update(double dt) {
        game.update(dt);
    }

    // =====================================================================
    //                                การวาด
    // =====================================================================

    @Override public void render(Graphics2D g) {
        double shake = game.getShake();
        int sx = shake > 0 ? (int) ((Math.random() - 0.5) * shake) : 0;
        int sy = shake > 0 ? (int) ((Math.random() - 0.5) * shake) : 0;

        Graphics2D f = (Graphics2D) g.create();
        f.translate(sx, Setting.FIELD_Y + sy);
        f.clipRect(-sx, -sy, Setting.PLAY_W, Setting.PLAY_H);
        f.drawImage(field.getBackground(), 0, 0, null);
        drawField(f);
        f.drawImage(field.getVignette(), 0, 0, null);
        if (difficulty.isOneLife()) {                 // โหมดฝันร้าย: ย้อมแดงทั้งสนาม
            f.setColor(new Color(120, 0, 10, 28));
            f.fillRect(0, 0, Setting.PLAY_W, Setting.PLAY_H);
        }
        f.dispose();

        hud.renderTop(g, game, host.getFps());
        hud.renderBottom(g, game);
        hud.renderMessages(g, game);
        hud.renderTowerPanel(g, game);

        if (game.getState() == GameState.PAUSED) drawPause(g);
        if (game.getState() == GameState.WIN || game.getState() == GameState.LOSE) drawResult(g);
    }

    private void drawField(Graphics2D g) {
        for (AreaEffect a : game.getAreas()) a.render(g);

        Tower sel = game.getSelectedTower();
        if (sel != null) sel.renderRange(g);
        drawPlacementPreview(g);

        for (Tower t : game.getTowers()) t.render(g);

        // เรียงตามแกน y ให้ตัวที่อยู่ล่างบังตัวที่อยู่บน และให้ผีลอยอยู่บนสุด
        List<Enemy> sorted = new ArrayList<>(game.getEnemies());
        sorted.sort(Comparator.comparingDouble(e -> e.getY() + (e.isFlying() ? 1000 : 0)));
        for (Enemy e : sorted) e.render(g);

        for (Projectile p : game.getProjectiles()) p.render(g);
        for (Effect e : game.getEffects()) e.render(g);

        if (sel != null) {
            g.setColor(new Color(0xFFD98E));
            g.setStroke(new BasicStroke(2f));
            g.drawRoundRect(sel.getTileX() * Setting.TILE + 2, sel.getTileY() * Setting.TILE + 2,
                            Setting.TILE - 4, Setting.TILE - 4, 8, 8);
            g.setStroke(new BasicStroke(1f));
        }
    }

    private void drawPlacementPreview(Graphics2D g) {
        if (game.getState() != GameState.PLAYING) return;
        if (hoverCol < 0 || hoverRow < 0 || game.towerAt(hoverCol, hoverRow) != null) return;
        if (hud.panelContains(mouseX, mouseY, game)) return;

        boolean buildable = game.getMap().isBuildable(hoverCol, hoverRow);
        boolean ok = game.canPlaceAt(hoverCol, hoverRow);
        int x = hoverCol * Setting.TILE, y = hoverRow * Setting.TILE;
        Color c = ok ? new Color(0x7FE08A) : new Color(0xE06A6A);

        g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 60));
        g.fillRect(x, y, Setting.TILE, Setting.TILE);
        g.setColor(c);
        g.drawRect(x, y, Setting.TILE - 1, Setting.TILE - 1);

        if (buildable) {
            TowerType t = game.getSelectedType();
            int r = (int) t.getBaseRange();
            int cx = x + Setting.TILE / 2, cy = y + Setting.TILE / 2;
            g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 22));
            g.fillOval(cx - r, cy - r, r * 2, r * 2);
            g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 120));
            g.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{6f, 5f}, 0f));
            g.drawOval(cx - r, cy - r, r * 2, r * 2);
            g.setStroke(new BasicStroke(1f));
        }
    }

    private void drawPause(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 170));
        g.fillRect(0, 0, Setting.WINDOW_W, Setting.WINDOW_H);
        g.setFont(Fonts.bold(40));
        g.setColor(new Color(0xE8D9A8));
        Fonts.drawCentered(g, "หยุดชั่วคราว", CX, 240);
        resumeBtn.render(g);
        settingsBtn.render(g);
        restartBtn.render(g);
        menuBtn.render(g);
    }

    private void drawResult(Graphics2D g) {
        boolean win = game.getState() == GameState.WIN;
        g.setColor(new Color(0, 0, 0, 190));
        g.fillRect(0, 0, Setting.WINDOW_W, Setting.WINDOW_H);

        g.setFont(Fonts.bold(52));
        g.setColor(win ? new Color(0xFFE39A) : new Color(0xE86A6A));
        Fonts.drawCentered(g, win ? "ฟ้าสางแล้ว" : "วัดแตก...", CX, 150);
        g.setFont(Fonts.get(16));
        g.setColor(new Color(0x9C9078));
        String sub;
        if (win)                            sub = "พระอุโบสถยังอยู่ครบ วิญญาณสงบลงแล้ว";
        else if (difficulty.isOneLife())    sub = "วิญญาณหลุดเข้าวัดไปหนึ่งตน — ฝันร้ายไม่ให้อภัย";
        else                                sub = "วิญญาณบุกเข้าถึงพระอุโบสถ";
        Fonts.drawCentered(g, sub, CX, 184);

        g.setColor(new Color(0x3A3428));
        g.drawLine(CX - 170, 204, CX + 170, 204);

        String[][] rows = {
            {"ด่าน", level.getNumber() + " · " + level.getThaiName()},
            {"ระดับความยาก", difficulty.getThaiName()},
            {"คลื่นที่ผ่าน", game.getWaves().getCurrentWave() + " / " + game.getWaves().getTotalWaves()},
            {"วิญญาณที่กำจัด", String.format("%,d ตน", game.getKills())},
            {"ดวงวิญญาณที่เหลือ", game.getLives() + " / " + difficulty.getStartLives()},
            {"เวลาที่ใช้", String.format("%02d:%02d", (int) game.getElapsed() / 60, (int) game.getElapsed() % 60)},
        };
        int y = 232;
        g.setFont(Fonts.get(16));
        for (String[] r : rows) {
            g.setColor(new Color(0xCFC6AE));
            g.drawString(r[0], CX - 160, y);
            g.setColor(Color.WHITE);
            Fonts.drawRight(g, r[1], CX + 160, y);
            y += 26;
        }
        g.setFont(Fonts.bold(22));
        g.setColor(new Color(0xFFD98E));
        g.drawString("คะแนนรวม", CX - 160, y + 14);
        Fonts.drawRight(g, String.format("%,d", game.getFinalScore()), CX + 160, y + 14);

        g.setFont(Fonts.get(14));
        if (game.isNewRecord()) {
            g.setColor(new Color(0x7FE08A));
            Fonts.drawCentered(g, "สถิติใหม่!  บันทึกลง " + Setting.SCORE_FILE + " แล้ว", CX, y + 44);
        } else {
            g.setColor(new Color(0x8A8068));
            Fonts.drawCentered(g, String.format("คะแนนสูงสุด: %,d", game.getBestScore()), CX, y + 44);
        }
        boolean hasNext = win && level.next() != null;
        nextBtn.setVisible(hasNext);
        againBtn.setStyle(hasNext ? Button.Style.SECONDARY : Button.Style.PRIMARY);
        againBtn.setLocation(hasNext ? CX - 290 : CX - 190, 486);
        endMenuBtn.setLocation(hasNext ? CX + 110 : CX + 10, 486);
        againBtn.render(g);
        nextBtn.render(g);
        endMenuBtn.render(g);
    }

    // =====================================================================
    //                                Input
    // =====================================================================

    @Override public void onMouseMoved(int x, int y) {
        super.onMouseMoved(x, y);
        hud.onMouseMoved(x, y);
        for (Button b : new Button[]{resumeBtn, settingsBtn, restartBtn, menuBtn, againBtn, nextBtn, endMenuBtn}) b.updateHover(x, y);
        if (y >= Setting.FIELD_Y && y < Setting.FIELD_Y + Setting.PLAY_H && x >= 0) {
            hoverCol = x / Setting.TILE;
            hoverRow = (y - Setting.FIELD_Y) / Setting.TILE;
        } else {
            hoverCol = hoverRow = -1;
        }
    }

    @Override public void onMousePressed(int x, int y, int button) {
        GameState s = game.getState();
        if (s == GameState.PAUSED) {
            if (resumeBtn.contains(x, y))        game.togglePause();
            else if (settingsBtn.contains(x, y)) openSettings();
            else if (restartBtn.contains(x, y))  restart();
            else if (menuBtn.contains(x, y))    toMenu();
            return;
        }
        if (s == GameState.WIN || s == GameState.LOSE) {
            if (againBtn.contains(x, y))        restart();
            else if (nextBtn.contains(x, y))    nextLevel();
            else if (endMenuBtn.contains(x, y)) toMenu();
            return;
        }

        if (button == MouseEvent.BUTTON3) { game.deselect(); return; }

        switch (hud.actionAt(x, y, game)) {
            case NEXT_WAVE: game.callNextWave(); return;
            case SPEED:     game.toggleSpeed(); return;
            case MUTE:      game.toggleMute(); return;
            case PAUSE:     game.togglePause(); return;
            case UPGRADE:   game.upgradeSelected(); return;
            case SELL:      game.sellSelected(); return;
            case PANEL:     return;
            default: break;
        }

        TowerType slot = hud.slotAt(x, y);
        if (slot != null) { game.selectType(slot); return; }

        if (hoverCol >= 0 && hoverRow >= 0) game.clickTile(hoverCol, hoverRow);
    }

    @Override public void onKeyPressed(int key) {
        GameState s = game.getState();
        if (s == GameState.WIN || s == GameState.LOSE) {
            if (key == KeyEvent.VK_R) restart();
            if (key == KeyEvent.VK_ENTER) {
                if (s == GameState.WIN && level.next() != null) nextLevel(); else restart();
            }
            if (key == KeyEvent.VK_ESCAPE) toMenu();
            return;
        }
        switch (key) {
            case KeyEvent.VK_1: game.selectType(TowerType.TALISMAN);   break;
            case KeyEvent.VK_2: game.selectType(TowerType.BELL);       break;
            case KeyEvent.VK_3: game.selectType(TowerType.THREAD);     break;
            case KeyEvent.VK_4: game.selectType(TowerType.HOLY_WATER); break;
            case KeyEvent.VK_U: game.upgradeSelected(); break;
            case KeyEvent.VK_X: case KeyEvent.VK_DELETE: game.sellSelected(); break;
            case KeyEvent.VK_SPACE: game.callNextWave(); break;
            case KeyEvent.VK_F: game.toggleSpeed(); break;
            case KeyEvent.VK_M: game.toggleMute(); break;
            case KeyEvent.VK_P: game.togglePause(); break;
            case KeyEvent.VK_ESCAPE:
                if (game.getSelectedTower() != null) game.deselect();
                else game.togglePause();
                break;
            default: break;
        }
    }

    /** เปิดหน้าตั้งค่าเสียง แล้วกลับมาที่เกมเดิม (เกมยังหยุดอยู่) */
    private void openSettings() {
        host.getSound().play(Sfx.CLICK);
        host.setScreen(new SettingsScreen(host, this));
    }

    private void restart() {
        host.getSound().play(Sfx.CLICK);
        host.setScreen(new PlayScreen(host, level, difficulty));
    }

    private void nextLevel() {
        if (level.next() == null) return;
        host.getSound().play(Sfx.WAVE);
        host.setScreen(new PlayScreen(host, level.next(), difficulty));
    }

    private void toMenu() {
        host.getSound().play(Sfx.CLICK);
        host.setScreen(new TitleScreen(host, level, difficulty));
    }
}
