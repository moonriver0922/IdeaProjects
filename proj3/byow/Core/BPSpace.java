package byow.Core;

import java.util.Random;

/**
 * @Author: Guosheng_Wang
 * @Date: 2022/03/25/12:22
 * @source: https://github.com/zangsy/cs61b_sp19/blob/master/proj3/byow/Core/BPSpace.java
 */
public class BPSpace {
    private static final int MIN_SIZE = 6;
    private int width;
    private int height;
    BPSpace leftChild;
    BPSpace rightChild;
    Position p;
    NewRoom room;

    BPSpace(Position p, int width, int height) {
        this.width = width;
        this.height = height;
        this.p = p;
        this.leftChild = null;
        this.rightChild = null;
        this.room = null;
    }

    public boolean Partition(Random random) {
        // if already split, break
        if (leftChild != null) return false;

        // partition
        boolean direction;
        if (width > height)
            direction = true;
        else if (height > width)
            direction = false;
        else direction = random.nextBoolean();

        // make sure that both bottom(left) and top(right) child can have MIN_SIZE
        int max = (direction ? width : height) - MIN_SIZE;
        // if space is too small, break
        if (max <= MIN_SIZE) return false;

        // generate child space
        int split = random.nextInt(max);
        // if split is less than MIN_SIZE, adjust it
        if (split < MIN_SIZE) split = MIN_SIZE;

        if (direction) {
            leftChild = new BPSpace(p, split, height); // leftSpace
            rightChild = new BPSpace(new Position(p.x + split, p.y), width - split, height); // rightSpace
        }
        else {
            leftChild = new BPSpace(p, width, split); // bottomSpace
            rightChild = new BPSpace(new Position(p.x, p.y + split), width, height - split); // topSpace
        }

        return true;
    }

    public void buildRoom(Random random) {
        // if current space has child areas, then we cannot build room in it
        // just go to check its child areas
        if (leftChild != null) {
            leftChild.buildRoom(random);
            rightChild.buildRoom(random);
        } else {
          int offsetX = (width - MIN_SIZE <= 0) ? 0 : random.nextInt(width - MIN_SIZE);
          int offsetY = (height - MIN_SIZE <= 0) ? 0 : random.nextInt(height - MIN_SIZE);

          // room is at least one grid away from the left, right, top edges of current space
          Position roomPos = new Position(p.x + offsetX, p.y + offsetY);
          int roomWidth = Math.max(random.nextInt(width - offsetX), MIN_SIZE);
          int roomHeight = Math.max(random.nextInt(height - offsetY), MIN_SIZE);
          room = new NewRoom(roomWidth, roomHeight, roomPos);
        }
    }
}
