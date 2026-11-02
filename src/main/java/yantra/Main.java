package yantra;

import javax.swing.SwingUtilities;
import yantra.core.Game;

/**
 * จุดเริ่มต้นของโปรแกรม
 * ยันต์ล้อมวัด (Yantra Ward : Temple Defense)
 * วิชา 040613204 Object-Oriented Programming — มจพ.
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Game().start());
    }
}
