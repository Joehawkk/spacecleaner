package com.spacecleaner.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.spacecleaner.Assets;
import com.spacecleaner.AudioManager;
import com.spacecleaner.Prefs;
import com.spacecleaner.SpaceCleanerGame;
import java.util.List;

public class MenuScreen implements Screen {

    private final SpaceCleanerGame game;
    private Stage stage;

    public MenuScreen(SpaceCleanerGame game) { this.game = game; }

    @Override
    public void show() {
        stage = new Stage(new FillViewport(480, 854));
        Gdx.input.setInputProcessor(stage);
        AudioManager.playMusic();

        Label.LabelStyle titleStyle = new Label.LabelStyle(Assets.font36, Color.WHITE);
        Label title = new Label("Space Cleaner", titleStyle);

        TextButton startBtn    = makeBtn("start",    Assets.buttonBgLong);
        TextButton settingsBtn = makeBtn("settings", Assets.buttonBgLong);
        TextButton exitBtn     = makeBtn("exit",     Assets.buttonBgLong);

        addScaleAnim(startBtn); addScaleAnim(settingsBtn); addScaleAnim(exitBtn);

        startBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, Actor a) {
                game.setScreen(new GameScreen(game));
            }
        });
        settingsBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, Actor a) {
                game.setScreen(new SettingsScreen(game));
            }
        });
        exitBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, Actor a) { Gdx.app.exit(); }
        });

        // ── Таблица рекордов ──────────────────────────────────────────────
        List<Integer> scores = Prefs.getScores();
        // Не используем stream() — он недоступен на Android < 7 (API 24)
        boolean hasAnyScore = false;
        for (int i = 0; i < scores.size(); i++) {
            if (scores.get(i) > 0) { hasAnyScore = true; break; }
        }

        Label.LabelStyle scoreHeaderStyle = new Label.LabelStyle(Assets.font20,
            new Color(0.7f, 0.85f, 1f, 1f));   // светло-голубой заголовок
        Label.LabelStyle scoreStyle = new Label.LabelStyle(Assets.font20, Color.WHITE);
        Label.LabelStyle emptyStyle = new Label.LabelStyle(Assets.font20,
            new Color(0.5f, 0.5f, 0.6f, 1f));  // серый для пустых слотов

        // ── Главный layout ────────────────────────────────────────────────
        Table root = new Table();
        root.setFillParent(true);
        root.center();

        root.add(title).padBottom(50).row();
        root.add(startBtn).width(220).padBottom(12).row();
        root.add(settingsBtn).width(220).padBottom(12).row();
        root.add(exitBtn).width(220).padBottom(36).row();

        // Рекорды показываем только если хоть один ненулевой
        if (hasAnyScore) {
            Label header = new Label("— best scores —", scoreHeaderStyle);
            root.add(header).padBottom(8).row();

            for (int i = 0; i < 5; i++) {
                int s = scores.get(i);
                if (s > 0) {
                    Label lbl = new Label((i + 1) + ".   " + s, scoreStyle);
                    root.add(lbl).left().padBottom(4).row();
                } else {
                    Label lbl = new Label((i + 1) + ".   —", emptyStyle);
                    root.add(lbl).left().padBottom(4).row();
                }
            }
        }

        stage.addActor(root);
    }

    private TextButton makeBtn(String text, com.badlogic.gdx.graphics.Texture tex) {
        TextButton.TextButtonStyle s = new TextButton.TextButtonStyle();
        s.font      = Assets.font28;
        s.fontColor = new Color(0.15f, 0.15f, 0.15f, 1f);
        s.up = s.down = new TextureRegionDrawable(new TextureRegion(tex));
        return new TextButton(text, s);
    }

    private void addScaleAnim(final TextButton btn) {
        btn.addListener(new InputListener() {
            @Override public boolean touchDown(InputEvent e, float x, float y, int ptr, int b) {
                btn.addAction(Actions.scaleTo(0.95f, 0.95f, 0.05f)); return false;
            }
            @Override public void touchUp(InputEvent e, float x, float y, int ptr, int b) {
                btn.addAction(Actions.scaleTo(1f, 1f, 0.05f));
            }
        });
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.08f, 0.10f, 0.18f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.getViewport().apply();
        game.batch.setProjectionMatrix(stage.getCamera().combined);

        game.batch.begin();
        game.batch.draw(Assets.background, 0, 0, 480, 854);
        game.batch.end();

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int w, int h) {
        stage.getViewport().update(w, h, true);
    }

    @Override public void hide()    { stage.dispose(); }
    @Override public void pause()   {}
    @Override public void resume()  {}
    @Override public void dispose() {}
}
