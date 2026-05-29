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
import com.spacecleaner.Prefs;
import com.spacecleaner.SpaceCleanerGame;
import com.spacecleaner.screens.MenuScreen;
import java.util.List;

/**
 * Game-over overlay showing the top-5 leaderboard.
 * Dim drawn by GameScreen; this class owns the Stage.
 *
 * Call show(score) once when game over occurs.
 */
public class GameOverOverlay {

    private final SpaceCleanerGame game;
    private Stage stage;
    private boolean ready = false;

    public GameOverOverlay(SpaceCleanerGame game) { this.game = game; }

    /** One-shot: saves score, builds Stage UI, activates input. */
    public void show(int score) {
        if (ready) return;
        ready = true;
        AudioManager.stopMusic();
        Prefs.saveScore(score);

        List<Integer> scores = Prefs.getScores();

        stage = new Stage(new StretchViewport(480, 854));
        Gdx.input.setInputProcessor(stage);

        Table t = new Table();
        t.setFillParent(true);
        t.center();

        Label.LabelStyle titleStyle = new Label.LabelStyle(Assets.font36, Color.WHITE);
        t.add(new Label("Last records", titleStyle)).padBottom(28).row();

        for (int i = 0; i < 5; i++) {
            int s = scores.get(i);
            String text = (i + 1) + ".  " + (s > 0 ? String.valueOf(s) : "—");
            // Highlight the current score in yellow (first match)
            Color c = (s == score && s > 0 && i == scores.indexOf(score))
                    ? new Color(1f, 0.85f, 0f, 1f) : Color.WHITE;
            Label.LabelStyle ls = new Label.LabelStyle(Assets.font28, c);
            t.add(new Label(text, ls)).left().padBottom(10).row();
        }

        TextButton.TextButtonStyle bs = new TextButton.TextButtonStyle();
        bs.font = Assets.font28; bs.fontColor = new Color(0.15f, 0.15f, 0.15f, 1f);
        bs.up = bs.down = new TextureRegionDrawable(new TextureRegion(Assets.buttonBgShort));

        TextButton homeBtn = new TextButton("Home", bs);
        homeBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, Actor a) {
                game.setScreen(new MenuScreen(game));
            }
        });
        t.add(homeBtn).width(160).padTop(24);
        stage.addActor(t);
    }

    public boolean isReady()         { return ready; }
    public Stage getStage()          { return stage; }
    public void act(float delta)     { if (stage != null) stage.act(delta); }
    public void draw()               { if (stage != null) { stage.getViewport().apply(); stage.draw(); } }
    public void resize(int w, int h) { if (stage != null) stage.getViewport().update(w, h, true); }
    public void dispose()            { if (stage != null) stage.dispose(); }
}
