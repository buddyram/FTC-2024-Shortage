package com.buddyram.rframe.ftc.decode.v1;

import com.buddyram.rframe.CachedOdometry;
import com.buddyram.rframe.BaseLogger;
import com.buddyram.rframe.GroundingOdometry;
import com.buddyram.rframe.Logger;
import com.buddyram.rframe.Pose3D;
import com.buddyram.rframe.RobotException;
import com.buddyram.rframe.SmartLogWrapper;
import com.buddyram.rframe.Vector3D;
import com.buddyram.rframe.drive.HolonomicPositionDriveAdapter;
import com.buddyram.rframe.drive.MecanumDriveTrain;
import com.buddyram.rframe.ftc.LimelightOdometry;
import com.buddyram.rframe.ftc.Motor;
import com.buddyram.rframe.ftc.RPMMotor;
import com.buddyram.rframe.ftc.SparkFunOTOSOdometry;
import com.buddyram.rframe.ftc.decode.BotUtils;
import com.buddyram.rframe.ftc.decode.DecodeBot;
import com.buddyram.rframe.ftc.decode.Globals;
import com.buddyram.rframe.ftc.decode.indexer.ColorSensor;
import com.buddyram.rframe.ftc.decode.indexer.DoubleSensor;
import com.buddyram.rframe.ftc.decode.indexer.Indexer;
import com.buddyram.rframe.ftc.decode.intake.Intake;
import com.buddyram.rframe.ftc.decode.intake.TwoStageSweeper;
import com.buddyram.rframe.ftc.decode.launcher.Feeder;
import com.buddyram.rframe.ftc.decode.launcher.Flywheel;
import com.buddyram.rframe.ftc.decode.launcher.Launcher;
import com.buddyram.rframe.ftc.decode.launcher.Turret;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.hardware.rev.RevColorSensorV3;
import com.qualcomm.hardware.sparkfun.SparkFunOTOS;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.PIDCoefficients;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;
import com.qualcomm.robotcore.hardware.PwmControl;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.ServoImplEx;

public abstract class BaseOpmode extends LinearOpMode {
    DecodeBot decodeBot;
    CachedOdometry<Pose3D> cachedOdometry;

    public static final Pose3D DEFAULT_POSITION = new Pose3D( // pose
            new Vector3D(0, 0, 0), //            new Vector3D(0, 9, 0), // position
            new Vector3D(0, 0, 0), // rotation
            new Vector3D(0, 0, 0), // position velocity
            new Vector3D(0, 0, 0) // rotation velocity
    );

    public static Pose3D currentPosition = DEFAULT_POSITION;
    public void runOpMode() throws InterruptedException {
        this.initializeHardware();
        Thread rememberLastPosition = new Thread(() -> {
            while(!Thread.currentThread().isInterrupted()) {
                if (opModeIsActive()) {
                    this.cachedOdometry.refresh();
                    try {
                        if (decodeBot.indexer.getCurrentMode() == Indexer.Mode.INTAKING) {
                            decodeBot.indexer.ifFullGoToNext();
                        }
                    } catch (Exception e) {
                        stop();
                    }
                    this.decodeBot.controlIntake();
                    try {
                        this.decodeBot.autoAim();
                    } catch (RobotException e) {
                        throw new RuntimeException(e);
                    }
                    this.decodeBot.adjustFlywheelSpeed();
                    Thread.yield();
                }
            }
        });
        rememberLastPosition.setPriority(Thread.MIN_PRIORITY);
        rememberLastPosition.start();
        this.waitForStart();


        try {
            this.execute();
        } catch (RobotException e) {
            throw new RuntimeException(e);
        } finally {
            rememberLastPosition.interrupt();
            rememberLastPosition.join();
            this.decodeBot.launcher.turret.setAngle(0);
            this.cachedOdometry.cleanup();
            Thread.sleep(100);

        }

    }


