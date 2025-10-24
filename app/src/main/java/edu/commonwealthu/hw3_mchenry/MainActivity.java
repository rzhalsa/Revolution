package edu.commonwealthu.hw3_mchenry;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.splashscreen.SplashScreen;

import com.google.android.material.appbar.MaterialToolbar;

/**
 * Plays the game of Revolution.
 *
 * Extra Features:
 *
 *      The app supports mobile phones as well as our Lenovo tablet, as I wanted to be
 *      able to play the game on my own phone. Feel free to try it out on the medium
 *      phone emulator or your own phone and let me know what you think!
 *
 *      Dark mode is supported.
 *
 * Known Issues:
 *
 *      There is a small gap between the rotation animation finishing and the drawBoard()
 *      method being called. This is causing the board to flicker.
 *
 * Credits and Acknowledgements:
 *
 *      Many methods, such as showCustomDialog(), showExitDialog(), and
 *      showCustomText() were copied over as is or modified slightly from the Frogs
 *      and Toads application.
 *
 *      Many of the xml files are direct copies, or were slightly modified from the
 *      Frogs and Toads application.
 *
 *      Every sound mp3 with the exception of whoosh.mp3 was copied over from the
 *      Frogs and Toads application.
 *
 *      The surrender_flag.png and achievement.png assets were obtained from
 *      https://www.vecteezy.com
 *
 * @author Ryan McHenry
 * Date: October 11, 2025
 */

//========================================================================================

public class MainActivity extends AppCompatActivity {

    private static final int initSolDepth = 3;  // solDepth for initial game generation
    private static int rows;                    // number of rows in the game
    private static int cols;                    // number of columns in the game
    private Revolution game;                    // the game object

    private int solDepth;                // dynamic solDepth set by numberPicker
    private GridLayout gridLayout;       // contains Buttons
    private Button[] buttons;            // grid Buttons
    private Button selectedButton;       // currently selected Button
    private int selectedIndex = -1;      // index of selectedButton
    private SoundManager soundManager;   // for sound effects

    // track whether a subgrid is currently in a rotation animation to prevent another
    // rotation while already in one
    private boolean alreadyRotating = false;

    // Keys used to preserve game state.
    private static final String NUM_MOVES = "moves";
    private static final String SOL_DEPTH = "sol_depth";
    private static final String MOVE = "move";
    private static final String DIR = "dir";
    private static final String DIM = "dim";
    private static final String SUR = "sur";
    private static final String BUTTON_VIS = "button_vis";
    private static final String SELECTED_INDEX = "selected_index";

    // Keys used to track achievements
    private static final String BOARD_SIZE = "board_size";
    private static final String SOLUTION_DEP = "solution_dep";
    private static final String SOUND_ENABLED = "sound_enabled";

