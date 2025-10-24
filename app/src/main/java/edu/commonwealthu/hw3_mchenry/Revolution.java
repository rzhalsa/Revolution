package edu.commonwealthu.hw3_mchenry;

import java.util.Random;
import java.util.Stack;

/**
 * Back end for the game of Revolution.
 *
 * Revolution is a single-player puzzle played on a rectangular grid of numbered tiles.
 * The player can rotate any 2x2 subgrid either clockwise or counterclockwise. The goal
 * is to sort the tiles into ascending order. Subgrids are specified by their top-left
 * tile (the anchor position).
 *
 * Known Issues:
 *
 *      None as of 10/15/25.
 *
 * Credits and Acknowledgements:
 *
 *      moveHistory() and dirHistory() are copies of the moveHistory() helper method
 *      present in the Frogs and Toads application.
 *
 * @author Ryan McHenry
 * Date: August 26, 2025
 */

//========================================================================================

public class Revolution {
    private final int rows;                                  // number of rows in the game
    private final int cols;                                  // number of cols in the game
    private final int[] grid;                                // the grid for the game
    private final Stack<int[]> stateHistory = new Stack<>(); // the board state stack
    private final Stack<Integer> moveStack = new Stack<>();  // the move index stack
    private final Stack<Integer> dirStack = new Stack<>();   // the move direction stack

    // number of times the player undoes a move
    private int undoCount = 0;

    // number of random valid moves used to initialize the puzzle from the solved state
    private final int solDepth;

    // for surrender mode
    private boolean surrenderMode;
    private final Stack<int[]> surrenderStateHistory = new Stack<>();

    /**
     * Standard constructor for the Revolution Class.
     *
     * @param rows      the amount of rows in the game
     * @param cols      the amount of columns in the game
     * @param solDepth  the solution depth
     * @param randomize whether to randomize the gameboard or not
     */
    public Revolution(int rows, int cols, int solDepth, boolean randomize) {
        this.rows = rows;
        this.cols = cols;
        this.solDepth = solDepth;
        this.surrenderMode = false;

        grid = new int[rows * cols];
        initGrid(grid);
        if (randomize) {
            randomizeGrid();
        }
    }

    /**
     * Alternate constructor, only takes solDepth as argument and uses a 3x3 grid.
     *
     * @param solDepth  the solution depth
     * @param randomize whether to randomize the gameboard or not
     */
    public Revolution(int solDepth, boolean randomize) {
        this(3, 3, solDepth, randomize);
    }

    /**
     * Initializes the grid to the winning configuration.
     *
     * @param grid the grid for the game
     */
    private void initGrid(int[] grid) {
        for (int i = 0; i < this.rows * this.cols; i++) {
            grid[i] = i + 1;
        }
    }

    /**
     * Makes a series of random valid moves from the winning configuration in order
     * to set up the grid for the player to solve.
     */
    public void randomizeGrid() {
        Random rando = new Random();
        int randomIndex, direction;
        int lastIndex = -1;     // last index rotated
        int lastDir = -1;       // last direction rotated in

        for (int i = 0; i < this.solDepth; i++) {
            /* Pick a random index and direction to rotate the game board. Ensure that
               the random move index is a valid anchor, and only allow a random move that
               doesn't undo the previous move, to ensure that solDepth moves are the
               minimum number of moves the player can make to win the game. */
            do {
                do {
                    randomIndex = rando.nextInt(this.rows * this.cols - 1);
                } while(!isValidAnchor(randomIndex));
                direction = rando.nextInt(2); // direction bound to be either 0 or 1
            } while(randomIndex == lastIndex && direction != lastDir);

            lastIndex = randomIndex;
            lastDir = direction;

            rotate(randomIndex, direction, false);
        }
    }

    /**
     * Rotate the 2x2 subgrid anchored at index.
     *
     * @param index the index of the rotation
     * @param dir the direction to rotate the subgrid (0 = right, 1 = left)
     * @param playerMove whether this rotation is a player move or not
     */
    public void rotate(int index, int dir, boolean playerMove) {
        // Ensure grid[index] is a valid anchor
        if (!isValidAnchor(index)) {
            return;
        }

        int[] gridCopy = copyOfGrid();
        int anchorTemp = gridCopy[index]; // temp value to hold grid[index]

        // Only push state histories for player moves, needed for undo method
        if (playerMove) {
            stateHistory.push(copyOfGrid());  // store deep copy of the grid
        }

        // Push player moves as well as random moves to move and dir stack, as we need
        // both types of moves for preserving game state
        moveStack.push(index);            // store move index
        dirStack.push(dir);               // store move dir

        // Push every move including randomization moves for surrender mode
        surrenderStateHistory.push(copyOfGrid());

        if (dir == 0) {
            // rotate subgrid to the right (clockwise)
            grid[index] = grid[index + getCols()];
            grid[index + getCols()] = grid[index + getCols() + 1];
            grid[index + getCols() + 1] = grid[index + 1];
            grid[index + 1] = anchorTemp;
        } else if (dir == 1) {
            // rotate subgrid to the left (counterclockwise)
            grid[index] = grid[index + 1];
            grid[index + 1] = grid[index + getCols() + 1];
            grid[index + getCols() + 1] = grid[index + getCols()];
            grid[index + getCols()] = anchorTemp;
        }
    }

