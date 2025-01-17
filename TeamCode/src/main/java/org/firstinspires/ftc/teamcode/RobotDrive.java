package org.firstinspires.ftc.teamcode;

import com.buddyram.rframe.Color;
import com.buddyram.rframe.HolonomicDriveInstruction;
import com.buddyram.rframe.HolonomicPositionDriveAdapter;
import com.buddyram.rframe.MecanumDriveTrain;
import com.buddyram.rframe.Pose3D;
import com.buddyram.rframe.Vector3D;
import com.buddyram.rframe.ftc.Motor;
import com.buddyram.rframe.ftc.SparkFunOTOSOdometry;
import com.qualcomm.hardware.rev.RevColorSensorV3;
import com.qualcomm.hardware.sparkfun.SparkFunOTOS;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;


/*
 OFF	0	0	1	Rest
-671	-3080	0	0.1	High Basket
-246	0	1	0.06	Far Reaching Pickup Init
OFF	0	1	0.19	Short Reaching Pickup Init
-335	0	0	0.35	Specimen Approach
-165	0	0	0.35	Specimen Hang
 */
@TeleOp(name = "A: Robot Drive main v1.1.1", group = "Sensor")
public class RobotDrive extends LinearOpMode {
    public final Color YELLOW = new Color(452, 533, 144, 376);
    public final Color RED = new Color(276, 165, 97, 180);
    public final Color BLUE = new Color(72, 139, 272, 161);
    public final Color FLOOR = new Color(62, 106, 91, 86);

