package org.firstinspires.ftc.teamcode;

public class GameMap {
    public static final int EMPTY = 0x0;

    // immovable objects are 0x10
    public static final int WALL = 0x10;
    public static final int BLOCKED_ZONE = 0x11;
    public static final int SUBMERSIBLE = 0x12;

    // samples are 0x20
    public static final int SAMPLE_YELLOW = 0x20;
    public static final int SAMPLE_RED = 0x21;
    public static final int SAMPLE_BLUE = 0x22;

    // moving objects are 0x30
    public static final int ROBOT = 0x30;

    int[][] grid;

    public GameMap(int width, int height) {
        this.grid = new int[width][height];
    }

    public void addRect(int type, int x1, int y1, int x2, int y2) {
        for (int x = x1; x <= x2; x++) {
            for (int y = y1; y <= y2; y++) {
                this.addPoint(type, x, y);
            }
        }
    }

    public void addPoint(int type, int x, int y) {
        this.grid[x][y] = type;
    }

    public boolean isSample(int x, int y) {
        return (this.getPoint(x, y) & 0x20) != 0;
    }

    public boolean isWall(int x, int y) {
        return (this.getPoint(x, y) & 0x10) != 0;
    }

    public boolean isMoving(int x, int y) {
        return (this.getPoint(x, y) & 0x30) != 0;
    }

    public int getPoint(int x, int y) {
        return this.grid[x][y];
    }

    public static final int INTO_THE_DEEP_FIELD_WIDTH_IN = 144;
    public static final double INTO_THE_DEEP_SUBMERSIBLE_WIDTH_IN = 27.5;
    public static final double INTO_THE_DEEP_SUBMERSIBLE_HEIGHT_IN = 42.75;
    // public static final double INTO_THE_DEEP_ASCENT_ZONE_WIDTH_IN = null;
    // public static final double INTO_THE_DEEP_ASCENT_ZONE_HEIGHT_IN = null;
    // public static final double INTO_THE_DEEP_ASCENT_ZONE_TRIANGLE_SIDE_LENGTH_IN = null;
    // public static final double INTO_THE_DEEP_NET_ZONE_LENGTH_IN = null;
    // public static final double INTO_THE_DEEP_SPECIMEN_ZONE_LENGTH_1_IN = null;
    // public static final double INTO_THE_DEEP_SPECIMEN_ZONE_LENGTH_2_IN = null;


    // public static GameMap createIntoTheDeepMap(int gridCellWidthIn) {
    //     GameMap map = new GameMap(INTO_THE_DEEP_FIELD_WIDTH_IN / gridCellWidthIn, INTO_THE_DEEP_FIELD_WIDTH_IN / gridCellWidthIn);
    //     map.addRect(EMPTY, INTO_THE_DEEP_FIELD_WIDTH_IN - );

    
    //     return map;
    // }
}