    /**
     * Determines if the game is over or not. The game is considered over once the player
     * has manipulated the board into the winning configuration.
     *
     * @return a boolean value stating whether the game is over or not
     */
    public boolean isOver() {
        int[] currentGrid = copyOfGrid();
        int tileValue = 1;

        for (int i = 0; i < this.rows * this.cols; i++) {
            if (currentGrid[i] != tileValue++)
                return false;
        }

        return true;
    }

    /**
     * Reverts the game to its previous state and returns true (or does nothing
     * and returns false if there are no player moves to undo).
     *
     * @return true if the game has been reverted to its previous state, or false
     * if there are no player moves to undo
     */
    public boolean undo() {
        if(!surrenderMode) {
            if (stateHistory.isEmpty())
                return false;

            int[] prevGrid = stateHistory.pop();

            System.arraycopy(prevGrid, 0, grid, 0, this.rows * this.cols);

            undoCount++;

            return true;
        } else { // surrender mode
            if(surrenderStateHistory.isEmpty())
                return false;

            int[] prevGrid = surrenderStateHistory.pop();

            System.arraycopy(prevGrid, 0, grid, 0, this.rows * this.cols);

            return true;
        }
    }

    /**
     * Returns the number of player moves made.
     * The amount of player moves is tracked using the size of the state history stack.
     * Does not include the number of times the player undid a move.
     *
     * @return the number of moves made
     */
    public int moves() {
        return stateHistory.size();
    }

    /**
     * Returns the total number of player moves made.
     * Includes the total number of times the player undid a move.
     *
     * @return the total number of player moves made
     */
    public int moveTotal() {
        return stateHistory.size() + undoCount;
    }

    /**
     * Returns a sequence of indices at which moves have been made.
     *
     * @return a sequence of indices at which moves have been made
     */
    public int[] moveHistory() {
        int[] a = new int[moves() + getSolDepth()];
        for (int i = 0; i < a.length; i++) {
            a[i] = moveStack.get(i);
        }
        return a;
    }

    /**
     * Returns a sequence of integers that show the move direction history.
     * 0 corresponds to a right rotation, and 1 to a left rotation.
     *
     * @return a sequence of integers that show the move direction history
     */
    public int[] dirHistory() {
        int[] a = new int[moves() + getSolDepth()];
        for (int i = 0; i < a.length; i++) {
            a[i] = dirStack.get(i);
        }
        return a;
    }

    /**
     * Returns true if the position (row, col) is a valid anchor.
     * An anchor is valid if it is neither in the bottom row nor furthest
     * right column.
     *
     * @param index the index for the possible anchor
     * @return true if the position (row, col) if a valid anchor
     */
    public boolean isValidAnchor(int index) {
        int row = index / this.cols;
        int col = index % this.cols;
        return row < this.rows - 1 && col < this.cols - 1;
    }

    /**
     * Returns a deep copy of the current grid.
     *
     * @return a deep copy of the current grid
     */
    private int[] copyOfGrid() {
        int[] gridCopy = new int[this.rows * this.cols];

        System.arraycopy(grid, 0, gridCopy, 0, this.rows * this.cols);

        return gridCopy;
    }

    /**
     * Returns the value at the given index.
     *
     * @param index the index to return the value of
     * @return the value at the given index
     */
    public int getValueAt(int index) {
        int[] gridCopy = copyOfGrid();
        return gridCopy[index];
    }

    /**
     * Returns the number of rows in the game.
     *
     * @return the number of rows in the game
     */
    public int getRows() {
        return this.rows;
    }

    /**
     * Returns the number of cols in the game.
     *
     * @return the number of cols in the game
     */
    public int getCols() {
        return this.cols;
    }

    /**
     * Returns the solution depth value for the game.
     *
     * @return the solution depth value for the game
     */
    public int getSolDepth() {
        return this.solDepth;
    }

    /**
     * Returns whether the game is in surrender mode
     */
    public boolean getSurrenderMode() {
        return this.surrenderMode;
    }

    /**
     * Enables surrender mode so the player can undo back to the solved state. The player
     * will lose the game if they choose to surrender.
     */
    public void enableSurrenderMode() {
        this.surrenderMode = true;
    }
}
//========================================================================================