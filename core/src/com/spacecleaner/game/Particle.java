package com.spacecleaner.game;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Pool;

/**
 * Частица: взрыв при уничтожении мусора ИЛИ огненный след корабля.
 * Поддерживает Pool для повторного использования.
 */
public class Particle implements Pool.Poolable {

    private static final float SIZE_EXPLOSION = 5f;
    private static final float SIZE_TRAIL     = 3f;

    public float   x, y;
    private float  vx, vy;
    private float  maxLife;       // индивидуальный lifetime
    private float  life;
    private float  alpha = 1f;
    private boolean orange;
    private boolean isTrail;      // true = огненный след, false = взрыв
    public boolean  active = true;

    public Particle() {}

    /** Инициализация взрывной частицы. */
    public Particle init(float ox, float oy, boolean orange) {
        this.x       = ox;
        this.y       = oy;
        this.orange  = orange;
        this.isTrail = false;
        this.maxLife = 0.4f;
        this.life    = maxLife;
        this.alpha   = 1f;
        this.active  = true;

        float angle = MathUtils.random(MathUtils.PI2);
        float speed = 80f + MathUtils.random(120f);
        vx = MathUtils.cos(angle) * speed;
        vy = MathUtils.sin(angle) * speed;
        return this;
    }

    /**
     * Инициализация частицы огненного следа.
     * Летит вниз с небольшим разбросом, быстро гаснет.
     */
    public Particle initTrail(float ox, float oy) {
        // Небольшой горизонтальный разброс из выхлопа
        this.x       = ox + (MathUtils.random() - 0.5f) * 10f;
        this.y       = oy;
        this.orange  = MathUtils.randomBoolean();
        this.isTrail = true;
        this.maxLife = 0.15f + MathUtils.random() * 0.15f;
        this.life    = maxLife;
        this.alpha   = 1f;
        this.active  = true;

        // Летит преимущественно вниз
        float spread = (MathUtils.random() - 0.5f) * 0.8f;
        float speed  = 50f + MathUtils.random(60f);
        vx = MathUtils.sin(spread) * speed;
        vy = -speed * MathUtils.cos(spread);
        return this;
    }

    @Override
    public void reset() {
        x = 0; y = 0; vx = 0; vy = 0;
        life = 0; maxLife = 0.4f; alpha = 0; active = false;
    }

    public void update(float delta) {
        x    += vx * delta;
        y    += vy * delta;
        life -= delta;
        alpha = Math.max(0f, life / maxLife);
        if (life <= 0) active = false;
    }

    /** Вызывать между shapeRenderer.begin(Filled) … end(). */
    public void draw(ShapeRenderer sr) {
        float size = isTrail ? SIZE_TRAIL : SIZE_EXPLOSION;
        if (isTrail) {
            // Цвет трейла: жёлтый → оранжевый → прозрачный
            float t = alpha;  // 1 = горячий, 0 = остывший
            sr.setColor(1f, 0.3f + 0.5f * t, 0f, alpha * 0.8f);
        } else {
            if (orange) sr.setColor(1f, 0.55f, 0f, alpha);
            else        sr.setColor(1f, 1f,    1f, alpha);
        }
        sr.rect(x - size / 2f, y - size / 2f, size, size);
    }
}
