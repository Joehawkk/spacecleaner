package com.spacecleaner;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Prefs {
    private static final String NAME = "space_cleaner";
    private static Preferences prefs;

    public static void init() {
        prefs = Gdx.app.getPreferences(NAME);
    }

    public static boolean isMusicEnabled()        { return prefs.getBoolean("musicEnabled", true); }
    public static void setMusicEnabled(boolean v) { prefs.putBoolean("musicEnabled", v); prefs.flush(); }

    public static boolean isSoundEnabled()        { return prefs.getBoolean("soundEnabled", true); }
    public static void setSoundEnabled(boolean v) { prefs.putBoolean("soundEnabled", v); prefs.flush(); }

    /** Returns top-5 scores, descending. Unfilled slots are 0. */
    public static List<Integer> getScores() {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) list.add(prefs.getInteger("score_" + i, 0));
        return list;
    }

    /** Inserts newScore into top-5, drops the lowest if needed. */
    public static void saveScore(int newScore) {
        List<Integer> list = getScores();
        list.add(newScore);
        Collections.sort(list, Collections.reverseOrder());
        for (int i = 0; i < 5; i++) prefs.putInteger("score_" + i, list.get(i));
        prefs.flush();
    }

    public static void clearScores() {
        for (int i = 0; i < 5; i++) prefs.putInteger("score_" + i, 0);
        prefs.flush();
    }
}
