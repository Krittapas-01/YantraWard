package yantra.effect;

import java.awt.*;

/** เศษประกายเล็ก ๆ ที่กระจายออกแล้วจางหาย */
public class Particle extends Effect {

    private double vx;
    private double vy;
    private final Color color;
    private final double size;

    public Particle(double x, double y, double vx, double vy, Color color, double size, double life) {
        super(x, y, life);
        this.vx = vx; this.vy = vy;
        this.color = color;
        this.size = size;
    }

    @Override protected void update(double dt) {
        super.update(dt);
        x += vx * dt;
        y += vy * dt;
        vx *= 0.92;
        vy = vy * 0.92 + 40 * dt;
    }

    @Override public void render(Graphics2D g) {
        int a = (int) (220 * fade());
        g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), a));
        int s = (int) Math.max(1, size * (0.5 + fade() / 2));
        g.fillOval((int) x - s / 2, (int) y - s / 2, s, s);
    }
}
