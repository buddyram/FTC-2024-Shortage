//package org.firstinspires.ftc.teamcode;
//
//import com.qualcomm.robotcore.eventloop.opmode.Disabled;
//import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
//import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
//import com.qualcomm.robotcore.hardware.DigitalChannel;
//import com.qualcomm.robotcore.hardware.DcMotor;
//
//
///*
// * This OpMode demonstrates how to use a digital channel.
// *
// * The OpMode assumes that the digital channel is configured with a name of "digitalTouch".
// *
// * Use Android Studio to Copy this Class, and Paste it into your team's code folder with a new name.
// * Remove or comment out the @Disabled line to add this OpMode to the Driver Station OpMode list.
// */
//@TeleOp(name = "Robot teleop", group = "Sensor")
//public class RobotDriveTest extends LinearOpMode {
//    @Override
//    public void runOpMode() throws InterruptedException {
//
//        // get a reference to our touchSensor object.
//        DcMotor motorFR = hardwareMap.get(DcMotor.class, "motorFR");
//        DcMotor motorFL = hardwareMap.get(DcMotor.class, "motorFL");
//        DcMotor motorBR = hardwareMap.get(DcMotor.class, "motorBR");
//        DcMotor motorBL = hardwareMap.get(DcMotor.class, "motorBL");
//        BaseRobot robot = new BaseRobot(motorFL, motorFR, motorBL, motorBR);
//        //set motorFR and motorBR into "rmotors"
//        //set motorFL and motorBL into "lmotors"
//        robot.setErrorCorrectionMultipliers(new double[]{1, 1, 1, -1});
//        //telemetry.update();
//
//        waitForStart();
//        robot.update();
//        robot.setSpeed(0.2);
//
//        //robot.setSpeed(0.2)
//        //robot.update()
//        //wait 5 secs
//        //robot.setSp
//
//        double dir = 0;
//        while (opModeIsActive()) {
//        double drive = -gamepad1.left_stick_y;
//        double turn  =  gamepad1.right_stick_x;
//        leftPower    = Range.clip(drive + turn, -1.0, 1.0) ;
//        rightPower   = Range.clip(drive - turn, -1.0, 1.0) ;
//        rmotors.setPower(rightPower);
//        lmotors.setPower(leftPower);
//        }
//        robot.setSpeed(0);
//        robot.update();
//    }
//}
