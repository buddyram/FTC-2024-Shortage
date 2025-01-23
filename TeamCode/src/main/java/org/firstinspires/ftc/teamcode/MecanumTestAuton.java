package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;


/*
 * This OpMode demonstrates how to use a digital channel.
 *
 * The OpMode assumes that the digital channel is configured with a name of "digitalTouch".
 *
 * Use Android Studio to Copy this Class, and Paste it into your team's code folder with a new name.
 * Remove or comment out the @Disabled line to add this OpMode to the Driver Station OpMode list.
 */
@Disabled
@TeleOp(name = "Robot Drive", group = "Sensor")
public class MecanumTestAuton extends LinearOpMode {
    @Override
    public void runOpMode() throws InterruptedException {

        // get a reference to our touchSensor object.
        DcMotor motorFR = hardwareMap.get(DcMotor.class, "motorFR");
        DcMotor motorFL = hardwareMap.get(DcMotor.class, "motorFL");
        DcMotor motorBR = hardwareMap.get(DcMotor.class, "motorBR");
        DcMotor motorBL = hardwareMap.get(DcMotor.class, "motorBL");
//        DcMotor armext = hardwareMap.get(DcMotor.class, "armext");
//        DcMotor armrot = hardwareMap.get(DcMotor.class, "armrot");
//        Servo claw = hardwareMap.get(Servo.class, "claw");
//        Servo wrist = hardwareMap.get(Servo.class, "wrist");

        MecanumBaseChassis robot = new MecanumBaseChassis(motorFL, motorFR, motorBL, motorBR);
        robot.setErrorCorrectionMultipliers(new double[]{-1, 1, -1, 1});
        //telemetry.update();

        waitForStart();
        double angle;
        double xl;
        double yl;
        double turnamnt;
        int targetangle = 0;
        double targetWristAngle = 0;
        int targetArmExt = 0;
        int mode = 1;
//        armrot.setTargetPosition(0);
//        armext.setTargetPosition(0);
//        armrot.setMode(DcMotor.RunMode.RUN_TO_POSITION);
//        armext.setMode(DcMotor.RunMode.RUN_TO_POSITION);
//        armrot.setPower(0.5);
//        armext.setPower(1);
//        claw.setPosition(0);

        while (opModeIsActive()) {
            xl = gamepad1.left_stick_x;
            yl = gamepad1.left_stick_y;
            angle = (Math.atan2(-yl, xl) * 180 / Math.PI);
            turnamnt = gamepad1.right_stick_x * 1;
            robot.setSpeed(Math.sqrt(xl * xl + yl * yl) * 1);
            robot.setDirection(angle);
            robot.setRobotDirection(turnamnt);
            telemetry.addData("RobotDrive", "" + robot.FlBrSpeed + robot.FrBlSpeed);
            if (mode == 1) {
                if (gamepad1.y) {
                    targetArmExt -= 4;
                } else if (gamepad1.a) {
                    targetArmExt += 4;
                }
                if (targetArmExt > 0) {
                    targetArmExt = 0;
                }
                if (gamepad1.x) {
                    targetangle += 1;
                } else if (gamepad1.b) {
                    targetangle -= 1;
                }
            } else {
                if (gamepad1.y) {
                    targetArmExt -= 20;
                } else if (gamepad1.a) {
                    targetArmExt = 0;
                }
                if (targetArmExt > 0) {
                    targetArmExt = 0;
                }
                if (gamepad1.x && targetArmExt >= -10) {
                    targetangle = 720;
                } else if (gamepad1.b && targetArmExt >= -10) {
                    targetangle = 0;
                }
            }
            if (gamepad1.back) {
                mode = 1;
            } else if (gamepad1.start) {
                mode = 0;
            }
//            if (gamepad1.right_bumper) {
//                claw.setPosition(1);
//            } else if (gamepad1.left_bumper) {
//                claw.setPosition(0);
//            }
            if (gamepad1.dpad_left) {
                targetWristAngle -= 0.01;
            } else if (gamepad1.dpad_right) {
                targetWristAngle += 0.01;
            }
            if (targetWristAngle < 0) {
                targetWristAngle = 0;
            }
            if (targetWristAngle > 1) {
                targetWristAngle = 1;
            }
//            wrist.setPosition(targetWristAngle);
//
//            telemetry.addData("arm rotation motor position: ", armrot.getCurrentPosition());
//            telemetry.addData("target: ", targetangle);
//            armrot.setTargetPosition(targetangle);
//            armext.setTargetPosition(targetArmExt);
//            telemetry.addData("armext: ", armext.getTargetPosition());
//            telemetry.addData("armext is busy: ", armext.isBusy());
            telemetry.addData("targetArmExt: ", targetArmExt);
            telemetry.update();
            robot.update();



        }
    }
}
