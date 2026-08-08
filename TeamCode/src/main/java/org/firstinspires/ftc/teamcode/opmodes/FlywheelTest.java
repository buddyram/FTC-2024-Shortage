package org.firstinspires.ftc.teamcode.opmodes;

import android.annotation.SuppressLint;

import com.buddyram.rframe.ftc.RPMMotor;
import com.buddyram.rframe.ftc.decode.launcher.Flywheel;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.hardware.camera.BuiltinCameraDirection;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
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
@TeleOp(name = "Flywheel Test", group = "Flywheel")
public class FlywheelTest extends LinearOpMode {
    private static final boolean USE_WEBCAM = true;  // true for webcam, false for phone camera
    private Position cameraPosition = new Position(DistanceUnit.INCH, 0, 0, 0, 0);
    private YawPitchRollAngles cameraOrientation = new YawPitchRollAngles(AngleUnit.DEGREES, 0, -90, 0, 0);
    private double dist;

    /**
     * The variable to store our instance of the AprilTag processor.
     */
    private AprilTagProcessor aprilTag;

    /**
     * The variable to store our instance of the vision portal.
     */
    private VisionPortal visionPortal;

    @Override
    public void runOpMode() throws InterruptedException {
        DcMotorEx wheelMotor = hardwareMap.get(DcMotorEx.class, "FlywheelMotor");
        Servo feed = hardwareMap.get(Servo.class, "FeedingServo");
        wheelMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        wheelMotor.setVelocity(0);
        wheelMotor.setDirection(DcMotorSimple.Direction.REVERSE);
        Flywheel flywheel = new Flywheel(null, new RPMMotor(wheelMotor, 28));
        
        initAprilTag();

        waitForStart();
        while (opModeIsActive()) {
            if (gamepad1.triangle) {
                flywheel.increaseRPM(1);
            }
            if (gamepad1.cross) {
                flywheel.increaseRPM(-1);
            }
            if (gamepad1.square) {
                flywheel.setRPM(3600);
            }
            if (gamepad1.circle) {
                flywheel.setRPM(0);
            }
            if (gamepad1.right_bumper) {
                flywheel.setRPM(2900 + Math.pow(this.dist, 1.4276));
            }
            if (gamepad1.dpad_up) {
                feed.setPosition(1);
            } else {
                feed.setPosition(0);
            }
            telemetry.addData("Target", flywheel.getTarget());
            telemetry.addData("RPM", flywheel.getRPM());
            telemetry.addData("Ready?", (Math.abs(flywheel.getRPM() - flywheel.getTarget()) < 50));
            telemetry.addData("powergoal", Math.pow(this.dist, 1.3276));
            telemetry.update();
            telemetryAprilTag();
        }
    }

    private void initAprilTag() {

        AprilTagLibrary.Builder myAprilTagLibraryBuilder;
        AprilTagProcessor.Builder myAprilTagProcessorBuilder;
        AprilTagLibrary myAprilTagLibrary;
        AprilTagProcessor myAprilTagProcessor;

        myAprilTagLibraryBuilder = new AprilTagLibrary.Builder();
        myAprilTagLibraryBuilder.addTags(AprilTagGameDatabase.getCurrentGameTagLibrary());
        myAprilTagLibraryBuilder.addTag(20, "BLUE GOAL", 6.5, DistanceUnit.INCH);
        myAprilTagLibrary = myAprilTagLibraryBuilder.build();

        // Create the AprilTag processor.
        aprilTag = new AprilTagProcessor.Builder().setCameraPose(cameraPosition, cameraOrientation).setTagLibrary(myAprilTagLibrary).build();
        VisionPortal.Builder builder = new VisionPortal.Builder();


        if (USE_WEBCAM) {
            builder.setCamera(hardwareMap.get(WebcamName.class, "Webcam 1"));
        } else {
            builder.setCamera(BuiltinCameraDirection.BACK);
        }
        builder.addProcessor(aprilTag);
        visionPortal = builder.build();
    }
    @SuppressLint("DefaultLocale")
    private void telemetryAprilTag() {

        List<AprilTagDetection> currentDetections = aprilTag.getDetections();
        telemetry.addData("# AprilTags Detected", currentDetections.size());

        // Step through the list of detections and display info for each one.
        for (AprilTagDetection detection : currentDetections) {
                if (detection.metadata != null) {
                    System.out.println(detection);
                    telemetry.addLine(String.format("\n==== (ID %d) %s", detection.id, detection.metadata.name));
                    telemetry.addLine(String.format("XYZ %6.1f %6.1f %6.1f  (inch)",
                            detection.robotPose.getPosition().x,
                            detection.robotPose.getPosition().y,
                            detection.robotPose.getPosition().z));
                    telemetry.addLine(String.format("PRY %6.1f %6.1f %6.1f  (deg)",
                            detection.robotPose.getOrientation().getPitch(AngleUnit.DEGREES),
                            detection.robotPose.getOrientation().getRoll(AngleUnit.DEGREES),
                            detection.robotPose.getOrientation().getYaw(AngleUnit.DEGREES)));
                    double x = detection.robotPose.getPosition().x;
                    double z = detection.robotPose.getPosition().z;
                    this.dist = Math.sqrt(Math.pow(x, 2) + Math.pow(z, 2));
                } else {
                    telemetry.addLine(String.format("\n==== (ID %d) Unknown", detection.id));
                    telemetry.addLine(String.format("Center %6.0f %6.0f   (pixels)", detection.center.x, detection.center.y));
                }
                telemetry.addLine("dist: " + dist);
            }

        // Add "key" information to telemetry
        telemetry.addLine("\nkey:\nXYZ = X (Right), Y (Forward), Z (Up) dist.");
        telemetry.addLine("PRY = Pitch, Roll & Yaw (XYZ Rotation)");

    }
}
