package com.buddyram.rframe.ftc.decode.v1;

import com.buddyram.rframe.BaseLogger;
import com.buddyram.rframe.GroundingOdometry;
import com.buddyram.rframe.Logger;
import com.buddyram.rframe.Pose3D;
import com.buddyram.rframe.RobotException;
import com.buddyram.rframe.SmartLogWrapper;
import com.buddyram.rframe.Vector3D;
import com.buddyram.rframe.drive.HolonomicPositionDriveAdapter;
import com.buddyram.rframe.drive.MecanumDriveTrain;
import com.buddyram.rframe.ftc.ApriltagOdometry;
import com.buddyram.rframe.ftc.LimelightOdometry;
import com.buddyram.rframe.ftc.Motor;
import com.buddyram.rframe.ftc.RPMMotor;
import com.buddyram.rframe.ftc.SparkFunOTOSOdometry;
import com.buddyram.rframe.ftc.decode.BotUtils;
import com.buddyram.rframe.ftc.decode.DecodeBot;
import com.buddyram.rframe.ftc.decode.indexer.ColorSensor;
import com.buddyram.rframe.ftc.decode.intake.Intake;
import com.buddyram.rframe.ftc.decode.intake.TwoStageSweeper;
import com.buddyram.rframe.ftc.decode.launcher.Feeder;
import com.buddyram.rframe.ftc.decode.launcher.Flywheel;
import com.buddyram.rframe.ftc.decode.launcher.Launcher;
import com.buddyram.rframe.ftc.decode.intake.Sweeper;
import com.buddyram.rframe.ftc.decode.launcher.Turret;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.hardware.rev.RevColorSensorV3;
import com.qualcomm.hardware.sparkfun.SparkFunOTOS;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Position;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagGameDatabase;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;

import java.util.HashSet;

public abstract class BaseOpmode extends LinearOpMode {
    DecodeBot decodeBot;
    public static final Pose3D DEFAULT_POSITION = new Pose3D( // pose
            new Vector3D(0, 0, 0), //            new Vector3D(0, 9, 0), // position
            new Vector3D(0, 0, 0), // rotation
            new Vector3D(0, 0, 0), // position velocity
            new Vector3D(0, 0, 0) // rotation velocity
    );

