package com.spacecleaner.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.spacecleaner.Assets;

/**
 * Бонус-предмет трёх видов:
 *   SHIELD      — щит (голубой, «S»)
 *   DOUBLE_SHOT — двойной выстрел на 10 сек (золотой, «×2»)
 *   TRIPLE_SHOT — тройной выстрел на 10 сек (оранжевый, «×3»)
 */
public class PowerUp {

    public enum Type { SHIELD, DOUBLE_SHOT, TRIPLE_SHOT }

    public static final float RADIUS = 24f;
    public static final float SPEED  = 150f;

    public float x;
    public float y;
    public boolean active = true;
    public final Type type;

    public PowerUp(float x, Type type) {
        this.x    = x;
        this.y    = 854f * 0.9f;
        this.type = type;
    }

    public void update(float delta) {
        y -= SPEED * delta;
        if (y < -RADIUS * 2) active = false;
    }

    /** Вызывать между shapeRenderer.begin(Filled) … end(). */
    public void drawShape(ShapeRenderer sr) {
        switch (type) {
            case SHIELD:
                sr.setColor(0.2f, 0.9f, 1f, 0.85f);
                break;
            case DOUBLE_SHOT:
                sr.setColor(1f, 0.85f, 0f, 0.85f);
                break;
            default: // TRIPLE_SHOT
                sr.setColor(1f, 0.45f, 0f, 0.85f);
                break;
        }
        sr.circle(x, y, RADIUS, 32);
        // Блик для объёма
        sr.setColor(1f, 1f, 1f, 0.2f);
        sr.circle(x - RADIUS * 0.2f, y + RADIUS * 0.2f, RADIUS * 0.3f, 16);
    }

    /** Вызывать между batch.begin() … end(). */
    public void drawLabel(SpriteBatch batch) {
        switch (type) {
            case SHIELD:
                Assets.font28.setColor(Color.WHITE);
                Assets.font28.draw(batch, "S", x - 8f, y + 10f);
                break;
            case DOUBLE_SHOT:
                Assets.font20.setColor(Color.WHITE);
                Assets.font20.draw(batch, "x2", x - 12f, y + 9f);
                break;
            default: // TRIPLE_SHOT
                Assets.font20.setColor(Color.WHITE);
                Assets.font20.draw(batch, "x3", x - 12f, y + 9f);
                break;
        }
    }
}
