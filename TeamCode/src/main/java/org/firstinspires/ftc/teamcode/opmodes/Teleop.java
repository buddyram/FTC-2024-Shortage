package org.firstinspires.ftc.teamcode.opmodes;

import android.annotation.SuppressLint;

import com.buddyram.rframe.RobotException;
import com.buddyram.rframe.Vector3D;
import com.buddyram.rframe.drive.HolonomicDriveInstruction;
import com.buddyram.rframe.ftc.decode.BotUtils;
import com.buddyram.rframe.ftc.decode.action.ShootAction;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.robotcore.external.hardware.camera.BuiltinCameraDirection;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.matrices.MatrixF;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Position;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagGameDatabase;
import org.firstinspires.ftc.vision.apriltag.AprilTagLibrary;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;

import java.util.List;
@Disabled
@TeleOp(name = "Teleop-DECODE", group = "Comp")
public class Teleop extends BaseOpmode {
    private static final boolean USE_WEBCAM = true;  // true for webcam, false for phone camera
    private Position cameraPosition = new Position(DistanceUnit.INCH, 0, 0, 0, 0);
    private YawPitchRollAngles cameraOrientation = new YawPitchRollAngles(AngleUnit.DEGREES, 0, -90, 0, 0);
    private double dist;

    private AprilTagProcessor aprilTag;
    private VisionPortal visionPortal;

    @Override
    public void execute() throws RobotException, InterruptedException {
//        initAprilTag();
        Gamepad currentGamepad1 = new Gamepad();
        while (this.decodeBot.isActive()) {
            currentGamepad1.copy(gamepad1);
            telemetry.addData("gamepad1 left sticks", currentGamepad1.left_stick_x + ", " + -currentGamepad1.left_stick_y);
            telemetry.addData("gamepad1 right stick", currentGamepad1.right_stick_x);

            telemetry.addData("Color", this.decodeBot.getLauncher().sensor.getColor());
            telemetry.addData("Match", this.decodeBot.getLauncher().sensor.findHueMatch());

            if (currentGamepad1.right_trigger > 0) {
//                this.decodeBot.syncOdometry();
            }

            colorRumbleFlywheel(currentGamepad1);

            if (currentGamepad1.right_bumper) {
                gamepad1.setLedColor(0, 255 ,255, 1000);
                new ShootAction().run(this.decodeBot);
            }
            this.decodeBot.adjustFlywheelSpeed();
            if (currentGamepad1.left_bumper) {
                this.decodeBot.getIntake().sweeper.setPower(-1);
            } else if (currentGamepad1.left_trigger > 0) {
                this.decodeBot.getIntake().sweeper.setPower(currentGamepad1.left_trigger);
            } else {
                this.decodeBot.getIntake().sweeper.setPower(0);
            }

            if (currentGamepad1.circle) {
                this.decodeBot.autoAim();
            }
            if (currentGamepad1.square) {
                BotUtils.rotateTo(90).run(decodeBot);
            }
            runDriveControls(currentGamepad1);
            telemetry.addData("AprilTag Position", this.decodeBot.getApriltagOdometry().get());
            telemetry.addData("OTOS Position", this.decodeBot.getOdometry().get());
            telemetry.addData("Key", "(x, y, z), (roll, pitch, yaw), (!!!), (!!!)");
            telemetry.addData("Speed", this.decodeBot.getLauncher().wheel.getRPM());
            telemetry.update();
//            telemetryAprilTag();
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
        double speed = 0.6;
        if (currentGamepad1.left_stick_button) {
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
//            HolonomicDriveInstruction i = this.decodeBot.calculateRelativeDriveInstruction(
//                    new Vector3D(currentGamepad1.left_stick_x, -currentGamepad1.left_stick_y, 0),
//                    speed * speedLevel);
//            decodeBot.getDrive().drive(new HolonomicDriveInstruction(currentGamepad1.right_stick_x, i.speed, i.direction + this.decodeBot.getOdometry().get().rotation.z));
            decodeBot.getDrive().drive(new HolonomicDriveInstruction(
                    currentGamepad1.right_stick_x,
                    speed * speedLevel,
                    Math.toDegrees(Math.atan2(-currentGamepad1.left_stick_y, currentGamepad1.left_stick_x)) + this.decodeBot.odometry.get().rotation.z
            ));
        }
    }
//
//
//
//
//    private void initAprilTag() {
//        // Create the AprilTag processor.
//        aprilTag = new AprilTagProcessor.Builder().setCameraPose(cameraPosition, cameraOrientation).setTagLibrary(AprilTagGameDatabase.getDecodeTagLibrary()).build();
//        VisionPortal.Builder builder = new VisionPortal.Builder();
//
//
//        if (USE_WEBCAM) {
//            builder.setCamera(hardwareMap.get(WebcamName.class, "Webcam 1"));
//        } else {
//            builder.setCamera(BuiltinCameraDirection.BACK);
//        }
//        builder.addProcessor(aprilTag);
//        visionPortal = builder.build();
//    }
//    @SuppressLint("DefaultLocale")
//    private void telemetryAprilTag() {
//
//        List<AprilTagDetection> currentDetections = aprilTag.getDetections();
//        telemetry.addData("# AprilTags Detected", currentDetections.size());
//
//        // Step through the list of detections and display info for each one.
//        for (AprilTagDetection detection : currentDetections) {
//            if (detection.metadata != null) {
//                System.out.println(detection);
//                telemetry.addLine(String.format("\n==== (ID %d) %s", detection.id, detection.metadata.name));
//                telemetry.addLine(String.format("XYZ %6.1f %6.1f %6.1f  (inch)",
//                        detection.robotPose.getPosition().x,
//                        detection.robotPose.getPosition().y,
//                        detection.robotPose.getPosition().z));
//                telemetry.addLine(String.format("PRY %6.1f %6.1f %6.1f  (deg)",
//                        detection.robotPose.getOrientation().getPitch(AngleUnit.DEGREES),
//                        detection.robotPose.getOrientation().getRoll(AngleUnit.DEGREES),
//                        detection.robotPose.getOrientation().getYaw(AngleUnit.DEGREES)));
//                double x = detection.robotPose.getPosition().x;
//                double z = detection.robotPose.getPosition().z;
//                telemetry.addData("Predict_Pos", (12 - detection.robotPose.getPosition().x) + " " + (132 + detection.robotPose.getPosition().z));
//                telemetry.addData("Actual_Pos", (this.decodeBot.odometry.get().position.x + " " + this.decodeBot.odometry.get().position.y));
//                this.dist = Math.sqrt(Math.pow(x, 2) + Math.pow(z, 2));
//
//            } else {
//                telemetry.addLine(String.format("\n==== (ID %d) Unknown", detection.id));
//                telemetry.addLine(String.format("Center %6.0f %6.0f   (pixels)", detection.center.x, detection.center.y));
//            }
//            telemetry.addLine("dist: " + dist);
//        }
//
//        // Add "key" information to telemetry
//        telemetry.addLine("\nkey:\nXYZ = X (Right), Y (Forward), Z (Up) dist.");
//        telemetry.addLine("PRY = Pitch, Roll & Yaw (XYZ Rotation)");
//
//    }
}
