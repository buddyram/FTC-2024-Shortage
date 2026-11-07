package com.buddyram.rframe.ftc.biobuzz.opmodes;

import com.buddyram.rframe.Pose3D;
import com.buddyram.rframe.RobotException;
import com.buddyram.rframe.Vector3D;
import com.buddyram.rframe.drive.HolonomicDriveInstruction;
import com.buddyram.rframe.ftc.biobuzz.Globals;
import com.buddyram.rframe.ftc.biobuzz.Robot.launcher.Flywheel;
import com.buddyram.rframe.ftc.biobuzz.Robot.launcher.Launcher;
import com.buddyram.rframe.ftc.biobuzz.Robot.launcher.Turret;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

/**
 * ManualTest's controls on the full BiobuzzBot / BaseOpmode stack.
 * BaseOpmode's background thread still runs autoAim, adjustFlywheelSpeed and controlIntake;
 * this opmode only steers them through the bot's override / offset fields.
 * SHARE switches the flywheel and hood between manual (like ManualTest) and distance-based (like Teleop).
 */
@TeleOp(name = "BIOBUZZ Manual Teleop", group = "Biobuzz")
public class ManualTeleop extends BaseOpmode {
    private static final double TURRET_DEG_PER_S = 90.0;
    private static final double RPM_PER_S = 1000.0;
    private static final double HOOD_PER_S = 0.25;
    private static final int FLYWHEEL_OFF = -1000000; // same as Teleop's triangle, clamps the RPM to 0

