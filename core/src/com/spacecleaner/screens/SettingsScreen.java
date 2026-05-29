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

public class SettingsScreen implements Screen {

    private final SpaceCleanerGame game;
    private Stage stage;
    private TextButton musicBtn, soundBtn;
    private Label toastLabel;
    private float toastTimer = 0f;

    public SettingsScreen(SpaceCleanerGame game) { this.game = game; }

    @Override
    public void show() {
        stage = new Stage(new FillViewport(480, 854));
        Gdx.input.setInputProcessor(stage);

        Label.LabelStyle titleStyle = new Label.LabelStyle(Assets.font36, Color.WHITE);
        Label title = new Label("Settings", titleStyle);

        musicBtn  = makeWideBtn("music: "  + on(Prefs.isMusicEnabled()));
        soundBtn  = makeWideBtn("sound: "  + on(Prefs.isSoundEnabled()));
        TextButton clearBtn  = makeShortBtn("clear records");
        TextButton returnBtn = makeShortBtn("return");

        addScaleAnim(musicBtn); addScaleAnim(soundBtn);
        addScaleAnim(clearBtn); addScaleAnim(returnBtn);

        musicBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, Actor a) {
                boolean next = !Prefs.isMusicEnabled();
                Prefs.setMusicEnabled(next);
                musicBtn.setText("music: " + on(next));
                if (next) AudioManager.playMusic(); else AudioManager.stopMusic();
            }
        });
        soundBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, Actor a) {
                boolean next = !Prefs.isSoundEnabled();
                Prefs.setSoundEnabled(next);
                soundBtn.setText("sound: " + on(next));
            }
        });
        clearBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, Actor a) {
                Prefs.clearScores();
                showToast("Records cleared");
            }
        });
        returnBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, Actor a) {
                game.setScreen(new MenuScreen(game));
            }
        });

        Label.LabelStyle toastStyle = new Label.LabelStyle(Assets.font20, new Color(0.8f, 1f, 0.8f, 1f));
        toastLabel = new Label("", toastStyle);
        toastLabel.setVisible(false);

        // Панель с кнопками — всё центрировано в Table
        Table panel = new Table();
        panel.add(title).padBottom(40).row();
        panel.add(musicBtn).width(220).padBottom(14).row();
        panel.add(soundBtn).width(220).padBottom(14).row();
        panel.add(clearBtn).width(180).padBottom(14).row();
        panel.add(toastLabel).padBottom(4).row();
        panel.add(returnBtn).width(160).padTop(10);

        // Центрируем всё на экране через fillParent
        Table root = new Table();
        root.setFillParent(true);
        root.center();
        root.add(panel);
        stage.addActor(root);
    }

    private String on(boolean b) { return b ? "on" : "off"; }

    private TextButton makeWideBtn(String text) {
        TextButton.TextButtonStyle s = new TextButton.TextButtonStyle();
        s.font = Assets.font28; s.fontColor = new Color(0.15f, 0.15f, 0.15f, 1f);
        s.up = s.down = new TextureRegionDrawable(new TextureRegion(Assets.buttonBgLong));
        return new TextButton(text, s);
    }

    private TextButton makeShortBtn(String text) {
        TextButton.TextButtonStyle s = new TextButton.TextButtonStyle();
        s.font = Assets.font28; s.fontColor = new Color(0.15f, 0.15f, 0.15f, 1f);
        s.up = s.down = new TextureRegionDrawable(new TextureRegion(Assets.buttonBgShort));
        return new TextButton(text, s);
    }

    private void addScaleAnim(final TextButton btn) {
        btn.addListener(new InputListener() {
            @Override public boolean touchDown(InputEvent e, float x, float y, int p, int b) {
                btn.addAction(Actions.scaleTo(0.95f, 0.95f, 0.05f)); return false;
            }
            @Override public void touchUp(InputEvent e, float x, float y, int p, int b) {
                btn.addAction(Actions.scaleTo(1f, 1f, 0.05f));
            }
        });
    }

    private void showToast(String msg) {
        toastLabel.setText(msg);
        toastLabel.setVisible(true);
        toastTimer = 1.5f;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.08f, 0.10f, 0.18f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (toastTimer > 0) {
            toastTimer -= delta;
            if (toastTimer <= 0) toastLabel.setVisible(false);
        }

        // FillViewport: применяем viewport и берём камеру
        stage.getViewport().apply();
        game.batch.setProjectionMatrix(stage.getCamera().combined);

        game.batch.begin();
        game.batch.draw(Assets.background, 0, 0, 480, 854);
        // Панель-подложка центрирована в виртуальном пространстве
        game.batch.draw(Assets.blackoutMiddle, 240 - 170, 427 - 220, 340, 440);
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
