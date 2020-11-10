package agents;

import java.util.*;

import wumpus.Agent;
import wumpus.Environment;
import wumpus.Environment.Action;
import wumpus.MyPoint;
import wumpus.Player;
import wumpus.Player.Direction;

/**
 * An Agent that implements a basic heuristic strategy. The heuristic actions are as following:
 * H1: Grab the gold if sees glitter;
 * H2: Shoots to every not visited tiles if feels a stench;
 * H3: Mark the adjacent, not visited tiles has dangerous if feels a breeze;
 * H4: If do not have gold, choose the branch the non visited branch with less turns to take;
 * H5: Choose the path that surely does not have a danger;
 * H5: If have found the gold get back by the visited path;
 */
public class LogicAgent implements Agent {
    private int w, h;

    private boolean debug = true;
    private boolean[][] isVisited;
    private boolean[][] isBREEZE;
    private Player.Direction[][] isBUMP;
    private boolean[][] isSTENCH;
    private boolean isSCREAM;
    private int[][] timesVisited;


    private LinkedList<Action> nextActions = new LinkedList<Action>();

    /**
     * The strategy constructor.
     *
     * @param width  The board width
     * @param height The board height
     */
    public LogicAgent(int width, int height) {
        w = width;
        h = height;
        timesVisited = new int[w][h];
        isVisited = new boolean[w][h];
        isBREEZE = new boolean[w][h];
        isSTENCH = new boolean[w][h];
        isBUMP = new Player.Direction[w][h];
    }

    /**
     * Sets weather to show the debug messages or not.
     *
     * @param value <tt>true</tt> to display messages
     */
    public void setDebug(boolean value) {
        debug = value;
    }

    /**
     * Prints the player board and debug message.
     *
     * @param player The player instance
     */
    public void beforeAction(Player player) {
        if (debug) {
            System.out.println(player.render());
            System.out.println(player.debug());
        }
    }

    /**
     * Prints the last action taken.
     *
     * @param player The player instance
     */
    public void afterAction(Player player) {
        if (debug) {
            // Players Last action
            System.out.println(player.getLastAction());
            // Show a very happy message
            if (player.isDead()) {
                System.out.println("GAME OVER!");
            }
            // Turn on step-by-step
            Environment.trace();
        }
    }

    /**
     * Implements the player artificial intelligence strategy.
     *
     * @param player The player instance
     * @return The next action
     */

    public Action getAction(Player player) {

        if (nextActions.size() > 0) {
            return nextActions.poll();
        }
        int x = player.getX();
        int y = player.getY();
        tell(player);

        if (player.hasGlitter()) {
            return Action.GRAB;
        }
        int[][] neighbours = getNeighbors(x, y);
        ArrayList<MyPoint> neibs = new ArrayList<MyPoint>();

        for (int[] n : neighbours) {
            if (!isVisited[n[0]][n[1]] && isNotWumpus(n[0], n[1]) && isNotPit(n[0], n[1])) {
                ArrayList<Action> actions = getActionsTo(player, n);
                nextActions.addAll(actions);
                return nextActions.poll();
            }else if(player.hasArrows() && isWumpus(n[0], n[1])){
                ArrayList<Action> actions = getActionsToShoot(player, n);
                nextActions.addAll(actions);
                return nextActions.poll();
            }
        }
        for (int[] n: neighbours) {
            if (isVisited[n[0]][n[1]]) {
                neibs.add(new MyPoint(n[0], n[1], timesVisited[n[0]][n[1]] == 3 ? 1 : 5));
            }else if(!isVisited[n[0]][n[1]] && (isNotWumpus(n[0], n[1]) || isNotPit(n[0], n[1]))){
                neibs.add(new MyPoint(n[0], n[1], 3));
            }
        }
        Collections.sort(neibs, Collections.reverseOrder());
        int[] next = {neibs.get(0).getX(), neibs.get(0).getY()};
        ArrayList<Action> actions = getActionsTo(player, next);
        nextActions.addAll(actions);
        return nextActions.poll();
    }

    // add info about tile to 'knowledge base'
    private void tell(Player player) {
        int x = player.getX();
        int y = player.getY();
        timesVisited[x][y] +=1;

        isVisited[x][y] = true;

        if (player.hasBreeze()) {
            isBREEZE[x][y] = true;
        }

        if (player.hasStench()) {
            isSTENCH[x][y] = true;
        }

        if (player.hasBump()) {
            isBUMP[x][y] = player.getDirection();
        }

        if (player.hasScream()) {
            isSCREAM = true;
        }
    }

