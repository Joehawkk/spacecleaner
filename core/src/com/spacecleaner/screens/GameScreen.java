package com.spacecleaner.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.spacecleaner.Assets;
import com.spacecleaner.AudioManager;
import com.spacecleaner.SpaceCleanerGame;
import com.spacecleaner.game.Bullet;
import com.spacecleaner.game.Debris;
import com.spacecleaner.game.HUD;
import com.spacecleaner.game.Particle;
import com.spacecleaner.game.PowerUp;
import com.spacecleaner.game.Ship;
import com.spacecleaner.ui.GameOverOverlay;
import com.spacecleaner.ui.PauseOverlay;

public class GameScreen implements Screen {

    static final float W = 480f;
    static final float H = 854f;

    static final float BG_SPEED       = 60f;
    static final float FIRE_INTERVAL  = 0.4f;
    static final float FLASH_DURATION = 0.25f;
    static final float SHIELD_CHANCE  = 0.15f;

    private enum State { PLAYING, PAUSED, GAME_OVER }

    // ── Core ────────────────────────────────────────────────────────────────
    private final SpaceCleanerGame game;
    private OrthographicCamera camera;
    private FillViewport        viewport;
    private ShapeRenderer       sr;

    // ── Пулы объектов (избегаем лишних аллокаций и GC) ──────────────────────
    private final Pool<Bullet> bulletPool = new Pool<Bullet>(32) {
        @Override protected Bullet newObject() { return new Bullet(); }
    };
    private final Pool<Particle> particlePool = new Pool<Particle>(64) {
        @Override protected Particle newObject() { return new Particle(); }
    };

    // ── Entities ────────────────────────────────────────────────────────────
    private Ship               ship;
    private Array<Bullet>      bullets    = new Array<>();
    private Array<Debris>      debrisList = new Array<>();
    private Array<PowerUp>     powerUps   = new Array<>();
    private Array<Particle>    particles  = new Array<>();

    // ── UI ──────────────────────────────────────────────────────────────────
    private HUD             hud;
    private PauseOverlay    pauseOverlay;
    private GameOverOverlay gameOverOverlay;

    // ── State ───────────────────────────────────────────────────────────────
    private State state = State.PLAYING;

    // ── Background ──────────────────────────────────────────────────────────
    private float bgY1 = 0f;
    private float bgY2 = H;

    // ── Scoring ─────────────────────────────────────────────────────────────
    private int   score        = 0;
    private float survivalTime = 0f;
    private float scoreAccum   = 0f;   // +1/sec accumulator
    private int   combo        = 1;

    // ── Timers ──────────────────────────────────────────────────────────────
    private float fireTimer  = 0f;
    private float spawnTimer = 0f;

    // ── Hit flash ───────────────────────────────────────────────────────────
    private float flashTimer = 0f;

    // ── Огненный след ────────────────────────────────────────────────────────
    private float trailTimer = 0f;
    private static final float TRAIL_INTERVAL = 0.04f;  // частицы 25 раз в секунду

    // ── Матрица для полноэкранного рендера ────────────────────────────────────
    private final Matrix4 fullScreenProj = new Matrix4().setToOrtho2D(0, 0, W, H);

    // ── Input ───────────────────────────────────────────────────────────────
    private InputMultiplexer gameMux;
    private final Vector3 touchWorld = new Vector3();
    private final InputAdapter dragAdapter = new InputAdapter() {
        @Override
        public boolean touchDragged(int sx, int sy, int pointer) {
            if (state != State.PLAYING) return false;
            touchWorld.set(sx, sy, 0);
            camera.unproject(touchWorld,
                viewport.getScreenX(), viewport.getScreenY(),
                viewport.getScreenWidth(), viewport.getScreenHeight());
            ship.moveTo(touchWorld.x);
            return true;
        }
    };

    public GameScreen(SpaceCleanerGame game) { this.game = game; }

    // ── Lifecycle ────────────────────────────────────────────────────────────

