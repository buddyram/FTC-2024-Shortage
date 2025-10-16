package org.firstinspires.ftc.teamcode.opmodes;

import android.annotation.SuppressLint;

import com.buddyram.rframe.RobotException;
import com.buddyram.rframe.Vector3D;
import com.buddyram.rframe.drive.HolonomicDriveInstruction;
import com.buddyram.rframe.ftc.decode.BotUtils;
import com.buddyram.rframe.ftc.decode.action.ShootAction;
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

@TeleOp(name = "Teleop-DECODE", group = "Comp")
public class Teleop extends BaseOpmode {
    private static final boolean USE_WEBCAM = true;  // true for webcam, false for phone camera
    private Position cameraPosition = new Position(DistanceUnit.INCH, 0, 0, 0, 0);
    private YawPitchRollAngles cameraOrientation = new YawPitchRollAngles(AngleUnit.DEGREES, 0, -90, 0, 0);
    private double dist;

    private AprilTagProcessor aprilTag;

    /**
     * The variable to store our instance of the vision portal.
     */
    private VisionPortal visionPortal;

    @Override
    public void execute() throws RobotException, InterruptedException {
        initAprilTag();
        Gamepad currentGamepad1 = new Gamepad();
        while (this.decodeBot.isActive()) {
            currentGamepad1.copy(gamepad1);
            telemetry.addData("gamepad1 left sticks", currentGamepad1.left_stick_x + ", " + -currentGamepad1.left_stick_y);
            telemetry.addData("gamepad1 right stick", currentGamepad1.right_stick_x);
            telemetry.update();

            if (currentGamepad1.right_bumper) {
                new ShootAction().run(this.decodeBot);
            }
            if (currentGamepad1.x) {
                this.decodeBot.getLauncher().wheel.setRPM(0);
            } else {
                this.decodeBot.getLauncher().wheel.setRPM(2800 + Math.pow(this.dist, 1.3976));
            }
            if (currentGamepad1.left_bumper) {
                this.decodeBot.getIntake().sweeper.setPower(-1);
            } else {
                this.decodeBot.getIntake().sweeper.setPower(0);
            }
            if (currentGamepad1.circle) {
                Vector3D posToGoal = new Vector3D(0, 144, 0).sub(this.decodeBot.odometry.get().position);
                BotUtils.rotateTo(Math.toDegrees(Math.atan2(posToGoal.y, posToGoal.x)) - 90).run(decodeBot);
            }
            if (currentGamepad1.square) {
                BotUtils.rotateTo(90).run(decodeBot);
            }
            runDriveControls(currentGamepad1);
            telemetryAprilTag();
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
