package com.buddyram.rframe.ftc.biobuzz.opmodes;

import com.buddyram.rframe.Pose3D;
import com.buddyram.rframe.RobotException;
import com.buddyram.rframe.Vector3D;
import com.buddyram.rframe.drive.HolonomicDriveInstruction;
import com.buddyram.rframe.ftc.biobuzz.BotUtilsNew;
import com.buddyram.rframe.ftc.biobuzz.Globals;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Gamepad;


@TeleOp(name = "TELEOP - BIOBUZZ/V2", group = "Biobuzz")
public class Teleop extends BaseOpmode {
    private boolean adjustmentMode = false;
    private Gamepad previousGamepad1 = new Gamepad();
    private long dpadHoldStartLR = 0;
    private long lastAdjustTimeLR = 0;
    private long dpadHoldStartUD = 0;
    private long lastAdjustTimeUD = 0;
    private static final long INITIAL_DELAY_MS = 400;
    private static final long REPEAT_MS = 80;
//    private double intakeHeight = 0.5;

    @Override
    public void execute() throws RobotException, InterruptedException {
        Gamepad currentGamepad1 = new Gamepad();
        Globals.DID_RUN_AUTO = false;
        while (this.biobuzzBot.isActive()) {
            previousGamepad1.copy(currentGamepad1);
            currentGamepad1.copy(gamepad1);

            // Always active controls
            this.biobuzzBot.block = !gamepad1.right_bumper;
            this.biobuzzBot.jamFix = gamepad1.left_bumper || gamepad1.right_bumper;
            colorRumbleFlywheel(currentGamepad1);

            if (currentGamepad1.square) {
                this.biobuzzBot.speed = 0;
            }
            if (currentGamepad1.circle) {
                this.biobuzzBot.speed = -1000000;
            }
            if (currentGamepad1.triangle) {
                this.biobuzzBot.odometry.setPosition(
                    BotUtilsNew.mirrorIfRed(
                        new Pose3D(
                            new Vector3D(7.98, 8.94, 0),
                            new Vector3D(),
                            new Vector3D(),
                            new Vector3D()
                        ),
                        !this.biobuzzBot.isRed
                    )
                );
            }
            if (currentGamepad1.cross) {
                this.biobuzzBot.overrideDistance = this.biobuzzBot.overrideDistance == null ? 67.8822509939 : null;
                this.biobuzzBot.overrideAngle = this.biobuzzBot.overrideAngle == null ? 0.0 : null;
            }

            // Toggle adjustment mode on OPTIONS press
            if (currentGamepad1.options && !previousGamepad1.options) {
                adjustmentMode = !adjustmentMode;
            }

            if (adjustmentMode) {
                runAdjustmentMode(currentGamepad1);
                // Joystick driving still works in adjustment mode
                double speed = 0.8;
                if (currentGamepad1.left_stick_button || currentGamepad1.right_stick_button) speed = 1;
                double speedLevel = Math.sqrt(Math.pow(currentGamepad1.left_stick_x, 2) + Math.pow(currentGamepad1.left_stick_y, 2));
                biobuzzBot.getDrive().drive(new HolonomicDriveInstruction(
                        currentGamepad1.right_stick_x * speed,
                        speed * speedLevel,
                        Math.toDegrees(Math.atan2(-currentGamepad1.left_stick_y, currentGamepad1.left_stick_x)) + this.biobuzzBot.odometry.get().rotation.z
                ));
                this.biobuzzBot.aimOn = true;
            } else {
                this.biobuzzBot.turretOffset += (gamepad1.right_trigger - gamepad1.left_trigger);
                telemetry.addData("turretOffset", this.biobuzzBot.turretOffset);
                telemetry.addData("hoodOffset", this.biobuzzBot.hoodOffset);
                runDriveControls(currentGamepad1);
                telemetry.addData("distance", this.biobuzzBot.odometry.get().position.distance(this.biobuzzBot.targetGoal));
                if (this.biobuzzBot.launcher != null && this.biobuzzBot.launcher.turret != null) {
                    Pose3D pose = this.biobuzzBot.odometry.get();
                    telemetry.addData("turret aim (deg)", "%.1f", this.biobuzzBot.calculateTurretAngle(pose.position, pose.rotation.z));
                    telemetry.addData("turret current (deg)", "%.1f", this.biobuzzBot.launcher.turret.getCurrentAngle());
                }
                telemetry.addData("OTOS Position", this.biobuzzBot.getOdometry().get());
//                telemetry.addData("Speed", this.biobuzzBot.getLauncher().wheel.getRPM());
            }

            telemetry.update();
        }
    }