    @Override
    public void show() {
        camera   = new OrthographicCamera();
        viewport = new FillViewport(W, H, camera);
        viewport.apply(true);
        sr = new ShapeRenderer();

        ship           = new Ship();
        hud            = new HUD(this::pauseGame);
        pauseOverlay   = new PauseOverlay(game, this::resumeGame);
        gameOverOverlay = new GameOverOverlay(game);

        gameMux = new InputMultiplexer(hud.getStage(), dragAdapter);
        Gdx.input.setInputProcessor(gameMux);

        AudioManager.playMusic();
        state = State.PLAYING;
    }

    // ── State transitions ────────────────────────────────────────────────────

    private void pauseGame() {
        state = State.PAUSED;
        pauseOverlay.activate();      // pauses music, sets input to pause stage
    }

    private void resumeGame() {
        state = State.PLAYING;
        Gdx.input.setInputProcessor(gameMux);
        AudioManager.resumeMusic();
    }

    private void triggerGameOver() {
        state = State.GAME_OVER;
        gameOverOverlay.show(score);  // saves score, sets input to game-over stage
    }

    // ── Update ───────────────────────────────────────────────────────────────

    private void update(float delta) {
        // Background scroll: два тайла движутся вниз.
        // FIX: при переносе тайла точно ставим его СРАЗУ над другим (bgOther + H),
        // без дробных сдвигов — это устраняет однопиксельный разрыв.
        bgY1 -= BG_SPEED * delta;
        bgY2 -= BG_SPEED * delta;
        if (bgY1 + H <= 0) bgY1 = bgY2 + H;
        if (bgY2 + H <= 0) bgY2 = bgY1 + H;

        ship.update(delta);

        // ── Огненный след из выхлопа ──────────────────────────────────────
        trailTimer += delta;
        if (trailTimer >= TRAIL_INTERVAL) {
            trailTimer -= TRAIL_INTERVAL;
            particles.add(particlePool.obtain().initTrail(
                ship.getExhaustX(), ship.getExhaustY()));
        }

        // Во время влёта счёт не идёт и выстрелы заблокированы
        if (ship.isEntering) return;

        // Time-based score: +1/sec
        survivalTime += delta;
        scoreAccum   += delta;
        while (scoreAccum >= 1f) { scoreAccum -= 1f; score++; }

        // ── Автовыстрел (одиночный / двойной / тройной) ───────────────────
        fireTimer += delta;
        if (fireTimer >= FIRE_INTERVAL) {
            fireTimer -= FIRE_INTERVAL;
            float bx = ship.getBulletX();
            float by = ship.getBulletY();
            switch (ship.shotMode) {
                case 3:   // тройной: центр + левый + правый
                    bullets.add(bulletPool.obtain().init(bx,       by));
                    bullets.add(bulletPool.obtain().init(bx - 22f, by));
                    bullets.add(bulletPool.obtain().init(bx + 22f, by));
                    break;
                case 2:   // двойной: два рядом
                    bullets.add(bulletPool.obtain().init(bx - 12f, by));
                    bullets.add(bulletPool.obtain().init(bx + 12f, by));
                    break;
                default:  // одиночный
                    bullets.add(bulletPool.obtain().init(bx, by));
            }
            AudioManager.playShoot();
        }

        // Update bullets — возвращаем в пул вместо GC
        for (int i = bullets.size - 1; i >= 0; i--) {
            Bullet b = bullets.get(i);
            b.update(delta);
            if (!b.active) { bulletPool.free(bullets.removeIndex(i)); }
        }

        // ── Спаун мусора / бонусов ────────────────────────────────────────
        float interval = Math.max(0.45f, 1.8f - survivalTime * 0.035f);
        spawnTimer += delta;
        if (spawnTimer >= interval) {
            spawnTimer -= interval;
            float spawnX = 32f + MathUtils.random(0f, 416f);
            float roll   = MathUtils.random();
            if (roll < 0.08f) {
                // 8% — щит
                powerUps.add(new PowerUp(spawnX, PowerUp.Type.SHIELD));
            } else if (roll < 0.14f) {
                // 6% — двойной выстрел
                powerUps.add(new PowerUp(spawnX, PowerUp.Type.DOUBLE_SHOT));
            } else if (roll < 0.18f) {
                // 4% — тройной выстрел (редкий!)
                powerUps.add(new PowerUp(spawnX, PowerUp.Type.TRIPLE_SHOT));
            } else {
                float speed = Math.min(420f, 180f + survivalTime * 8f);
                debrisList.add(new Debris(spawnX, speed));
            }
        }

        // Update and collide debris
        for (int i = debrisList.size - 1; i >= 0; i--) {
            Debris d = debrisList.get(i);
            d.update(delta);
            if (!d.active) { debrisList.removeIndex(i); continue; }

            // Bullet ↔ Debris
            boolean shot = false;
            for (int j = bullets.size - 1; j >= 0; j--) {
                Bullet b = bullets.get(j);
                float dx = b.x - d.x, dy = b.y - d.y;
                float rSum = Bullet.RADIUS + d.radius;
                if (dx * dx + dy * dy < rSum * rSum) {
                    bulletPool.free(bullets.removeIndex(j));
                    d.active = false;
                    shot     = true;
                    score   += 10 * combo;
                    combo    = Math.min(5, combo + 1);
                    AudioManager.playDestroy();
                    spawnParticles(d.x, d.y);
                    break;
                }
            }
            if (shot) { debrisList.removeIndex(i); continue; }

            // Ship ↔ Debris
            float dx = ship.x - d.x, dy = ship.y - d.y;
            float rSum = Ship.RADIUS + d.radius;
            if (dx * dx + dy * dy < rSum * rSum) {
                d.active = false;
                debrisList.removeIndex(i);
                AudioManager.playDestroy();
                spawnParticles(d.x, d.y);
                // Ship.takeDamage() consumes hasShield on first hit (returns false if shield
                // absorbed the hit without losing a life); returns true only if a life was lost.
                boolean lifeLost = ship.takeDamage();
                if (lifeLost) {
                    combo      = 1;
                    flashTimer = FLASH_DURATION;
                }
                if (!ship.isAlive()) { triggerGameOver(); return; }
            }
        }

        // Update and collide power-ups
        for (int i = powerUps.size - 1; i >= 0; i--) {
            PowerUp p = powerUps.get(i);
            p.update(delta);
            if (!p.active) { powerUps.removeIndex(i); continue; }

            float dx = ship.x - p.x, dy = ship.y - p.y;
            float rSum = Ship.RADIUS + PowerUp.RADIUS;
            if (dx * dx + dy * dy < rSum * rSum) {
                PowerUp.Type t = p.type;
                p.active = false;
                powerUps.removeIndex(i);
                switch (t) {
                    case SHIELD:      ship.hasShield = true;         break;
                    case DOUBLE_SHOT: ship.applyMultiShot(2);        break;
                    case TRIPLE_SHOT: ship.applyMultiShot(3);        break;
                }
            }
        }

        // Update particles — возвращаем в пул вместо GC
        for (int i = particles.size - 1; i >= 0; i--) {
            Particle p = particles.get(i);
            p.update(delta);
            if (!p.active) { particlePool.free(particles.removeIndex(i)); }
        }

        // Decay flash
        if (flashTimer > 0) flashTimer = Math.max(0f, flashTimer - delta);
    }

