package byow.lab12;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import org.junit.Test;


import java.util.Random;

import static org.junit.Assert.assertEquals;

/**
 * Draws a world consisting of hexagonal regions.
 */
public class HexWorld {
    /**
     *
     */
    public static class Position {
        int x;
        int y;
        private Position(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
    /**
     * Computes the width of row i for a size s hexagon.
     * @param s The size of the hex.
     * @param i The row number where i = 0 is the bottom row.
     * @return s + 2 * effectiveI
     */
    public static int hexRowWidth(int s, int i) {
        int effectiveI = i;
        if (i >= s) {
            effectiveI = 2 * s - 1 - effectiveI;
        }

        return s + 2 * effectiveI;
    }

    /**
     * Computes-relative x coordinate of the leftmost tile in the ith
     * row of a hexagon, assuming that the bottom row has an x-coordinate
     * of zero. For example, if s = 3, and i = 2, this function
     * returns -2, because the row 2 up from the bottom starts 2 to the left
     * of the start position, e.g.
     *   xxx
     *  xxxxx
     * xxxxxxx
     * xxxxxxx <-- i = 2, starts 2 spots to the left of the bottom of the hex
     *  xxxxx
     *   xxx
     *
     * @param s size of the hexagon
     * @param i row num of the hexagon, where i = 0 is the bottom
     * @return -effectiveI
     */
    public static int hexRowOffset(int s, int i) {
        int effectiveI = i;
        if (i >= s) {
            effectiveI = 2 * s - 1 - effectiveI;
        }
        return -effectiveI;
    }

    /** Adds a row of the same tile.
     * @param world the world to draw on
     * @param p the leftmost position of the row
     * @param width the number of tiles wide to draw
     * @param t the tile to draw
     */
    public static void addRow(TETile[][] world, Position p, int width, TETile t) {
        for (int xi = 0; xi < width; xi += 1) {
            int xCoord = p.x + xi;
            int yCoord = p.y;
            world[xCoord][yCoord] = TETile.colorVariant(t, 32, 32, 32, new Random());
        }
    }

    /**
     * Adds a hexagon to the world.
     * @param world the world to draw on
     * @param p the bottom left coordinate of the hexagon
     * @param s the size of the hexagon
     * @param t the tile to draw
     */
    public static void addHexagon(TETile[][] world, Position p, int s, TETile t) {

        if (s < 2) {
            throw new IllegalArgumentException("Hexagon must be at least size 2.");
        }

        // hexagons have 2*s rows. this code iterates up from the bottom row,
        // which we call row 0.
        for (int yi = 0; yi < 2 * s; yi += 1) {
            int thisRowY = p.y + yi;

            int xRowStart = p.x + hexRowOffset(s, yi);
            Position rowStartP = new Position(xRowStart, thisRowY);

            int rowWidth = hexRowWidth(s, yi);

            addRow(world, rowStartP, rowWidth, t);

        }
    }

    public static void addTesselationOfHexagons(TETile[][] world, Position p, int s) {
        for (int diff = -s + 1; diff < s; diff++) {
            addColumnHexagons(world, p, s, diff);
        }
    }

    public static void addColumnHexagons(TETile[][] world, Position p, int s, int diff) {
        int startX = calcStartX(p, s, diff);
        int startY = calcStartY(p, s, diff);
        Position p_new = new Position(startX, startY);
        TETile tile;
        for (int i = 0; i < 2 * s - 1 - Math.abs(diff); i++) {
            tile = randomTile();
            addHexagon(world, p_new, s ,tile);
            p_new.y += 2 * s;
        }
    }

    public static int calcStartX(Position p, int s, int diff) {
        int offset = Math.abs(diff) * (2 * s - 1);
        return diff > 0 ? p.x + offset : p.x - offset;
    }

    public static int calcStartY(Position p, int s, int diff) {
        return p.y + s * Math.abs(diff);
    }

    /** Picks a RANDOM tile with a 20% change of being
     *  a wall, 20% chance of being a flower, 20%
     *  chance of being a grass, 20% chance of being a
     *  floor, 20% chance of being a tree.
     */
    private static TETile randomTile() {
        Random dice = new Random();
        int tileNum = dice.nextInt(5);
        switch (tileNum) {
            case 0: return Tileset.WALL;
            case 1: return Tileset.FLOWER;
            case 2: return Tileset.GRASS;
            case 3: return Tileset.FLOOR;
            case 4: return Tileset.TREE;
            default: return Tileset.NOTHING;
        }
    }

    public static void main(String[] args) {
        Position p1 = new Position(29,10);
        Position p2 = new Position(29,16);
        Position p3 = new Position(24,13);
        // initialize the tile rendering engine with a window of size WIDTH x HEIGHT
        TERenderer ter = new TERenderer();
        ter.initialize(70, 70);
        TETile[][] world = new TETile[70][70];
        for (int x = 0; x < 70; x += 1) {
            for (int y = 0; y < 70; y += 1) {
                world[x][y] = Tileset.NOTHING;
            }
        }
        HexWorld.addHexagon(world, p1, 3,  Tileset.FLOOR);
//        HexWorld.addHexagon(world, p2, 3,  Tileset.GRASS);
//        HexWorld.addHexagon(world, p3, 3,  Tileset.WALL);
//        HexWorld.addTesselationOfHexagons(world, p1, 3);
        ter.renderFrame(world);

//        assertEquals(24, calcStartX(29, 3, -1));
//        assertEquals(29, calcStartX(29, 3, 0));
//        assertEquals(34, calcStartX(29, 3, 1));
//        assertEquals(19, calcStartX(29, 3, -2));
//        assertEquals(39, calcStartX(29, 3, 2));
    }
/*
    @Test
    public void testHexRowWidth() {
        assertEquals(3, hexRowWidth(3, 5));
        assertEquals(5, hexRowWidth(3, 4));
        assertEquals(7, hexRowWidth(3, 3));
        assertEquals(7, hexRowWidth(3, 2));
        assertEquals(5, hexRowWidth(3, 1));
        assertEquals(3, hexRowWidth(3, 0));
        assertEquals(2, hexRowWidth(2, 0));
        assertEquals(4, hexRowWidth(2, 1));
        assertEquals(4, hexRowWidth(2, 2));
        assertEquals(2, hexRowWidth(2, 3));
    }

    @Test
    public void testHexRowOffset() {
        assertEquals(0, hexRowOffset(3, 5));
        assertEquals(-1, hexRowOffset(3, 4));
        assertEquals(-2, hexRowOffset(3, 3));
        assertEquals(-2, hexRowOffset(3, 2));
        assertEquals(-1, hexRowOffset(3, 1));
        assertEquals(0, hexRowOffset(3, 0));
        assertEquals(0, hexRowOffset(2, 0));
        assertEquals(-1, hexRowOffset(2, 1));
        assertEquals(-1, hexRowOffset(2, 2));
        assertEquals(0, hexRowOffset(2, 3));
    }*/
}
