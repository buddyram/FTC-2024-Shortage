package com.buddyram.rframe.ftc.v2.opmodes;

import com.buddyram.rframe.Pose3D;
import com.buddyram.rframe.RobotException;
import com.buddyram.rframe.Vector3D;
import com.buddyram.rframe.drive.HolonomicDriveInstruction;
import com.buddyram.rframe.ftc.v2.BotUtilsNew;
import com.buddyram.rframe.ftc.v2.Globals;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Gamepad;

@Disabled
@TeleOp(name = "TELEOP - DECODE/V2", group = "Decode")
public class Teleop extends BaseOpmode {
    private boolean adjustmentMode = false;
    private Gamepad previousGamepad1 = new Gamepad();
    private long dpadHoldStartLR = 0;
    private long lastAdjustTimeLR = 0;
    private long dpadHoldStartUD = 0;
    private long lastAdjustTimeUD = 0;
    private static final long INITIAL_DELAY_MS = 400;
    private static final long REPEAT_MS = 80;

    @Override
    protected Pose3D getOTOSOverridePosition() {
        if (Globals.POSITION != null) {
            telemetry.addData("OTOS Position", "SQUARE = Reset (72,72), TRIANGLE = Keep (" +
                String.format("%.1f, %.1f, h%.1f", Globals.POSITION.position.x, Globals.POSITION.position.y, Globals.POSITION.rotation.z) + ")");
        } else {
            telemetry.addData("OTOS Position", "SQUARE = Reset (72,72) | No cached position");
        }
        telemetry.update();
        while (true) {
            if (gamepad1.square) {
                return new Pose3D(new Vector3D(72, 72, 0), new Vector3D(), new Vector3D(), new Vector3D());
            }
            if (gamepad1.triangle && Globals.POSITION != null) {
                return Globals.POSITION;
            }
        }
    }

    @Override
    public void execute() throws RobotException, InterruptedException {
        Gamepad currentGamepad1 = new Gamepad();
        Globals.DID_RUN_AUTO = false;
        while (this.decodeBot.isActive()) {
            previousGamepad1.copy(currentGamepad1);
            currentGamepad1.copy(gamepad1);

            // Always active controls
            this.decodeBot.block = !gamepad1.right_bumper;
            this.decodeBot.jamFix = gamepad1.left_bumper || gamepad1.right_bumper;
            colorRumbleFlywheel(currentGamepad1);

            if (currentGamepad1.square) {
                this.decodeBot.speed = 0;
            }
            if (currentGamepad1.circle) {
                this.decodeBot.speed = -1000000;
            }
            if (currentGamepad1.triangle) {
                this.decodeBot.odometry.setPosition(
                    BotUtilsNew.mirrorIfRed(
                        new Pose3D(
                            new Vector3D(7.98, 8.94, 0),
                            new Vector3D(),
                            new Vector3D(),
                            new Vector3D()
                        ),
                        !this.decodeBot.isRed
                    )
                );
            }
            if (currentGamepad1.cross) {
                this.decodeBot.overrideDistance = this.decodeBot.overrideDistance == null ? 67.8822509939 : null;
                this.decodeBot.overrideAngle = this.decodeBot.overrideAngle == null ? 0.0 : null;
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
                decodeBot.getDrive().drive(new HolonomicDriveInstruction(
                        currentGamepad1.right_stick_x * speed,
                        speed * speedLevel,
                        Math.toDegrees(Math.atan2(-currentGamepad1.left_stick_y, currentGamepad1.left_stick_x)) + this.decodeBot.odometry.get().rotation.z
                ));
                this.decodeBot.aimOn = true;
            } else {
                this.decodeBot.turretOffset += (gamepad1.right_trigger - gamepad1.left_trigger);
                telemetry.addData("turretOffset", this.decodeBot.turretOffset);
                telemetry.addData("hoodOffset", this.decodeBot.hoodOffset);
                runDriveControls(currentGamepad1);
                telemetry.addData("distance", this.decodeBot.odometry.get().position.distance(this.decodeBot.targetGoal));
                telemetry.addData("OTOS Position", this.decodeBot.getOdometry().get());
                telemetry.addData("Speed", this.decodeBot.getLauncher().wheel.getRPM());
            }

            telemetry.update();
        }
    }