    public static Pose3D currentPosition = DEFAULT_POSITION;
    public void runOpMode() throws InterruptedException {
        this.initializeHardware();
        this.waitForStart();

        Thread rememberLastPosition = new Thread(() -> {
            while(!Thread.currentThread().isInterrupted()) {
                if (opModeIsActive()) {
                    this.currentPosition = this.decodeBot.getOdometry().get();
                    Thread.yield();
                }
            }
        });
        rememberLastPosition.setPriority(Thread.MIN_PRIORITY);
        rememberLastPosition.start();


        try {
            this.execute();
        } catch (RobotException e) {
            throw new RuntimeException(e);
        } finally {
            rememberLastPosition.interrupt();
            rememberLastPosition.join();
        }

    }
    public abstract void execute() throws RobotException, InterruptedException;
    public void initializeHardware() throws InterruptedException {
        DcMotor motorFR = hardwareMap.get(DcMotor.class, "dfr");
        DcMotor motorFL = hardwareMap.get(DcMotor.class, "dfl");
        DcMotor motorBR = hardwareMap.get(DcMotor.class, "dbr");
        DcMotor motorBL = hardwareMap.get(DcMotor.class, "dbl");
        motorFR.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        motorFL.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        motorBR.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        motorBL.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        telemetry.addLine("O for red and X for blue.");
        telemetry.update();
        int isRed = -1;
        while (isRed == -1) {
            if (gamepad1.cross) {
                isRed = 0;
            }
            if (gamepad1.circle) {
                isRed = 1;
            }
        }
        telemetry.addData("isRed", isRed);

        SparkFunOTOSOdometry otosOdometry = new SparkFunOTOSOdometry(
                hardwareMap.get(SparkFunOTOS.class, "otos"),
                BotUtils.mirrorIfRed(DEFAULT_POSITION, isRed == 1)
        );
        if (!otosOdometry.init()) {
            telemetry.addData("OTOS", "Failed");
        }
        telemetry.addData("OTOS", "Yay!!s");
        telemetry.update();

//        AprilTagProcessor aprilTagProcessor = new AprilTagProcessor.Builder().setCameraPose(
//                new Position(DistanceUnit.INCH, -(5.61 / 2 + 2.73), -1.1, 12, 0),
//                new YawPitchRollAngles(AngleUnit.DEGREES, 0, -90, 0, 0)
//        ).setTagLibrary(AprilTagGameDatabase.getDecodeTagLibrary()).build();

//        WebcamName cam = hardwareMap.get(WebcamName.class, "Webcam 1");
//        VisionPortal.Builder builder = new VisionPortal.Builder();
//        builder.setCamera(cam);
//        builder.addProcessor(aprilTagProcessor);
//        VisionPortal visionPortal = builder.build();
//        HashSet<String> positionalTags = new HashSet<>();
//        positionalTags.add("BlueTarget");
//        positionalTags.add("RedTarget");
//        ApriltagOdometry apriltagOdometry = new ApriltagOdometry(
//                aprilTagProcessor,
//                positionalTags
//        );
        Limelight3A limelightDEVICE = hardwareMap.get(Limelight3A.class, "limelight");

        LimelightOdometry limelight = new LimelightOdometry(limelightDEVICE);

        /*
         * Starts polling for data.
         */
        limelight.init();
        GroundingOdometry<Pose3D> groundingOdometry = new GroundingOdometry<>(limelight, otosOdometry, () -> {
            Pose3D res = otosOdometry.get();
            System.out.println("res: " + res + " \nmagnitude: " + res.positionVelocity.magnitude());
            return res.positionVelocity.magnitude() + res.rotationVelocity.magnitude() < 0.7;
        });


        this.decodeBot = new DecodeBot() {
            @Override
            public boolean isActive() {
                return opModeIsActive();
            }
            @Override
            public void adjustFlywheelSpeed() {
                double dist = this.odometry.get().position.distance(this.targetGoal);
                this.getLauncher().wheel.setRPM(0);
            }
            @Override
            public void autoAim() throws RobotException {
                Vector3D posToGoal = this.targetGoal.sub(this.odometry.get().position);
                launcher.turret.setAngle(this.odometry.get().rotation.z - (Math.toDegrees(Math.atan2(posToGoal.y, posToGoal.x)) - 90));
            }
        };
        MecanumDriveTrain drive = new MecanumDriveTrain(
                new Motor(motorFL, -1),
                new Motor(motorFR, 1),
                new Motor(motorBL, -1),
                new Motor(motorBR, 1),
                1
        );




        HolonomicPositionDriveAdapter adapter = new HolonomicPositionDriveAdapter(drive, groundingOdometry);
        adapter.init();



        DcMotorEx motorFly = hardwareMap.get(DcMotorEx.class, "LFly");
        motorFly.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motorFly.setDirection(DcMotorSimple.Direction.REVERSE);
        motorFly.setVelocity(0);

        Servo servoFeed = hardwareMap.get(Servo.class, "LFeed");
        servoFeed.setPosition(0);
        CRServo servoInt1 = hardwareMap.get(CRServo.class, "ints1");
        CRServo servoInt2 = hardwareMap.get(CRServo.class, "ints2");
        servoInt1.setPower(0);
        servoInt2.setPower(0);

        RevColorSensorV3 colorSensor = hardwareMap.get(RevColorSensorV3.class, "CSens");
        colorSensor.initialize();
        telemetry.addData("Status", "Initialized");
        DcMotor turret = hardwareMap.get(DcMotor.class, "turret");
        turret.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        turret.setTargetPosition(0);
        turret.setPower(0.5);
        turret.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        Launcher launcher = new Launcher(
                this.decodeBot,
                new Flywheel(this.decodeBot, new RPMMotor(motorFly, 28)),
                new Feeder(this.decodeBot, servoFeed),
                new ColorSensor(this.decodeBot, colorSensor),
                new Turret(this.decodeBot, turret)
        );
        Logger logger = new SmartLogWrapper(
                new BaseLogger() {
                    public void log(String caption, Object value) {
                        telemetry.addData(caption, value);
                    }

                    public void flush() {
                        telemetry.update();
                    }
                }
        );
        Intake intake = new Intake(this.decodeBot, new TwoStageSweeper(this.decodeBot, servoInt1, servoInt2));

        this.decodeBot.init(logger, groundingOdometry, adapter, launcher, intake, limelight, isRed == 1);
        while (! this.isStarted()) {
            telemetry.addData("POS", groundingOdometry.get());
            telemetry.update();
        }
    }
}
