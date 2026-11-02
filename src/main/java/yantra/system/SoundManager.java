package yantra.system;

import java.util.EnumMap;
import java.util.Map;
import java.util.Random;
import javax.sound.sampled.*;
import yantra.util.Sfx;

/**
 * ระบบเสียง — สังเคราะห์เสียงทั้งหมดด้วยคณิตศาสตร์ตอนเริ่มเกม ไม่ต้องใช้ไฟล์เสียงเลย
 * มีระดับเสียง 3 ชั้น: เสียงรวม × (เพลงประกอบ หรือ เสียงเอฟเฟกต์)
 * ถ้าเครื่องไม่มีอุปกรณ์เสียง เกมยังเล่นได้ตามปกติ (แค่ไม่มีเสียง)
 */
public class SoundManager {

    private static final float RATE = 22050f;

    private final Map<Sfx, Clip[]> clips = new EnumMap<>(Sfx.class);
    private final Map<Sfx, Integer> nextIndex = new EnumMap<>(Sfx.class);
    private final Map<Sfx, Long> lastPlayed = new EnumMap<>(Sfx.class);
    private boolean enabled;
    private boolean muted = false;
    private Clip music;
    private boolean musicPlaying = false;
    private double masterVolume = 0.8;
    private double musicVolume  = 0.6;
    private double sfxVolume    = 0.8;

    public SoundManager() {
        this(true);
    }

    /** Overloading: new SoundManager(false) = สร้างแบบเงียบ (ใช้ตอนทดสอบ) */
    public SoundManager(boolean withAudio) {
        this.enabled = withAudio && init();
    }

    private boolean init() {
        try {
            AudioFormat fmt = new AudioFormat(RATE, 16, 1, true, false);
            for (Sfx s : Sfx.values()) {
                byte[] pcm = toPcm(synth(s));
                int n = (s == Sfx.SHOOT || s == Sfx.DEATH || s == Sfx.BELL) ? 2 : 1;
                Clip[] pool = new Clip[n];
                for (int i = 0; i < n; i++) {
                    Clip c = AudioSystem.getClip();
                    c.open(fmt, pcm, 0, pcm.length);
                    pool[i] = c;
                }
                clips.put(s, pool);
                nextIndex.put(s, 0);
            }
            byte[] bgm = toPcm(synthMusic(), false);
            music = AudioSystem.getClip();
            music.open(fmt, bgm, 0, bgm.length);
            return true;
        } catch (LineUnavailableException | IllegalArgumentException | SecurityException e) {
            System.out.println("[Sound] ปิดระบบเสียง: " + e.getMessage());
            return false;
        }
    }

    public void play(Sfx s) {
        if (!enabled || muted) return;
        long now = System.currentTimeMillis();
        Long last = lastPlayed.get(s);
        if (last != null && now - last < minInterval(s)) return;
        lastPlayed.put(s, now);

        Clip[] pool = clips.get(s);
        if (pool == null) return;
        int i = nextIndex.get(s);
        nextIndex.put(s, (i + 1) % pool.length);
        Clip c = pool[i];
        c.stop();
        applyGain(c, effectiveSfx());
        c.setFramePosition(0);
        c.start();
    }

    // ---------------- เพลงประกอบ ----------------

    public void startMusic() {
        if (!enabled || music == null || musicPlaying) return;
        applyGain(music, effectiveMusic());
        music.setFramePosition(0);
        music.loop(Clip.LOOP_CONTINUOUSLY);
        musicPlaying = true;
    }

    public void stopMusic() {
        if (music != null) music.stop();
        musicPlaying = false;
    }

    // ---------------- ระดับเสียง ----------------

    public void setVolumes(double master, double music, double sfx) {
        this.masterVolume = clamp(master);
        this.musicVolume  = clamp(music);
        this.sfxVolume    = clamp(sfx);
        if (this.music != null) applyGain(this.music, effectiveMusic());
    }

    public double getMasterVolume() { return masterVolume; }
    public double getMusicVolume()  { return musicVolume; }
    public double getSfxVolume()    { return sfxVolume; }

    private double effectiveSfx()   { return muted ? 0 : masterVolume * sfxVolume; }
    private double effectiveMusic() { return muted ? 0 : masterVolume * musicVolume; }

    private static double clamp(double v) { return Math.max(0, Math.min(1, v)); }

