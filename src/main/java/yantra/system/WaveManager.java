package yantra.system;

import java.util.*;
import yantra.core.Battlefield;
import yantra.entity.Enemy;
import yantra.entity.EnemyType;
import yantra.util.Vec2;

/** ปล่อยวิญญาณออกมาเป็นคลื่น ๆ และจับเวลาพักระหว่างคลื่น */
public class WaveManager {

    private static final double FIRST_REST = 8.0;
    private static final double REST_TIME  = 10.0;
    private static final double SPEED_GROWTH = 0.03;   // ผีเร็วขึ้น 3% ทุกคลื่น (คลื่น 15 = เร็วขึ้น 42%)

    private final List<List<Vec2<Integer>>> lanes;   // เส้นทางจากแต่ละป่าช้า
    private final int    totalWaves;
    private final double hpScale;
    private final double growth;

    private final Deque<EnemyType> queue = new ArrayDeque<>();
    private int     currentWave = 0;
    private double  spawnTimer  = 0;
    private double  restTimer   = FIRST_REST;
    private boolean running     = false;
    private boolean justStarted = false;
    private boolean justCleared = false;
    private int     nextLane    = 0;

    public WaveManager(List<List<Vec2<Integer>>> lanes, int totalWaves, double hpScale, double growth) {
        this.lanes = lanes;
        this.totalWaves = totalWaves;
        this.hpScale = hpScale;
        this.growth = growth;
    }

    public void update(double dt, Battlefield field) {
        if (!running) {
            if (currentWave < totalWaves) {
                restTimer -= dt;
                if (restTimer <= 0) startNextWave();
            }
            return;
        }
        spawnTimer -= dt;
        if (spawnTimer <= 0 && !queue.isEmpty()) {
            Enemy e = queue.poll().create(lanes.get(nextLane), waveScale());
            e.setSpeedMultiplier(getSpeedMultiplier());
            e.setRewardMultiplier(getRewardMultiplier());
            field.spawnEnemy(e);
            nextLane = (nextLane + 1) % lanes.size();      // ถ้ามีหลายป่าช้า จะปล่อยสลับกัน
            spawnTimer = spawnInterval();
        }
        if (queue.isEmpty() && field.getAliveEnemyCount() == 0) {
            running = false;
            restTimer = REST_TIME;
            justCleared = true;
        }
    }

    /** เรียกคลื่นถัดไปก่อนเวลา ได้โบนัสตามเวลาที่เหลือ */
    public int callEarly() {
        if (running || currentWave >= totalWaves) return 0;
        int bonus = (int) Math.ceil(restTimer) * 3;
        startNextWave();
        return bonus;
    }

    private void startNextWave() {
        currentWave++;
        running = true;
        justStarted = true;
        spawnTimer = 0.4;
        buildQueue(currentWave);
    }

    // Overloading: ใส่ศัตรูทีละตัว หรือทีละหลายตัว
    private void enqueue(List<EnemyType> list, EnemyType type)            { list.add(type); }
    private void enqueue(List<EnemyType> list, EnemyType type, int count) { for (int i = 0; i < count; i++) list.add(type); }

    private void buildQueue(int w) {
        List<EnemyType> list = new ArrayList<>();
        enqueue(list, EnemyType.PRET, 5 + w * 2);
        if (w >= 3) enqueue(list, EnemyType.KRASUE, (w + 1) / 3);
        if (w >= 4) enqueue(list, EnemyType.POP, w / 3);
        if (w >= 6) enqueue(list, EnemyType.KONGKOI, (w - 4) / 2);
        Collections.shuffle(list.subList(Math.min(4, list.size()), list.size()), new Random(w * 31L));
        if (w == totalWaves) enqueue(list, EnemyType.NAK);   // บอสปิดท้าย
        queue.clear();
        queue.addAll(list);
    }

    /** ศรัทธาต่อตัวลดลง 2.5% ทุกคลื่น (คลื่น 15 เหลือราว 65%) — กันเงินล้นมือช่วงท้ายเกม */
    public double getRewardMultiplier() { return Math.max(0.5, 1 - Math.max(0, currentWave - 1) * 0.025); }

    /** ตัวคูณความเร็วของคลื่นปัจจุบัน เช่น 1.21 = เร็วขึ้น 21% */
    public double getSpeedMultiplier() { return 1 + Math.max(0, currentWave - 1) * SPEED_GROWTH; }

    private double waveScale()     { return hpScale * (1 + (currentWave - 1) * growth); }
    private double spawnInterval() { return Math.max(0.38, 0.95 - currentWave * 0.035); }

    /** อ่านแล้วล้างค่า — ให้ GameManager รู้ว่าเพิ่งเริ่ม/จบคลื่น */
    public boolean consumeStarted() { boolean b = justStarted; justStarted = false; return b; }
    public boolean consumeCleared() { boolean b = justCleared; justCleared = false; return b; }

    public int     getCurrentWave() { return currentWave; }
    public int     getTotalWaves()  { return totalWaves; }
    public boolean isRunning()      { return running; }
    public boolean isBossWave()     { return currentWave == totalWaves; }
    public double  getRestTimer()   { return Math.max(0, restTimer); }
    public int     getQueued()      { return queue.size(); }
    public boolean isFinished()     { return currentWave >= totalWaves && !running; }
    public boolean canCallEarly()   { return !running && currentWave < totalWaves; }
}
