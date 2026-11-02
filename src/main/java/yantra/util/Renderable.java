package yantra.util;

import java.awt.Graphics2D;

/** สิ่งที่วาดตัวเองลงบนหน้าจอได้ */
public interface Renderable {
    void render(Graphics2D g);
}