    private boolean isWumpus(int x, int y) {
        if (isSCREAM || isVisited[x][y]) {
            return false;
        }

        int[] west = new int[]{x - 1, y};
        int[] north = new int[]{x, y + 1};
        int[] east = new int[]{x + 1, y};
        int[] south = new int[]{x, y - 1};

        if (isValid(south[0], south[1]) && isVisited[south[0]][south[1]] && isSTENCH[south[0]][south[1]]) {
            if (isValid(east[0], east[1]) && isVisited[east[0]][east[1]] && isSTENCH[east[0]][east[1]] && isVisited[x + 1][y - 1]) {
                return true;
            }
            if (isValid(west[0], west[1]) && isVisited[west[0]][west[1]] && isSTENCH[west[0]][west[1]] && isVisited[x - 1][y - 1]) {
                return true;
            }
            if (isValid(north[0], north[1]) && isVisited[north[0]][north[1]] && isSTENCH[north[0]][north[1]]) {
                return true;
            }
        }
        if (isValid(north[0], north[1]) && isVisited[north[0]][north[1]] && isSTENCH[north[0]][north[1]]) {
            if (isValid(east[0], east[1]) && isVisited[east[0]][east[1]] && isSTENCH[east[0]][east[1]] && isVisited[x + 1][y + 1]) {
                return true;
            }
            if (isValid(west[0], west[1]) && isVisited[west[0]][west[1]] && isSTENCH[west[0]][west[1]] && isVisited[x - 1][y + 1]) {
                return true;
            }
        }

        if (isValid(east[0], east[1]) && isVisited[east[0]][east[1]] && isSTENCH[east[0]][east[1]] &&
                isValid(west[0], west[1]) && isVisited[west[0]][west[1]] && isSTENCH[west[0]][west[1]]) {
            return true;
        }

        if (isValid(south[0], south[1]) && isVisited[south[0]][south[1]] && isSTENCH[south[0]][south[1]]
                && (!isValid(south[0], south[1] - 1) || isVisited[south[0]][south[1] - 1])){

            if((isValid(x+1,y-1) && isVisited[x+1][y-1] && (isBUMP[south[0]][south[1]] == Direction.W))
                || (isValid(x-1,y-1) && isVisited[x-1][y-1] && (isBUMP[south[0]][south[1]] == Direction.E))){
                return true;
            }
        }

        if (isValid(north[0], north[1]) && isVisited[north[0]][north[1]] && isSTENCH[north[0]][north[1]]
                && (!isValid(north[0], north[1] + 1) || isVisited[north[0]][north[1] + 1])){

            if((isValid(x+1,y+1) && isVisited[x+1][y+1] && (isBUMP[north[0]][north[1]] == Direction.W))
                || (isValid(x-1,y+1) && isVisited[x-1][y+1] && (isBUMP[north[0]][north[1]] == Direction.E))){
                return true;
            }
        }

        if (isValid(west[0], west[1]) && isVisited[west[0]][west[1]] && isSTENCH[west[0]][west[1]]
                && (!isValid(west[0]-1, west[1]) || isVisited[west[0]-1][west[1]])){

            if ((isValid(x-1,y-1) && isVisited[x-1][y-1] && (isBUMP[west[0]][west[1]] == Direction.N))
                || (isValid(x-1,y+1) && isVisited[x-1][y+1] && (isBUMP[west[0]][west[1]] == Direction.S))){
                return true;
            }
        }

        if (isValid(east[0], east[1]) && isVisited[east[0]][east[1]] && isSTENCH[east[0]][east[1]]
                && (!isValid(east[0]+1, east[1]) || isVisited[east[0]+1][east[1]])){

            if((isValid(x+1,y-1) && isVisited[x+1][y-1] && (isBUMP[east[0]][east[1]] == Direction.N))
                || (isValid(x+1,y+1) && isVisited[x+1][y+1] && (isBUMP[east[0]][east[1]] == Direction.S))) {
                return true;
            }
        }

        return false;
    }

    private boolean isNotWumpus(int x, int y) {
        if (isSCREAM || isVisited[x][y]) {
            return true;
        }

        //neighbours are numbered from west to south
        int[][] neighbours = new int[4][2];

        neighbours[0] = new int[]{x - 1, y};
        neighbours[1] = new int[]{x, y + 1};
        neighbours[2] = new int[]{x + 1, y};
        neighbours[3] = new int[]{x, y - 1};

        for (int[] n : neighbours) {
            if (isValid(n[0], n[1]) && isVisited[n[0]][n[1]] && !isSTENCH[n[0]][n[1]]) {
                return true;
            }
        }
        return false;
    }

    private boolean isNotPit(int x, int y) {

        if (isVisited[x][y]) {
            return true;
        }

        //neighbours are numbered from west to south
        int[][] neighbours = new int[4][2];

        neighbours[0] = new int[]{x - 1, y};
        neighbours[1] = new int[]{x, y + 1};
        neighbours[2] = new int[]{x + 1, y};
        neighbours[3] = new int[]{x, y - 1};

        for (int[] n : neighbours) {
            if (isValid(n[0], n[1]) && isVisited[n[0]][n[1]] && !isBREEZE[n[0]][n[1]]) {
                return true;
            }
        }
        return false;
    }

