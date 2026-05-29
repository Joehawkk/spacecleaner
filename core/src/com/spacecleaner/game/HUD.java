package com.spacecleaner.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.spacecleaner.Assets;

/**
 * Верхняя панель HUD: счёт, жизни, комбо, мультивыстрел, кнопка паузы.
 *
 * Порядок вызова из GameScreen:
 *   1. hud.drawBar(batch, score, lives, combo, shotMode, shotTimer)  — внутри batch.begin/end
 *   2. hud.act(delta); hud.drawStage()                               — снаружи batch.begin/end
 */
public class HUD {

    private static final float BAR_H     = 64f;
    private static final float LIFE_SIZE = 22f;
    private static final float LIFE_GAP  = 6f;
    private static final float PAUSE_SZ  = 36f;
    private static final float VIRT_W    = 480f;
    private static final float VIRT_H    = 854f;

    private static final Color COLOR_DOUBLE = new Color(1f, 0.85f, 0f, 1f);
    private static final Color COLOR_TRIPLE = new Color(1f, 0.45f, 0f, 1f);

    private final GlyphLayout layout = new GlyphLayout();
    private final Stage hudStage;

    public HUD(Runnable onPause) {
        hudStage = new Stage(new StretchViewport(VIRT_W, VIRT_H));

        ImageButton pauseBtn = new ImageButton(
            new TextureRegionDrawable(new TextureRegion(Assets.pauseIcon)));
        pauseBtn.setSize(PAUSE_SZ, PAUSE_SZ);
        pauseBtn.setPosition(
            VIRT_W - PAUSE_SZ - 12f,
            VIRT_H - BAR_H + (BAR_H - PAUSE_SZ) / 2f);
        pauseBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, Actor a) { onPause.run(); }
        });
        hudStage.addActor(pauseBtn);
    }

    /**
     * Рисует фон полосы, очки, жизни, комбо, индикатор мультивыстрела.
     * Вызывать ВНУТРИ открытого SpriteBatch.
     *
     * @param shotMode    1/2/3 — режим стрельбы
     * @param shotTimer   секунды до конца мультивыстрела (0 = неактивен)
     */
    public void drawBar(SpriteBatch batch, int score, int lives, int combo,
                        int shotMode, float shotTimer) {
        float barY = VIRT_H - BAR_H;
        float midY = barY + BAR_H / 2f;

        // Фон полосы
        batch.draw(Assets.blackoutTop, 0, barY, VIRT_W, BAR_H);

        BitmapFont font = Assets.font20;

        // ── Левый блок: счёт + комбо + мультивыстрел ──────────────────────
        float textX = 12f;
        String scoreText = "Score: " + score;
        layout.setText(font, scoreText);
        float scoreH = layout.height;

        // Сколько строк нужно в левом блоке
        boolean showCombo = combo > 1;
        boolean showShot  = shotMode > 1;

        int lineCount = 1 + (showCombo ? 1 : 0) + (showShot ? 1 : 0);
        float lineH   = scoreH + 3f;
        float topY    = midY + (lineCount * lineH) / 2f;

        float curY = topY;

        // Строка 1: счёт
        font.setColor(Color.WHITE);
        font.draw(batch, scoreText, textX, curY);
        curY -= lineH;

        // Строка 2: комбо (если > 1)
        if (showCombo) {
            font.setColor(1f, 0.85f, 0f, 1f);
            font.draw(batch, "x" + combo + " combo", textX, curY);
            curY -= lineH;
        }

        // Строка 3: мультивыстрел (если активен)
        if (showShot) {
            font.setColor(shotMode == 2 ? COLOR_DOUBLE : COLOR_TRIPLE);
            font.draw(batch, "x" + shotMode + " shot  " + (int)(shotTimer + 1) + "s", textX, curY);
        }

        // ── Центр: жизни ──────────────────────────────────────────────────
        int safeCount = Math.max(0, Math.min(3, lives));
        if (safeCount > 0) {
            float totalW = safeCount * LIFE_SIZE + (safeCount - 1) * LIFE_GAP;
            float lifeX  = (VIRT_W - totalW) / 2f;
            float lifeY  = midY - LIFE_SIZE / 2f;
            for (int i = 0; i < safeCount; i++) {
                batch.draw(Assets.life,
                    lifeX + i * (LIFE_SIZE + LIFE_GAP), lifeY,
                    LIFE_SIZE, LIFE_SIZE);
            }
        }
    }

    public void act(float delta)  { hudStage.act(delta); }
    public void drawStage()       { hudStage.getViewport().apply(); hudStage.draw(); }
    public Stage getStage()       { return hudStage; }
    public void resize(int w, int h) { hudStage.getViewport().update(w, h, true); }
    public void dispose()            { hudStage.dispose(); }
}
