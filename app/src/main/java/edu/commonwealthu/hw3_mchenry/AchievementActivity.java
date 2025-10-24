package edu.commonwealthu.hw3_mchenry;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

/**
 * Displays gameplay achievements for the player.
 *
 * @author Ryan McHenry
 */

//========================================================================================

public class AchievementActivity extends AppCompatActivity {

    private static final int MIN_SOL_DEPTH = 1;
    private static final int MAX_SOL_DEPTH = 8;

    private SoundManager soundManager;  // for sound effects

    // Keys used to track achievements
    private static final String BOARD_SIZE = "board_size";
    private static final String SOLUTION_DEP = "solution_dep";
    private static final String SOUND_ENABLED = "sound_enabled";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievement);

        // Create and style the toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.showOverflowMenu();
        toolbar.setTitleTextAppearance(this, R.style.ToolbarTitleStyle);
        setSupportActionBar(toolbar);

        // display up button in toolbar for navigating back to parent activity
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        soundManager = new SoundManager(this);
        SharedPreferences preferences = getSharedPreferences(SOUND_ENABLED,
                MODE_PRIVATE);
        soundManager.setSoundEnabled(preferences.getBoolean(SOUND_ENABLED, true));

        loadAchievements();

        findViewById(R.id.resetButton).setOnClickListener(this::resetAchievements);
    }

    /**
     * Load saved player achievements.
     */
    private void loadAchievements() {
        SharedPreferences boardPreferences = getSharedPreferences(BOARD_SIZE,
                MODE_PRIVATE);
        SharedPreferences solPreferences = getSharedPreferences(SOLUTION_DEP,
                MODE_PRIVATE);

        // Load board size achievements
        if (boardPreferences.contains(BOARD_SIZE + 9)) {
            TextView view = findViewById(R.id.threeByThreeBox);
            view.setText(getString(R.string.checkmark));
        }
        if(boardPreferences.contains(BOARD_SIZE + 12)) {
            TextView view = findViewById(R.id.threeByFourBox);
            view.setText(getString(R.string.checkmark));
        }
        if(boardPreferences.contains(BOARD_SIZE + 16)) {
            TextView view = findViewById(R.id.fourByFourBox);
            view.setText(getString(R.string.checkmark));
        }

        // Load solDepth achievements
        for(int i = MIN_SOL_DEPTH; i <= MAX_SOL_DEPTH; i++) {
            if(solPreferences.contains(SOLUTION_DEP + i)) {
                if(i == 1) {
                    TextView view = findViewById(R.id.solOneBox);
                    view.setText(getString(R.string.checkmark));
                } else if(i == 2) {
                    TextView view = findViewById(R.id.solTwoBox);
                    view.setText(getString(R.string.checkmark));
                } else if(i == 3) {
                    TextView view = findViewById(R.id.solThreeBox);
                    view.setText(getString(R.string.checkmark));
                } else if(i == 4) {
                    TextView view = findViewById(R.id.solFourBox);
                    view.setText(getString(R.string.checkmark));
                } else if(i == 5) {
                    TextView view = findViewById(R.id.solFiveBox);
                    view.setText(getString(R.string.checkmark));
                } else if(i == 6) {
                    TextView view = findViewById(R.id.solSixBox);
                    view.setText(getString(R.string.checkmark));
                } else if(i == 7) {
                    TextView view = findViewById(R.id.solSevenBox);
                    view.setText(getString(R.string.checkmark));
                } else {
                    TextView view = findViewById(R.id.solEightBox);
                    view.setText(getString(R.string.checkmark));
                }
            }
        }
    }

    /**
     * Reset all achievements so that the player has none.
     */
    private void resetAchievements(View view) {
        SharedPreferences boardPreferences = getSharedPreferences(BOARD_SIZE,
                MODE_PRIVATE);
        SharedPreferences solPreferences = getSharedPreferences(SOLUTION_DEP,
                MODE_PRIVATE);

        SharedPreferences.Editor boardEditor = boardPreferences.edit();
        SharedPreferences.Editor solEditor = solPreferences.edit();

        // Clear disk of achievement data
        boardEditor.clear();
        solEditor.clear();

        boardEditor.apply();
        solEditor.apply();

        resetAchievementText();

        soundManager.playStartSound();
    }

    /**
     * Reset text for all achievements to reflect the player's achievements being
     * reset.
     */
    private void resetAchievementText() {
        // Reset board size achievement text
        TextView view = findViewById(R.id.threeByThreeBox);
        view.setText(getString(R.string.cross));

        view = findViewById(R.id.threeByFourBox);
        view.setText(getString(R.string.cross));

        view = findViewById(R.id.fourByFourBox);
        view.setText(getString(R.string.cross));


        // Reset solDepth achievement text
        for(int i = MIN_SOL_DEPTH; i <= MAX_SOL_DEPTH; i++) {
            view = findViewById(R.id.solOneBox);
            view.setText(getString(R.string.cross));

            view = findViewById(R.id.solTwoBox);
            view.setText(getString(R.string.cross));

            view = findViewById(R.id.solThreeBox);
            view.setText(getString(R.string.cross));

            view = findViewById(R.id.solFourBox);
            view.setText(getString(R.string.cross));

            view = findViewById(R.id.solFiveBox);
            view.setText(getString(R.string.cross));

            view = findViewById(R.id.solSixBox);
            view.setText(getString(R.string.cross));

            view = findViewById(R.id.solSevenBox);
            view.setText(getString(R.string.cross));

            view = findViewById(R.id.solEightBox);
            view.setText(getString(R.string.cross));
        }
    }

    /**
     * Releases memory and other resources used by the sound manager.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        soundManager.release();
    }
}

//========================================================================================