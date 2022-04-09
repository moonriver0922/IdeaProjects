package byow.Core;

import byow.TileEngine.TETile;

/**
 * @Author: Guosheng_Wang
 * @Date: 2022/03/24/22:24
 */
public class Room {
    int x;
    int y;
    Position p;
    static boolean[][] isOccupied = new boolean[Engine.WIDTH][Engine.HEIGHT];
    public Room(int x, int y, Position p) {
        this.x = x;
        this.y = y;
        this.p = p;
    }
    public void fillRoom(TETile[][] tiles, TETile tile) {
        for (int i = 0; i < this.x; i++) {
            for (int j = 0; j < this.y; j++) {
                tiles[this.p.x + i][this.p.y + j] = tile;
            }
        }
        this.occupyRoom(this.x, this.y, this.p);
    }

    public void occupyRoom(int x, int y, Position p) {
        for (int i = p.x; i < p.x + x; i++) {
            for (int j = p.y; j < p.y + y; j++) {
                isOccupied[i][j] = true;
            }
        }
    }

    public static boolean isOccupied(int x, int y, Position p) {
        for (int i = p.x; i < p.x + x; i++) {
            for (int j = p.y; j < p.y + y; j++) {
                if (isOccupied[i][j])
                    return true;
            }
        }
        return false;
    }
}
