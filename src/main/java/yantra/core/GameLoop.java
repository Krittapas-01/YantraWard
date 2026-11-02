package yantra.core;

/** เธรดคำนวณเกมแยกจากเธรดวาดภาพ ทำให้ความเร็วเกมคงที่ไม่ขึ้นกับความเร็วเครื่อง */
public class GameLoop implements Runnable {

    private final GamePanel panel;
    private volatile boolean running = false;
    private volatile int fps = 0;

    public GameLoop(GamePanel panel) {
        this.panel = panel;
    }

    public void start() {
        running = true;
        Thread t = new Thread(this, "game-loop");
        t.setDaemon(true);
        t.start();
    }

    public void stop() { running = false; }

    @Override public void run() {
        final long frameNs = 1_000_000_000L / Setting.MAX_FPS;
        long last = System.nanoTime();
        long fpsTimer = System.currentTimeMillis();
        int frames = 0;

        while (running) {
            long now = System.nanoTime();
            double dt = Math.min(0.05, (now - last) / 1_000_000_000.0);   // กันเกมกระตุกเมื่อเครื่องสะดุด
            last = now;

            panel.tick(dt);
            panel.repaint();

            frames++;
            if (System.currentTimeMillis() - fpsTimer >= 1000) {
                fps = frames;
                frames = 0;
                fpsTimer += 1000;
            }

            long sleepNs = frameNs - (System.nanoTime() - now);
            if (sleepNs > 0) {
                try {
                    Thread.sleep(sleepNs / 1_000_000, (int) (sleepNs % 1_000_000));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    public int getFps() { return fps; }
}
