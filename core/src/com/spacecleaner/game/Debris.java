package com.spacecleaner.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.spacecleaner.Assets;

/**
 * Кусок космического мусора.
 * Визуально разнообразен: 4 варианта цвета × 3 варианта размера.
 */
public class Debris {

    // Возможные размеры (и радиус пропорционален)
    private static final float[] SIZES = { 48f, 64f, 80f };

    // Варианты окраски: белый (нормальный), серый, коричневатый, зеленоватый
    private static final Color[] TINTS = {
        new Color(1.00f, 1.00f, 1.00f, 1f),   // белый  — обычный
        new Color(0.65f, 0.65f, 0.70f, 1f),   // серый  — металлический
        new Color(0.80f, 0.55f, 0.25f, 1f),   // ржавый — старый
        new Color(0.45f, 0.70f, 0.45f, 1f),   // зелёный — органика
    };

    public float   x;           // центр
    public float   y;           // центр
    public float   speed;
    public float   size;        // размер спрайта
    public float   radius;      // радиус коллизии
    public float   rotation     = 0f;
    public float   rotationSpeed;
    public boolean active       = true;

    private final Color tint;

    public Debris(float centerX, float speed) {
        this.x     = centerX;
        this.y     = 854f * 0.9f;
        this.speed = speed;

        // Случайный размер
        this.size   = SIZES[MathUtils.random(SIZES.length - 1)];
        this.radius = size * 0.44f;   // ~44% от размера → коллизия чуть меньше спрайта

        // Случайная окраска
        this.tint = TINTS[MathUtils.random(TINTS.length - 1)];

        // Случайная скорость вращения ±45..90 °/с
        float mag = 45f + MathUtils.random(45f);
        this.rotationSpeed = MathUtils.randomBoolean() ? mag : -mag;
    }

    public void update(float delta) {
        y        -= speed * delta;
        rotation += rotationSpeed * delta;
        if (y < -size) active = false;
    }

    public void draw(SpriteBatch batch) {
        // Применяем тинт, рисуем с вращением вокруг центра
        batch.setColor(tint);
        batch.draw(
            Assets.trash,
            x - size / 2f, y - size / 2f,   // позиция левого нижнего угла
            size / 2f,     size / 2f,        // точка вращения (центр)
            size, size,                      // размер
            1f, 1f,                          // масштаб
            rotation,                        // градусы
            0, 0,                            // srcX, srcY
            Assets.trash.getWidth(), Assets.trash.getHeight(),
            false, false
        );
        batch.setColor(Color.WHITE);   // сброс цвета для других объектов
    }
}
