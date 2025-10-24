package edu.commonwealthu.hw3_mchenry;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;

/**
 * Provides game-related sound effects to an activity.
 *
 * @author Ryan McHenry
 */
public class SoundManager {
    private SoundPool soundPool;
    private boolean soundEnabled = true;
    private final int startSound;  // start a new game
    private final int whooshSound; // rotate a subgrid
    private final int failSound;   // invalid move
    private final int undoSound;   // take back a move
    private final int winSound;    // puzzle solved
    private final int cancelSound; // cancel dialog
    private final int overSound;   // game over

    /**
     * Initializes a new sound manager for a given context
     */
    public SoundManager(Context context) {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder().setMaxStreams(1)
                .setAudioAttributes(audioAttributes).build();
        startSound = soundPool.load(context, R.raw.start, 1);
        whooshSound = soundPool.load(context, R.raw.whoosh, 1);
        failSound = soundPool.load(context, R.raw.fail, 1);
        winSound = soundPool.load(context, R.raw.win, 1);
        undoSound = soundPool.load(context, R.raw.undo, 1);
        cancelSound = soundPool.load(context, R.raw.cancel, 1);
        overSound = soundPool.load(context, R.raw.game_over, 1);
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    public void toggleSoundEnabled() {
        soundEnabled = !soundEnabled;
    }

    public void setSoundEnabled(boolean enabled) {
        soundEnabled = enabled;
    }

    /**
     * Releases all memory and resources used by the SoundPool.
     */
    public void release() {
        if(soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }

    public void playStartSound() {
        play(startSound);
    }

    public void playWhooshSound() {play(whooshSound);}

    public void playFailSound() {
        play(failSound);
    }

    public void playWinSound() {
        play(winSound);
    }

    public void playUndoSound() {
        play(undoSound);
    }

    public void playCancelSound() { play(cancelSound); }

    public void playOverSound() { play(overSound); }

    /**
     * Plays a sound specified by its resource ID.
     */
    private void play(int id) {
        if(soundEnabled && soundPool != null) {
            soundPool.play(id, 1, 1, 0, 0, 1);
        }
    }
}