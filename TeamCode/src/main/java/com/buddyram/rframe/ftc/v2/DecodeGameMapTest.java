package com.buddyram.rframe.ftc.v2;

import com.buddyram.rframe.ftc.v2.DecodeGameMap.Zone;
import com.buddyram.rframe.ftc.v2.DecodeGameMap.Side;
import com.buddyram.rframe.ftc.v2.DecodeGameMap.ZoneResult;

/**
 * Quick smoke tests for DecodeGameMap zone detection.
 * Run with: right-click > Run 'DecodeGameMapTest.main()' in IntelliJ
 */
public class DecodeGameMapTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("=== DecodeGameMap Point Tests ===\n");

        // ── Near shooting zone (big triangle at top) ──
        assertZone(36, 120, Zone.SHOOTING_ZONE_NEAR, Side.RED,  "deep in near zone, red side");
        assertZone(108, 130, Zone.SHOOTING_ZONE_NEAR, Side.BLUE, "deep in near zone, blue side");
        assertZone(72, 110, Zone.SHOOTING_ZONE_NEAR, Side.BLUE, "near zone center line (x=72 -> BLUE)");
        assertZone(10, 140, Zone.SHOOTING_ZONE_NEAR, Side.RED,  "near zone top-left corner area");
        assertZone(72, 72, Zone.SHOOTING_ZONE_NEAR, Side.BLUE,  "near zone apex (on boundary, included)");

        // ── Far shooting zone (small triangle at bottom) ──
        assertZone(72, 10,  Zone.SHOOTING_ZONE_FAR, Side.BLUE,  "center of far zone");
        assertZone(60, 5,   Zone.SHOOTING_ZONE_FAR, Side.RED,   "far zone red side");
        assertZone(85, 5,   Zone.SHOOTING_ZONE_FAR, Side.BLUE,  "far zone blue side");

        // ── Human player zones ──
        assertZone(10, 10,  Zone.HUMAN_PLAYER, Side.RED,  "red HP center");
        assertZone(130, 10, Zone.HUMAN_PLAYER, Side.BLUE, "blue HP center");
        assertZone(0, 0,    Zone.HUMAN_PLAYER, Side.RED,  "red HP exact corner");
        assertZone(144, 0,  Zone.HUMAN_PLAYER, Side.BLUE, "blue HP exact corner");

        // ── Parking zones ──
        assertZone(39, 33,  Zone.PARKING, Side.RED,  "red parking center");
        assertZone(105, 33, Zone.PARKING, Side.BLUE, "blue parking center");
        assertZone(30, 24,  Zone.PARKING, Side.RED,  "red parking min corner (on boundary)");
        assertZone(114, 42, Zone.PARKING, Side.BLUE, "blue parking max corner (on boundary)");

        // ── Secret tunnels ──
        assertZone(3, 50,   Zone.SECRET_TUNNEL, Side.RED,  "red tunnel middle");
        assertZone(140, 50, Zone.SECRET_TUNNEL, Side.BLUE, "blue tunnel middle");
        assertZone(3, 25,   Zone.SECRET_TUNNEL, Side.RED,  "red tunnel bottom edge");
        assertZone(140, 70, Zone.SECRET_TUNNEL, Side.BLUE, "blue tunnel near gate");
        assertZone(10, 50,  Zone.GENERAL_FIELD, Side.RED,  "outside red tunnel (x=10 > 6.125)");
        assertZone(130, 50, Zone.GENERAL_FIELD, Side.BLUE, "outside blue tunnel (x=130 < 137.875)");

        // ── General field ──
        assertZone(72, 60,  Zone.GENERAL_FIELD, Side.BLUE, "field center-ish (below near zone)");
        assertZone(36, 50,  Zone.GENERAL_FIELD, Side.RED,  "general red side");
        assertZone(108, 50, Zone.GENERAL_FIELD, Side.BLUE, "general blue side");
        assertZone(72, 36,  Zone.GENERAL_FIELD, Side.BLUE, "mid-field low");

        // ── Points that should NOT be in certain zones ──
        assertZone(30, 10,  Zone.GENERAL_FIELD, Side.RED,  "outside far zone (x=30 < 48)");
        assertZone(100, 10, Zone.GENERAL_FIELD, Side.BLUE, "outside far zone (x=100 > 96)");
        assertZone(72, 30,  Zone.GENERAL_FIELD, Side.BLUE, "above far zone apex (y=30 > 24)");

        System.out.println("\n=== DecodeGameMap Robot Overlap Tests ===\n");

        // Robot fully inside near zone
        assertRobotZone(72, 120, 0, Zone.SHOOTING_ZONE_NEAR, "robot fully in near zone, heading 0");

        // Robot fully in general field
        assertRobotZone(72, 55, 0, Zone.GENERAL_FIELD, "robot fully in general field");

        // Robot on near zone boundary - barely touching
        // Near zone boundary at x=72: y = 144 - x = 72 at center
        // Robot at (72, 72) with depth 17.5 -> front at y=80.75, back at y=63.25
        // About half in near zone, half in general -> should detect SHOOTING_ZONE_NEAR at 10%
        assertRobotZone(72, 72, 0, Zone.SHOOTING_ZONE_NEAR, "robot straddling near zone boundary");

        // Robot just barely grazing the near zone edge
        // At (72, 63), back at y=54.25, front at y=71.75 -> barely touching near zone at apex
        assertRobotZone(72, 63, 0, Zone.GENERAL_FIELD, "robot just below near zone (< 10% overlap)");

        // Robot in parking zone
        assertRobotZone(36, 36, 0, Zone.PARKING, "robot in red parking zone");

        // Robot rotated 45 degrees in near zone
        assertRobotZone(72, 120, 45, Zone.SHOOTING_ZONE_NEAR, "robot rotated 45 deg in near zone");

        System.out.println("\n=== Results ===");
        System.out.println("Passed: " + passed + " / " + (passed + failed));
        if (failed > 0) {
            System.out.println("FAILED: " + failed);
            System.exit(1);
        } else {
            System.out.println("All tests passed!");
        }
    }

    private static void assertZone(double x, double y, Zone expectedZone, Side expectedSide, String label) {
        ZoneResult result = DecodeGameMap.getZone(x, y);
        boolean pass = result.zone == expectedZone && result.side == expectedSide;
        String status = pass ? "PASS" : "FAIL";
        System.out.printf("  [%s] (%6.1f, %6.1f) -> %-25s %-4s  %s%n",
                status, x, y, result, expectedSide, label);
        if (!pass) {
            System.out.printf("         Expected: %s (%s)%n", expectedZone, expectedSide);
            failed++;
        } else {
            passed++;
        }
    }

    private static void assertRobotZone(double cx, double cy, double heading,
                                         Zone expectedZone, String label) {
        ZoneResult result = DecodeGameMap.getRobotZone(cx, cy, heading);
        boolean pass = result.zone == expectedZone;
        String status = pass ? "PASS" : "FAIL";
        System.out.printf("  [%s] robot@(%5.1f, %5.1f) h=%3.0f -> %-25s %s%n",
                status, cx, cy, heading, result, label);
        if (!pass) {
            System.out.printf("         Expected zone: %s%n", expectedZone);
            failed++;
        } else {
            passed++;
        }
    }
}