    private void runAdjustmentMode(Gamepad gp) {
        telemetry.addData("== ADJUSTMENT MODE ==", "OPTIONS to exit");
        telemetry.addData("Left/Right", "Turret offset (1 deg/tap)");
        telemetry.addData("Up/Down", "Hood offset (0.01/tap)");
        telemetry.addData("Turret Offset", String.format("%.1f deg", biobuzzBot.turretOffset));
        telemetry.addData("Hood Offset", String.format("%.3f", biobuzzBot.hoodOffset));
        telemetry.addData("OTOS Position", this.biobuzzBot.getOdometry().get());

        long now = System.currentTimeMillis();

        // Turret offset: left/right d-pad
        boolean turretPressed = gp.dpad_left || gp.dpad_right;
        boolean turretWasPressed = previousGamepad1.dpad_left || previousGamepad1.dpad_right;
        if (turretPressed) {
            double dir = gp.dpad_right ? 1 : -1;
            if (!turretWasPressed) {
                biobuzzBot.turretOffset = Math.max(-180, Math.min(180, biobuzzBot.turretOffset + dir));
                dpadHoldStartLR = now;
                lastAdjustTimeLR = now;
            } else if (now - dpadHoldStartLR > INITIAL_DELAY_MS && now - lastAdjustTimeLR > REPEAT_MS) {
                biobuzzBot.turretOffset = Math.max(-180, Math.min(180, biobuzzBot.turretOffset + dir));
                lastAdjustTimeLR = now;
            }
        }

        // Hood offset: up/down d-pad
        boolean hoodPressed = gp.dpad_up || gp.dpad_down;
        boolean hoodWasPressed = previousGamepad1.dpad_up || previousGamepad1.dpad_down;
        if (hoodPressed) {
            double dir = gp.dpad_up ? 0.01 : -0.01;
            if (!hoodWasPressed) {
                biobuzzBot.hoodOffset += dir;
                dpadHoldStartUD = now;
                lastAdjustTimeUD = now;
            } else if (now - dpadHoldStartUD > INITIAL_DELAY_MS && now - lastAdjustTimeUD > REPEAT_MS) {
                biobuzzBot.hoodOffset += dir;
                lastAdjustTimeUD = now;
            }
        }
    }

    private void colorRumbleFlywheel(Gamepad currentGamepad1) {
        if (this.biobuzzBot.getLauncher().wheel.isReady()) {
            gamepad1.setLedColor(0, 255 ,0, 100);
        } else {
            gamepad1.rumble(100);

            gamepad1.setLedColor(255, 0 ,0, 100);
        }
    }

    private void runDriveControls(Gamepad currentGamepad1) throws RobotException {
        double speed = 0.8;
        if (currentGamepad1.left_stick_button || currentGamepad1.right_stick_button) {
            speed = 1;
        }

        if (currentGamepad1.dpad_up) {
            this.biobuzzBot.getDrive().drive(this.biobuzzBot.calculateRelativeDriveInstruction(new Vector3D(0, 1, 0), speed));
        } else if (currentGamepad1.dpad_down) {
            this.biobuzzBot.getDrive().drive(this.biobuzzBot.calculateRelativeDriveInstruction(new Vector3D(0, -1, 0), speed));
        } else if (currentGamepad1.dpad_right) {
            this.biobuzzBot.getDrive().drive(this.biobuzzBot.calculateRelativeDriveInstruction(new Vector3D(1, 0, 0), speed));
        } else if (currentGamepad1.dpad_left) {
            this.biobuzzBot.getDrive().drive(this.biobuzzBot.calculateRelativeDriveInstruction(new Vector3D(-1, 0, 0), speed));
        } else {
            double speedLevel = Math.sqrt(Math.pow(currentGamepad1.left_stick_x, 2) + Math.pow(currentGamepad1.left_stick_y, 2));
            biobuzzBot.getDrive().drive(new HolonomicDriveInstruction(
                    currentGamepad1.right_stick_x * speed,
                    speed * speedLevel,
                    Math.toDegrees(Math.atan2(-currentGamepad1.left_stick_y, currentGamepad1.left_stick_x)) + this.biobuzzBot.odometry.get().rotation.z
            ));
        }
        this.biobuzzBot.aimOn = true;
    }
}
