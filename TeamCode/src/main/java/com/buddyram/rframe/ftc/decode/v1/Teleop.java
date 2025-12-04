package com.buddyram.rframe.ftc.decode.v1;

import android.annotation.SuppressLint;

import com.buddyram.rframe.RobotException;
import com.buddyram.rframe.Vector3D;
import com.buddyram.rframe.drive.HolonomicDriveInstruction;
import com.buddyram.rframe.ftc.decode.BotUtils;
import com.buddyram.rframe.ftc.decode.action.ShootAction;
import com.buddyram.rframe.ftc.decode.indexer.Indexer;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.Gamepad;


@TeleOp(name = "TELEOP - DECODE/V1", group = "Decode")
public class Teleop extends BaseOpmode {
    @Override
    public void execute() throws RobotException, InterruptedException {
        Gamepad currentGamepad1 = new Gamepad();
        long lastTime = System.currentTimeMillis();
        while (this.decodeBot.isActive()) {
            telemetry.addData("cycle time 0", System.currentTimeMillis() - lastTime);

            currentGamepad1.copy(gamepad1);
            telemetry.addData("gamepad1 left sticks", currentGamepad1.left_stick_x + ", " + -currentGamepad1.left_stick_y);
            telemetry.addData("gamepad1 right stick", currentGamepad1.right_stick_x);

            telemetry.addData("cycle time 1", System.currentTimeMillis() - lastTime);
            colorRumbleFlywheel(currentGamepad1);
            telemetry.addData("cycle time 2", System.currentTimeMillis() - lastTime);

            if (currentGamepad1.right_bumper) {
                gamepad1.setLedColor(0, 255 ,255, 1000);
                new ShootAction().run(this.decodeBot);
                this.decodeBot.indexer.setCurrentMode(Indexer.Mode.INTAKING);
            }
            if (currentGamepad1.left_bumper) {
                gamepad1.setLedColor(0, 255 ,255, 1000);
                while (!this.decodeBot.indexer.isEmpty()) {
                    new ShootAction().run(this.decodeBot);
                }
                this.decodeBot.indexer.setCurrentMode(Indexer.Mode.INTAKING);
            }
            telemetry.addData("cycle time 3", System.currentTimeMillis() - lastTime);
            this.decodeBot.adjustFlywheelSpeed();
            telemetry.addData("cycle time 4", System.currentTimeMillis() - lastTime);
            this.decodeBot.controlIntake();
            telemetry.addData("cycle time 5", System.currentTimeMillis() - lastTime);
//            try {
//                decodeBot.indexer.ifFullGoToNext();
//            } catch (Exception e) {
//                stop();
//            }
            telemetry.addData("cycle time 6", System.currentTimeMillis() - lastTime);
//            if (currentGamepad1.left_bumper) {
//                this.decodeBot.getIntake().sweeper.setPower(-1);
//            } else if (currentGamepad1.left_trigger > 0) {
//                this.decodeBot.getIntake().sweeper.setPower(currentGamepad1.left_trigger);
//            } else {
//                this.decodeBot.getIntake().sweeper.setPower(0);
//            }
            telemetry.addData("cycle time 7", System.currentTimeMillis() - lastTime);
//            this.decodeBot.autoAim();
            telemetry.addData("cycle time 8", System.currentTimeMillis() - lastTime);
            if (currentGamepad1.square) {
                BotUtils.rotateTo(90).run(decodeBot);
            }
            this.decodeBot.autoAim();
            runDriveControls(currentGamepad1);
            telemetry.addData("cycle time 9", System.currentTimeMillis() - lastTime);
//            telemetry.addData("AprilTag Position", this.decodeBot.getApriltagOdometry().get());
            telemetry.addData("OTOS Position", this.decodeBot.getOdometry().get());
            telemetry.addData("Key", "(x, y, z), (roll, pitch, yaw), (!!!), (!!!)");
            telemetry.addData("Speed", this.decodeBot.getLauncher().wheel.getRPM());
            telemetry.addData("cycle time 10", System.currentTimeMillis() - lastTime);
            telemetry.update();
            telemetry.addData("cycle time 11", System.currentTimeMillis() - lastTime);
            lastTime = System.currentTimeMillis();
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
    }
}