    public abstract void execute() throws RobotException, InterruptedException;
    public void initializeHardware() throws InterruptedException {
        Boolean reset = null;
        telemetry.addData("DID RUN AUTO? YES: SQUARE , NO: TRIANGLE", "");
        telemetry.update();
        while (reset == null) {
            if (gamepad1.square) {
                reset = false;
            } else if (gamepad1.triangle) {
                reset = true;
            }
        }

        Limelight3A limelightDEVICE = hardwareMap.get(Limelight3A.class, "limelight");

        LimelightOdometry limelight = new LimelightOdometry(limelightDEVICE);

        /*
         * Starts polling for data.
         */
        if (Globals.POSITION == null) {
            limelight.init();
        } else {
            telemetry.addLine("Used Backup Position Value");
            limelight.initWithBackup(Globals.POSITION.rotation.z);
        }



        DcMotor motorFR = hardwareMap.get(DcMotor.class, "dfr");
        DcMotor motorFL = hardwareMap.get(DcMotor.class, "dfl");
        DcMotor motorBR = hardwareMap.get(DcMotor.class, "dbr");
        DcMotor motorBL = hardwareMap.get(DcMotor.class, "dbl");
        motorFR.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        motorFL.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        motorBR.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        motorBL.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        int isRed = -1;
        if (Globals.IS_RED == null) {
            telemetry.addLine("O for red and X for blue.");
            telemetry.update();
            while (isRed == -1) {
                if (gamepad1.cross) {
                    isRed = 0;
                }
                if (gamepad1.circle) {
                    isRed = 1;
                }
            }
            Globals.IS_RED = isRed == 1;
        } else {
            isRed = Globals.IS_RED ? 1 : 0;
        }
        telemetry.addData("isRed", isRed);

        SparkFunOTOSOdometry otosOdometry = new SparkFunOTOSOdometry(
                hardwareMap.get(SparkFunOTOS.class, "otos"),
                BotUtils.mirrorIfRed(DEFAULT_POSITION, isRed == 1)
        );
        if (!otosOdometry.init()) {
            telemetry.addData("OTOS", "Failed");
        }
        try {
            otosOdometry.setPosition(limelight.get());
            telemetry.addData("OTOS", "used limelight");
        } catch (RuntimeException e) {
            telemetry.addData("OTOS", "used cached");
            otosOdometry.setPosition(Globals.POSITION);
        }

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
        GroundingOdometry<Pose3D> groundingOdometry = new GroundingOdometry<>(limelight, otosOdometry, () -> {
            Pose3D res = otosOdometry.get();
            double otosYaw = res.rotation.z;
            double limelightYaw = otosYaw <= 0 ? otosYaw + 180 : otosYaw - 180;
            limelight.updateOrientation(limelightYaw);
            System.out.println("res: " + res + " \nmagnitude: " + res.positionVelocity.magnitude());
            return res.positionVelocity.magnitude() + res.rotationVelocity.magnitude() < 0.7;
        });


        this.decodeBot = new DecodeBot() {
            @Override
            public boolean isActive() {
                return opModeIsActive();
            }
//            @Override
//            public void adjustFlywheelSpeed() {
//                double dist = this.odometry.get().position.distance(this.targetGoal);
//                this.getLauncher().wheel.setRPM(2700 + Math.pow(dist, 1.3));
//            }
//            @Override
//            public void autoAim() throws RobotException {
//                Vector3D posToGoal = this.targetGoal.sub(this.odometry.get().position);
//                launcher.turret.setAngle(-(this.odometry.get().rotation.z - (Math.toDegrees(Math.atan2(posToGoal.y, posToGoal.x)) - 90)));
//            }
        };
        MecanumDriveTrain drive = new MecanumDriveTrain(
                new Motor(motorFL, -1),
                new Motor(motorFR, 1),
                new Motor(motorBL, -1),
                new Motor(motorBR, 1),
                1
        );
        Servo servoFeed = hardwareMap.get(Servo.class, "LFeed");
        servoFeed.setPosition(0.3);
        Thread.sleep(2000);

        DcMotor indexMotor = hardwareMap.get(DcMotor.class, "idx");
        RevColorSensorV3 colorSensor = hardwareMap.get(RevColorSensorV3.class, "CSens");
        RevColorSensorV3 colorSensor2 = hardwareMap.get(RevColorSensorV3.class, "CSens2");
        colorSensor.initialize();
        colorSensor2.initialize();
        ColorSensor sensor = new DoubleSensor(
                null,
                colorSensor,
                2.5,
                new int[]{123, 145, 175},
                new ColorSensor(null, colorSensor2, 0.9, new int[]{138, 145, 170})
        );
        if (reset) {
            indexMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        }
        indexMotor.setTargetPosition(0);
        indexMotor.setPower(0.5);
        indexMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        Indexer indexer = new Indexer(null, indexMotor,  28 * 2.89 * 5.23, Globals.INDEXER, sensor);



        this.cachedOdometry = new CachedOdometry<>(groundingOdometry);
        HolonomicPositionDriveAdapter adapter = new HolonomicPositionDriveAdapter(drive, this.cachedOdometry);
        adapter.init();



        DcMotorEx motorFly = hardwareMap.get(DcMotorEx.class, "LFly");
        motorFly.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motorFly.setVelocity(0);
        PIDFCoefficients pidNew = new PIDFCoefficients(354, 0, 0, 20);
        motorFly.setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER, pidNew);

        CRServo servoInt1 = hardwareMap.get(CRServo.class, "ints1");
        CRServo servoInt2 = hardwareMap.get(CRServo.class, "ints2");
        servoInt1.setPower(0);
        servoInt2.setPower(0);
        telemetry.addData("Status", "Initialized");
        DcMotor turret = hardwareMap.get(DcMotor.class, "turret");
        if (reset) {
            turret.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        }
        turret.setTargetPosition(0);
        turret.setPower(0.2);
        turret.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        Launcher launcher = new Launcher(
                this.decodeBot,
                new Flywheel(this.decodeBot, new RPMMotor(motorFly, 28)),
                new Feeder(this.decodeBot, servoFeed),
                sensor,
                new Turret(this.decodeBot, turret)
        );
        launcher.feeder.setPosition(Feeder.CLOSE);
        PIDFCoefficients pidOrig = motorFly.getPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER);
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
        Intake intake = new Intake(this.decodeBot, new TwoStageSweeper(this.decodeBot, servoInt2, servoInt1));
        this.decodeBot.init(logger, new CachedOdometry<>(groundingOdometry), adapter, launcher, intake, limelight, isRed == 1, indexer);
        while (! this.isStarted()) {
            telemetry.addData("P,I,D (orig)", "%.04f, %.04f, %.0f, %.04f",
                    pidOrig.p, pidOrig.i, pidOrig.d, pidOrig.f);
            telemetry.addData("POS", groundingOdometry.get());
            telemetry.update();
        }
    }
}