    private void runAdjustmentMode(Gamepad gp) {
        telemetry.addData("== ADJUSTMENT MODE ==", "OPTIONS to exit");
        telemetry.addData("Left/Right", "Turret offset (1 deg/tap)");
        telemetry.addData("Up/Down", "Hood offset (0.01/tap)");
        telemetry.addData("Turret Offset", String.format("%.1f deg", decodeBot.turretOffset));
        telemetry.addData("Hood Offset", String.format("%.3f", decodeBot.hoodOffset));
        telemetry.addData("OTOS Position", this.decodeBot.getOdometry().get());

        long now = System.currentTimeMillis();

        // Turret offset: left/right d-pad
        boolean turretPressed = gp.dpad_left || gp.dpad_right;
        boolean turretWasPressed = previousGamepad1.dpad_left || previousGamepad1.dpad_right;
        if (turretPressed) {
            double dir = gp.dpad_right ? 1 : -1;
            if (!turretWasPressed) {
                decodeBot.turretOffset = Math.max(-180, Math.min(180, decodeBot.turretOffset + dir));
                dpadHoldStartLR = now;
                lastAdjustTimeLR = now;
            } else if (now - dpadHoldStartLR > INITIAL_DELAY_MS && now - lastAdjustTimeLR > REPEAT_MS) {
                decodeBot.turretOffset = Math.max(-180, Math.min(180, decodeBot.turretOffset + dir));
                lastAdjustTimeLR = now;
            }
        }

        // Hood offset: up/down d-pad
        boolean hoodPressed = gp.dpad_up || gp.dpad_down;
        boolean hoodWasPressed = previousGamepad1.dpad_up || previousGamepad1.dpad_down;
        if (hoodPressed) {
            double dir = gp.dpad_up ? 0.01 : -0.01;
            if (!hoodWasPressed) {
                decodeBot.hoodOffset += dir;
                dpadHoldStartUD = now;
                lastAdjustTimeUD = now;
            } else if (now - dpadHoldStartUD > INITIAL_DELAY_MS && now - lastAdjustTimeUD > REPEAT_MS) {
                decodeBot.hoodOffset += dir;
                lastAdjustTimeUD = now;
            }
        }
    }

    private void colorRumbleFlywheel(Gamepad currentGamepad1) {
        if (this.decodeBot.getLauncher().wheel.isReady()) {
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
            this.decodeBot.getDrive().drive(this.decodeBot.calculateRelativeDriveInstruction(new Vector3D(0, 1, 0), speed));
        } else if (currentGamepad1.dpad_down) {
            this.decodeBot.getDrive().drive(this.decodeBot.calculateRelativeDriveInstruction(new Vector3D(0, -1, 0), speed));
        } else if (currentGamepad1.dpad_right) {
            this.decodeBot.getDrive().drive(this.decodeBot.calculateRelativeDriveInstruction(new Vector3D(1, 0, 0), speed));
        } else if (currentGamepad1.dpad_left) {
            this.decodeBot.getDrive().drive(this.decodeBot.calculateRelativeDriveInstruction(new Vector3D(-1, 0, 0), speed));
        } else {
            double speedLevel = Math.sqrt(Math.pow(currentGamepad1.left_stick_x, 2) + Math.pow(currentGamepad1.left_stick_y, 2));
            decodeBot.getDrive().drive(new HolonomicDriveInstruction(
                    currentGamepad1.right_stick_x * speed,
                    speed * speedLevel,
                    Math.toDegrees(Math.atan2(-currentGamepad1.left_stick_y, currentGamepad1.left_stick_x)) + this.decodeBot.odometry.get().rotation.z
            ));
        }
        this.decodeBot.aimOn = true;
    }
}
