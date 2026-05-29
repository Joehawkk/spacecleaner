package com.spacecleaner.game;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.spacecleaner.Assets;

public class Ship {
    public static final float W      = 80f;
    public static final float H      = 80f;
    public static final float RADIUS = 30f;

    public static final float MAX_Y  = 427f;
    public static final float MIN_X  = W / 2f;
    public static final float MAX_X  = 480f - W / 2f;

    // ── Позиция ──────────────────────────────────────────────────────────────
    public float x = 240f;
    public float y;   // инициализируется анимацией влёта

    // ── Жизни / щит ──────────────────────────────────────────────────────────
    public int     lives     = 3;
    public boolean hasShield = false;

    // ── Неуязвимость / мигание ───────────────────────────────────────────────
    private float   invincTimer  = 0f;
    private static final float INVINC_DURATION = 1.5f;
    private float   blinkTimer   = 0f;
    private boolean blinkVisible = true;

    // ── Мультивыстрел ────────────────────────────────────────────────────────
    /** Режим стрельбы: 1 = одиночный, 2 = двойной, 3 = тройной. */
    public int   shotMode = 1;
    /** Оставшееся время мультивыстрела (секунды). */
    public float multiShotTimer = 0f;
    public static final float MULTISHOT_DURATION = 10f;

    /** Активировать мультивыстрел (2 или 3 ствола). */
    public void applyMultiShot(int shots) {
        shotMode       = Math.min(3, shots);
        multiShotTimer = MULTISHOT_DURATION;
    }

    // ── Анимация влёта ───────────────────────────────────────────────────────
    /** true пока корабль летит снизу к стартовой позиции. */
    public boolean isEntering = true;
    private float enterTimer  = 0f;
    private static final float ENTER_DURATION = 1.2f;
    private static final float ENTER_START_Y  = -H;          // за нижним краем
    private static final float ENTER_END_Y    = 128f;         // рабочая позиция

    // ── Движение ─────────────────────────────────────────────────────────────
    public void moveTo(float worldX) {
        if (isEntering) return;   // нельзя двигать во время влёта
        x = Math.max(MIN_X, Math.min(MAX_X, worldX));
    }

    // ── Урон ─────────────────────────────────────────────────────────────────
    /**
     * Наносит урон. Возвращает true если потеряна жизнь.
     * Во время влёта урон не принимается.
     */
    public boolean takeDamage() {
        if (isEntering)         return false;
        if (invincTimer > 0)    return false;
        if (hasShield) {
            hasShield   = false;
            invincTimer = INVINC_DURATION;
            blinkTimer  = 0f;
            return false;
        }
        lives--;
        invincTimer = INVINC_DURATION;
        blinkTimer  = 0f;
        return true;
    }

    public boolean isAlive()      { return lives > 0; }
    public boolean isInvincible() { return invincTimer > 0 || isEntering; }

    // ── Обновление ───────────────────────────────────────────────────────────
    public void update(float delta) {
        // Анимация влёта (ease-out квадратичная)
        if (isEntering) {
            enterTimer += delta;
            float t    = Math.min(1f, enterTimer / ENTER_DURATION);
            float ease = 1f - (1f - t) * (1f - t);
            y = ENTER_START_Y + (ENTER_END_Y - ENTER_START_Y) * ease;
            if (t >= 1f) {
                isEntering = false;
                y          = ENTER_END_Y;
            }
            return;
        }

        // Неуязвимость + мигание
        if (invincTimer > 0) {
            invincTimer -= delta;
            blinkTimer  += delta;
            if (blinkTimer >= 0.1f) { blinkTimer = 0f; blinkVisible = !blinkVisible; }
            if (invincTimer <= 0)   { blinkVisible = true; }
        }

        // Таймер мультивыстрела
        if (multiShotTimer > 0) {
            multiShotTimer -= delta;
            if (multiShotTimer <= 0) {
                multiShotTimer = 0f;
                shotMode       = 1;
            }
        }
    }

    // ── Рендер ───────────────────────────────────────────────────────────────
    public void draw(SpriteBatch batch) {
        if (!blinkVisible) return;
        batch.draw(Assets.ship, x - W / 2f, y - H / 2f, W, H);
    }

    /** Точка спауна пули (верхний центр корабля). */
    public float getBulletX() { return x; }
    public float getBulletY() { return y + H / 2f; }

    /** Позиция выхлопа для огненного следа (нижний центр). */
    public float getExhaustX() { return x; }
    public float getExhaustY() { return y - H / 2f + 8f; }
}
