package com.spacecleaner;

public class AudioManager {

    public static void playMusic() {
        if (Prefs.isMusicEnabled() && !Assets.backgroundMusic.isPlaying())
            Assets.backgroundMusic.play();
    }

    public static void stopMusic()   { Assets.backgroundMusic.stop(); }
    public static void pauseMusic()  { Assets.backgroundMusic.pause(); }

    public static void resumeMusic() {
        if (Prefs.isMusicEnabled() && !Assets.backgroundMusic.isPlaying())
            Assets.backgroundMusic.play();
    }

    public static void playShoot()   { if (Prefs.isSoundEnabled()) Assets.shootSound.play(); }
    public static void playDestroy() { if (Prefs.isSoundEnabled()) Assets.destroySound.play(); }
}
