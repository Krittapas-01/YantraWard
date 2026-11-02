package yantra.system;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import yantra.core.Setting;
import yantra.util.Vec2;

/**
 * แผนที่แบบตาราง โหลดจากไฟล์ข้อความใน resources (เพิ่มด่านใหม่ได้โดยไม่ต้องแก้โค้ด)
 *   .  พื้นดิน (วางป้อมได้)        #  ทางเดิน        =  สะพาน (ทางเดินข้ามน้ำ)
 *   S  จุดเกิด (ป่าช้า มีได้หลายจุด)  E  พระอุโบสถ
 *   T  ต้นไม้   G  หลุมศพ   C  เจดีย์   ~  น้ำ   W  เขตวัด  (ตกแต่ง วางป้อมไม่ได้)
 */
public class GameMap {

    public static final char GROUND = '.', PATH = '#', BRIDGE = '=', SPAWN = 'S', GOAL = 'E';
    public static final char TREE = 'T', GRAVE = 'G', CHEDI = 'C', WATER = '~', TEMPLE = 'W';

    private final char[][] tiles = new char[Setting.ROWS][Setting.COLS];
    private final Set<Integer> occupied = new HashSet<>();
    private final List<Vec2<Integer>> spawns = new ArrayList<>();
    private final List<List<Vec2<Integer>>> paths = new ArrayList<>();
    private Vec2<Integer> goal;

    public GameMap(String resourcePath) {
        parse(readLines(resourcePath));
        Pathfinder finder = new Pathfinder();
        for (Vec2<Integer> s : spawns) {
            List<Vec2<Integer>> p = finder.findPath(this, s, goal);
            if (p.isEmpty()) throw new IllegalStateException("จุดเกิด " + s + " ไม่มีทางไปถึงพระอุโบสถ");
            paths.add(Collections.unmodifiableList(p));
        }
    }

    /** ตัวอย่างการจัดการ IOException — อ่านไม่ได้ก็ใช้แผนที่สำรอง เกมไม่พัง */
    private List<String> readLines(String resourcePath) {
        List<String> lines = new ArrayList<>();
        try (InputStream in = GameMap.class.getResourceAsStream(resourcePath)) {
            if (in == null) throw new FileNotFoundException(resourcePath);
            BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            String line;
            while ((line = br.readLine()) != null) if (!line.trim().isEmpty()) lines.add(line.trim());
        } catch (IOException e) {
            System.out.println("[GameMap] อ่านแผนที่ไม่สำเร็จ (" + e.getMessage() + ") — ใช้แผนที่สำรอง");
            lines = Arrays.asList(FALLBACK);
        }
        return lines;
    }

    private void parse(List<String> lines) {
        for (int r = 0; r < Setting.ROWS; r++) {
            String line = r < lines.size() ? lines.get(r) : "";
            for (int c = 0; c < Setting.COLS; c++) {
                char ch = c < line.length() ? line.charAt(c) : GROUND;
                tiles[r][c] = ch;
                if (ch == SPAWN) spawns.add(new Vec2<>(c, r));
                if (ch == GOAL)  goal = new Vec2<>(c, r);
            }
        }
        if (spawns.isEmpty() || goal == null) throw new IllegalStateException("แผนที่ต้องมีทั้ง S และ E");
    }

    public boolean isWalkable(int c, int r) {
        char t = tiles[r][c];
        return t == PATH || t == BRIDGE || t == SPAWN || t == GOAL;
    }

    public boolean isBuildable(int c, int r) {
        if (!inBounds(c, r)) return false;
        return tiles[r][c] == GROUND && !occupied.contains(r * Setting.COLS + c);
    }

    public boolean inBounds(int c, int r) {
        return c >= 0 && r >= 0 && c < Setting.COLS && r < Setting.ROWS;
    }

    /** มุมซ้ายบนของเขตวัด (บล็อก W) ใช้วางรูปพระอุโบสถ — ถ้าไม่มีจะคืน null */
    public Vec2<Integer> getTempleOrigin() {
        for (int r = 0; r < Setting.ROWS; r++)
            for (int c = 0; c < Setting.COLS; c++)
                if (tiles[r][c] == TEMPLE) return new Vec2<>(c, r);
        return null;
    }

    public void occupy(int c, int r)  { occupied.add(r * Setting.COLS + c); }
    public void release(int c, int r) { occupied.remove(r * Setting.COLS + c); }

    public char getTile(int c, int r)              { return tiles[r][c]; }
    public int  getCols()                          { return Setting.COLS; }
    public int  getRows()                          { return Setting.ROWS; }
    public Vec2<Integer> getGoal()                 { return goal; }
    public List<Vec2<Integer>> getSpawns()         { return Collections.unmodifiableList(spawns); }
    public List<List<Vec2<Integer>>> getPaths()    { return Collections.unmodifiableList(paths); }

    private static final String[] FALLBACK = {
        "TT..G.....T....TT...",
        "S########.....T...T.",
        ".G.G....#...........",
        "........#.....T.....",
        "..#######.........T.",
        "..#..............T..",
        "T.#.......T.........",
        "..############......",
        ".T...........#...TWW",
        ".............#....WW",
        "T...T........######E",
        "TT......T.....T...TT"
    };
}
