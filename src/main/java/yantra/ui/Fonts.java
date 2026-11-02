package yantra.ui;

import java.awt.*;
import java.util.*;
import java.util.List;

/** เลือกฟอนต์ที่รองรับภาษาไทยในเครื่องนั้น ๆ และมีเครื่องมือช่วยวาดข้อความ */
public final class Fonts {

    private static final String[] PREFERRED = {
        "Leelawadee UI", "Leelawadee", "Tahoma", "Sarabun", "TH Sarabun New",
        "Garuda", "Loma", "Norasi", "Dialog"
    };

    private static String family;
    private static final Map<Integer, Font> CACHE = new HashMap<>();

    private Fonts() { }

    private static String family() {
        if (family == null) {
            Set<String> available = new HashSet<>();
            for (String f : GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()) {
                available.add(f.toLowerCase(Locale.ROOT));
            }
            family = "Dialog";
            for (String p : PREFERRED) {
                if (available.contains(p.toLowerCase(Locale.ROOT))) { family = p; break; }
            }
        }
        return family;
    }

    public static Font get(int size)  { return get(size, Font.PLAIN); }
    public static Font bold(int size) { return get(size, Font.BOLD); }

    public static Font get(int size, int style) {
        return CACHE.computeIfAbsent(size * 4 + style, k -> new Font(family(), style, size));
    }

    public static void drawCentered(Graphics2D g, String s, int cx, int y) {
        g.drawString(s, cx - g.getFontMetrics().stringWidth(s) / 2, y);
    }

    public static void drawRight(Graphics2D g, String s, int rightX, int y) {
        g.drawString(s, rightX - g.getFontMetrics().stringWidth(s), y);
    }

    /** วาดข้อความพร้อมเงา อ่านง่ายบนพื้นหลังรก ๆ */
    public static void drawShadow(Graphics2D g, String s, int x, int y, Color c) {
        Color old = g.getColor();
        g.setColor(new Color(0, 0, 0, 160));
        g.drawString(s, x + 1, y + 2);
        g.setColor(c);
        g.drawString(s, x, y);
        g.setColor(old);
    }

    /** ตัดบรรทัดตามช่องว่างให้ไม่เกินความกว้างที่กำหนด */
    public static List<String> wrap(String text, FontMetrics fm, int maxWidth) {
        List<String> lines = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        for (String word : text.split(" ")) {
            String trial = cur.length() == 0 ? word : cur + " " + word;
            if (fm.stringWidth(trial) <= maxWidth || cur.length() == 0) {
                cur = new StringBuilder(trial);
            } else {
                lines.add(cur.toString());
                cur = new StringBuilder(word);
            }
        }
        if (cur.length() > 0) lines.add(cur.toString());
        return lines;
    }
}
