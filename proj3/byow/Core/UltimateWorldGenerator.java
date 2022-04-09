package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.*;

/**
 * @Author: Guosheng_Wang
 * @Date: 2022/03/25/12:21
 */
public class UltimateWorldGenerator {

    // create a new world
    // return the position of avatar
    public static Position createWorld(TETile[][] world, Random random) {
        setWorldBackgroundAsGrass(world);
        Queue<NewRoom> rooms = partitionRoomsToWorld(world, random);
        connectRoomsInWorld(world, rooms, random);
        addNPC(world, rooms, random, Tileset.FLOWER);
        return addAvatar(world, rooms, random);
    }

    // add an avatar to the world and the avatar born in a random room
    // return the position of avatar
    private static Position addAvatar(TETile[][] world, Queue<NewRoom> rooms, Random random) {
        List<NewRoom> birthRooms = new ArrayList<>(rooms);
        int roomNum = random.nextInt(birthRooms.size());
        // set the origin birth position int the center of room
        int avatarX = birthRooms.get(roomNum).p.x + birthRooms.get(roomNum).width / 2;
        int avatarY = birthRooms.get(roomNum).p.y + birthRooms.get(roomNum).height / 2;
        world[avatarX][avatarY] = Tileset.AVATAR;
        return new Position(avatarX, avatarY);
    }

    // connect each rooms in the world
    private static void connectRoomsInWorld(TETile[][] world, Queue<NewRoom> rooms, Random random) {
        Queue<NewRoom> toBeConnected = new LinkedList<>();
        // protect the rooms
        for (NewRoom room : rooms)
            toBeConnected.offer(room);

        while (toBeConnected.size() > 1) {
            NewRoom roomA = toBeConnected.poll();
            NewRoom roomB = toBeConnected.poll();

            // calculate center position of each room
            Position roomACenter = new Position(roomA.p.x + roomA.width / 2, roomA.p.y + roomA.height / 2);
            Position roomBCenter = new Position(roomB.p.x + roomB.width / 2, roomB.p.y + roomB.height / 2);

            // choose the best position to add floor
            int horzStartX = Math.min(roomACenter.x, roomBCenter.x);
            int horzStartY = Math.min(roomACenter.y, roomBCenter.y);
            int rowLength = Math.abs(roomACenter.x - roomBCenter.x);
            Position rowPos = new Position(horzStartX, horzStartY);

            int vertStartX = roomACenter.y > roomBCenter.y ? roomACenter.x : roomBCenter.x;
            int vertStartY = Math.min(roomACenter.y, roomBCenter.y);
            int colLength = Math.abs(roomACenter.y - roomBCenter.y);
            Position colPos = new Position(vertStartX, vertStartY);

            // build hallways from roomA to roomB
            addHallwaysRowToWorld(world, rowLength, rowPos, Tileset.FLOOR);
            addHallwaysColumnToWorld(world, colLength, colPos, Tileset.FLOOR);

            // Pick a random room from two polled rooms, reinsert it into queue,
            // thus guaranteeing already connected rooms can be connected to other rooms.
            int i = random.nextInt(2);
            switch (i) {
                case 0 -> toBeConnected.offer(roomA);
                case 1 -> toBeConnected.offer(roomB);
            }
        }
    }

    // partition the Space and return a queue of rooms prepared for connection
    private static Queue<NewRoom> partitionRoomsToWorld(TETile[][] world, Random random) {
        List<BPSpace> spaces = new LinkedList<>();
        Queue<BPSpace> queue = new LinkedList<>();
        Queue<NewRoom> rooms = new LinkedList<>();
        //the origin space
        BPSpace root = new BPSpace(new Position(0, 0), Engine.WIDTH, Engine.HEIGHT - 1);
        spaces.add(root);
        queue.offer(root);

        int num = 15 + random.nextInt(15); // a suitable number for splitting
        while (num > 0 && !queue.isEmpty()) {
            BPSpace toPartition = queue.poll();
            if (toPartition.Partition(random)) {
                spaces.add(toPartition.leftChild);
                spaces.add(toPartition.rightChild);
                queue.offer(toPartition.leftChild);
                queue.offer(toPartition.rightChild);
            }
            num--;
        }
        root.buildRoom(random);
        for (BPSpace childSpace : spaces) {
            if (childSpace.room != null) {
                addRoomToWorld(world, childSpace.room);
                rooms.offer(childSpace.room);
            }
        }
        return rooms;
    }