    /**
     * Initializes instance variables, registers event handlers for control buttons, and
     * displays the game board in the initial configuration.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Install splash screen with a custom duration
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        splashScreen.setKeepOnScreenCondition(() -> true);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            splashScreen.setKeepOnScreenCondition(() -> false);
        }, 500);

        setContentView(R.layout.activity_main);

        // Create and style the toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.showOverflowMenu();
        toolbar.setTitleTextAppearance(this, R.style.ToolbarTitleStyle);
        setSupportActionBar(toolbar);

        gridLayout = findViewById(R.id.mainGridLayout);
        soundManager = new SoundManager(this);

        NumberPicker numberPicker = findViewById(R.id.mainNumberPicker);
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(8);
        numberPicker.setOnValueChangedListener(this::setSolDepth);

        if(savedInstanceState == null) {
            game = new Revolution(initSolDepth, true);
            numberPicker.setValue(initSolDepth);
        } else {
            game = getSavedGame(savedInstanceState);
            numberPicker.setValue(game.getSolDepth());
        }

        rows = game.getRows();
        cols = game.getCols();
        solDepth = game.getSolDepth();

        // Set up grid buttons
        setButtons();
        for(Button button : buttons) {
            button.setOnClickListener(this::setSelectedButton);
        }

        drawBoard();

        // Used to restore subgrid highlighting when restoring game state
        if(selectedIndex > -1) {
            highlightSubgrid(selectedIndex);
        }

        findViewById(R.id.mainLeftButton).setOnClickListener(this::rotateLeft);
        findViewById(R.id.mainRightButton).setOnClickListener(this::rotateRight);
        findViewById(R.id.mainUndoButton).setOnClickListener(this::undo);
        findViewById(R.id.mainNewGameButton).setOnClickListener(this::newGame);
    }

    /**
     * Saves the number of moves made and the board states.
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        saveGame(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        // for the overflow menu
        return true;
    }

    /**
     * Actions for menu items in the options menu
     */
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.menu_3x3) {
            handleOption3x3();
        } else if(id == R.id.menu_3x4) {
            handleOption3x4();
        } else if(id == R.id.menu_4x4) {
            handleOption4x4();
        } else if(id == R.id.menu_surrender) {
            handleOptionSurrender();
        } else if(id == R.id.menu_achievement) {
            handleOptionAchievement();
        } else if(id == R.id.menu_sound) {
            handleOptionSound(item);
        } else if(id == R.id.menu_about) {
            // display app info
            showCustomDialog(R.layout.dialog_about);
        } else if(id == R.id.menu_exit) {
            // terminate the app
            showExitDialog();
        }

        return super.onOptionsItemSelected(item);
    }

    private void handleOption3x3() { // start a new 3 x 3 Revolution game
        DialogHelper.showConfirmationDialog(
                this,
                getString(R.string.menu_33),
                (dialog, which) -> {
                    rows = 3;
                    cols = 3;
                    startGame();
                },
                (dialog, which) -> soundManager.playCancelSound());
    }

    private void handleOption3x4() { // start a new 3 x 4 Revolution game
        DialogHelper.showConfirmationDialog(
                this,
                getString(R.string.menu_34),
                (dialog, which) -> {
                    rows = 3;
                    cols = 4;
                    startGame();
                },
                (dialog, which) -> soundManager.playCancelSound());
    }

    private void handleOption4x4() { // start a new 4 x 4 Revolution game
        DialogHelper.showConfirmationDialog(
                this,
                getString(R.string.menu_44),
                (dialog, which) -> {
                    rows = 4;
                    cols = 4;
                    startGame();
                },
                (dialog, which) -> soundManager.playCancelSound());
    }

    private void handleOptionSurrender() { // forfeit the game and see the solution
        if(game.isOver() && !game.getSurrenderMode()) {
            DialogHelper.showInformationDialog(this, getString(R.string.game_is_over));
        } else {
            if(!game.getSurrenderMode()) {
                DialogHelper.showConfirmationDialog(
                        this,
                        getString(R.string.menu_surrender_confirm),
                        (dialog, which) -> {
                            game.enableSurrenderMode();

                            // Remove player's ability to win the game
                            findViewById(R.id.mainRightButton).setVisibility(View.GONE);
                            findViewById(R.id.mainLeftButton).setVisibility(View.GONE);

                            soundManager.playOverSound();
                        },
                        (dialog, which) -> soundManager.playCancelSound());
            } else {
                DialogHelper.showInformationDialog(this,
                        getString(R.string.already_surrendered));
            }
        }
    }

    private void handleOptionAchievement() { // show the player their achievements
        DialogHelper.showConfirmationDialog(
                this,
                getString(R.string.menu_achievement_confirm),
                (dialog, which) -> {
                    // Track whether sound is enabled or not for AchievementActivity
                    SharedPreferences preferences = getSharedPreferences(SOUND_ENABLED,
                            MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean(SOUND_ENABLED, soundManager.isSoundEnabled());
                    editor.apply();

                    // Start AchievementActivity
                    Intent intent = new Intent(this, AchievementActivity.class);
                    startActivity(intent);
                },
                (dialog, which) -> soundManager.playCancelSound());
    }

    private void handleOptionSound(MenuItem item) { // toggle sound effects
        if (soundManager.isSoundEnabled()) {
            item.setIcon(R.drawable.ic_no_sound);
        } else {
            item.setIcon(R.drawable.ic_sound);
        }
        soundManager.toggleSoundEnabled();

        // Update title of menu_sound in case the item is in the overflow menu
        if(soundManager.isSoundEnabled()) {
            item.setTitle(getString(R.string.menu_sound_disable));
        } else {
            item.setTitle(getString(R.string.menu_sound_enable));
        }
    }

    /**
     * Initializes the Buttons in a specified GridLayout
     */
    private void setButtons() {
        gridLayout.removeAllViews();
        buttons = new Button[rows * cols];

        // Calculate size of each button so that the game will occupy an equal width
        // of the screen in its current orientation.
        // The board will be 80% of the smallest screen length, except in the case the
        // device is a phone in landscape mode, in which case the board will be 60%
        int displayWidth = getResources().getDisplayMetrics().widthPixels;
        int displayHeight = getResources().getDisplayMetrics().heightPixels;
        int numCellsHeight = game.getRows();
        int numCellsWidth = game.getCols();
        int buttonSizeWidth;
        int buttonSizeHeight;
        int multiplier;

        int minLength = Math.min(displayWidth, displayHeight);

        if(getResources().getConfiguration().smallestScreenWidthDp < 600
                && displayWidth > displayHeight) { // phone in landscape
            multiplier = 6;
        } else {
            multiplier = 8;
        }
        buttonSizeHeight = (multiplier * minLength / 10) / numCellsHeight;
        buttonSizeWidth = (multiplier * minLength / 10) / numCellsWidth;

        // create each button and add it to grid layout
        for(int i = 0; i < buttons.length; i++) {
            // need new LayoutParams for each button
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.rowSpec = GridLayout.spec(i / game.getCols());
            params.columnSpec = GridLayout.spec(i % game.getCols());
            params.width = buttonSizeWidth;
            params.height = buttonSizeHeight;
            params.setMargins(2, 2, 2, 2); // left, top, right, bottom

            buttons[i] = new Button(this);
            buttons[i].setTag(i);
            buttons[i].setLayoutParams(params);
            buttons[i].setBackground(ContextCompat.getDrawable(
                    this, R.drawable.button_unselected_background));

            gridLayout.addView(buttons[i]);
        }

        // initialize selectedButton to the top-left Button
        selectedButton = buttons[0];
    }

    /**
     * Ensure all game buttons are visible
     */
    private void ensureVisible() {
        findViewById(R.id.mainRightButton).setVisibility(View.VISIBLE);
        findViewById(R.id.mainLeftButton).setVisibility(View.VISIBLE);
        findViewById(R.id.mainUndoButton).setVisibility(View.VISIBLE);
    }

    /**
     * Fills the grid with text to represent the current game state
     */
    private void drawBoard() {
        for(int i = 0; i < game.getRows() * game.getCols(); i++) {
            buttons[i].setText(String.valueOf(game.getValueAt(i)));
            buttons[i].setTextAppearance(R.style.ButtonTextStyle);
        }
    }

    /**
     * Event handler for the game buttons.
     */
    private void setSelectedButton(View view) {
        int buttonIndex = (Integer) view.getTag();

        if(game.isValidAnchor(buttonIndex)) {
            highlightSubgrid(buttonIndex);
        } else {
            showCustomToast(getString(R.string.invalid_anchor));
            soundManager.playFailSound();
        }
    }

    /**
     * Highlights the 2x2 subgrid centered at buttonIndex
     */
    private void highlightSubgrid(int buttonIndex) {
            selectedIndex = buttonIndex;
            selectedButton = buttons[buttonIndex];

            // Remove highlight from all buttons
            for (Button button : buttons) {
                button.setBackground(ContextCompat.getDrawable(
                        this, R.drawable.button_unselected_background));
            }

            // Highlight 2x2 subgrid
            buttons[buttonIndex].setBackground(ContextCompat.getDrawable(
                    this, R.drawable.button_selected_background));
            buttons[buttonIndex + 1].setBackground(ContextCompat.getDrawable(
                    this, R.drawable.button_selected_background));
            buttons[buttonIndex + game.getCols()]
                    .setBackground(ContextCompat.getDrawable(
                            this, R.drawable.button_selected_background));
            buttons[buttonIndex + game.getCols() + 1]
                    .setBackground(ContextCompat.getDrawable(
                            this, R.drawable.button_selected_background));
            drawBoard();
    }

    /**
     * Event handler for the Left button.
     */
    private void rotateLeft(View view) {
        if(!alreadyRotating) {
            int moveIndex = (Integer) selectedButton.getTag();
            rotate(moveIndex, 1);
        }
    }

    /**
     * Event handler for the Right button.
     */
    private void rotateRight(View view) {
        if(!alreadyRotating) {
            int moveIndex = (Integer) selectedButton.getTag();
            rotate(moveIndex, 0);
        }
    }

    /**
     * Consolidated helper method to rotate the board.
     */
    private void rotate(int moveIndex, int dir) {
        alreadyRotating = true;

        Animation animation;

        Button topLeft = buttons[moveIndex];
        Button topRight = buttons[moveIndex + 1];
        Button bottomLeft = buttons[moveIndex + game.getCols()];
        Button bottomRight = buttons[moveIndex + game.getCols() + 1];

        // Only update backend once animations have finished to prevent synchronization
        // issues
        Animation.AnimationListener listener = new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                game.rotate(moveIndex, dir, true);
                drawBoard();
                alreadyRotating = false;

                if (game.isOver()) {
                    if (game.moveTotal() > 1) {
                        showCustomToast(getString(R.string.success_part1) +
                                " " + game.moveTotal() + " " +
                                getString(R.string.success_part2_multiple));
                    } else {
                        showCustomToast(getString(R.string.success_part1) +
                                " " + game.moveTotal() + " " +
                                getString(R.string.success_part2_single));
                    }

                    // Hide all buttons except New Game button
                    findViewById(R.id.mainRightButton).setVisibility(View.GONE);
                    findViewById(R.id.mainLeftButton).setVisibility(View.GONE);
                    findViewById(R.id.mainUndoButton).setVisibility(View.GONE);

                    soundManager.playWinSound();
                    updateAchievements();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}

            @Override
            public void onAnimationStart(Animation animation) {}
        };

        if(dir == 0) { // animate clockwise rotations
            animation = AnimationUtils.loadAnimation(this, R.anim.slide_right);
            animation.setAnimationListener(listener);
            topLeft.startAnimation(animation);

            animation = AnimationUtils.loadAnimation(this, R.anim.slide_down);
            topRight.startAnimation(animation);

            animation = AnimationUtils.loadAnimation(this, R.anim.slide_left);
            bottomRight.startAnimation(animation);

            animation = AnimationUtils.loadAnimation(this, R.anim.slide_up);
            bottomLeft.startAnimation(animation);
        } else { // animate counter-clockwise rotations
            animation = AnimationUtils.loadAnimation(this, R.anim.slide_down);
            animation.setAnimationListener(listener);
            topLeft.startAnimation(animation);

            animation = AnimationUtils.loadAnimation(this, R.anim.slide_left);
            topRight.startAnimation(animation);

            animation = AnimationUtils.loadAnimation(this, R.anim.slide_up);
            bottomRight.startAnimation(animation);

            animation = AnimationUtils.loadAnimation(this, R.anim.slide_right);
            bottomLeft.startAnimation(animation);
        }

        soundManager.playWhooshSound();
    }

    /**
     * Updates the player's achievements after winning a game. Achievements are updated
     * by saving them to disk.
     */
    private void updateAchievements() {
        SharedPreferences boardPreferences = getSharedPreferences(BOARD_SIZE,
                MODE_PRIVATE);
        SharedPreferences solPreferences = getSharedPreferences(SOLUTION_DEP,
                MODE_PRIVATE);

        SharedPreferences.Editor boardEditor = boardPreferences.edit();
        SharedPreferences.Editor solEditor = solPreferences.edit();

        // Store board size and solution depth
        int boardSize = game.getRows() * game.getCols();
        boardEditor.putInt(BOARD_SIZE + boardSize, boardSize);
        solEditor.putInt(SOLUTION_DEP + game.getSolDepth(), game.getSolDepth());

        boardEditor.apply();
        solEditor.apply();
    }

    /**
     * Event handler for the Undo button (takes back a move).
     */
    private void undo(View view) {
        if(game.undo()) {
            soundManager.playUndoSound();
            drawBoard();
        } else { // there is no move to undo
            showCustomToast(getString(R.string.undo_fail));
            soundManager.playFailSound();
        }
    }

    /**
     * Event handler for the New Game button.
     * This will start a new game with the selected solution depth and board size.
     */
    private void newGame(View view) {
        ensureVisible();

        game = new Revolution(rows, cols, solDepth, true);
        setButtons();
        for(Button button : buttons) {
            button.setOnClickListener(this::setSelectedButton);
        }
        drawBoard();
        soundManager.playStartSound();
    }

    private void startGame() { // start a new game
        ensureVisible();

        game = new Revolution(rows, cols, solDepth, true);
        setButtons();
        for(Button button : buttons) {
            button.setOnClickListener(this::setSelectedButton);
        }
        drawBoard();
        soundManager.playStartSound();
    }

    /**
     * Writes the state of game to bundle.
     */
    public void saveGame(Bundle bundle) {
        bundle.putInt(NUM_MOVES, game.moves() + game.getSolDepth());
        bundle.putInt(SOL_DEPTH, game.getSolDepth());
        bundle.putInt(DIM + 0, game.getRows());
        bundle.putInt(DIM + 1, game.getCols());
        bundle.putBoolean(SUR, game.getSurrenderMode());
        bundle.putInt(BUTTON_VIS + 0, findViewById(R.id.mainRightButton).getVisibility());
        bundle.putInt(BUTTON_VIS + 1, findViewById(R.id.mainLeftButton).getVisibility());
        bundle.putInt(BUTTON_VIS + 2, findViewById(R.id.mainUndoButton).getVisibility());
        bundle.putInt(SELECTED_INDEX, selectedIndex);

        int[] moves = game.moveHistory();
        int[] dirs = game.dirHistory();
        for(int i = 0; i < moves.length; i++) {
            bundle.putInt(MOVE + i, moves[i]);
            bundle.putInt(DIR + i, dirs[i]);
        }
    }

    /**
     * Returns a new game in the state represented by bundle.
     */
    public Revolution getSavedGame(Bundle bundle) {
        int prevSolDepth = bundle.getInt(SOL_DEPTH);
        int numMoves = bundle.getInt(NUM_MOVES);
        int rows = bundle.getInt(DIM + 0);
        int cols = bundle.getInt(DIM + 1);
        selectedIndex = bundle.getInt(SELECTED_INDEX);
        boolean surrenderMode = bundle.getBoolean(SUR);

        int rightVis = bundle.getInt(BUTTON_VIS + 0);
        int leftVis = bundle.getInt(BUTTON_VIS + 1);
        int undoVis = bundle.getInt(BUTTON_VIS + 2);
        findViewById(R.id.mainRightButton).setVisibility(rightVis);
        findViewById(R.id.mainLeftButton).setVisibility(leftVis);
        findViewById(R.id.mainUndoButton).setVisibility(undoVis);

        // make new game in winning config
        Revolution game = new Revolution(rows, cols, prevSolDepth, false);

        // redo all saved moves, including previous randomization moves, in order to
        // restore the previous board state
        for(int i = 0; i < prevSolDepth; i++) {
            game.rotate(bundle.getInt(MOVE + i), bundle.getInt(DIR + i), false);
        }
        for(int j = prevSolDepth; j < numMoves; j++) {
            game.rotate(bundle.getInt(MOVE + j), bundle.getInt(DIR + j), true);
        }
        if(surrenderMode)
            game.enableSurrenderMode();

        return game;
    }

    /**
     * Updates the solDepth for the game.
     * The parameters i and i1 are unused, but required to use with
     * setOnValueChangedListener().
     */
    private void setSolDepth(NumberPicker numberPicker, int i, int i1) {
        solDepth = numberPicker.getValue();
    }

    /**
     * Displays a custom dialog using a specified layout
     */
    public void showCustomDialog(int layoutId) {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(layoutId, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView)
                .setPositiveButton(android.R.string.ok, null);
        AlertDialog dialog = builder.create();
        dialog.show();

        Window window = dialog.getWindow();
        if(window != null) {
            window.setBackgroundDrawableResource(R.color.toolbar_background);
        }

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.WHITE);
    }

    /**
     * Shows an exit dialog asking if the user wants to exit (if so, terminates)
     */
    private void showExitDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_exit, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView)
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, (v, n) -> finish());
        AlertDialog dialog = builder.create();
        dialog.show();

        Window window = dialog.getWindow();
        if(window != null) {
            window.setBackgroundDrawableResource(R.color.toolbar_background);
        }

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.WHITE);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.WHITE);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setText(R.string.button_positive);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setText(R.string.button_negative);
    }

    /**
     * Displays a given string in a custom toast.
     */
    public void showCustomToast(String message) {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast,
                findViewById(R.id.toast_layout_root));

        TextView text = layout.findViewById(R.id.toast_text);
        text.setText(message);

        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    protected void onDestroy() {
        super.onDestroy();
        soundManager.release();
    }
}

//========================================================================================