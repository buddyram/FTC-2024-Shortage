package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.DcMotor;


/*
 * This OpMode demonstrates how to use a digital channel.
 *
 * The OpMode assumes that the digital channel is configured with a name of "digitalTouch".
 *
 * Use Android Studio to Copy this Class, and Paste it into your team's code folder with a new name.
 * Remove or comment out the @Disabled line to add this OpMode to the Driver Station OpMode list.
 */
@TeleOp(name = "Robot Drive Test", group = "Sensor")
public class RobotDriveTest extends LinearOpMode {
    @Override
    public void runOpMode() throws InterruptedException {

        // get a reference to our touchSensor object.
        DcMotor motorFR = hardwareMap.get(DcMotor.class, "motorFR");
        DcMotor motorFL = hardwareMap.get(DcMotor.class, "motorFL");
        DcMotor motorBR = hardwareMap.get(DcMotor.class, "motorBR");
        DcMotor motorBL = hardwareMap.get(DcMotor.class, "motorBL");
        DcMotor motor = hardwareMap.get(DcMotor.class, "armext");

        BaseRobot robot = new BaseRobot(motorFL, motorFR, motorBL, motorBR);
        robot.setErrorCorrectionMultipliers(new double[]{1, 1, 1, -1});
        //telemetry.update();

        waitForStart();
        robot.update();
        robot.setSpeed(0.2);
        double dir = 0;
        while (opModeIsActive()) {
            dir = (dir + 1
            ) % 360;
            //robot.setRobotDirection(360 - dir);
            robot.setDirection(dir);
            robot.update();
            motor.setPower(Math.sin(dir * 2 * Math.PI / 180));
        }
        robot.setSpeed(0);
        robot.update();
    }
}
