package com.buddyram.rframe.ftc.v3;

import java.util.HashMap;

/**
 * Field zone detection for FTC Into The Deep / Shortage.
 * Field is 144" x 144" with origin at bottom-left (0,0).
 * Heading convention: 0 degrees = facing +y (forward), positive = counter-clockwise.
 *
 * Zone priority (highest first): PARKING > HUMAN_PLAYER > SECRET_TUNNEL > SHOOTING > GENERAL
 * Side split: RED = left half (x < 72), BLUE = right half (x >= 72).
 */
public class DecodeGameMap {

    // ── Field ──
    public static final double FIELD_SIZE = 144.0;
    public static final double FIELD_CENTER = FIELD_SIZE / 2;

    // ── Default robot dimensions (inches) ──
    public static final double ROBOT_WIDTH = 17.7;
    public static final double ROBOT_DEPTH = 17.5;

    // ── Overlap detection ──
    public static final double DEFAULT_OVERLAP_PERCENT = 0.10;
    private static final int SAMPLE_GRID = 10;

    // ── Near shooting zone: large triangle at top ──
    //     (0, 144) ──────────── (144, 144)
    //          \                /
    //           \              /
    //            \            /
    //             (72, 72)
    private static final double[] NEAR_TRI_X = {0, 72, 144};
    private static final double[] NEAR_TRI_Y = {144, 72, 144};

    // ── Far shooting zone: small triangle at bottom center ──
    //             (72, 24)
    //            /        \
    //     (48, 0) ──────── (96, 0)
    private static final double[] FAR_TRI_X = {48, 72, 96};
    private static final double[] FAR_TRI_Y = {0, 24, 0};

    // ── Rectangular zones: {minX, minY, maxX, maxY} ──
    private static final double[] RED_HP_BOUNDS      = {0,   0,  24,  24};
    private static final double[] BLUE_HP_BOUNDS     = {120, 0,  144, 24};
    private static final double[] RED_PARK_BOUNDS    = {30,     24, 48,      42};
    private static final double[] BLUE_PARK_BOUNDS   = {96,     24, 114,    42};
    private static final double[] RED_TUNNEL_BOUNDS  = {0,      24, 6.125,  72};
    private static final double[] BLUE_TUNNEL_BOUNDS = {137.875,24, 144,    72};

    public enum Zone {
        SHOOTING_ZONE_NEAR,
        SHOOTING_ZONE_FAR,
        PARKING,
        HUMAN_PLAYER,
        SECRET_TUNNEL,
        GENERAL_FIELD
    }

    public enum Side {
        RED, BLUE
    }

    public static class ZoneResult {
        public final Zone zone;
        public final Side side;

        public ZoneResult(Zone zone, Side side) {
            this.zone = zone;
            this.side = side;
        }

