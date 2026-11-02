package yantra.effect;

import java.awt.*;

/** วงแหวนขยายออก ใช้ตอนวางป้อม ตอนผีแตกร่าง หรือตอนถูกสาป */
public class RingEffect extends Effect {

    private final Color color;
    private final double maxRadius;

    public RingEffect(double x, double y, Color color, double maxRadius, double life) {
        super(x, y, life);
        this.color = color;
        this.maxRadius = maxRadius;
    }

    @Override public void render(Graphics2D g) {
        double p = 1 - fade();
        int r = (int) (maxRadius * (0.3 + 0.7 * p));
        g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (200 * fade())));
        g.setStroke(new BasicStroke(2f));
        g.drawOval((int) x - r, (int) y - r, r * 2, r * 2);
        g.setStroke(new BasicStroke(1f));
    }
}
