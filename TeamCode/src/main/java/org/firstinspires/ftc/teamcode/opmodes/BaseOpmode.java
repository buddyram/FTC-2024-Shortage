package org.firstinspires.ftc.teamcode.opmodes;

import com.buddyram.rframe.BaseLogger;
import com.buddyram.rframe.Logger;
import com.buddyram.rframe.Pose3D;
import com.buddyram.rframe.RobotException;
import com.buddyram.rframe.SmartLogWrapper;
import com.buddyram.rframe.Vector3D;
import com.buddyram.rframe.drive.HolonomicPositionDriveAdapter;
import com.buddyram.rframe.drive.MecanumDriveTrain;
import com.buddyram.rframe.ftc.Motor;
import com.buddyram.rframe.ftc.RPMMotor;
import com.buddyram.rframe.ftc.SparkFunOTOSOdometry;
import com.buddyram.rframe.ftc.decode.DecodeBot;
import com.buddyram.rframe.ftc.decode.intake.Intake;
import com.buddyram.rframe.ftc.decode.launcher.Feeder;
import com.buddyram.rframe.ftc.decode.launcher.Flywheel;
import com.buddyram.rframe.ftc.decode.launcher.Launcher;
import com.buddyram.rframe.ftc.decode.launcher.Sweeper;
import com.qualcomm.hardware.sparkfun.SparkFunOTOS;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

public abstract class BaseOpmode extends LinearOpMode {
    DecodeBot decodeBot;
    public static final Pose3D DEFAULT_POSITION = new Pose3D( // pose
            new Vector3D(60, 84, 0), // position
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
        DcMotor motorFR = hardwareMap.get(DcMotor.class, "DFR");
        DcMotor motorFL = hardwareMap.get(DcMotor.class, "DFL");
        DcMotor motorBR = hardwareMap.get(DcMotor.class, "DBR");
        DcMotor motorBL = hardwareMap.get(DcMotor.class, "DBL");

        SparkFunOTOSOdometry odometry = new SparkFunOTOSOdometry(
                hardwareMap.get(SparkFunOTOS.class, "TO"),
                DEFAULT_POSITION
        );
        odometry.init();
        this.decodeBot = new DecodeBot() {
            @Override
            public boolean isActive() {
                return opModeIsActive();
            }
        };
        MecanumDriveTrain drive = new MecanumDriveTrain(
                new Motor(motorFL, 1),
                new Motor(motorFR, -1),
                new Motor(motorBL, 1),
                new Motor(motorBR, -1),
                1
        );




        HolonomicPositionDriveAdapter adapter = new HolonomicPositionDriveAdapter(drive, odometry);
        adapter.init();



        DcMotorEx motorFly = hardwareMap.get(DcMotorEx.class, "LFly");
        motorFly.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motorFly.setDirection(DcMotorSimple.Direction.REVERSE);
        motorFly.setVelocity(0);

        Servo servoFeed = hardwareMap.get(Servo.class, "LFeed");
        servoFeed.setPosition(0);
        CRServo servoInt = hardwareMap.get(CRServo.class, "IntS");
        servoInt.setPower(0);

        Launcher launcher = new Launcher(
                this.decodeBot,
                new Flywheel(this.decodeBot, new RPMMotor(motorFly, 28)),
                new Feeder(this.decodeBot, servoFeed)
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
        Intake intake = new Intake(this.decodeBot, new Sweeper(this.decodeBot, servoInt));
        this.decodeBot.init(logger, odometry, adapter, launcher, intake);
    }
}
