/*   MIT License
 *   Copyright (c) [2025] [Base 10 Assets, LLC]
 *
 *   Permission is hereby granted, free of charge, to any person obtaining a copy
 *   of this software and associated documentation files (the "Software"), to deal
 *   in the Software without restriction, including without limitation the rights
 *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the Software is
 *   furnished to do so, subject to the following conditions:

 *   The above copyright notice and this permission notice shall be included in all
 *   copies or substantial portions of the Software.

 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *   AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *   LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *   SOFTWARE.
 */

package org.firstinspires.ftc.teamcode;

import android.graphics.Color;

import com.buddyram.rframe.ftc.decode.indexer.ColorSensor;
import com.buddyram.rframe.ftc.decode.indexer.DoubleSensor;
import com.buddyram.rframe.ftc.decode.indexer.Indexer;
import com.qualcomm.hardware.rev.RevColorSensorV3;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;


/*
 * This file shows some of the cool features included in ServoImplEx. The extended implementation
 * of the standard Servo class.
 */

@TeleOp(name="Servo Extended Example", group="Concept")
public class IndexerTest extends LinearOpMode {
    public final int GREEN_HUE = 145;
    public final int PURPLE_HUE = 175;
    public final int BLANK_HUE = 123;


    // Declare OpMode member.
    private DcMotor motor = null;


    @Override
    public void runOpMode() throws InterruptedException {
        telemetry.addData("Status", "Initialized");


        motor = hardwareMap.get(DcMotor.class, "idx");
        CRServo servo = hardwareMap.get(CRServo.class, "ints1");
        CRServo servo2 = hardwareMap.get(CRServo.class, "ints2");
        RevColorSensorV3 colorSensor = hardwareMap.get(RevColorSensorV3.class, "CSens");
        RevColorSensorV3 colorSensor2 = hardwareMap.get(RevColorSensorV3.class, "CSens2");
        colorSensor.initialize();
        colorSensor2.initialize();
//        ColorSensor sensor = new ColorSensor(null, colorSensor);
//        ColorSensor sensor2 = new ColorSensor(null, colorSensor2, 0.9, new int[]{138, 145, 170});
        ColorSensor sensor = new DoubleSensor(
            null,
            colorSensor,
            1.2,
            new int[]{123, 145, 175},
            new ColorSensor(null, colorSensor2, 0.9, new int[]{138, 145, 170})
        );
//        new DoubleSensor(null, colorSensor, sensor2);
        motor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motor.setTargetPosition(0);
        motor.setPower(0.5);
        motor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        Indexer indexer = new Indexer(null, motor,  28 * 2.89 * 5.23, new ColorSensor.ColorMatch[]{ColorSensor.ColorMatch.NONE, ColorSensor.ColorMatch.NONE, ColorSensor.ColorMatch.NONE}, sensor);


        waitForStart();

        int k = 0;
        int currentSlot = 0;

        while (opModeIsActive()) {
            int m = (int) Math.floor(k / 360.0 * 28 * 2.89 * 5.23);
            if (indexer.isFull()) {
                servo.setPower(0.5);
                servo2.setPower(-0.25);
            } else {
                if (indexer.isReady()) {
                    servo.setPower(1);
                    servo2.setPower(1);
                }
                else {
                    servo.setPower(-0.1);
                    servo2.setPower(-0.1);
                }
            }

            telemetry.addData("color", sensor.indexerBall());
            try {
//                indexer.ifFullGoToNext();
            } catch (Exception e) {
                stop();
            }
//            ColorSensor.ColorMatch currentColor = null;
//            float[] hsv = new float[3];
//
//            Color.RGBToHSV(
//                    distanceSensor.red(),
//                    distanceSensor.green(),
//                    distanceSensor.blue(),
//                    hsv
//            );
//            if (distanceSensor.getDistance(DistanceUnit.INCH) < 2) {
//                float best = 3232;
//                float score = Math.abs(PURPLE_HUE - hsv[0]);
//                if (score < best) {
//                    currentColor = ColorSensor.ColorMatch.MATCH_PURPLE;
//                    best = score;
//                }
//                score = Math.abs(GREEN_HUE - hsv[0]);
//                if (Math.abs(GREEN_HUE - hsv[0]) < best) {
//                    currentColor = ColorSensor.ColorMatch.MATCH_GREEN;
//                    best = score;
//                }
//                score = Math.abs(BLANK_HUE - hsv[0]);
//                if (Math.abs(BLANK_HUE - hsv[0]) < best) {
//                    currentColor = ColorSensor.ColorMatch.NONE;
//                    best = score;
//                }
//            } else {
//                currentColor = ColorSensor.ColorMatch.NONE;
//            }
//            if (Math.abs(motor.getCurrentPosition() - m) < 3 && currentColor != ColorSensor.ColorMatch.NONE) {
//                indexer.fillSlot(currentColor);
//                if (indexer.isFull()) {
//                    stop();
//                } else {
//                    try {
//                        int nearest = indexer.getNearestEmpty();
//                        indexer.goToSlot(nearest);
//                    } catch (Exception e) {
//                        throw new RuntimeException(e);
//                    }
//
//                }
//            }
            telemetry.addData("currentslot", currentSlot);
//            telemetry.addData("c", currentColor);
//            telemetry.addData("ac[0]", hsv[0]);
//            telemetry.addData("ac[1]", hsv[1]);
//            telemetry.addData("ac[2]", hsv[2]);
            try {
                telemetry.addData("getnearestempty", indexer.getNearestEmpty());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            telemetry.addData("sensor1", sensor.getColor());
//            telemetry.addData("sensor2", sensor2.getColor());
            telemetry.addData("distance1", colorSensor.getDistance(DistanceUnit.INCH));
            telemetry.addData("distance2", colorSensor2.getDistance(DistanceUnit.INCH));
            telemetry.update();
        }
    }
}