    private boolean isValid(int x, int y) {
        return x < w && x > -1 && y > -1 && y < h;
    }

    /**
     * Gets the adjacent tiles of the given coordinates.
     *
     * @param x The tile X coordinate
     * @param y The tile Y coordinate
     * @return An array of 2D coordinates
     */
    private int[][] getNeighbors(int x, int y) {
        HashMap<Direction, Integer> nodesMap = new HashMap<Direction, Integer>();

        // Calculate the next block
        int north = y - 1;
        int south = y + 1;
        int east = x + 1;
        int west = x - 1;

        // Check if branch is into bounds
        if (north >= 0) nodesMap.put(Direction.N, north);
        if (south < h) nodesMap.put(Direction.S, south);
        if (east < w) nodesMap.put(Direction.E, east);
        if (west >= 0) nodesMap.put(Direction.W, west);

        // Build the branches array
        int branch = 0;
        int[][] nodes = new int[nodesMap.size()][2];
        for (Direction direction : nodesMap.keySet()) {
            switch (direction) {
                case N:
                    nodes[branch] = new int[]{x, north};
                    break;
                case S:
                    nodes[branch] = new int[]{x, south};
                    break;
                case E:
                    nodes[branch] = new int[]{east, y};
                    break;
                case W:
                    nodes[branch] = new int[]{west, y};
                    break;
            }
            branch++;
        }

        return nodes;
    }

    /**
     * Returns the amount of turns player need to take to get into given position.
     *
     * @param player The player's instance
     * @param to     The destination tile
     * @return The number of turns
     */
    private int getTurns(Player player, int[] to) {
        // The current vector
        int[] from = {1, 0};
        switch (player.getDirection()) {
            case N:
                from[0] = 0;
                from[1] = 1;
                break;
            case S:
                from[0] = 0;
                from[1] = -1;
                break;
            case W:
                from[0] = -1;
                from[1] = 0;
                break;
        }
        // The destination vector
        int[] dest = {to[0] - player.getX(), player.getY() - to[1]};
        // The angle between the two vectors
        double dotProduct = from[0] * dest[0] + from[1] * dest[1];
        double lenProduct = Math.hypot(from[0], from[1]) * Math.hypot(dest[0], dest[1]);
        double theta = Math.acos(dotProduct / lenProduct);
        // Inverts when facing backwards
        if (player.getDirection() == Direction.N && getDirection(dest) == Direction.E ||
                player.getDirection() == Direction.E && getDirection(dest) == Direction.S ||
                player.getDirection() == Direction.S && getDirection(dest) == Direction.W ||
                player.getDirection() == Direction.W && getDirection(dest) == Direction.N) {
            theta *= -1;
        }
        // Count how many turns
        return (int) (theta / (Math.PI / 2));
    }

    /**
     * Returns the actions that player must take to reach the given destination.
     *
     * @param player The player's instance
     * @param to     The destination tile coordinates
     * @return An array of actions
     */
    private ArrayList<Action> getActionsTo(Player player, int[] to) {
        ArrayList<Action> actions = new ArrayList<Action>();
        int turns = getTurns(player, to);
        for (int i = 0; i < Math.abs(turns); i++) {
            if (turns < 0) actions.add(Action.TURN_RIGHT);
            if (turns > 0) actions.add(Action.TURN_LEFT);

        }
        // Go to the block
        actions.add(Action.GO_FORWARD);

        return actions;
    }

    /**
     * Returns the actions that player must take to reach the given destination.
     *
     * @param player The player's instance
     * @param to     The destination tile coordinates
     * @return An array of actions
     */
    private ArrayList<Action> getActionsToShoot(Player player, int[] to) {
        ArrayList<Action> actions = new ArrayList<Action>();
        int turns = getTurns(player, to);
        for (int i = 0; i < Math.abs(turns); i++) {
            if (turns < 0) actions.add(Action.TURN_RIGHT);
            if (turns > 0) actions.add(Action.TURN_LEFT);

        }
        // Go to the block
        actions.add(Action.SHOOT_ARROW);

        return actions;
    }

    /**
     * Returns the direction based on the vector coordinates
     *
     * @param coords The 2D coordinates
     * @return The direction
     */
    private Direction getDirection(int[] coords) {
        if (coords[0] == +0 && coords[1] == +1) return Direction.N;
        if (coords[0] == +1 && coords[1] == +0) return Direction.E;
        if (coords[0] == +0 && coords[1] == -1) return Direction.S;
        if (coords[0] == -1 && coords[1] == +0) return Direction.W;
        return Direction.E;
    }
}