package com.buddyram.rframe.ftc.biobuzz.opmodes;

import com.buddyram.rframe.drive.HolonomicDriveInstruction;
import com.buddyram.rframe.RobotException;
import com.buddyram.rframe.Utils;
import com.buddyram.rframe.Vector3D;
import com.buddyram.rframe.ftc.biobuzz.BotUtilsNew;
import com.buddyram.rframe.ftc.biobuzz.Globals;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

/**
 * Basic auto: drive from the start corner to one shooting spot, shoot the preloads, then park.
 * Feeds only while the shot is ready (flywheel in band, 150 ms after the last ball, turret on, robot still),
 * same as Teleop's right bumper.
 */
@Autonomous(name = "BIOBUZZ Auto Shoot", group = "Biobuzz")
public class AutoShoot extends BaseOpmode {
    // Blue positions. Red is the same field turned 180 degrees around the center (see BaseOpmode.startFor),
    // so these get rotated for red, not mirrored like the old DECODE autos.
    private static final Vector3D SHOOT_POS = new Vector3D(114, 30, 0); // ~44 in from the blue scoring-side CELL
    private static final double SHOOT_HEADING = 90;                     // same as the start, turret aims ~-43 deg
    private static final Vector3D PARK_POS = new Vector3D(114, 12, 0);

    private static final int MAX_BALLS = 3;
    private static final int NO_BALL_TIMEOUT_MS = 2500; // stop if feeding this long with no ball leaving
    private static final int SHOOT_TIMEOUT_MS = 8000;
    private static final int SPIN_UP_TIMEOUT_MS = 3000;

    @Override
    public void execute() throws RobotException, InterruptedException {
        Globals.DID_RUN_AUTO = true;
        boolean isRed = biobuzzBot.isRed;

        // Aim while driving so the turret is already there on arrival
        biobuzzBot.preloadTurretForPosition(forAlliance(SHOOT_POS, isRed), forAlliance(SHOOT_HEADING, isRed));
        BotUtilsNew.driveAndRotateTo(forAlliance(SHOOT_POS, isRed), forAlliance(SHOOT_HEADING, isRed), 2).run(biobuzzBot);
        biobuzzBot.getDrive().drive(new HolonomicDriveInstruction(0, 0, 0));

        // Lock rpm/hood to where we actually stopped, then switch the turret to live aim
        biobuzzBot.overrideDistance = biobuzzBot.getOdometry().get().position.distance(biobuzzBot.getTargetGoal());
        biobuzzBot.overrideAngle = null;

        if (biobuzzBot.getLauncher() != null) shoot();

        biobuzzBot.overrideDistance = null;
        BotUtilsNew.driveAndRotateTo(forAlliance(PARK_POS, isRed), forAlliance(SHOOT_HEADING, isRed)).run(biobuzzBot);
        biobuzzBot.getDrive().drive(new HolonomicDriveInstruction(0, 0, 0));
    }

    private void shoot() {
        long start = System.currentTimeMillis();
        while (opModeIsActive() && !biobuzzBot.isReadyToShoot() && System.currentTimeMillis() - start < SPIN_UP_TIMEOUT_MS) {
            showStatus("spinning up / aiming");
            sleep(10);
        }

        int startBalls = biobuzzBot.getLauncher().wheel.getBallCount();
        long feedStart = System.currentTimeMillis();
        long lastBall = feedStart;
        int lastCount = startBalls;
        while (opModeIsActive()) {
            int count = biobuzzBot.getLauncher().wheel.getBallCount();
            long now = System.currentTimeMillis();
            if (count != lastCount) {
                lastCount = count;
                lastBall = now;
            }
            if (count - startBalls >= MAX_BALLS || now - lastBall > NO_BALL_TIMEOUT_MS || now - feedStart > SHOOT_TIMEOUT_MS) break;

            boolean ready = biobuzzBot.isReadyToShoot();
            biobuzzBot.block = !ready;
            biobuzzBot.jamFix = ready;
            showStatus("shooting, " + (count - startBalls) + " balls");
            sleep(5);
        }
        biobuzzBot.block = true;
        biobuzzBot.jamFix = false;
    }

    private void showStatus(String state) {
        telemetry.addData("auto", state);
        for (String reason : biobuzzBot.notReadyReasons()) telemetry.addLine(">> NOT READY: " + reason);
        telemetry.update();
    }

    private static Vector3D forAlliance(Vector3D bluePos, boolean isRed) {
        return isRed ? new Vector3D(144 - bluePos.x, 144 - bluePos.y, 0) : bluePos;
    }

    private static double forAlliance(double blueHeading, boolean isRed) {
        return isRed ? Utils.normalizeAngle(blueHeading + 180) : blueHeading;
    }
}
