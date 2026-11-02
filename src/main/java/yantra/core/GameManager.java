package yantra.core;

import java.awt.Color;
import java.util.*;
import yantra.effect.*;
import yantra.entity.*;
import yantra.system.*;
import yantra.util.*;

/**
 * หัวใจของเกม 1 รอบ — ประกอบระบบย่อยทั้งหมดเข้าด้วยกัน (Composition)
 * และ implement Battlefield เพื่อให้ป้อม/ศัตรูสื่อสารกับสนามได้โดยไม่ต้องรู้จักคลาสนี้โดยตรง
 */
public class GameManager implements Battlefield {

    private final Level         level;
    private final Difficulty    difficulty;
    private final GameMap       map;
    private final EconomySystem economy;
    private final WaveManager   waves;
    private final SoundManager  sound;
    private final ScoreStore    scores;

    private final List<Tower>      towers      = new ArrayList<>();
    private final List<Enemy>      enemies     = new ArrayList<>();
    private final List<Projectile> projectiles = new ArrayList<>();
    private final List<AreaEffect> areas       = new ArrayList<>();
    private final List<Effect>     effects     = new ArrayList<>();

    // ของที่เกิดระหว่างเฟรม จะถูกรวมเข้าลิสต์จริงตอนท้าย (กัน ConcurrentModificationException)
    private final List<Enemy>      newEnemies     = new ArrayList<>();
    private final List<Projectile> newProjectiles = new ArrayList<>();
    private final List<AreaEffect> newAreas       = new ArrayList<>();
    private final List<Effect>     newEffects     = new ArrayList<>();

    private GameState state = GameState.PLAYING;
    private int    lives;
    private int    kills = 0;
    private int    wavesCleared = 0;
    private double elapsed = 0;
    private int    speed = 1;
    private int    finalScore = 0;
    private boolean newRecord = false;

    private TowerType selectedType = TowerType.TALISMAN;
    private Tower     selectedTower;
    private TowerType infoType;          // เครื่องรางในร้านที่กำลังเปิดดูรายละเอียด (null = ไม่ได้เปิด)

    private String toast = "";
    private double toastTimer = 0;
    private String banner = "";
    private Color  bannerColor = Color.WHITE;
    private double bannerTimer = 0;
    private double shakeTimer = 0;

    public GameManager(Level level, Difficulty difficulty, SoundManager sound, ScoreStore scores) {
        this.level      = level;
        this.difficulty = difficulty;
        this.sound      = sound;
        this.scores     = scores;
        this.map        = new GameMap(level.getMapResource());
        this.economy    = new EconomySystem(difficulty.getStartFaith());
        this.waves      = new WaveManager(map.getPaths(), Setting.TOTAL_WAVES,
                                          difficulty.getHpScale(level),
                                          difficulty.getWaveGrowth());
        this.lives      = difficulty.getStartLives();
    }

    // =====================================================================
    //                              วงจรหลัก
    // =====================================================================

    public void update(double dt) {
        if (toastTimer > 0)  toastTimer  -= dt;
        if (bannerTimer > 0) bannerTimer -= dt;
        if (shakeTimer > 0)  shakeTimer  -= dt;
        if (state != GameState.PLAYING) {
            for (Effect e : effects) e.tick(dt);
            effects.removeIf(e -> !e.isAlive());
            return;
        }
        for (int i = 0; i < speed; i++) step(dt);
    }

