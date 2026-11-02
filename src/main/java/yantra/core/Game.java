package yantra.core;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;
import yantra.screen.TitleScreen;
import yantra.system.ScoreStore;
import yantra.system.SettingsStore;
import yantra.system.SoundManager;

/** หน้าต่างเกม — ประกอบทุกส่วนเข้าด้วยกันแล้วเริ่มที่หน้าเมนู */
public class Game extends JFrame {

    private final GamePanel panel;
    private final GameLoop  loop;

    public Game() {
        super(Setting.TITLE);
        SoundManager  sound    = new SoundManager();
        ScoreStore    scores   = new ScoreStore(Setting.SCORE_FILE);
        SettingsStore settings = new SettingsStore(Setting.SETTINGS_FILE);
        sound.setVolumes(settings.getMasterVolume(), settings.getMusicVolume(), settings.getSfxVolume());

        panel = new GamePanel(sound, scores, settings);
        loop  = new GameLoop(panel);
        panel.setLoop(loop);
        panel.setScreen(new TitleScreen(panel));

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setIconImage(createIcon());
        add(panel);
        pack();
        setLocationRelativeTo(null);
    }

    public void start() {
        setVisible(true);
        panel.requestFocusInWindow();
        loop.start();
        panel.getSound().startMusic();
    }

    /** วาดไอคอนหน้าต่างด้วยโค้ด ไม่ต้องใช้ไฟล์รูป */
    private static Image createIcon() {
        BufferedImage img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(0x14110C));
        g.fillOval(2, 2, 60, 60);
        g.setColor(new Color(0xD9B24A));
        g.setStroke(new BasicStroke(3f));
        g.drawOval(4, 4, 56, 56);
        g.drawPolygon(new int[]{32, 52, 12}, new int[]{12, 46, 46}, 3);
        g.drawPolygon(new int[]{32, 52, 12}, new int[]{52, 18, 18}, 3);
        g.fillOval(28, 28, 8, 8);
        g.dispose();
        return img;
    }
}
