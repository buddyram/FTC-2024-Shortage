package com.buddyram.rframe.ftc.v2.opmodes;

import com.buddyram.rframe.Pose3D;
import com.buddyram.rframe.RobotException;
import com.buddyram.rframe.Vector3D;
import com.buddyram.rframe.ftc.v2.BotUtilsNew;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

@Autonomous(name = "OTOS Square Test", group = "Diagnostic")
public class OTOSSquareTest extends BaseOpmode {

    private static final double LEG = 48.0;

    @Override
    public void execute() throws RobotException, InterruptedException {
        telemetry.addLine("=== OTOS Square Test ===");
        telemetry.addLine("Back 48 -> Right 48 -> Forward 48 -> Left 48");
        telemetry.addLine("All relative to current heading");
        telemetry.update();
        Thread.sleep(2000);

        // Read actual starting pose from OTOS
        Pose3D start = this.decodeBot.getOdometry().get();
        double sx = start.position.x;
        double sy = start.position.y;
        int heading = (int) Math.round(start.rotation.z);
        double h = Math.toRadians(heading);

        // Forward = (-sin(h), cos(h)), Right = (cos(h), sin(h)) in field coords
        // (heading 0 = +Y, increasing CCW)
        double fwdX = -Math.sin(h);
        double fwdY = Math.cos(h);
        double rightX = Math.cos(h);
        double rightY = Math.sin(h);

        telemetry.addData("Start", "(%.2f, %.2f) @ %d°", sx, sy, heading);
        telemetry.update();
        Thread.sleep(1000);

        // Leg 1: Back 48"
        double c1x = sx - fwdX * LEG;
        double c1y = sy - fwdY * LEG;
        telemetry.addLine("--- Leg 1: Back 48\" ---");
        telemetry.update();
        BotUtilsNew.driveAndRotateTo(new Vector3D(c1x, c1y, 0), heading).run(this.decodeBot);
        logPosition("After Back", c1x, c1y, heading);
        Thread.sleep(1000);

        // Leg 2: Right 48"
        double c2x = c1x + rightX * LEG;
        double c2y = c1y + rightY * LEG;
        telemetry.addLine("--- Leg 2: Right 48\" ---");
        telemetry.update();
        BotUtilsNew.driveAndRotateTo(new Vector3D(c2x, c2y, 0), heading).run(this.decodeBot);
        logPosition("After Right", c2x, c2y, heading);
        Thread.sleep(1000);

        // Leg 3: Forward 48"
        double c3x = c2x + fwdX * LEG;
        double c3y = c2y + fwdY * LEG;
        telemetry.addLine("--- Leg 3: Forward 48\" ---");
        telemetry.update();
        BotUtilsNew.driveAndRotateTo(new Vector3D(c3x, c3y, 0), heading).run(this.decodeBot);
        logPosition("After Forward", c3x, c3y, heading);
        Thread.sleep(1000);

        // Leg 4: Left 48" (back to start)
        double c4x = c3x - rightX * LEG;
        double c4y = c3y - rightY * LEG;
        telemetry.addLine("--- Leg 4: Left 48\" (return) ---");
        telemetry.update();
        BotUtilsNew.driveAndRotateTo(new Vector3D(c4x, c4y, 0), heading).run(this.decodeBot);
        logPosition("After Left", c4x, c4y, heading);

        // Final error report
        Pose3D end = this.decodeBot.getOdometry().get();
        double errorX = end.position.x - sx;
        double errorY = end.position.y - sy;
        double totalError = Math.sqrt(errorX * errorX + errorY * errorY);
        double headingError = end.rotation.z - heading;

        telemetry.addLine("\n=== FINAL RESULTS ===");
        telemetry.addData("Start", "(%.2f, %.2f)", sx, sy);
        telemetry.addData("End", "(%.2f, %.2f)", end.position.x, end.position.y);
        telemetry.addData("X Error", "%.2f\"", errorX);
        telemetry.addData("Y Error", "%.2f\"", errorY);
        telemetry.addData("Total Error", "%.2f\"", totalError);
        telemetry.addData("Heading Error", "%.2f°", headingError);
        telemetry.addData("Error %%", "%.2f%% of 192\"", (totalError / 192.0) * 100);
        telemetry.update();

        while (opModeIsActive()) {
            Thread.sleep(100);
        }
    }

    private void logPosition(String label, double expectedX, double expectedY, int expectedHeading) {
        Pose3D current = this.decodeBot.getOdometry().get();
        double errorX = current.position.x - expectedX;
        double errorY = current.position.y - expectedY;
        double errorTotal = Math.sqrt(errorX * errorX + errorY * errorY);

        telemetry.addData(label + " expected", "(%.2f, %.2f)", expectedX, expectedY);
        telemetry.addData(label + " actual", "(%.2f, %.2f) @ %.1f°",
            current.position.x, current.position.y, current.rotation.z);
        telemetry.addData(label + " error", "X:%.2f Y:%.2f Total:%.2f",
            errorX, errorY, errorTotal);
        telemetry.update();
    }
}