    @Override
    public void runOpMode() throws InterruptedException {

        // get a reference to our touchSensor object.
        DcMotor motorFR = hardwareMap.get(DcMotor.class, "motorFR");
        DcMotor motorFL = hardwareMap.get(DcMotor.class, "motorFL");
        DcMotor motorBR = hardwareMap.get(DcMotor.class, "motorBR");
        DcMotor motorBL = hardwareMap.get(DcMotor.class, "motorBL");
        DcMotor armext = hardwareMap.get(DcMotor.class, "armext");
        DcMotor armrotL = hardwareMap.get(DcMotor.class, "armrotL");
        DcMotor armrotR = hardwareMap.get(DcMotor.class, "armrotR");
        Servo armElbowL = hardwareMap.get(Servo.class, "armElbowL");
        Servo armElbowR = hardwareMap.get(Servo.class, "armElbowR");
        Servo claw = hardwareMap.get(Servo.class, "claw");
        Servo wrist = hardwareMap.get(Servo.class, "wrist");
        RevColorSensorV3 colorSensor = hardwareMap.get(RevColorSensorV3.class, "color");
        SparkFunOTOSOdometry odometry = new SparkFunOTOSOdometry(
                hardwareMap.get(SparkFunOTOS.class, "otos"),
                new Pose3D( // pose
                        new Vector3D(0, 0, 0), // position
                        new Vector3D(0, 0, 0), // rotation
                        new Vector3D(0, 0, 0), // position velocity
                        new Vector3D(0, 0, 0)) // rotation velocity
        );
        if (odometry.init()) {
            telemetry.addData("initialized", "true");
            telemetry.update();
        }
        MecanumDriveTrain chassis = new MecanumDriveTrain(
                new Motor(motorFL),
                new Motor(motorFR),
                new Motor(motorBL),
                new Motor(motorBR),
                1
        );

        HolonomicPositionDriveAdapter drive = new HolonomicPositionDriveAdapter(chassis, odometry);


        //MecanumDriveTrain drive = new MecanumDriveTrain(
        //                new Motor(motorFL, -1),
        //                new Motor(motorFR, -1),
        //                new Motor(motorBL, -1),
        //                new Motor(motorBR),
        //                0.7
        //        );

        waitForStart();
        double xl;
        double yl;
        int targetAngle = 0;
        double targetWristAngle = 0.5;
        int targetArmExt = 0;
        double targetElbowAngle = 0.5;
        armrotL.setTargetPosition(0);
        armrotR.setTargetPosition(0);
        armext.setTargetPosition(0);
        armElbowL.setPosition(0.5);
        armElbowR.setPosition(0.5);
        armrotL.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        armrotR.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        armext.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        armrotL.setPower(0.5);
        armrotR.setPower(0.5 / 3);
        armext.setPower(1);
        claw.setPosition(0);

        while (opModeIsActive()) {

            Pose3D pos = odometry.get();
            telemetry.addData("x", pos.position.x);
            telemetry.addData("y", pos.position.y);
            telemetry.addData("h", pos.rotation.z);

            xl = gamepad1.left_stick_x;
            yl = gamepad1.left_stick_y;
            HolonomicDriveInstruction instruction = new HolonomicDriveInstruction(
                    gamepad1.right_stick_x, // rotation
                    Math.sqrt(xl * xl + yl * yl), // speed
                    Math.atan2(-yl, xl) * 180 / Math.PI + drive.odometry.get().rotation.z // direction
            );
            drive.drive(instruction);
            if (gamepad1.y) {
                targetArmExt += 70;
            } else if (gamepad1.a) {
                targetArmExt -= 70;
            }
            if (targetArmExt < 0) {
                targetArmExt = 0;
            }
            if (targetArmExt > 3080) {
                targetArmExt = 3080;
            }
            if (targetAngle < -634) {
                targetAngle = -634;
            }
            if (targetAngle > 0) {
                targetAngle = 0;
            }
            if (gamepad1.x) {
                targetAngle += 2;
            } else if (gamepad1.b) {
                targetAngle -= 2;
            }
            if (gamepad1.right_bumper) {
                claw.setPosition(1);
            } else if (gamepad1.left_bumper) {
                claw.setPosition(0);
            }
            if (gamepad1.dpad_left) {
                targetWristAngle -= 0.03;
            } else if (gamepad1.dpad_right) {
                targetWristAngle += 0.03;
            }
            if (gamepad1.dpad_up) {
                targetElbowAngle -= 0.03;
            } else if (gamepad1.dpad_down) {
                targetElbowAngle += 0.03;
            }
            if (targetWristAngle < 0) {
                targetWristAngle = 0;
            }
            if (targetWristAngle > 1) {
                targetWristAngle = 1;
            }

            if (targetElbowAngle < 0) {
                targetElbowAngle = 0;
            }
            if (targetElbowAngle > 1) {
                targetElbowAngle = 1;
            }
            wrist.setPosition(targetWristAngle);
            armElbowR.setPosition(targetElbowAngle);
            armElbowL.setPosition(1 - targetElbowAngle);
            
            telemetry.addData("Angle", armrotL.getCurrentPosition());
            armrotL.setTargetPosition(targetAngle);
            armrotR.setTargetPosition(targetAngle / 3);
            armext.setTargetPosition(targetArmExt);
            telemetry.addData("Extension", armext.getTargetPosition());
            telemetry.addData("Claw", claw.getPosition());
            telemetry.addData("Wrist", wrist.getPosition());
            telemetry.addData("Elbow", armElbowR.getPosition());
            telemetry.addData("RGBA", colorSensor.red() + " " + colorSensor.green() + " " + colorSensor.blue() + " " + colorSensor.alpha());
            double dist = colorSensor.getDistance(DistanceUnit.INCH);
            Color color = new Color(colorSensor.red() * dist, colorSensor.green() * dist, colorSensor.blue() * dist, colorSensor.alpha() * dist);
            double redConfidence = 1000 / this.RED.distanceColor(color);
            double yellowConfidence = 1000 / this.YELLOW.distanceColor(color);
            double blueConfidence = 1000 / this.BLUE.distanceColor(color);
            double grayConfidence = 1500 / this.FLOOR.distanceColor(color);
            telemetry.addData("RED", redConfidence);
            telemetry.addData("YELLOW", yellowConfidence);
            telemetry.addData("BLUE", blueConfidence);
            telemetry.addData("FLOOR", grayConfidence);
            String guess;
            if (redConfidence > yellowConfidence && redConfidence > blueConfidence && redConfidence > grayConfidence) {
                guess = "RED";
            } else if (yellowConfidence > redConfidence && yellowConfidence > blueConfidence && yellowConfidence > grayConfidence) {
                guess = "YELLOW";
            } else if (blueConfidence > yellowConfidence && blueConfidence > redConfidence && blueConfidence > grayConfidence) {
                guess = "BLUE";
            } else {
                guess = "FLOOR";
            }
            telemetry.addData("GUESS", guess);
            boolean pickupReady = !guess.equals("FLOOR") && dist < 1.2;
            if (pickupReady) {
                telemetry.addData("PICKUP", "READY");
            }
            telemetry.addData("Distance", colorSensor.getDistance(DistanceUnit.INCH));
            telemetry.update();
        }
    }
}