        @Override
        public String toString() {
            return zone + " (" + side + ")";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ZoneResult)) return false;
            ZoneResult that = (ZoneResult) o;
            return zone == that.zone && side == that.side;
        }

        @Override
        public int hashCode() {
            return zone.hashCode() * 31 + side.hashCode();
        }
    }

    /**
     * Returns the zone and side for a single field point.
     */
    public static ZoneResult getZone(double x, double y) {
        // Parking (highest priority, small specific areas)
        if (inRect(x, y, RED_PARK_BOUNDS))  return new ZoneResult(Zone.PARKING, Side.RED);
        if (inRect(x, y, BLUE_PARK_BOUNDS)) return new ZoneResult(Zone.PARKING, Side.BLUE);

        // Human player stations (corner areas)
        if (inRect(x, y, RED_HP_BOUNDS))  return new ZoneResult(Zone.HUMAN_PLAYER, Side.RED);
        if (inRect(x, y, BLUE_HP_BOUNDS)) return new ZoneResult(Zone.HUMAN_PLAYER, Side.BLUE);

        // Secret tunnels (corridors from HP to gate)
        if (inRect(x, y, RED_TUNNEL_BOUNDS))  return new ZoneResult(Zone.SECRET_TUNNEL, Side.RED);
        if (inRect(x, y, BLUE_TUNNEL_BOUNDS)) return new ZoneResult(Zone.SECRET_TUNNEL, Side.BLUE);

        // Shooting zones
        Side side = x < FIELD_CENTER ? Side.RED : Side.BLUE;
        if (inTriangle(x, y, NEAR_TRI_X, NEAR_TRI_Y)) return new ZoneResult(Zone.SHOOTING_ZONE_NEAR, side);
        if (inTriangle(x, y, FAR_TRI_X, FAR_TRI_Y))   return new ZoneResult(Zone.SHOOTING_ZONE_FAR, side);

        return new ZoneResult(Zone.GENERAL_FIELD, side);
    }

    /**
     * Returns the zone for a robot rectangle at the given position and heading.
     * Uses default robot dimensions (17.7" x 17.5") and 10% overlap threshold.
     *
     * @param cx         Robot center X (inches)
     * @param cy         Robot center Y (inches)
     * @param headingDeg Robot heading in degrees (0 = facing +y)
     */
    public static ZoneResult getRobotZone(double cx, double cy, double headingDeg) {
        return getRobotZone(cx, cy, headingDeg, DEFAULT_OVERLAP_PERCENT);
    }

    /**
     * Returns the zone for a robot rectangle at the given position and heading.
     *
     * @param cx             Robot center X (inches)
     * @param cy             Robot center Y (inches)
     * @param headingDeg     Robot heading in degrees (0 = facing +y)
     * @param overlapPercent Minimum overlap fraction (0.0-1.0) in a non-GENERAL zone to report it
     */
    public static ZoneResult getRobotZone(double cx, double cy, double headingDeg,
                                          double overlapPercent) {
        return getRobotZone(cx, cy, headingDeg, overlapPercent, ROBOT_WIDTH, ROBOT_DEPTH);
    }

    /**
     * Returns the zone for a robot rectangle with custom dimensions.
     *
     * @param cx             Robot center X (inches)
     * @param cy             Robot center Y (inches)
     * @param headingDeg     Robot heading in degrees (0 = facing +y)
     * @param overlapPercent Minimum overlap fraction (0.0-1.0) in a non-GENERAL zone to report it
     * @param width          Robot width (inches, side-to-side)
     * @param depth          Robot depth (inches, front-to-back)
     */
    public static ZoneResult getRobotZone(double cx, double cy, double headingDeg,
                                          double overlapPercent, double width, double depth) {
        double headRad = Math.toRadians(headingDeg);
        double halfW = width / 2.0;
        double halfD = depth / 2.0;

        // Basis vectors: heading 0 = +y forward
        double fwdX = -Math.sin(headRad);
        double fwdY =  Math.cos(headRad);
        double rightX = Math.cos(headRad);
        double rightY = Math.sin(headRad);

        int totalPoints = SAMPLE_GRID * SAMPLE_GRID;
        HashMap<ZoneResult, Integer> counts = new HashMap<>();
        ZoneResult bestNonGeneral = null;
        int bestCount = 0;

        for (int i = 0; i < SAMPLE_GRID; i++) {
            for (int j = 0; j < SAMPLE_GRID; j++) {
                double localRight = -halfW + (i + 0.5) * width / SAMPLE_GRID;
                double localFwd   = -halfD + (j + 0.5) * depth / SAMPLE_GRID;

                double fx = cx + localRight * rightX + localFwd * fwdX;
                double fy = cy + localRight * rightY + localFwd * fwdY;

                ZoneResult zr = getZone(fx, fy);
                if (zr.zone != Zone.GENERAL_FIELD) {
                    Integer prev = counts.get(zr);
                    int count = (prev == null ? 0 : prev) + 1;
                    counts.put(zr, count);
                    if (count > bestCount) {
                        bestCount = count;
                        bestNonGeneral = zr;
                    }
                }
            }
        }

        if (bestNonGeneral != null && (double) bestCount / totalPoints >= overlapPercent) {
            return bestNonGeneral;
        }

        return new ZoneResult(Zone.GENERAL_FIELD, cx < FIELD_CENTER ? Side.RED : Side.BLUE);
    }

    /**
     * Returns true if the robot is in either the near or far shooting zone.
     * Uses default robot dimensions (17.7" x 17.5") and 10% overlap threshold.
     */
    public static boolean isInShootingZone(double cx, double cy, double headingDeg) {
        return isInShootingZone(cx, cy, headingDeg, ROBOT_WIDTH, ROBOT_DEPTH);
    }

    /**
     * Returns true if the robot is in either the near or far shooting zone.
     * Uses 10% overlap threshold with custom robot dimensions.
     */
    public static boolean isInShootingZone(double cx, double cy, double headingDeg,
                                           double width, double depth) {
        Zone zone = getRobotZone(cx, cy, headingDeg, DEFAULT_OVERLAP_PERCENT, width, depth).zone;
        return zone == Zone.SHOOTING_ZONE_NEAR || zone == Zone.SHOOTING_ZONE_FAR;
    }

    // ── Geometry helpers ──

    private static boolean inRect(double x, double y, double[] r) {
        return x >= r[0] && x <= r[2] && y >= r[1] && y <= r[3];
    }

    private static boolean inTriangle(double px, double py, double[] tx, double[] ty) {
        double d1 = cross(px, py, tx[0], ty[0], tx[1], ty[1]);
        double d2 = cross(px, py, tx[1], ty[1], tx[2], ty[2]);
        double d3 = cross(px, py, tx[2], ty[2], tx[0], ty[0]);
        boolean hasNeg = (d1 < 0) || (d2 < 0) || (d3 < 0);
        boolean hasPos = (d1 > 0) || (d2 > 0) || (d3 > 0);
        return !(hasNeg && hasPos);
    }

    private static double cross(double px, double py,
                                double x1, double y1,
                                double x2, double y2) {
        return (x1 - px) * (y2 - py) - (x2 - px) * (y1 - py);
    }
}
