package com.buddyram.rframe.ftc.v2.opmodes;

import com.buddyram.rframe.Pose3D;
import com.buddyram.rframe.RobotException;
import com.buddyram.rframe.Vector3D;
import com.buddyram.rframe.ftc.v2.BotUtilsNew;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

@Autonomous(name = "OTOS Axis Test", group = "Diagnostic")
public class OTOSAxisTest extends BaseOpmode {

    private static final double TEST_DISTANCE = 48.0;

    @Override
    public void execute() throws RobotException, InterruptedException {
        telemetry.addLine("=== OTOS Axis Test ===");
        telemetry.addLine("Tests forward/back and right/left separately");
        telemetry.addLine("All relative to current heading");
        telemetry.update();
        Thread.sleep(3000);

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
        Thread.sleep(2000);

        // TEST 1: Forward/Back axis
        telemetry.addLine("\n=== TEST 1: FORWARD/BACK ===");
        telemetry.update();
        Thread.sleep(1000);

        // Forward 48"
        double fTargetX = sx + fwdX * TEST_DISTANCE;
        double fTargetY = sy + fwdY * TEST_DISTANCE;
        telemetry.addLine("Moving FORWARD 48\"...");
        telemetry.update();
        BotUtilsNew.driveAndRotateTo(new Vector3D(fTargetX, fTargetY, 0), heading).run(this.decodeBot);
        Pose3D afterForward = this.decodeBot.getOdometry().get();
        double fwdErrorX = afterForward.position.x - fTargetX;
        double fwdErrorY = afterForward.position.y - fTargetY;
        double fwdError = Math.sqrt(fwdErrorX * fwdErrorX + fwdErrorY * fwdErrorY);
        telemetry.addData("After Forward", "(%.2f, %.2f)", afterForward.position.x, afterForward.position.y);
        telemetry.addData("Error from target", "%.2f\"", fwdError);
        telemetry.update();
        Thread.sleep(2000);

        // Back to start
        telemetry.addLine("Moving BACK to start...");
        telemetry.update();
        BotUtilsNew.driveAndRotateTo(new Vector3D(sx, sy, 0), heading).run(this.decodeBot);
        Pose3D afterBack = this.decodeBot.getOdometry().get();
        double backErrorX = afterBack.position.x - sx;
        double backErrorY = afterBack.position.y - sy;
        double backError = Math.sqrt(backErrorX * backErrorX + backErrorY * backErrorY);
        telemetry.addData("After Back", "(%.2f, %.2f)", afterBack.position.x, afterBack.position.y);
        telemetry.addData("Error from start", "%.2f\"", backError);
        telemetry.addData("Fwd/Back Total Error", "%.2f\" over 96\"", backError);
        telemetry.update();
        Thread.sleep(3000);

        // TEST 2: Right/Left axis
        telemetry.addLine("\n=== TEST 2: RIGHT/LEFT ===");
        telemetry.update();
        Thread.sleep(1000);

        // Right 48"
        double rTargetX = sx + rightX * TEST_DISTANCE;
        double rTargetY = sy + rightY * TEST_DISTANCE;
        telemetry.addLine("Moving RIGHT 48\"...");
        telemetry.update();
        BotUtilsNew.driveAndRotateTo(new Vector3D(rTargetX, rTargetY, 0), heading).run(this.decodeBot);
        Pose3D afterRight = this.decodeBot.getOdometry().get();
        double rightErrorX = afterRight.position.x - rTargetX;
        double rightErrorY = afterRight.position.y - rTargetY;
        double rightError = Math.sqrt(rightErrorX * rightErrorX + rightErrorY * rightErrorY);
        telemetry.addData("After Right", "(%.2f, %.2f)", afterRight.position.x, afterRight.position.y);
        telemetry.addData("Error from target", "%.2f\"", rightError);
        telemetry.update();
        Thread.sleep(2000);

        // Left back to start
        telemetry.addLine("Moving LEFT to start...");
        telemetry.update();
        BotUtilsNew.driveAndRotateTo(new Vector3D(sx, sy, 0), heading).run(this.decodeBot);
        Pose3D afterLeft = this.decodeBot.getOdometry().get();
        double leftErrorX = afterLeft.position.x - sx;
        double leftErrorY = afterLeft.position.y - sy;
        double leftError = Math.sqrt(leftErrorX * leftErrorX + leftErrorY * leftErrorY);
        telemetry.addData("After Left", "(%.2f, %.2f)", afterLeft.position.x, afterLeft.position.y);
        telemetry.addData("Error from start", "%.2f\"", leftError);
        telemetry.addData("Right/Left Total Error", "%.2f\" over 96\"", leftError);
        telemetry.update();
        Thread.sleep(3000);

        // FINAL ANALYSIS
        Pose3D finalPose = this.decodeBot.getOdometry().get();
        double headingError = finalPose.rotation.z - heading;

        telemetry.addLine("\n=== FINAL ANALYSIS ===");
        telemetry.addData("Fwd/Back Error", "%.2f\"", backError);
        telemetry.addData("Right/Left Error", "%.2f\"", leftError);
        telemetry.addData("Heading Error", "%.2f°", headingError);
        telemetry.addLine("");

        if (leftError > backError * 1.5) {
            telemetry.addLine("RIGHT/LEFT (strafe) is significantly worse");
        } else if (backError > leftError * 1.5) {
            telemetry.addLine("FORWARD/BACK is significantly worse");
        } else {
            telemetry.addLine("Both axes have similar error");
        }

        if (Math.abs(headingError) > 3) {
            telemetry.addLine("WARNING: Significant heading drift");
        }

        telemetry.update();

        while (opModeIsActive()) {
            Thread.sleep(100);
        }
    }
}
