
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
@TeleOp(name = "Motor Test Decode", group = "Sensor")
public class MotorTestDecode extends LinearOpMode {
    @Override
    public void runOpMode() {

        // get a reference to our touchSensor object.
        DcMotor motorFR = hardwareMap.get(DcMotor.class, "dfr");
        DcMotor motorFL = hardwareMap.get(DcMotor.class, "dfl");
        DcMotor motorBR = hardwareMap.get(DcMotor.class, "dbr");
        DcMotor motorBL = hardwareMap.get(DcMotor.class, "dbl");
        //digitalTouch.setMode(DigitalChannel.Mode.INPUT);
        telemetry.update();
        DcMotor motor = motorFR;
        // wait for the start button to be pressed.
        waitForStart();

        // while the OpMode is active, loop and read the digital channel.
        // Note we use opModeIsActive() as our loop condition because it is an interruptible method.
        while (opModeIsActive()) {
            if (gamepad1.dpad_up) {
                motor = motorFL;
            } else if (gamepad1.dpad_right) {
                motor = motorFR;
            } else if (gamepad1.dpad_down) {
                motor = motorBL;
            } else if (gamepad1.dpad_left) {
                motor = motorBR;
            }
            // button is pressed if value returned is LOW or false.
            // send the info back to driver station using telemetry function.
            motor.setPower(gamepad1.left_stick_y);


            telemetry.update();
        }
    }

//     public calc
}