package byow.lab13;

import edu.princeton.cs.introcs.StdDraw;

import java.awt.Color;
import java.awt.Font;
import java.util.Random;

public class MemoryGame {
    private int width;
    private int height;
    private int round;
    private Random rand;
    private boolean gameOver;
    private boolean playerTurn;
    private static final char[] CHARACTERS = "abcdefghijklmnopqrstuvwxyz".toCharArray();
    private static final String[] ENCOURAGEMENT = {"You can do this!", "I believe in you!",
                                                   "You got this!", "You're a star!", "Go Bears!",
                                                   "Too easy for you!", "Wow, so impressive!"};

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Please enter a seed");
            return;
        }

        long seed = Integer.parseInt(args[0]);
        MemoryGame game = new MemoryGame(40, 40, seed);
        game.startGame();
    }

    public MemoryGame(int width, int height, long seed) {
        /* Sets up StdDraw so that it has a width by height grid of 16 by 16 squares as its canvas
         * Also sets up the scale so the top left is (0,0) and the bottom right is (width, height)
         */
        this.width = width;
        this.height = height;
        StdDraw.setCanvasSize(this.width * 16, this.height * 16);
        Font font = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(font);
        StdDraw.setXscale(0, this.width);
        StdDraw.setYscale(0, this.height);
        StdDraw.clear(Color.BLACK);
        StdDraw.enableDoubleBuffering();

        //TODO: Initialize random number generator
        rand = new Random(seed);
    }

    public String generateRandomString(int n) {
        //TODO: Generate random string of letters of length n
        StringBuilder randomString = new StringBuilder();
        while (n > 0) {
            randomString.append((char) (rand.nextInt(26) + 'a'));
            n--;
        }
        return randomString.toString();
    }

    public void drawFrame(String s) {
        //TODO: Take the string and display it in the center of the screen
        //TODO: If game is not over, display relevant game information at the top of the screen
        StdDraw.clear();
        StdDraw.clear(StdDraw.BLACK);

        //ready for the backgrounds
        if (!gameOver) {
            Font smallFont = new Font("Monaco", Font.BOLD, 20);
            StdDraw.setFont(smallFont);
            StdDraw.textLeft(1, height - 1, "Round:" + round);
            StdDraw.text(width / 2.0, height - 1, playerTurn ? "Typed!" : "Watch!");
            StdDraw.textRight(width - 1, height - 1, ENCOURAGEMENT[round % 7]);
            StdDraw.line(0, height - 2, width, height - 2);
        }

        //draw the actual text
        Font bigFont = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(bigFont);
        StdDraw.setPenColor(Color.white);
        StdDraw.text(width / 2.0,height / 2.0, s);
        StdDraw.show();
    }

    public void flashSequence(String letters) {
        //TODO: Display each character in letters, making sure to blank the screen between letters
        for (int i = 0; i < letters.length(); i++) {
            drawFrame(String.valueOf(letters.charAt(i)));
            StdDraw.pause(1000);
            drawFrame("");
            StdDraw.pause(500);
        }
    }

    public String solicitNCharsInput(int n) {
        //TODO: Read n letters of player input
        StringBuilder keyStrokes = new StringBuilder();
        while (n > 0) {
            if (!StdDraw.hasNextKeyTyped())
                continue;
            keyStrokes.append(StdDraw.nextKeyTyped());
            drawFrame(keyStrokes.toString());
            n--;
        }
        StdDraw.pause(500);
        return keyStrokes.toString();
    }

    public void startGame() {
        //TODO: Set any relevant variables before the game starts
        // TODO: Establish Engine loop
        gameOver = false;
        round = 1;
        while (!gameOver) {
            playerTurn = false;
            drawFrame("Round:" + round + " Good Luck!");
            StdDraw.pause(1500);
            String question = generateRandomString(round);
            flashSequence(question);
            playerTurn = true;
            String ans = solicitNCharsInput(round);
            if (ans.equals(question)) {
                round++;
                drawFrame("Bingo!");
                StdDraw.pause(1500);
            }
            else {
                drawFrame("Game Over! You made it to round:" + round);
                break;
            }
        }
    }
}
