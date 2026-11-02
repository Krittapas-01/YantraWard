package yantra.effect;

import java.awt.*;
import yantra.ui.Fonts;

/** ตัวหนังสือลอยขึ้นแล้วจางหาย เช่น +12 เมื่อกำจัดผีได้ */
public class FloatingText extends Effect {

    private final String text;
    private final Color color;

    public FloatingText(double x, double y, String text, Color color) {
        super(x, y, 0.9);
        this.text = text;
        this.color = color;
    }

    @Override protected void update(double dt) {
        super.update(dt);
        y -= 28 * dt;
    }

    @Override public void render(Graphics2D g) {
        g.setFont(Fonts.bold(13));
        int a = (int) (255 * fade());
        g.setColor(new Color(0, 0, 0, a / 2));
        Fonts.drawCentered(g, text, (int) x + 1, (int) y + 1);
        g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), a));
        Fonts.drawCentered(g, text, (int) x, (int) y);
    }
}