    private void spawnParticles(float ox, float oy) {
        int n = 6 + MathUtils.random(2);   // 6–8
        for (int i = 0; i < n; i++) {
            particles.add(particlePool.obtain().init(ox, oy, i % 2 == 0));
        }
    }

    // ── Render ───────────────────────────────────────────────────────────────

    @Override
    public void render(float delta) {
        if (state == State.PLAYING) update(delta);

        final int scW = Gdx.graphics.getWidth();
        final int scH = Gdx.graphics.getHeight();

        Gdx.gl.glClearColor(0.08f, 0.10f, 0.18f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // ── 0. Фон на ВЕСЬ экран ──────────────────────────────────────────────
        Gdx.gl.glViewport(0, 0, scW, scH);
        game.batch.setProjectionMatrix(fullScreenProj);
        game.batch.begin();
        game.batch.draw(Assets.background, 0, bgY1, W, H);
        game.batch.draw(Assets.background, 0, bgY2, W, H);
        game.batch.end();

        // ── 1. Игровое поле — viewport (правильные координаты) ──────────────
        viewport.apply();
        camera.update();
        game.batch.setProjectionMatrix(camera.combined);
        sr.setProjectionMatrix(camera.combined);

        game.batch.begin();
        for (Bullet  b : bullets)    b.draw(game.batch);
        for (Debris  d : debrisList) d.draw(game.batch);
        for (PowerUp p : powerUps)   p.drawLabel(game.batch);
        ship.draw(game.batch);
        game.batch.end();

        // ── 2. Shape pass — в FitViewport ────────────────────────────────────
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        sr.begin(ShapeRenderer.ShapeType.Filled);
        for (PowerUp  p : powerUps)  p.drawShape(sr);
        for (Particle p : particles) p.draw(sr);
        sr.end();

        if (ship.hasShield) {
            sr.begin(ShapeRenderer.ShapeType.Line);
            sr.setColor(0.2f, 0.9f, 1f, 0.8f);
            sr.circle(ship.x, ship.y, Ship.RADIUS + 12f, 48);
            sr.end();
        }

        if (flashTimer > 0) {
            float alpha = (flashTimer / FLASH_DURATION) * 0.5f;
            Gdx.gl.glViewport(0, 0, scW, scH);
            sr.setProjectionMatrix(fullScreenProj);
            sr.begin(ShapeRenderer.ShapeType.Filled);
            sr.setColor(1f, 0f, 0f, alpha);
            sr.rect(0, 0, W, H);
            sr.end();
        }
        Gdx.gl.glDisable(GL20.GL_BLEND);

        // ── 3. HUD полоса — на ВЕСЬ экран (не только ширину FitViewport) ─────
        Gdx.gl.glViewport(0, 0, scW, scH);
        game.batch.setProjectionMatrix(fullScreenProj);
        game.batch.begin();
        hud.drawBar(game.batch, score, ship.lives, combo, ship.shotMode, ship.multiShotTimer);
        game.batch.end();

        // ── 4. Кнопка паузы ──────────────────────────────────────────────────
        if (state == State.PLAYING) hud.act(delta);
        hud.drawStage();

        // ── 5. Пауза-оверлей ─────────────────────────────────────────────────
        if (state == State.PAUSED) {
            Gdx.gl.glViewport(0, 0, scW, scH);
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            game.batch.setProjectionMatrix(fullScreenProj);
            game.batch.begin();
            game.batch.setColor(0f, 0f, 0f, 0.7f);
            game.batch.draw(Assets.blackoutFull, 0, 0, W, H);
            game.batch.setColor(Color.WHITE);
            game.batch.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);
            pauseOverlay.act(delta);
            pauseOverlay.draw();
        }

        // ── 6. Game-over оверлей ─────────────────────────────────────────────
        if (state == State.GAME_OVER) {
            Gdx.gl.glViewport(0, 0, scW, scH);
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            game.batch.setProjectionMatrix(fullScreenProj);
            game.batch.begin();
            game.batch.setColor(0f, 0f, 0f, 0.85f);
            game.batch.draw(Assets.blackoutFull, 0, 0, W, H);
            game.batch.setColor(Color.WHITE);
            game.batch.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);
            gameOverOverlay.act(delta);
            gameOverOverlay.draw();
        }
    }

    @Override
    public void resize(int w, int h) {
        viewport.update(w, h, true);
        hud.resize(w, h);
        pauseOverlay.resize(w, h);
        gameOverOverlay.resize(w, h);
    }

    @Override public void pause()  { if (state == State.PLAYING) pauseGame(); }
    @Override public void resume() {}

    @Override
    public void hide() {
        // Возвращаем все активные объекты в пулы перед уничтожением экрана
        bulletPool.freeAll(bullets);   bullets.clear();
        particlePool.freeAll(particles); particles.clear();
        debrisList.clear();
        powerUps.clear();

        hud.dispose();
        pauseOverlay.dispose();
        gameOverOverlay.dispose();
        sr.dispose();
    }

    // dispose() is not called by the LibGDX framework on screen switches (hide() is).
    // Left empty to avoid double-dispose of hud/overlays/sr.
    @Override public void dispose() {}
}
