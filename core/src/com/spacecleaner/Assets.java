package com.spacecleaner;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;

public class Assets {

    private static AssetManager manager;

    // ── Textures ──────────────────────────────────────────────────────────
    public static Texture background;
    public static Texture blackoutFull;
    public static Texture blackoutMiddle;
    public static Texture blackoutTop;
    public static Texture bullet;
    public static Texture buttonBgLong;
    public static Texture buttonBgShort;
    public static Texture life;
    public static Texture pauseIcon;
    public static Texture ship;
    public static Texture trash;

    // ── Audio ─────────────────────────────────────────────────────────────
    public static Music backgroundMusic;
    public static Sound shootSound;
    public static Sound destroySound;

    // ── Fonts ─────────────────────────────────────────────────────────────
    public static BitmapFont font20;
    public static BitmapFont font28;
    public static BitmapFont font36;

    public static void load() {
        manager = new AssetManager();

        manager.load("textures/background.png",              Texture.class);
        manager.load("textures/blackout_full.png",           Texture.class);
        manager.load("textures/blackout_middle.png",         Texture.class);
        manager.load("textures/blackout_top.png",            Texture.class);
        manager.load("textures/bullet.png",                  Texture.class);
        manager.load("textures/button_background_long.png",  Texture.class);
        manager.load("textures/button_background_short.png", Texture.class);
        manager.load("textures/life.png",                    Texture.class);
        manager.load("textures/pause_icon.png",              Texture.class);
        manager.load("textures/ship.png",                    Texture.class);
        manager.load("textures/trash.png",                   Texture.class);
        manager.load("sounds/background_music.mp3",          Music.class);
        manager.load("sounds/shoot.mp3",                     Sound.class);
        manager.load("sounds/destroy.mp3",                   Sound.class);
        manager.finishLoading();

        background     = manager.get("textures/background.png",              Texture.class);
        blackoutFull   = manager.get("textures/blackout_full.png",           Texture.class);
        blackoutMiddle = manager.get("textures/blackout_middle.png",         Texture.class);
        blackoutTop    = manager.get("textures/blackout_top.png",            Texture.class);
        bullet         = manager.get("textures/bullet.png",                  Texture.class);
        buttonBgLong   = manager.get("textures/button_background_long.png",  Texture.class);
        buttonBgShort  = manager.get("textures/button_background_short.png", Texture.class);
        life           = manager.get("textures/life.png",                    Texture.class);
        pauseIcon      = manager.get("textures/pause_icon.png",              Texture.class);
        ship           = manager.get("textures/ship.png",                    Texture.class);
        trash          = manager.get("textures/trash.png",                   Texture.class);
        backgroundMusic= manager.get("sounds/background_music.mp3",          Music.class);
        shootSound     = manager.get("sounds/shoot.mp3",                     Sound.class);
        destroySound   = manager.get("sounds/destroy.mp3",                   Sound.class);
        backgroundMusic.setLooping(true);

        FreeTypeFontGenerator gen = new FreeTypeFontGenerator(
                Gdx.files.internal("fonts/Montserrat-Bold.ttf"));
        FreeTypeFontParameter p = new FreeTypeFontParameter();
        p.color = Color.WHITE;

        p.size = 20; font20 = gen.generateFont(p);
        p.size = 28; font28 = gen.generateFont(p);
        p.size = 36; font36 = gen.generateFont(p);
        gen.dispose();
    }

    public static void dispose() {
        if (font20  != null) { font20.dispose();  font20  = null; }
        if (font28  != null) { font28.dispose();  font28  = null; }
        if (font36  != null) { font36.dispose();  font36  = null; }
        if (manager != null) { manager.dispose(); manager = null; }
    }
}
