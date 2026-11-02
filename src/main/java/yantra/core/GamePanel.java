package yantra.core;

import java.awt.*;
import java.awt.event.*;
import javax.swing.JPanel;
import yantra.screen.Screen;
import yantra.system.ScoreStore;
import yantra.system.SettingsStore;
import yantra.system.SoundManager;

/**
 * พื้นที่วาดภาพหลัก — ถือ "หน้าจอปัจจุบัน" (Screen) ไว้หนึ่งหน้า
 * แล้วส่งต่อการวาดและ Event ทั้งหมดไปให้หน้าจอนั้น (Polymorphism ระดับหน้าจอ)
 */
public class GamePanel extends JPanel implements MouseListener, MouseMotionListener, KeyListener {

    private final Object lock = new Object();   // กันเธรดเกมกับเธรดวาดภาพชนกัน
    private final SoundManager sound;
    private final ScoreStore scores;
    private final SettingsStore settings;
    private Screen screen;
    private GameLoop loop;

    public GamePanel(SoundManager sound, ScoreStore scores, SettingsStore settings) {
        this.sound = sound;
        this.scores = scores;
        this.settings = settings;
        setPreferredSize(new Dimension(Setting.WINDOW_W, Setting.WINDOW_H));
        setBackground(Color.BLACK);
        setDoubleBuffered(true);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        addMouseListener(this);
        addMouseMotionListener(this);
        addKeyListener(this);
    }

    public void setLoop(GameLoop loop) { this.loop = loop; }

    public void setScreen(Screen next) {
        synchronized (lock) {
            if (screen != null) screen.onExit();
            screen = next;
            screen.onEnter();
        }
    }

    public void tick(double dt) {
        synchronized (lock) {
            if (screen != null) screen.update(dt);
        }
    }

    @Override protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0.create();
        renderTo(g);
        g.dispose();
    }

    /** วาดทั้งหน้าจอลง Graphics ใดก็ได้ (ใช้ได้ทั้งบนจอจริงและตอนบันทึกภาพ) */
    public void renderTo(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        synchronized (lock) {
            if (screen != null) screen.render(g);
        }
    }

    // ------------------------------------------------ Event → Screen
    @Override public void mousePressed(MouseEvent e) {
        requestFocusInWindow();
        synchronized (lock) { if (screen != null) screen.onMousePressed(e.getX(), e.getY(), e.getButton()); }
    }
    @Override public void mouseMoved(MouseEvent e) {
        synchronized (lock) { if (screen != null) screen.onMouseMoved(e.getX(), e.getY()); }
    }
    @Override public void mouseDragged(MouseEvent e) {
        synchronized (lock) { if (screen != null) screen.onMouseDragged(e.getX(), e.getY()); }
    }
    @Override public void mouseExited(MouseEvent e) {
        synchronized (lock) { if (screen != null) screen.onMouseMoved(-1, -1); }
    }
    @Override public void keyPressed(KeyEvent e) {
        synchronized (lock) { if (screen != null) screen.onKeyPressed(e.getKeyCode()); }
    }
    @Override public void mouseClicked(MouseEvent e)  { }
    @Override public void mouseReleased(MouseEvent e) {
        synchronized (lock) { if (screen != null) screen.onMouseReleased(e.getX(), e.getY()); }
    }
    @Override public void mouseEntered(MouseEvent e)  { }
    @Override public void keyReleased(KeyEvent e)     { }
    @Override public void keyTyped(KeyEvent e)        { }

    // ------------------------------------------------ บริการที่หน้าจอต่าง ๆ ใช้ร่วมกัน
    public SoundManager getSound()  { return sound; }
    public ScoreStore   getScores() { return scores; }
    public SettingsStore getSettings() { return settings; }
    public int          getFps()    { return loop == null ? 0 : loop.getFps(); }

    public void exitGame() {
        Window w = javax.swing.SwingUtilities.getWindowAncestor(this);
        if (w != null) w.dispose();
        System.exit(0);
    }
}
