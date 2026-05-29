package com.spacecleaner.game;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Pool;
import com.spacecleaner.Assets;

/** Пуля игрока. Поддерживает Pool для избежания лишних аллокаций. */
public class Bullet implements Pool.Poolable {
    public static final float W      = 12f;
    public static final float H      = 24f;
    public static final float SPEED  = 500f;
    public static final float RADIUS = 8f;

    public float   x;
    public float   y;
    public boolean active = true;

    public Bullet() {}

    /** Инициализирует пулю после получения из пула. */
    public Bullet init(float x, float y) {
        this.x = x;
        this.y = y;
        this.active = true;
        return this;
    }

    /** Сбрасывает состояние при возврате в пул (вызывается Pool.free()). */
    @Override
    public void reset() {
        x = 0; y = 0; active = false;
    }

    public void update(float delta) {
        y += SPEED * delta;
        if (y > 854f + H) active = false;
    }

    public void draw(SpriteBatch batch) {
        batch.draw(Assets.bullet, x - W / 2f, y - H / 2f, W, H);
    }
}