    private void step(double dt) {
        elapsed += dt;
        flushSpawns();

        waves.update(dt, this);
        if (waves.consumeStarted()) announceWave();
        if (waves.consumeCleared()) rewardWaveClear();

        for (Tower t : towers) {
            t.tick(dt);
            t.tryAttack(this);          // Polymorphism: ป้อมแต่ละชนิดโจมตีคนละแบบ
        }
        for (Projectile p : projectiles) {
            p.tick(dt);
            if (p.hasImpacted()) p.onImpact(this);
        }
        for (AreaEffect a : areas) {
            a.tick(dt);
            a.affect(enemies, dt);
        }
        for (Enemy e : enemies) {
            e.tick(dt);                 // Polymorphism: ผีแต่ละชนิดเคลื่อนที่คนละแบบ
            e.interact(this, dt);
            if (e.hasReachedGoal() && e.isAlive()) onLeak(e);
        }

        Iterator<Enemy> it = enemies.iterator();
        while (it.hasNext()) {
            Enemy e = it.next();
            if (e.isAlive()) continue;
            if (!e.hasReachedGoal()) onKill(e);
            it.remove();
        }
        for (Effect e : effects) e.tick(dt);

        projectiles.removeIf(p -> !p.isAlive());
        areas.removeIf(a -> !a.isAlive());
        effects.removeIf(e -> !e.isAlive());
        flushSpawns();

        if (lives <= 0) {
            lives = 0;
            finish(false);
        } else if (waves.isFinished() && enemies.isEmpty()) {
            finish(true);
        }
    }

    private void flushSpawns() {
        enemies.addAll(newEnemies);         newEnemies.clear();
        projectiles.addAll(newProjectiles); newProjectiles.clear();
        areas.addAll(newAreas);             newAreas.clear();
        effects.addAll(newEffects);         newEffects.clear();
    }

    private void onKill(Enemy e) {
        kills++;
        economy.earn(e.getReward());
        e.onDeath(this);                    // Polymorphism: กองกอยแตกร่าง ตัวอื่นไม่ทำอะไร
        spawnEffect(new FloatingText(e.getX(), e.getY() - 14, "+" + e.getReward(), new Color(0xFFD98E)));
        burst(e.getX(), e.getY(), new Color(0xCFE8C0), e.isBoss() ? 40 : 8);
        playSound(Sfx.DEATH);
        if (e.isBoss()) showBanner("ปราบแม่นาคสำเร็จ!", new Color(0xFFE39A));
    }

    private void onLeak(Enemy e) {
        e.kill();
        lives -= e.getLeakDamage();
        shakeTimer = 0.25;
        spawnEffect(new FloatingText(e.getX(), e.getY() - 14, "-" + e.getLeakDamage(), new Color(0xFF6A6A)));
        playSound(Sfx.LEAK);
    }

    private void announceWave() {
        int w = waves.getCurrentWave();
        if (waves.isBossWave()) {
            showBanner("คลื่นสุดท้าย — แม่นาคมาแล้ว", new Color(0xFF7A8A));
            playSound(Sfx.BOSS);
        } else {
            int faster = (int) Math.round((waves.getSpeedMultiplier() - 1) * 100);
            showBanner(faster > 0 ? "คลื่นที่ " + w + "  ·  ผีเร็วขึ้น " + faster + "%" : "คลื่นที่ " + w,
                       new Color(0xE8D9A8));
            playSound(Sfx.WAVE);
        }
    }

    private void rewardWaveClear() {
        wavesCleared++;
        int bonus = 20 + waves.getCurrentWave() * 4;
        economy.earn(bonus);
        showToast("ผ่านคลื่นที่ " + waves.getCurrentWave() + "  +" + bonus + " ศรัทธา");
    }

    private void finish(boolean win) {
        state = win ? GameState.WIN : GameState.LOSE;
        if (win) wavesCleared = waves.getTotalWaves();
        finalScore = computeScore(win);
        newRecord = scores.submit(level, difficulty, finalScore);
        playSound(win ? Sfx.WIN : Sfx.LOSE);
        selectedTower = null;
        infoType = null;
        bannerTimer = 0;
        toastTimer = 0;
    }

    private int computeScore(boolean win) {
        double raw = kills * 10 + wavesCleared * 150 + lives * 80 + (win ? 2000 : 0);
        return (int) (raw * difficulty.getScoreMultiplier());
    }

