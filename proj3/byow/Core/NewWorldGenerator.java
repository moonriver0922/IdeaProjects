package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.util.EmptyStackException;
import java.util.Random;
import java.util.WeakHashMap;

/**
 * @Author: Guosheng_Wang
 * @Date: 2022/03/24/21:42
 */
public class NewWorldGenerator {
        public Position createNewWorld(TETile[][] tiles, Random random) {
            return null;
        }

        public void createRoom(TETile[][] tiles, Random random) {
            int roomNum = Engine.WIDTH * Engine.HEIGHT / 15;
            while (roomNum > 0) {
                int x = random.nextInt(Engine.WIDTH);
                int y = random.nextInt(Engine.HEIGHT);
                Position p = new Position(x, y);
                Room room = new Room(random.nextInt(3) + 2, random.nextInt(3) + 2, p);
                if (room.x + p.x < Engine.WIDTH && room.y + p.y < Engine.HEIGHT) {
                    if (!Room.isOccupied(room.x, room.y, p)) {
                        room.fillRoom(tiles, Tileset.WALL);
                        roomNum--;
                    }
                }
            }
        }

        public void createMazes(TETile[][] tiles) {
            for (int x = 0; x < Engine.WIDTH; x++) {
                for (int y = 0; y < Engine.HEIGHT; y++) {
                    Position p = new Position(x, y);
                    if (!Room.isOccupied(1, 1, p) && tiles[p.x][p.y] != Tileset.FLOOR) {
                        createMazesHelper(tiles, p);
                    }
                }
            }
        }

        private void createMazesHelper(TETile[][] tiles, Position p) {
            if (p.x >= Engine.WIDTH || p.x < 0 || p.y >= Engine.HEIGHT || p.y < 0 || tiles[p.x][p.y] == Tileset.WALL || tiles[p.x][p.y] == Tileset.FLOOR)
                return;
            tiles[p.x][p.y] = Tileset.FLOOR;
            createMazesHelper(tiles, new Position(p.x - 1, p.y));
            createMazesHelper(tiles, new Position(p.x + 1, p.y));
            createMazesHelper(tiles, new Position(p.x, p.y - 1));
            createMazesHelper(tiles, new Position(p.x, p.y + 1));
        }
}
