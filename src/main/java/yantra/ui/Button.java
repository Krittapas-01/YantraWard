package yantra.ui;

import java.awt.*;

/** ปุ่มกดที่ใช้ร่วมกันทุกหน้าจอ */
public class Button {

    public enum Style { PRIMARY, SECONDARY, DANGER }

    private final Rectangle bounds;
    private String  label;
    private Style   style;
    private boolean hovered = false;
    private boolean enabled = true;
    private boolean visible = true;

    public Button(String label, int x, int y, int w, int h, Style style) {
        this.label = label;
        this.bounds = new Rectangle(x, y, w, h);
        this.style = style;
    }

    /** Overloading: ปุ่มแบบธรรมดา */
    public Button(String label, int x, int y, int w, int h) {
        this(label, x, y, w, h, Style.SECONDARY);
    }

    public boolean contains(int px, int py) {
        return visible && enabled && bounds.contains(px, py);
    }

    public void updateHover(int px, int py) {
        hovered = visible && bounds.contains(px, py);
    }

    public void render(Graphics2D g) {
        if (!visible) return;
        Color fill, border, text;
        switch (style) {
            case PRIMARY:
                fill = hovered ? new Color(0x4A3A1C) : new Color(0x2E2414);
                border = new Color(0xD9B24A);
                text = new Color(0xFFE9B0);
                break;
            case DANGER:
                fill = hovered ? new Color(0x4A1C22) : new Color(0x2A1418);
                border = new Color(0xB05060);
                text = new Color(0xF0C0C8);
                break;
            default:
                fill = hovered ? new Color(0x2E2A22) : new Color(0x1C1A16);
                border = hovered ? new Color(0x9C8A62) : new Color(0x5C5240);
                text = new Color(0xDCCFAE);
        }
        if (!enabled) {
            fill = new Color(0x18171A);
            border = new Color(0x3A3834);
            text = new Color(0x6A665C);
        }
        g.setColor(fill);
        g.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 8, 8);
        g.setColor(border);
        g.drawRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 8, 8);

        g.setFont(bounds.height >= 34 ? Fonts.bold(17) : Fonts.get(bounds.height >= 24 ? 14 : 12));
        FontMetrics fm = g.getFontMetrics();
        int ty = bounds.y + (bounds.height + fm.getAscent() - fm.getDescent()) / 2;
        g.setColor(text);
        Fonts.drawCentered(g, label, bounds.x + bounds.width / 2, ty);
    }

    public void setLabel(String label)   { this.label = label; }
    public void setStyle(Style style)    { this.style = style; }
    public void setEnabled(boolean e)    { this.enabled = e; }
    public void setVisible(boolean v)    { this.visible = v; if (!v) hovered = false; }
    public void setLocation(int x, int y){ bounds.setLocation(x, y); }
    public Rectangle getBounds()         { return bounds; }
    public boolean isVisible()           { return visible; }
}
