package yantra.system;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Properties;

/**
 * คลาสแม่ของไฟล์ตั้งค่าแบบ key=value
 * ScoreStore (คะแนนสูงสุด) และ SettingsStore (ตั้งค่าเสียง) สืบทอดไปใช้ ไม่ต้องเขียนโค้ดอ่าน/เขียนไฟล์ซ้ำ
 */
public abstract class PropertiesFile {

    private final Path file;
    private final String header;
    protected final Properties props = new Properties();

    protected PropertiesFile(String fileName, String header) {
        this.file = Paths.get(fileName);
        this.header = header;
        load();
    }

    private void load() {
        if (!Files.exists(file)) return;
        try (Reader r = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            props.load(r);
        } catch (IOException e) {
            System.out.println("[" + getClass().getSimpleName() + "] อ่านไฟล์ไม่ได้: " + e.getMessage());
        }
    }

    protected void save() {
        try (Writer w = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            props.store(w, header);
        } catch (IOException e) {
            System.out.println("[" + getClass().getSimpleName() + "] บันทึกไฟล์ไม่ได้: " + e.getMessage());
        }
    }

    protected double getDouble(String key, double def) {
        try {
            return Double.parseDouble(props.getProperty(key, String.valueOf(def)));
        } catch (NumberFormatException e) {
            return def;
        }
    }

    protected int getInt(String key, int def) {
        try {
            return Integer.parseInt(props.getProperty(key, String.valueOf(def)));
        } catch (NumberFormatException e) {
            return def;
        }
    }
}
