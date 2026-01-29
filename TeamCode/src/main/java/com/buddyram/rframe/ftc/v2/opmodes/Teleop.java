package com.buddyram.rframe.ftc.v2.opmodes;

import com.buddyram.rframe.RobotException;
import com.buddyram.rframe.Vector3D;
import com.buddyram.rframe.drive.HolonomicDriveInstruction;
import com.buddyram.rframe.ftc.v2.Globals;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Gamepad;


@TeleOp(name = "TELEOP - DECODE/V2", group = "Decode")
public class Teleop extends BaseOpmode {
    @Override
    public void execute() throws RobotException, InterruptedException {
        Gamepad currentGamepad1 = new Gamepad();
        Globals.DID_RUN_AUTO = false;
        while (this.decodeBot.isActive()) {
            this.decodeBot.block = !gamepad1.right_bumper;

            currentGamepad1.copy(gamepad1);
            telemetry.addData("things", this.decodeBot.launcher.blocker.angle);
            telemetry.addData("gamepad1 left sticks", currentGamepad1.left_stick_x + ", " + -currentGamepad1.left_stick_y);
            telemetry.addData("gamepad1 right stick", currentGamepad1.right_stick_x);

            colorRumbleFlywheel(currentGamepad1);

            if (currentGamepad1.square) {
                this.decodeBot.aimOn = false;
            }
            if (currentGamepad1.circle) {
                this.decodeBot.aimOn = true;
            }

            if (currentGamepad1.right_bumper) {

            }
//            if (currentGamepad1.circle) {
//                this.decodeBot.speed = 0;
//            }
//            if (currentGamepad1.cross) {
//                this.decodeBot.speed = -100;
//            }
            this.decodeBot.speed += (int) (gamepad1.right_trigger - gamepad1.left_trigger);
            this.decodeBot.turretOffset += (gamepad1.right_trigger - gamepad1.left_trigger);
            telemetry.addData("turretOffset", this.decodeBot.turretOffset);
            runDriveControls(currentGamepad1);
            telemetry.addData("OTOS Position", this.decodeBot.getOdometry().get());
            telemetry.addData("Key", "(x, y, z), (roll, pitch, yaw), (!!!), (!!!)");
            telemetry.addData("Speed", this.decodeBot.getLauncher().wheel.getRPM());
            telemetry.update();
            this.decodeBot.jamFix = gamepad1.left_bumper || gamepad1.right_bumper;
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
//            this.decodeBot.aimOn = false;
        } else if (currentGamepad1.dpad_down) {
            this.decodeBot.getDrive().drive(this.decodeBot.calculateRelativeDriveInstruction(new Vector3D(0, -1, 0), speed));
//            this.decodeBot.aimOn = false;
        } else if (currentGamepad1.dpad_right) {
            this.decodeBot.getDrive().drive(this.decodeBot.calculateRelativeDriveInstruction(new Vector3D(1, 0, 0), speed));
//            this.decodeBot.aimOn = false;
        } else if (currentGamepad1.dpad_left) {
            this.decodeBot.getDrive().drive(this.decodeBot.calculateRelativeDriveInstruction(new Vector3D(-1, 0, 0), speed));
//            this.decodeBot.aimOn = false;

        } else {
            double speedLevel = Math.sqrt(Math.pow(currentGamepad1.left_stick_x, 2) + Math.pow(currentGamepad1.left_stick_y, 2));
            if (speedLevel > 0) {
//                this.decodeBot.aimOn = false;
            }
            decodeBot.getDrive().drive(new HolonomicDriveInstruction(
                    currentGamepad1.right_stick_x * speed,
                    speed * speedLevel,
                    Math.toDegrees(Math.atan2(-currentGamepad1.left_stick_y, currentGamepad1.left_stick_x)) + this.decodeBot.odometry.get().rotation.z
            ));
        }
        this.decodeBot.aimOn = true;
    }
}
