package yantra.system;

/** เก็บค่าตั้งเสียงลงไฟล์ settings.txt — เปิดเกมครั้งต่อไปจะได้ค่าเดิม */
public class SettingsStore extends PropertiesFile {

    public SettingsStore(String fileName) {
        super(fileName, "Yantra Ward - settings");
    }

    public double getMasterVolume() { return clamp(getDouble("volume.master", 0.8)); }
    public double getMusicVolume()  { return clamp(getDouble("volume.music", 0.6)); }
    public double getSfxVolume()    { return clamp(getDouble("volume.sfx", 0.8)); }

    public void setVolumes(double master, double music, double sfx) {
        props.setProperty("volume.master", String.format(java.util.Locale.ROOT, "%.2f", clamp(master)));
        props.setProperty("volume.music",  String.format(java.util.Locale.ROOT, "%.2f", clamp(music)));
        props.setProperty("volume.sfx",    String.format(java.util.Locale.ROOT, "%.2f", clamp(sfx)));
        save();
    }

    private static double clamp(double v) { return Math.max(0, Math.min(1, v)); }
}
