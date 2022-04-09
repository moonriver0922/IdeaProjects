package byow.Core;

import byow.InputDemo.InputSource;
import byow.InputDemo.KeyboardInputSource;
import byow.InputDemo.StringInputDevice;
import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import edu.princeton.cs.introcs.StdDraw;
import org.junit.Test;

import java.awt.*;
import java.io.*;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class Engine {
    TERenderer ter = new TERenderer();
    /* Feel free to change the width and height. */
    public static final int WIDTH = 80;
    public static final int HEIGHT = 50;
    public static final int TILE_SIZE = 16;

    private Position avatarPos;
    private boolean gameBegin;
    private long beginTime;
    private int ghostX;
    private int ghostY;
    private TETile[][] world = new TETile[WIDTH][HEIGHT];
    private StringBuilder record = new StringBuilder();

    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     */
    public void interactWithKeyboard() {
        InputSource source = new KeyboardInputSource();
        ter.initialize(WIDTH, HEIGHT);
        drawUserMenu();
        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < HEIGHT; j++) {
                world[i][j] = Tileset.NOTHING;
            }
        }
        while (true) {
            if (record.length() > 0 && gameBegin) {
                ghostWantsToKillYou(world);
                ter.renderFrame(world);
                tileInfo(world, (int) StdDraw.mouseX(), (int) StdDraw.mouseY());
                showTime();
            }
            if (StdDraw.hasNextKeyTyped()) {
                char action = source.getNextKey();
                takeAction(source, action);
            }
        }
    }

    // draw the User menu
    private static void drawUserMenu() {
        StdDraw.setCanvasSize(WIDTH * 16, HEIGHT * 16);
        StdDraw.setXscale(0,WIDTH);
        StdDraw.setYscale(0,HEIGHT);
        StdDraw.clear(StdDraw.BLACK);

        Font font = new Font("Monaco",Font.BOLD,50);
        StdDraw.setFont(font);
        StdDraw.setPenColor(StdDraw.WHITE);
        StdDraw.text(WIDTH / 2.0, HEIGHT * 0.7, "CS61B: The Game");

        Font smallFont = new Font("Monaco",Font.BOLD,30);
        StdDraw.setFont(smallFont);
        StdDraw.setPenColor(StdDraw.WHITE);
        StdDraw.text(WIDTH / 2.0, HEIGHT * 0.5, "New Game(Press N/n)");
        StdDraw.text(WIDTH / 2.0, HEIGHT * 0.4, "Load Game(Press L/l)");
        StdDraw.text(WIDTH / 2.0, HEIGHT * 0.3, "Quit Game(Press Q/q)");

        StdDraw.show();
    }

    /**
     * Method used for autograding and testing your code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The engine should
     * behave exactly as if the user typed these characters into the engine using
     * interactWithKeyboard.
     *
     * Recall that strings ending in ":q" should cause the game to quite save. For example,
     * if we do interactWithInputString("n123sss:q"), we expect the game to run the first
     * 7 commands (n123sss) and then quit and save. If we then do
     * interactWithInputString("l"), we should be back in the exact same state.
     *
     * In other words, both of these calls:
     *   - interactWithInputString("n123sss:q")
     *   - interactWithInputString("lww")
     *
     * should yield the exact same world state as:
     *   - interactWithInputString("n123sssww")
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public TETile[][] interactWithInputString(String input) {
        // TODO: Fill out this method so that it run the engine using the input
        // passed in as an argument, and return a 2D tile representation of the
        // world that would have been drawn if the same inputs had been given
        // to interactWithKeyboard().
        //
        // See proj3.byow.InputDemo for a demo of how you can make a nice clean interface
        // that works for many different input types.
        StringInputDevice inputDevice = new StringInputDevice(input);
        while (inputDevice.possibleNextInput()) {
            char action = inputDevice.getNextKey();
            takeAction(inputDevice, action);
        }
        return world;
    }

    // analyze the character that player inputs
    private void takeAction(InputSource inputSource, char action) {
        record.append(action);
        if (action == 'N' || action == 'n') {
            long seed = calcSeed(inputSource);
            record.append(seed);
            record.append('S');
            gameBegin = true;
            beginTime = System.currentTimeMillis();
            Random random = new Random(seed);
            avatarPos = UltimateWorldGenerator.createWorld(world, random);
            setGhost(world, random);
            System.out.println("ok ok");
            if (inputSource.getClass().equals(KeyboardInputSource.class))
                ter.renderFrame(world);
        }
        else if (action == ';') {
            char nextAction = inputSource.getNextKey();
            if (nextAction == 'Q' || nextAction == 'q' ) {
                gameBegin = false;
                record.deleteCharAt(record.length() - 1);
                saveWorld(record.toString());
                record = new StringBuilder();
                if (inputSource.getClass().equals(KeyboardInputSource.class))
                    System.exit(0);
            }
        }
        else if (action == 'L' || action == 'l') {
            record.deleteCharAt(record.length() - 1);
            String saveWorld = loadWorld();
            if (Objects.equals(saveWorld, "")) {
                System.exit(0);
            } else {
                world = interactWithInputString(saveWorld);
                if (inputSource.getClass().equals(KeyboardInputSource.class))
                    ter.renderFrame(world);
            }
        }
        else if (gameBegin) {
            if (action == 'W' || action == 'w') {
                resetAvatar(inputSource, avatarPos.x, avatarPos.y + 1, action);
            }
            else if (action == 'S' || action == 's') {
                resetAvatar(inputSource, avatarPos.x, avatarPos.y - 1, action);
            }
            else if (action == 'A' || action == 'a') {
                resetAvatar(inputSource, avatarPos.x - 1, avatarPos.y, action);
            }
            else if (action == 'D' || action == 'd') {
                resetAvatar(inputSource, avatarPos.x + 1, avatarPos.y, action);
            }
        }
    }

    // change the avatar's position according to action
    private void resetAvatar(InputSource inputSource, int x ,int y, char action) {
        // go ahead
        if (world[x][y].equals(Tileset.FLOOR) || world[x][y].equals(Tileset.UNLOCKED_DOOR)) {
            world[x][y] = Tileset.AVATAR; //update world
            world[avatarPos.x][avatarPos.y] = Tileset.FLOOR;
            avatarPos = new Position(x, y); //update avatar's position
            if (inputSource.getClass().equals(KeyboardInputSource.class))
                ter.renderFrame(world);
        }
        // kick twice to open the door
        else if (world[x][y].equals(Tileset.LOCKED_DOOR)) {
            int pressNum = 1;
            int sNum = 1;
            char nextAction = '\n';
            while (pressNum < 3) {
                ter.renderFrame(world);
                tileInfo(world, (int) StdDraw.mouseX(), (int) StdDraw.mouseY());
                showTime();
                ghostWantsToKillYou(world);
                if (inputSource.getClass().equals(KeyboardInputSource.class) ? StdDraw.hasNextKeyTyped() : inputSource.possibleNextInput()) {
                    nextAction = inputSource.getNextKey();
                    // should append nextAction to record !
                    record.append(nextAction);
                    pressNum++;
                    if (nextAction == action)
                        sNum++;
                    else takeAction(inputSource, nextAction);
                    if (sNum == 2) {
                        world[x][y] = Tileset.UNLOCKED_DOOR; //update world
                        if (inputSource.getClass().equals(KeyboardInputSource.class))
                            ter.renderFrame(world);
                    }
                }
            }
            if (sNum == 3) {
                world[x][y] = Tileset.AVATAR; //update world
                world[avatarPos.x][avatarPos.y] = Tileset.FLOOR;
                avatarPos = new Position(x, y); //update avatar's position
                if (inputSource.getClass().equals(KeyboardInputSource.class))
                    ter.renderFrame(world);
            }
        }
        // get it!
        else if (world[x][y].equals(Tileset.FLOWER)) {
            world[x][y] = Tileset.AVATAR; //update world
            world[avatarPos.x][avatarPos.y] = Tileset.FLOOR;
            avatarPos = new Position(x, y); //update avatar's position
            if (inputSource.getClass().equals(KeyboardInputSource.class))
                ter.renderFrame(world);
            drawFinishMenu();
            // initialize some fields
            record = new StringBuilder();
            gameBegin = false;
            waitingForZ();
            /*while (true) {
                if (StdDraw.hasNextKeyTyped()) {
                    // move the index!
                    char key = inputSource.getNextKey();
                    if (key == 'Z' || key == 'z') {
                        //return to the UserMenu
                        interactWithKeyboard();
                    break;
                    }
                }
            }*/
        }
        else if (world[x][y].equals(Tileset.MOUNTAIN)) {
            drawFinishMenu();
            // initialize some fields
            record = new StringBuilder();
            gameBegin = false;
            waitingForZ();
        }
    }

    // set up a ghost that can roam around the world
    private void setGhost(TETile[][] world, Random random) {
        while (true) {
            ghostX = random.nextInt(WIDTH);
            ghostY = random.nextInt(HEIGHT - 1);
            if (world[ghostX][ghostY] == Tileset.FLOOR) {
                world[ghostX][ghostY] = Tileset.MOUNTAIN;
                System.out.println("" + ghostX + " " + ghostY);
                break;
            }
        }
    }

    private void ghostWantsToKillYou(TETile[][] world) {
        int curX = ghostX;
        int curY = ghostY;

        Random random = new Random();

       int moveX = random.nextInt(3) - 1;
       int moveY = random.nextInt(3) - 1;
       world[ghostX][ghostY] = Tileset.FLOOR;
       ghostX = ghostX + moveX >= 0 && ghostX + moveX < WIDTH ? ghostX + moveX : ghostX;
       ghostY = ghostY + moveY >= 0 && ghostY + moveY < HEIGHT - 1 ? ghostY + moveY : ghostY;
       if (world[ghostX][ghostY] == Tileset.AVATAR) {
           record = new StringBuilder();
           gameBegin = false;
           drawFinishMenu();
           waitingForZ();
           /*while (true) {
               if (StdDraw.hasNextKeyTyped()) {
                   // move the index!
                   char key = StdDraw.nextKeyTyped();
                   if (key == 'Z' || key == 'z') {
                       //return to the UserMenu
                       interactWithKeyboard();
                       break;
                   }
               }
           }*/
       }
       else if (world[ghostX][ghostY] == Tileset.FLOOR) {
           world[ghostX][ghostY] = Tileset.MOUNTAIN;
           ter.renderFrame(world);
       } else {
           ghostX = curX;
           ghostY = curY;
           world[ghostX][ghostY] = Tileset.MOUNTAIN;
       }
    }

    // wait for user to enter Z
    private void waitingForZ() {
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                // move the index!
                char key = StdDraw.nextKeyTyped();
                if (key == 'Z' || key == 'z') {
                    //return to the UserMenu
                    interactWithKeyboard();
                    break;
                }
            }
        }
    }



    // When the player collects flowers, the finishMenu will pop up
    private void drawFinishMenu() {
        StdDraw.clear(StdDraw.BLACK);

        double spendTime = (System.currentTimeMillis() - beginTime) / 1000.0;
        Font font = new Font("Monaco",Font.BOLD,50);
        StdDraw.setFont(font);
        StdDraw.setPenColor(StdDraw.WHITE);
        StdDraw.text(WIDTH / 2.0, HEIGHT * 0.6, "You got it! Spent " + spendTime + "s");

        Font smallFont = new Font("Monaco",Font.BOLD,30);
        StdDraw.setFont(smallFont);
        StdDraw.setPenColor(StdDraw.WHITE);
        StdDraw.text(WIDTH / 2.0, HEIGHT * 0.35, "Press Z/z to go back!");

        StdDraw.show();
    }

    // calculate the seed that player inputs
    private long calcSeed(InputSource inputSource) {
        if (inputSource.getClass().equals(KeyboardInputSource.class))
            drawSeed("");
        StringBuilder seedRecord = new StringBuilder();
        while (inputSource.possibleNextInput()) {
            char next = inputSource.getNextKey();
            if (next == 'N' || next == 'n')
                continue;
            if (next != 's' && next != 'S') {
                seedRecord.append(next);
                if (inputSource.getClass().equals(KeyboardInputSource.class)) {
                    drawSeed(seedRecord.toString());
                    StdDraw.show();
                }
            }
            else break;
        }
        return Long.parseLong(seedRecord.toString());
    }

    // ask player for the seed
    private void drawSeed(String s) {
        StdDraw.clear(StdDraw.BLACK);

        Font font = new Font("Monaco",Font.BOLD,30);
        StdDraw.setFont(font);
        StdDraw.setPenColor(StdDraw.WHITE);
        StdDraw.text(WIDTH / 2.0,HEIGHT * 6.0 / 10.0,"Please type a seed, press 'S' to confirm");

        StdDraw.text(WIDTH / 2.0, HEIGHT * 5.0 / 10.0, s);
        font = new Font("Monaco", Font.BOLD, TILE_SIZE - 2);
        StdDraw.setFont(font);
        StdDraw.show();
    }

    // give the information about the tile where the mouse in
    private static void tileInfo(TETile[][] world, int x, int y) {
        Font font = new Font("Monaco",Font.BOLD,15);
        StdDraw.setFont(font);
        StdDraw.setPenColor(StdDraw.WHITE);
        StdDraw.textLeft(0, HEIGHT - 1, world[x][y].description());
        StdDraw.show();
    }

    // show how long the player spent
    private void showTime() {
        long currentTime = System.currentTimeMillis();
        Font font = new Font("Monaco",Font.BOLD,15);
        StdDraw.setFont(font);
        StdDraw.setPenColor(StdDraw.WHITE);
        StdDraw.textRight(WIDTH, HEIGHT - 1,  (currentTime - beginTime) / 1000.0 + "s");
        StdDraw.show();
    }

    // save the world
    private void saveWorld(String record) {
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try {
            File file = new File("./saveWorld_data.txt");
            if (!file.exists()) {
                file.createNewFile();
            }
            fos = new FileOutputStream(file);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(record);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // load the world saved early
    private String loadWorld() {
        File file = new File("./saveWorld_data.txt");
        if (file.exists()) {
            FileInputStream fis = null;
            ObjectInputStream ois = null;
            try {
                fis = new FileInputStream(file);
                ois = new ObjectInputStream(fis);
                return (String) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (ois != null) {
                    try {
                        ois.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return "";
    }
}
