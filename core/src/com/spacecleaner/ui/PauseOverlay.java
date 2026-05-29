package com.spacecleaner.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.spacecleaner.Assets;
import com.spacecleaner.AudioManager;
import com.spacecleaner.SpaceCleanerGame;
import com.spacecleaner.screens.MenuScreen;

/**
 * Pause overlay: dim is drawn by GameScreen; this class owns only the Stage.
 *
 * GameScreen usage:
 *   pauseOverlay.activate();   // pauses music, sets input processor
 *
 *   // in render():
 *   batch.begin(); draw dim; batch.end();
 *   pauseOverlay.act(delta); pauseOverlay.draw();
 */
public class PauseOverlay {

    private final Stage stage;

    public PauseOverlay(SpaceCleanerGame game, Runnable onContinue) {
        stage = new Stage(new StretchViewport(480, 854));

        Label.LabelStyle ts = new Label.LabelStyle(Assets.font36, Color.WHITE);
        Label title = new Label("Pause", ts);

        TextButton.TextButtonStyle bs = new TextButton.TextButtonStyle();
        bs.font = Assets.font28; bs.fontColor = new Color(0.15f, 0.15f, 0.15f, 1f);
        bs.up = bs.down = new TextureRegionDrawable(new TextureRegion(Assets.buttonBgShort));

        TextButton homeBtn = new TextButton("Home", bs);
        TextButton contBtn = new TextButton("Continue", bs);

        homeBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, Actor a) {
                AudioManager.stopMusic();
                game.setScreen(new MenuScreen(game));
            }
        });
        contBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, Actor a) { onContinue.run(); }
        });

        Table t = new Table();
        t.setFillParent(true);
        t.center();
        t.add(title).colspan(2).padBottom(40).row();
        t.add(homeBtn).width(160).padRight(20);
        t.add(contBtn).width(160);
        stage.addActor(t);
    }

    public Stage getStage()          { return stage; }
    public void act(float delta)     { stage.act(delta); }
    public void draw()               { stage.getViewport().apply(); stage.draw(); }
    public void resize(int w, int h) { stage.getViewport().update(w, h, true); }
    public void dispose()            { stage.dispose(); }

    /** Call when pausing — sets this stage as the active input processor and pauses music. */
    public void activate() {
        AudioManager.pauseMusic();
        Gdx.input.setInputProcessor(stage);
    }
}