    /** แปลงระดับเสียง 0..1 เป็นเดซิเบลแล้วตั้งให้ Clip */
    private void applyGain(Clip c, double volume) {
        if (!c.isControlSupported(FloatControl.Type.MASTER_GAIN)) return;
        FloatControl gain = (FloatControl) c.getControl(FloatControl.Type.MASTER_GAIN);
        float db = volume <= 0.001 ? gain.getMinimum() : (float) (20 * Math.log10(volume));
        gain.setValue(Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), db)));
    }

    private long minInterval(Sfx s) {
        switch (s) {
            case SHOOT:  return 110;
            case DEATH:  return 80;
            case BELL:   return 180;
            case SPLASH: return 140;
            default:     return 40;
        }
    }

    public boolean isMuted()        { return muted; }
    public void    toggleMute() {
        muted = !muted;
        if (music != null) applyGain(music, effectiveMusic());
    }
    public boolean isEnabled()      { return enabled; }

    // =====================================================================
    //                         สังเคราะห์เสียง
    // =====================================================================

    private double[] synth(Sfx s) {
        switch (s) {
            case CLICK: {
                double[] b = buf(0.05);
                sine(b, 0, 0.05, 1200, 1200, 0.22, 60);
                return b;
            }
            case PLACE: {
                double[] b = buf(0.35);
                sine(b, 0, 0.12, 180, 90, 0.45, 25);
                sine(b, 0.02, 0.3, 880, 880, 0.18, 10);
                sine(b, 0.02, 0.3, 1320, 1320, 0.08, 12);
                return b;
            }
            case SHOOT: {
                double[] b = buf(0.08);
                sine(b, 0, 0.08, 1500, 800, 0.10, 35);
                return b;
            }
            case BELL: {
                double[] b = buf(1.3);
                sine(b, 0, 1.3, 523, 523, 0.22, 3.2);
                sine(b, 0, 1.3, 1445, 1445, 0.10, 4.5);
                sine(b, 0, 1.3, 2830, 2830, 0.05, 7);
                sine(b, 0, 1.3, 262, 262, 0.10, 2.4);
                return b;
            }
            case SPLASH: {
                double[] b = buf(0.3);
                noise(b, 0, 0.3, 0.30, 14, 0.25);
                sine(b, 0, 0.12, 700, 300, 0.08, 20);
                return b;
            }
            case DEATH: {
                double[] b = buf(0.28);
                sine(b, 0, 0.28, 420, 130, 0.18, 9);
                noise(b, 0, 0.12, 0.08, 25, 0.4);
                return b;
            }
            case LEAK: {
                double[] b = buf(0.4);
                for (int h = 1; h <= 7; h += 2) sine(b, 0, 0.4, 110 * h, 100 * h, 0.22 / h, 5);
                return b;
            }
            case WAVE: {
                double[] b = buf(2.2);
                noise(b, 0, 0.05, 0.15, 60, 0.3);
                sine(b, 0, 2.2, 150, 148, 0.30, 1.6);
                sine(b, 0, 2.2, 231, 229, 0.16, 1.9);
                sine(b, 0, 2.2, 391, 388, 0.08, 2.4);
                sine(b, 0, 2.2, 610, 606, 0.05, 3.0);
                return b;
            }
            case BOSS: {
                double[] b = buf(2.6);
                for (int i = 0; i < b.length; i++) {
                    double t = i / RATE;
                    double env = Math.min(1, t * 4) * Math.exp(-t * 0.9);
                    double trem = 0.6 + 0.4 * Math.sin(2 * Math.PI * 6 * t);
                    b[i] += env * trem * (0.30 * Math.sin(2 * Math.PI * 70 * t)
                                        + 0.15 * Math.sin(2 * Math.PI * 104 * t)
                                        + 0.06 * Math.sin(2 * Math.PI * 211 * t));
                }
                return b;
            }
            case CURSE: {
                double[] b = buf(0.6);
                for (int i = 0; i < b.length; i++) {
                    double t = i / RATE;
                    double f = 620 - 450 * t / 0.6 + 25 * Math.sin(2 * Math.PI * 12 * t);
                    b[i] += 0.16 * Math.exp(-t * 3) * Math.sin(2 * Math.PI * f * t);
                }
                return b;
            }
            case UPGRADE: {
                double[] b = buf(0.45);
                double[] notes = {523, 659, 784};
                for (int i = 0; i < notes.length; i++) sine(b, i * 0.08, 0.3, notes[i], notes[i], 0.18, 9);
                return b;
            }
            case SELL: {
                double[] b = buf(0.3);
                sine(b, 0, 0.15, 784, 784, 0.16, 14);
                sine(b, 0.09, 0.2, 523, 523, 0.16, 12);
                return b;
            }
            case WIN: {
                double[] b = buf(1.8);
                double[] notes = {523, 659, 784, 1047};
                for (int i = 0; i < notes.length; i++) sine(b, i * 0.13, 1.4, notes[i], notes[i], 0.14, 2.5);
                return b;
            }
            case LOSE: {
                double[] b = buf(1.9);
                double[] notes = {392, 311, 262, 196};
                for (int i = 0; i < notes.length; i++) sine(b, i * 0.28, 0.9, notes[i], notes[i] * 0.98, 0.17, 3);
                return b;
            }
            case ERROR:
            default: {
                double[] b = buf(0.14);
                for (int h = 1; h <= 5; h += 2) sine(b, 0, 0.14, 180 * h, 180 * h, 0.12 / h, 12);
                return b;
            }
        }
    }

    /**
     * เพลงประกอบวนลูป 16 วินาที: เสียงโดรนต่ำ + ฆ้อง + ทำนองเพนทาโทนิกแบบไทยคล้ายระนาด
     * ความถี่ถูกปัดให้ครบรอบพอดีในลูป เพื่อให้รอยต่อตอนวนกลับเนียน
     */
    private double[] synthMusic() {
        final double LEN = 16.0;
        double[] b = buf(LEN);
        int n = b.length;
        // โดรน C3 + G3 แบบหายใจเข้าออกช้า ๆ
        double[] drone = {131, 196, 262};
        double[] dAmp  = {0.050, 0.030, 0.012};
        for (int i = 0; i < n; i++) {
            double t = i / RATE;
            double breathe = 0.75 + 0.25 * Math.sin(2 * Math.PI * t / 8.0);
            double v = 0;
            for (int k = 0; k < drone.length; k++) v += dAmp[k] * Math.sin(2 * Math.PI * drone[k] * t);
            b[i] += v * breathe;
        }
        // ฆ้องที่จังหวะ 0 และ 8 วินาที (หางเสียงวนต่อไปต้นลูป)
        for (double start : new double[]{0, 8}) {
            int s0 = (int) (start * RATE);
            for (int i = 0; i < (int) (6 * RATE); i++) {
                double t = i / RATE;
                double env = Math.exp(-t * 0.9) * Math.min(1, t / 0.01);
                double v = 0.07 * Math.sin(2 * Math.PI * 98 * t) + 0.035 * Math.sin(2 * Math.PI * 147.5 * t)
                         + 0.018 * Math.sin(2 * Math.PI * 233 * t);
                b[(s0 + i) % n] += v * env;
            }
        }
        // ทำนองระนาด: บันไดเสียง C D E G A
        double[] scale = {523.25, 587.33, 659.26, 783.99, 880.00, 1046.5};
        int[] melody = {0, 2, 3, 2, 4, 3, 2, -1, 1, 2, 4, 5, 4, 2, 3, -1,
                        3, 4, 3, 2, 1, 2, 0, -1, 2, 3, 2, 1, 0, -1, 0, -1};
        double step = LEN / melody.length;
        for (int m = 0; m < melody.length; m++) {
            if (melody[m] < 0) continue;
            double f = scale[melody[m]];
            int s0 = (int) (m * step * RATE);
            for (int i = 0; i < (int) (0.9 * RATE); i++) {
                double t = i / RATE;
                double env = Math.exp(-t * 7) * Math.min(1, t / 0.003);
                double v = 0.09 * Math.sin(2 * Math.PI * f * t) + 0.03 * Math.sin(2 * Math.PI * f * 2.76 * t);
                b[(s0 + i) % n] += v * env;
            }
        }
        return b;
    }

    private double[] buf(double seconds) { return new double[(int) (seconds * RATE)]; }

    /** เพิ่มคลื่นไซน์ที่ไล่ความถี่จาก f0 ไป f1 และค่อย ๆ จางลงแบบ exponential */
    private void sine(double[] b, double start, double dur, double f0, double f1,
                      double amp, double decay) {
        int s0 = (int) (start * RATE);
        int n = (int) (dur * RATE);
        double phase = 0;
        for (int i = 0; i < n && s0 + i < b.length; i++) {
            double t = i / RATE;
            double f = f0 + (f1 - f0) * (i / (double) n);
            phase += 2 * Math.PI * f / RATE;
            double attack = Math.min(1, t / 0.004);
            b[s0 + i] += amp * attack * Math.exp(-t * decay) * Math.sin(phase);
        }
    }

    /** เพิ่มเสียงซ่า (noise) ผ่าน low-pass filter อย่างง่าย */
    private void noise(double[] b, double start, double dur, double amp, double decay, double lp) {
        Random rnd = new Random(7);
        int s0 = (int) (start * RATE);
        int n = (int) (dur * RATE);
        double prev = 0;
        for (int i = 0; i < n && s0 + i < b.length; i++) {
            double t = i / RATE;
            prev = prev + lp * ((rnd.nextDouble() * 2 - 1) - prev);
            b[s0 + i] += amp * Math.exp(-t * decay) * prev;
        }
    }

    private byte[] toPcm(double[] samples) {
        return toPcm(samples, true);
    }

    /** Overloading: เพลงวนลูปไม่ต้องเฟดท้าย (เดี๋ยวรอยต่อจะสะดุด) */
    private byte[] toPcm(double[] samples, boolean fadeOut) {
        byte[] out = new byte[samples.length * 2];
        int fade = fadeOut ? Math.min(200, samples.length) : 0;
        for (int i = 0; i < samples.length; i++) {
            double v = samples[i];
            if (i > samples.length - fade) v *= (samples.length - i) / (double) fade;
            v = Math.max(-1, Math.min(1, v));
            short s = (short) (v * 32000);
            out[i * 2]     = (byte) (s & 0xFF);
            out[i * 2 + 1] = (byte) ((s >> 8) & 0xFF);
        }
        return out;
    }
}
