package yantra.system;

import yantra.util.Difficulty;
import yantra.util.Level;

/** เก็บคะแนนสูงสุดของแต่ละด่าน/ความยากลงไฟล์ (สืบทอดการอ่าน-เขียนไฟล์จาก PropertiesFile) */
public class ScoreStore extends PropertiesFile {

    public ScoreStore(String fileName) {
        super(fileName, "Yantra Ward - high scores");
    }

    private static String key(Level level, Difficulty d) {
        return level.name() + "." + d.name();
    }

    public int getBest(Level level, Difficulty d) {
        return getInt(key(level, d), 0);
    }

    /** @return true ถ้าเป็นสถิติใหม่ */
    public boolean submit(Level level, Difficulty d, int score) {
        if (score <= getBest(level, d)) return false;
        props.setProperty(key(level, d), String.valueOf(score));
        save();
        return true;
    }
}