    private void burst(double x, double y, Color c, int n) {
        for (int i = 0; i < n; i++) {
            double a = Math.random() * Math.PI * 2;
            double v = 30 + Math.random() * 70;
            spawnEffect(new Particle(x, y, Math.cos(a) * v, Math.sin(a) * v - 20, c, 3, 0.45));
        }
    }

    // =====================================================================
    //                           การกระทำของผู้เล่น
    // =====================================================================

    public int countOf(TowerType type) {
        int n = 0;
        for (Tower t : towers) if (t.getType() == type) n++;
        return n;
    }

    /**
     * ราคาตัวถัดไป — ยิ่งมีเครื่องรางชนิดเดียวกันมาก ตัวต่อไปยิ่งแพง
     * (เพิ่มกี่ % ขึ้นกับระดับความยาก) กันการสร้างซ้ำ ๆ จนล้นสนาม
     */
    public int getPrice(TowerType type) {
        double raw = type.getCost() * (1 + difficulty.getPriceStep() * countOf(type));
        return (int) Math.round(raw / 5.0) * 5;
    }

    public void clickTile(int col, int row) {
        Tower existing = towerAt(col, row);
        if (existing != null) {
            selectedTower = (selectedTower == existing) ? null : existing;
            infoType = null;
            playSound(Sfx.CLICK);
            return;
        }
        selectedTower = null;
        if (!map.isBuildable(col, row)) {
            showToast("วางเครื่องรางตรงนี้ไม่ได้");
            playSound(Sfx.ERROR);
            return;
        }
        try {
            int price = getPrice(selectedType);
            economy.spend(price);
            Tower t = selectedType.create(col, row);
            t.setPurchasePrice(price);
            towers.add(t);
            map.occupy(col, row);
            infoType = null;
            spawnEffect(new RingEffect(t.getX(), t.getY(), selectedType.getColor(), 26, 0.35));
            playSound(Sfx.PLACE);
        } catch (InsufficientFaithException ex) {
            showToast("ศรัทธาไม่พอ — ต้องใช้ " + ex.getRequired());
            playSound(Sfx.ERROR);
        }
    }

    public void upgradeSelected() {
        if (selectedTower == null) return;
        if (!selectedTower.canUpgrade()) { showToast("เครื่องรางนี้เต็มระดับแล้ว"); return; }
        try {
            economy.spend(selectedTower.getUpgradeCost());
            selectedTower.upgrade();
            spawnEffect(new RingEffect(selectedTower.getX(), selectedTower.getY(), new Color(0xFFE39A), 30, 0.4));
            playSound(Sfx.UPGRADE);
        } catch (InsufficientFaithException ex) {
            showToast("ศรัทธาไม่พอสำหรับอัปเกรด (ต้องใช้ " + ex.getRequired() + ")");
            playSound(Sfx.ERROR);
        }
    }

    public void sellSelected() {
        if (selectedTower == null) return;
        int value = selectedTower.getSellValue();
        economy.earn(value);
        map.release(selectedTower.getTileX(), selectedTower.getTileY());
        towers.remove(selectedTower);
        spawnEffect(new FloatingText(selectedTower.getX(), selectedTower.getY() - 10, "+" + value, new Color(0xFFD98E)));
        selectedTower = null;
        playSound(Sfx.SELL);
    }

    public void callNextWave() {
        if (!waves.canCallEarly()) return;
        int bonus = waves.callEarly();
        if (bonus > 0) {
            economy.earn(bonus);
            showToast("เรียกคลื่นก่อนเวลา  +" + bonus + " ศรัทธา");
        }
    }

    /** วนความเร็ว x1 -> x2 -> x4 -> x1 (เดินเกมหลายสเต็ปย่อยต่อเฟรม จึงไม่มีกระสุนทะลุเป้า) */
    public void toggleSpeed() { speed = speed == 1 ? 2 : speed == 2 ? 4 : 1; playSound(Sfx.CLICK); }