    // add room to the world
    private static void addRoomToWorld(TETile[][] world, NewRoom room) {
        Position p = room.p;
        int width = room.width;
        int height = room.height;

        Position bottomWallsPos = p;
        Position leftWallsPos = new Position(p.x, p.y + 1);
        Position topWallsPos = new Position(p.x + 1, p.y + height - 1);
        Position rightWallsPos = new Position(p.x + width - 1, p.y);

        addTileRowToWorld(world, width - 1, bottomWallsPos, Tileset.WALL);
        addTileRowToWorld(world, width - 1, topWallsPos, Tileset.WALL);
        addTileColumnToWorld(world, height - 1, leftWallsPos, Tileset.WALL);
        addTileColumnToWorld(world, height - 1, rightWallsPos, Tileset.WALL);

        for (int i = 1; i < width - 1; i++) {
            addTileColumnToWorld(world, height - 2, new Position(p.x + i, p.y + 1), Tileset.FLOOR);
        }
    }

    // set the all world with Grass
    private static void setWorldBackgroundAsGrass(TETile[][] world) {
        for (int i = 0; i < Engine.WIDTH; i++) {
            for (int j = 0; j < Engine.HEIGHT - 2; j++) {
                world[i][j] = Tileset.WATER;
            }
        }
    }

    // set one Row from left to right with certain tile
    private static void addTileRowToWorld(TETile[][] world, int length, Position p, TETile tile) {
        for (int i = 0; i < length; i++) {
            world[p.x + i][p.y] = tile;
        }
    }

    // set one Column from bottom to top with certain tile
    private static void addTileColumnToWorld(TETile[][] world, int length, Position p, TETile tile) {
        for (int j = 0; j < length; j++) {
            world[p.x][p.y + j] = tile;
        }
    }

    // in the process of connecting each rooms
    // set one Row from left to right with certain tile
    private static void addHallwaysRowToWorld(TETile[][] world, int length, Position p, TETile tile) {
        for (int i = 0; i < length; i++) {
            if (tile != Tileset.WALL && world[p.x + i][p.y] == Tileset.WALL)
                world[p.x + i][p.y] = Tileset.LOCKED_DOOR;
            else if (world[p.x + i][p.y] != Tileset.LOCKED_DOOR)
                world[p.x + i][p.y] = tile;
        }
    }

    // in the process of connecting each rooms
    // set one Column from bottom to top with certain tile
    private static void addHallwaysColumnToWorld(TETile[][] world, int length, Position p, TETile tile) {
        for (int j = 0; j < length; j++) {
            if (tile != Tileset.WALL && world[p.x][p.y + j] == Tileset.WALL)
                world[p.x][p.y + j] = Tileset.LOCKED_DOOR;
            else if (world[p.x][p.y + j] != Tileset.LOCKED_DOOR)
                world[p.x][p.y + j] = tile;
        }
    }

    // set treasure into certain room of the world
    private static void addNPC(TETile[][] world, Queue<NewRoom> rooms, Random random, TETile tile) {
        List<NewRoom> roomList = new ArrayList<>(rooms);
        int roomNum = random.nextInt(rooms.size());

        // calculate the position of treasure
        int offsetX = random.nextInt(roomList.get(roomNum).width - 2) + 1;
        int offsetY = random.nextInt(roomList.get(roomNum).height - 2) + 1;
        world[roomList.get(roomNum).p.x + offsetX][roomList.get(roomNum).p.y + offsetY] = tile;
    }
}
