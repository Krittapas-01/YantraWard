package yantra.ui;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.util.Random;
import yantra.core.Setting;
import yantra.system.GameMap;
import yantra.util.Vec2;

/**
 * วาดพื้นหลังของแผนที่เพียงครั้งเดียวแล้วเก็บเป็นรูปไว้ (cache)
 * ทุกเฟรมแค่ drawImage — ประหยัดกว่าวาดทีละช่องทุกเฟรมมาก
 */
public class FieldRenderer {

    private static final int T = Setting.TILE;

    private final GameMap map;
    private final Color ground;           // สีพื้นประจำด่าน
    private final BufferedImage background;
    private final BufferedImage vignette;

    public FieldRenderer(GameMap map, Color ground) {
        this.map = map;
        this.ground = ground;
        this.background = buildBackground();
        this.vignette = buildVignette(Setting.PLAY_W, Setting.PLAY_H);
    }

    public BufferedImage getBackground() { return background; }
    public BufferedImage getVignette()   { return vignette; }

    // =====================================================================

    private BufferedImage buildBackground() {
        BufferedImage img = new BufferedImage(Setting.PLAY_W, Setting.PLAY_H, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        Random rnd = new Random(2569);

        // ชั้นที่ 1: พื้นทุกช่อง
        for (int r = 0; r < map.getRows(); r++) {
            for (int c = 0; c < map.getCols(); c++) {
                char t = map.getTile(c, r);
                if (t == GameMap.WATER)        drawWater(g, c, r, rnd);
                else if (t == GameMap.BRIDGE)  drawBridge(g, c, r);
                else if (map.isWalkable(c, r)) drawPath(g, c, r, rnd);
                else if (t == GameMap.TEMPLE)  drawTempleGround(g, c, r);
                else drawGround(g, c, r, rnd);
            }
        }
        // ชั้นที่ 2: ของตกแต่ง (วาดทีหลังเพื่อให้ล้นช่องได้)
        for (int r = 0; r < map.getRows(); r++) {
            for (int c = 0; c < map.getCols(); c++) {
                char t = map.getTile(c, r);
                if (t == GameMap.GRAVE) drawGrave(g, c, r, rnd);
                if (t == GameMap.TREE)  drawTree(g, c, r, rnd);
                if (t == GameMap.CHEDI) drawChedi(g, c, r, rnd);
            }
        }
        for (Vec2<Integer> s : map.getSpawns()) drawSpawn(g, s);
        drawTemple(g);
        g.dispose();
        return img;
    }

    private void drawGround(Graphics2D g, int c, int r, Random rnd) {
        int v = rnd.nextInt(7);
        g.setColor(new Color(ground.getRed() + v, ground.getGreen() + v, ground.getBlue() + v));
        g.fillRect(c * T, r * T, T, T);
        g.setColor(new Color(ground.getRed() + 6, ground.getGreen() + 8, ground.getBlue() + 6));
        g.drawRect(c * T, r * T, T, T);
        g.setColor(new Color(48, 64, 40));
        for (int i = 0; i < 3; i++) {
            int x = c * T + 4 + rnd.nextInt(T - 8), y = r * T + 6 + rnd.nextInt(T - 10);
            g.drawLine(x, y, x - 1, y - 3);
            g.drawLine(x + 2, y, x + 3, y - 3);
        }
    }

    private void drawPath(Graphics2D g, int c, int r, Random rnd) {
        int x = c * T, y = r * T;
        g.setColor(new Color(118, 101, 76));
        g.fillRect(x, y, T, T);
        for (int i = 0; i < 5; i++) {
            int v = rnd.nextInt(30) - 15;
            g.setColor(new Color(118 + v, 101 + v, 76 + v));
            int px = x + rnd.nextInt(T - 4), py = y + rnd.nextInt(T - 4);
            g.fillOval(px, py, 3 + rnd.nextInt(3), 2 + rnd.nextInt(2));
        }
        g.setColor(new Color(84, 72, 56));
        int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        for (int[] d : dirs) {
            int nc = c + d[0], nr = r + d[1];
            boolean edge = !map.inBounds(nc, nr)
                        || (!map.isWalkable(nc, nr) && map.getTile(nc, nr) != GameMap.WATER);
            if (!edge || !map.inBounds(nc, nr)) continue;
            if (d[0] == 1)  g.fillRect(x + T - 3, y, 3, T);
            if (d[0] == -1) g.fillRect(x, y, 3, T);
            if (d[1] == 1)  g.fillRect(x, y + T - 3, T, 3);
            if (d[1] == -1) g.fillRect(x, y, T, 3);
        }
    }

    private void drawWater(Graphics2D g, int c, int r, Random rnd) {
        int x = c * T, y = r * T;
        g.setColor(new Color(20, 42, 56));
        g.fillRect(x, y, T, T);
        g.setColor(new Color(34, 62, 80));
        for (int i = 0; i < 3; i++) {
            int wx = x + 3 + rnd.nextInt(T - 16), wy = y + 6 + rnd.nextInt(T - 12);
            g.drawArc(wx, wy, 12, 5, 0, 180);
        }
        // ตลิ่ง
        g.setColor(new Color(58, 52, 40));
        if (c > 0 && map.getTile(c - 1, r) != GameMap.WATER && map.getTile(c - 1, r) != GameMap.BRIDGE) g.fillRect(x, y, 3, T);
        if (c < Setting.COLS - 1 && map.getTile(c + 1, r) != GameMap.WATER && map.getTile(c + 1, r) != GameMap.BRIDGE) g.fillRect(x + T - 3, y, 3, T);
    }

    private void drawBridge(Graphics2D g, int c, int r) {
        int x = c * T, y = r * T;
        g.setColor(new Color(20, 42, 56));
        g.fillRect(x, y, T, T);
        boolean horizontal = (c > 0 && map.isWalkable(c - 1, r)) || (c < Setting.COLS - 1 && map.isWalkable(c + 1, r));
        g.setColor(new Color(120, 88, 56));
        if (horizontal) g.fillRect(x, y + 4, T, T - 8); else g.fillRect(x + 4, y, T - 8, T);
        g.setColor(new Color(86, 62, 40));
        for (int i = 0; i < T; i += 6) {
            if (horizontal) g.drawLine(x + i, y + 4, x + i, y + T - 5);
            else            g.drawLine(x + 4, y + i, x + T - 5, y + i);
        }
        g.setColor(new Color(70, 50, 32));
        if (horizontal) { g.fillRect(x, y + 3, T, 2); g.fillRect(x, y + T - 5, T, 2); }
        else            { g.fillRect(x + 3, y, 2, T); g.fillRect(x + T - 5, y, 2, T); }
    }

    private void drawChedi(Graphics2D g, int c, int r, Random rnd) {
        drawGround(g, c, r, rnd);
        int cx = c * T + T / 2, by = r * T + T - 6;
        g.setColor(new Color(0, 0, 0, 80));
        g.fillOval(cx - 12, by - 3, 24, 7);
        g.setColor(new Color(170, 164, 150));
        g.fillRect(cx - 11, by - 6, 22, 6);
        g.fillRect(cx - 8, by - 10, 16, 5);
        g.setColor(new Color(206, 200, 184));
        g.fillOval(cx - 8, by - 22, 16, 15);
        g.setColor(new Color(214, 178, 95));
        g.fillPolygon(new int[]{cx - 3, cx + 3, cx}, new int[]{by - 19, by - 19, by - 36}, 3);
        g.setColor(new Color(150, 144, 130));
        g.drawOval(cx - 8, by - 22, 16, 15);
    }

    private void drawTempleGround(Graphics2D g, int c, int r) {
        g.setColor(new Color(76, 70, 58));
        g.fillRect(c * T, r * T, T, T);
        g.setColor(new Color(62, 57, 47));
        g.drawLine(c * T, r * T + T / 2, c * T + T, r * T + T / 2);
        g.drawLine(c * T + T / 2, r * T, c * T + T / 2, r * T + T);
    }

    private void drawTree(Graphics2D g, int c, int r, Random rnd) {
        int cx = c * T + T / 2 + rnd.nextInt(7) - 3, cy = r * T + T / 2 + rnd.nextInt(7) - 3;
        g.setColor(new Color(0, 0, 0, 70));
        g.fillOval(cx - 17, cy - 11, 38, 30);
        int[][] blobs = {{-8, -4, 22}, {6, -6, 20}, {-2, 6, 22}, {0, -2, 24}};
        for (int[] b : blobs) {
            g.setColor(new Color(24, 44, 26));
            g.fillOval(cx + b[0] - b[2] / 2, cy + b[1] - b[2] / 2, b[2], b[2]);
        }
        for (int[] b : blobs) {
            g.setColor(new Color(36, 60, 36));
            g.fillOval(cx + b[0] - b[2] / 2 + 2, cy + b[1] - b[2] / 2 + 1, b[2] - 8, b[2] - 8);
        }
        g.setColor(new Color(52, 80, 48));
        g.fillOval(cx - 6, cy - 10, 8, 6);
    }

    private void drawGrave(Graphics2D g, int c, int r, Random rnd) {
        drawGround(g, c, r, rnd);
        int cx = c * T + T / 2, cy = r * T + T / 2;
        g.setColor(new Color(0, 0, 0, 80));
        g.fillOval(cx - 9, cy + 6, 20, 6);
        if (rnd.nextBoolean()) {
            g.setColor(new Color(112, 110, 102));
            g.fillRoundRect(cx - 7, cy - 10, 14, 19, 10, 10);
            g.setColor(new Color(80, 78, 72));
            g.drawRoundRect(cx - 7, cy - 10, 14, 19, 10, 10);
            g.drawLine(cx - 3, cy - 3, cx + 3, cy - 3);
            g.drawLine(cx - 3, cy + 1, cx + 3, cy + 1);
        } else {
            g.setColor(new Color(150, 144, 130));
            g.fillRect(cx - 7, cy + 2, 14, 6);
            g.fillPolygon(new int[]{cx - 5, cx + 5, cx}, new int[]{cy + 2, cy + 2, cy - 14}, 3);
            g.setColor(new Color(110, 104, 92));
            g.drawLine(cx, cy - 14, cx, cy + 2);
        }
    }

    private void drawSpawn(Graphics2D g, Vec2<Integer> spawn) {
        int x = spawn.getX() * T, y = spawn.getY() * T;
        RadialGradientPaint mist = new RadialGradientPaint(new Point(x + T / 2, y + T / 2), 50f,
                new float[]{0f, 1f}, new Color[]{new Color(120, 80, 150, 110), new Color(120, 80, 150, 0)});
        g.setPaint(mist);
        g.fillOval(x - 30, y - 30, T + 60, T + 60);
        g.setColor(new Color(40, 30, 34));
        g.fillRect(x + 4, y + 4, 5, T - 6);
        g.fillRect(x + T - 12, y + 4, 5, T - 6);
        g.setStroke(new BasicStroke(3f));
        g.drawLine(x + 1, y + 7, x + T - 4, y + 3);
        g.setStroke(new BasicStroke(1f));
        g.setFont(Fonts.bold(12));
        int lw = g.getFontMetrics().stringWidth("ป่าช้า");
        int lx = Math.max(2, Math.min(Setting.PLAY_W - lw - 2, x + T / 2 - lw / 2));
        int ly = y + T + 14 > Setting.PLAY_H - 4 ? y - 6 : y + T + 14;
        Fonts.drawShadow(g, "ป่าช้า", lx, ly, new Color(0xC0A8D0));
    }

    private void drawTemple(Graphics2D g) {
        // พระอุโบสถกินพื้นที่ 2x2 ช่อง ตามบล็อก W ในไฟล์แผนที่ (ถ้าไม่มี W ให้ใช้ตำแหน่งจุดหมาย)
        Vec2<Integer> origin = map.getTempleOrigin();
        if (origin == null) origin = new Vec2<>(Math.max(0, map.getGoal().getX() - 1), Math.max(0, map.getGoal().getY() - 1));
        int x0 = origin.getX() * T, y0 = origin.getY() * T;
        RadialGradientPaint glow = new RadialGradientPaint(new Point(x0 + T, y0 + T + 10), 70f,
                new float[]{0f, 1f}, new Color[]{new Color(255, 200, 120, 70), new Color(255, 200, 120, 0)});
        g.setPaint(glow);
        g.fillOval(x0 - 30, y0 - 20, 2 * T + 60, 2 * T + 60);

        g.setColor(new Color(128, 118, 100));
        g.fillRect(x0 + 2, y0 + 58, 76, 20);
        g.setColor(new Color(100, 92, 78));
        g.drawLine(x0 + 2, y0 + 64, x0 + 78, y0 + 64);
        g.drawLine(x0 + 2, y0 + 71, x0 + 78, y0 + 71);

        g.setColor(new Color(222, 210, 184));
        g.fillRect(x0 + 10, y0 + 34, 60, 26);
        g.setColor(new Color(120, 30, 26));
        g.fillRect(x0 + 34, y0 + 40, 12, 20);
        g.setColor(new Color(214, 178, 95));
        g.drawRect(x0 + 34, y0 + 40, 12, 20);
        g.setColor(new Color(255, 214, 140));
        g.fillRect(x0 + 17, y0 + 42, 6, 8);
        g.fillRect(x0 + 57, y0 + 42, 6, 8);

        roof(g, x0 + 4, x0 + 76, y0 + 36, x0 + 16, x0 + 64, y0 + 18, new Color(150, 44, 32));
        roof(g, x0 + 14, x0 + 66, y0 + 20, x0 + 26, x0 + 54, y0 + 4, new Color(176, 60, 38));
        g.setColor(new Color(214, 178, 95));
        g.fillPolygon(new int[]{x0 + 30, x0 + 50, x0 + 40}, new int[]{y0 + 6, y0 + 6, y0 - 12}, 3);
        g.setStroke(new BasicStroke(2f));
        g.drawLine(x0 + 4, y0 + 36, x0, y0 + 28);
        g.drawLine(x0 + 76, y0 + 36, x0 + 80, y0 + 28);
        g.drawLine(x0 + 40, y0 - 12, x0 + 40, y0 - 18);
        g.setStroke(new BasicStroke(1f));

        g.setFont(Fonts.bold(12));
        int lw = g.getFontMetrics().stringWidth("พระอุโบสถ");
        int lx = Math.max(2, Math.min(Setting.PLAY_W - lw - 2, x0 + T - lw / 2));
        int ly = y0 - 22 < 12 ? y0 + 2 * T + 14 : y0 - 22;
        Fonts.drawShadow(g, "พระอุโบสถ", lx, ly, new Color(0xE8C878));
    }

    private void roof(Graphics2D g, int bl, int br, int by, int tl, int tr, int ty, Color c) {
        GeneralPath p = new GeneralPath();
        p.moveTo(bl, by); p.lineTo(br, by); p.lineTo(tr, ty); p.lineTo(tl, ty); p.closePath();
        g.setColor(c);
        g.fill(p);
        g.setColor(new Color(214, 178, 95));
        g.draw(p);
    }

    public static BufferedImage buildVignette(int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        RadialGradientPaint p = new RadialGradientPaint(new Point(w / 2, h / 2), w * 0.66f,
                new float[]{0f, 0.55f, 1f},
                new Color[]{new Color(0, 0, 10, 0), new Color(0, 0, 10, 40), new Color(0, 0, 10, 175)});
        g.setPaint(p);
        g.fillRect(0, 0, w, h);
        g.dispose();
        return img;
    }
}