    public void togglePause() {
        if (state == GameState.PLAYING)     state = GameState.PAUSED;
        else if (state == GameState.PAUSED) state = GameState.PLAYING;
    }

    /**
     * เลือกเครื่องรางจากร้าน — คลิกครั้งแรก = เลือก, คลิกซ้ำ = เปิด/ปิดรายละเอียด
     * ถ้าแผงรายละเอียดเปิดอยู่แล้วกดชนิดอื่น แผงจะเปลี่ยนไปแสดงชนิดใหม่ทันที
     */
    public void selectType(TowerType t) {
        if (t == selectedType) {
            infoType = (infoType == null) ? t : null;
        } else {
            selectedType = t;
            if (infoType != null) infoType = t;
        }
        selectedTower = null;
        playSound(Sfx.CLICK);
    }

    public void deselect() {
        selectedTower = null;
        infoType = null;
    }

    public Tower towerAt(int col, int row) {
        for (Tower t : towers) if (t.getTileX() == col && t.getTileY() == row) return t;
        return null;
    }

    public boolean canPlaceAt(int col, int row) {
        return map.isBuildable(col, row) && economy.canAfford(getPrice(selectedType));
    }

    public Enemy getBoss() {
        for (Enemy e : enemies) if (e.isBoss() && e.isAlive()) return e;
        return null;
    }

    private void showToast(String s)            { toast = s; toastTimer = 2.0; }
    private void showBanner(String s, Color c)  { banner = s; bannerColor = c; bannerTimer = 2.4; }

    // =====================================================================
    //                            Battlefield
    // =====================================================================

    @Override public List<Enemy> getEnemies()             { return enemies; }
    @Override public List<Tower> getTowers()              { return towers; }
    @Override public void spawnEnemy(Enemy e)             { newEnemies.add(e); }
    @Override public void spawnProjectile(Projectile p)   { newProjectiles.add(p); }
    @Override public void spawnArea(AreaEffect a)         { newAreas.add(a); }
    @Override public void spawnEffect(Effect e)           { newEffects.add(e); }
    @Override public void playSound(Sfx s)                { sound.play(s); }
    @Override public int  getAliveEnemyCount()            { return enemies.size() + newEnemies.size(); }

    // =====================================================================
    //                              Getter
    // =====================================================================

    public GameMap       getMap()         { return map; }
    public EconomySystem getEconomy()     { return economy; }
    public WaveManager   getWaves()       { return waves; }
    public Difficulty    getDifficulty()  { return difficulty; }
    public Level         getLevel()       { return level; }
    public List<Projectile> getProjectiles() { return projectiles; }
    public List<AreaEffect> getAreas()    { return areas; }
    public List<Effect>  getEffects()     { return effects; }
    public GameState getState()           { return state; }
    public int    getLives()              { return lives; }
    public int    getKills()              { return kills; }
    public double getElapsed()            { return elapsed; }
    public int    getSpeed()              { return speed; }
    public int    getFinalScore()         { return finalScore; }
    public boolean isNewRecord()          { return newRecord; }
    public int    getBestScore()          { return scores.getBest(level, difficulty); }
    public TowerType getSelectedType()    { return selectedType; }
    public Tower  getSelectedTower()      { return selectedTower; }
    public TowerType getInfoType()        { return infoType; }
    public String getToast()              { return toastTimer > 0 ? toast : ""; }
    public double getToastAlpha()         { return Math.min(1, toastTimer / 0.4); }
    public String getBanner()             { return bannerTimer > 0 ? banner : ""; }
    public Color  getBannerColor()        { return bannerColor; }
    public double getBannerAlpha()        { return Math.min(1, Math.min(bannerTimer / 0.5, (2.4 - bannerTimer) / 0.3)); }
    public double getShake()              { return shakeTimer > 0 ? shakeTimer * 12 : 0; }
    public boolean isMuted()              { return sound.isMuted(); }
    public void   toggleMute()            { sound.toggleMute(); }
}
