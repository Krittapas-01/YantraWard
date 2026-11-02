package yantra.screen;

import java.awt.Graphics2D;
import yantra.core.GamePanel;

/**
 * คลาสนามธรรมของ "หน้าจอ" — เมนู วิธีเล่น และหน้าเล่นเกม ต่างก็เป็น Screen
 * GamePanel ไม่ต้องรู้ว่าตอนนี้อยู่หน้าไหน แค่เรียก update/render ผ่านชนิดแม่ (Polymorphism)
 */
public abstract class Screen {

    protected final GamePanel host;
    protected int mouseX = -1;
    protected int mouseY = -1;

    protected Screen(GamePanel host) {
        this.host = host;
    }

    public void onEnter() { }
    public void onExit()  { }

    public abstract void update(double dt);
    public abstract void render(Graphics2D g);

    public void onMousePressed(int x, int y, int button) { }
    public void onMouseMoved(int x, int y) { mouseX = x; mouseY = y; }
    public void onMouseDragged(int x, int y) { onMouseMoved(x, y); }
    public void onMouseReleased(int x, int y) { }
    public void onKeyPressed(int keyCode) { }
}