    @Override
    public void execute() throws RobotException, InterruptedException {
        Globals.DID_RUN_AUTO = false;
        Gamepad currentGamepad1 = new Gamepad();
        Gamepad previousGamepad1 = new Gamepad();
        currentGamepad1.copy(gamepad1); // so buttons held from init don't toggle on the first loop
        Launcher launcher = this.biobuzzBot.launcher;
        boolean hasTurret = launcher != null && launcher.turret != null;
        boolean hasFlywheel = launcher != null && launcher.wheel != null;

        // Start position comes from BaseOpmode: carried over from auto / the last opmode, else the start corner.f

        boolean autoAim = true;
        boolean autoShooter = false;
        boolean flywheelOn = false;
        boolean intakeOn = false;
        double manualTurret = 0;
        double rpm = Flywheel.DEFAULT_RPM;
        double hoodPos = BaseOpmode.START_HOOD;
        double rpmOffset = 0;
        this.biobuzzBot.aimOn = true;
        ElapsedTime timer = new ElapsedTime();
        while (this.biobuzzBot.isActive()) {
            previousGamepad1.copy(currentGamepad1);
            currentGamepad1.copy(gamepad1);
            double dt = timer.seconds();
            timer.reset();

            // Odometry resets
            if (currentGamepad1.cross && !previousGamepad1.cross) {
                this.biobuzzBot.odometry.setPosition(BaseOpmode.ZERO);
            }

            // Turret: auto-aim with trigger trim, or fully manual through overrideAngle
            if (currentGamepad1.share && !previousGamepad1.share) {
                autoAim = !autoAim;
                // Pick up manual aim from wherever the turret is now so it doesn't jump
                if (!autoAim && hasTurret) manualTurret = launcher.turret.getCurrentAngle();
            }
            double triggers = currentGamepad1.left_trigger - currentGamepad1.right_trigger;
            if (autoAim) {
                this.biobuzzBot.overrideAngle = null;
                this.biobuzzBot.turretOffset += triggers * TURRET_DEG_PER_S * dt;
                if (currentGamepad1.options) this.biobuzzBot.turretOffset = 0;
            } else {
                manualTurret = Range.clip(manualTurret + triggers * TURRET_DEG_PER_S * dt, Turret.MIN_ANGLE, Turret.MAX_ANGLE);
                if (currentGamepad1.options) manualTurret = 0;
                this.biobuzzBot.overrideAngle = manualTurret;
            }

            if (currentGamepad1.circle && !previousGamepad1.circle) {
                intakeOn = !intakeOn;
            }
            // Flywheel / hood: D-pad sets them directly in manual, or trims the distance-based values in auto
            if (currentGamepad1.share && !previousGamepad1.share) autoShooter = !autoShooter;
            if (currentGamepad1.triangle && !previousGamepad1.triangle) flywheelOn = !flywheelOn;
            double rpmStep = ((currentGamepad1.dpad_up ? 1 : 0) - (currentGamepad1.dpad_down ? 1 : 0)) * RPM_PER_S * dt;
            double hoodStep = ((currentGamepad1.dpad_right ? 1 : 0) - (currentGamepad1.dpad_left ? 1 : 0)) * HOOD_PER_S * dt;
            if (autoShooter) {
                rpmOffset += rpmStep;
                this.biobuzzBot.hoodOffset += hoodStep;
                if (currentGamepad1.options) {
                    rpmOffset = 0;
                    this.biobuzzBot.hoodOffset = 0;
                }
                this.biobuzzBot.overrideRPM = null;
                this.biobuzzBot.overrideHood = null;
                this.biobuzzBot.speed = flywheelOn ? (int) rpmOffset : FLYWHEEL_OFF;
            } else {
                rpm = Range.clip(rpm + rpmStep, Flywheel.MIN, Flywheel.MAX);
                hoodPos = Range.clip(hoodPos + hoodStep, 0, 1);
                this.biobuzzBot.overrideRPM = flywheelOn ? rpm : 0.0;
                this.biobuzzBot.overrideHood = hoodPos;
            }

            // Holding R bumper only feeds while the shot is ready, so every ball leaves at the same speed
            boolean ready = rumbleWhenReadyToShoot();
            boolean feeding = flywheelOn && currentGamepad1.right_bumper && ready;
            this.biobuzzBot.block = !feeding;
            this.biobuzzBot.jamFix = intakeOn || feeding;
            this.biobuzzBot.fastFeed = feeding;

            runDrive(currentGamepad1);

            // Telemetry
            Pose3D pose = this.biobuzzBot.odometry.get();
            Vector3D goal = this.biobuzzBot.getTargetGoal();
            double dist = pose.position.distance(goal);
            double aimAngle = this.biobuzzBot.currentTurretAngle();
            telemetry.addData(">> DISTANCE TO GOAL", "%.1f in (%.2f ft)", dist, dist / 12);
            telemetry.addData(">> AIMING AT", "%s HIVE, %s side CELL (%.1f, %.1f)", this.biobuzzBot.isRed ? "RED" : "BLUE",
                    this.biobuzzBot.isAimingAtScoringCell() ? "scoring" : "audience", goal.x, goal.y);
            telemetry.addLine("share aim on/off, SHARE flywheel+hood auto/manual, TRIANGLE flywheel on/off");
            telemetry.addLine("OPTIONS center turret / clear trims");
            telemetry.addData("alliance", this.biobuzzBot.isRed ? "RED" : "BLUE");
            telemetry.addData("pos x / y (in)", "%.2f / %.2f", pose.position.x, pose.position.y);
            telemetry.addData("heading (deg)", "%.2f", pose.rotation.z);
            telemetry.addData("auto-aim", autoAim ? "ON (share)" : "off (share)");
            String reach = aimAngle < Turret.MIN_ANGLE || aimAngle > Turret.MAX_ANGLE ? " OUT OF RANGE" : "";
            telemetry.addData("auto-aim angle (deg)", "%.1f%s", aimAngle, reach);
            telemetry.addData("auto-aim offset (deg)", "%.1f", this.biobuzzBot.turretOffset);
            if (hasTurret) {
                telemetry.addData("turret target / current (deg)", "%.1f / %.1f",
                        autoAim ? Range.clip(aimAngle, Turret.MIN_ANGLE, Turret.MAX_ANGLE) : manualTurret,
                        launcher.turret.getCurrentAngle());
            }
            telemetry.addData("flywheel + hood", autoShooter ? "AUTO from distance (SHARE)" : "manual (SHARE)");
            if (hasFlywheel) {
                telemetry.addData("flywheel", flywheelOn ? (launcher.wheel.isReady() ? "ON - ready" : "ON - spinning up") : "off (triangle)");
                telemetry.addData("flywheel set / actual (RPM)", "%.0f / %.0f", launcher.wheel.getTarget(), launcher.wheel.getRPM());
                if (autoShooter) telemetry.addData("RPM offset", "%.0f", rpmOffset);
            }
            if (autoShooter) {
                telemetry.addData("hood (offset)", "%.3f (%.3f)",
                        this.biobuzzBot.calculateHoodAngle(dist) + this.biobuzzBot.hoodOffset, this.biobuzzBot.hoodOffset);
            } else {
                telemetry.addData("hood", "%.3f", hoodPos);
            }
            telemetry.addData("feed", feeding ? "SHOOTING" : currentGamepad1.right_bumper ? "waiting until ready"
                    : intakeOn ? "intaking" : "idle");
            telemetry.addData("balls shot", launcher.wheel.getBallCount());
            telemetry.update();
        }
    }

    // Robot-relative stick drive, same as ManualTest
    private void runDrive(Gamepad gp) throws RobotException {
        double speed = gp.left_stick_button || gp.right_stick_button ? 1 : 0.8;
        double speedLevel = Math.hypot(gp.left_stick_x, gp.left_stick_y);
        robotCentricDrive.drive(new HolonomicDriveInstruction(
                gp.right_stick_x * speed,
                speed * speedLevel,
                Math.toDegrees(Math.atan2(-gp.left_stick_y, gp.left_stick_x))
        ));
    }
}
