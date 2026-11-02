package yantra.screen;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import yantra.core.GamePanel;
import yantra.core.Setting;
import yantra.system.SoundManager;
import yantra.ui.Button;
import yantra.ui.FieldRenderer;
import yantra.ui.Fonts;
import yantra.ui.Slider;
import yantra.util.Sfx;

/**
 * หน้าตั้งค่าเสียง — เปิดได้ทั้งจากหน้าเมนูและตอนหยุดเกม
 * กดกลับแล้วจะคืนไปหน้าที่เปิดมา (ถ้ามาจากเกม เกมจะยังอยู่ครบ)
 */
public class SettingsScreen extends Screen {

    private static final int CX = Setting.WINDOW_W / 2;
    private static final int PANEL_W = 420;

    private final Screen returnTo;
    private final Slider master;
    private final Slider music;
    private final Slider sfx;
    private final Slider[] sliders;
    private final Button testBtn = new Button("ทดสอบเสียงเอฟเฟกต์", CX - 110, 392, 220, 32);
    private final Button backBtn = new Button("บันทึกและกลับ", CX - 110, 470, 220, 40, Button.Style.PRIMARY);
    private final BufferedImage vignette = FieldRenderer.buildVignette(Setting.WINDOW_W, Setting.WINDOW_H);
    private double sfxPreviewCooldown = 0;

    public SettingsScreen(GamePanel host, Screen returnTo) {
        super(host);
        this.returnTo = returnTo;
        SoundManager s = host.getSound();
        int x = CX - PANEL_W / 2 + 30, w = PANEL_W - 60;
        master = new Slider("เสียงรวม", x, 200, w, s.getMasterVolume());
        music  = new Slider("เพลงประกอบ", x, 270, w, s.getMusicVolume());
        sfx    = new Slider("เสียงเอฟเฟกต์", x, 340, w, s.getSfxVolume());
        sliders = new Slider[]{master, music, sfx};
    }

    @Override public void update(double dt) {
        if (sfxPreviewCooldown > 0) sfxPreviewCooldown -= dt;
    }

    @Override public void render(Graphics2D g) {
        // ถ้าเปิดมาจากหน้าอื่น ให้เห็นหน้านั้นจาง ๆ อยู่ข้างหลัง
        if (returnTo != null) returnTo.render(g);
        g.setColor(new Color(6, 6, 10, returnTo != null ? 200 : 255));
        g.fillRect(0, 0, Setting.WINDOW_W, Setting.WINDOW_H);
        g.drawImage(vignette, 0, 0, null);

        int px = CX - PANEL_W / 2, py = 110, ph = 420;
        g.setColor(new Color(20, 17, 13, 240));
        g.fillRoundRect(px, py, PANEL_W, ph, 14, 14);
        g.setColor(new Color(0x6A5A3A));
        g.drawRoundRect(px, py, PANEL_W, ph, 14, 14);

        g.setFont(Fonts.bold(26));
        g.setColor(new Color(0xFFE39A));
        Fonts.drawCentered(g, "ตั้งค่าเสียง", CX, py + 44);

        for (Slider s : sliders) s.render(g);
        testBtn.render(g);

        g.setFont(Fonts.get(11));
        g.setColor(new Color(0x7A7058));
        Fonts.drawCentered(g, host.getSound().isEnabled()
                ? "ค่าจะถูกบันทึกลง " + Setting.SETTINGS_FILE + " · กด M ในเกมเพื่อปิดเสียงชั่วคราว"
                : "เครื่องนี้ไม่พบอุปกรณ์เสียง — ค่าจะถูกบันทึกไว้ใช้ภายหลัง", CX, 448);
        backBtn.render(g);
    }

    private void applyVolumes() {
        host.getSound().setVolumes(master.getValue(), music.getValue(), sfx.getValue());
    }

    private void previewSfx() {
        if (sfxPreviewCooldown > 0) return;
        sfxPreviewCooldown = 0.15;
        host.getSound().play(Sfx.CLICK);
    }

    @Override public void onMouseMoved(int x, int y) {
        super.onMouseMoved(x, y);
        for (Slider s : sliders) s.hover(x, y);
        testBtn.updateHover(x, y);
        backBtn.updateHover(x, y);
    }

    @Override public void onMousePressed(int x, int y, int button) {
        for (Slider s : sliders) {
            if (s.press(x, y) || s.isDragging()) {
                applyVolumes();
                if (s != music) previewSfx();
                return;
            }
        }
        if (testBtn.contains(x, y)) host.getSound().play(Sfx.BELL);
        else if (backBtn.contains(x, y)) back();
    }

    @Override public void onMouseDragged(int x, int y) {
        super.onMouseDragged(x, y);
        for (Slider s : sliders) {
            if (s.drag(x, y)) {
                applyVolumes();
                if (s != music) previewSfx();
            }
        }
    }

    @Override public void onMouseReleased(int x, int y) {
        for (Slider s : sliders) s.release();
    }

    @Override public void onKeyPressed(int key) {
        if (key == KeyEvent.VK_ESCAPE || key == KeyEvent.VK_ENTER || key == KeyEvent.VK_BACK_SPACE) back();
    }

    private void back() {
        applyVolumes();
        host.getSettings().setVolumes(master.getValue(), music.getValue(), sfx.getValue());
        host.getSound().play(Sfx.CLICK);
        host.setScreen(returnTo != null ? returnTo : new TitleScreen(host));
    }
}
