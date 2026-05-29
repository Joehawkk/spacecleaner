package com.spacecleaner;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.spacecleaner.screens.MenuScreen;

public class SpaceCleanerGame extends Game {

    /** Shared batch for direct sprite rendering (game world + HUD bar).
     *  Stages each own their own internal batch. */
    public SpriteBatch batch;

    @Override
    public void create() {
        batch = new SpriteBatch();
        Prefs.init();
        Assets.load();
        setScreen(new MenuScreen(this));
    }

    @Override public void pause()  { AudioManager.pauseMusic(); }
    @Override public void resume() { AudioManager.resumeMusic(); }

    @Override
    public void dispose() {
        if (getScreen() != null) getScreen().hide();
        batch.dispose();
        Assets.dispose();
    }
}
