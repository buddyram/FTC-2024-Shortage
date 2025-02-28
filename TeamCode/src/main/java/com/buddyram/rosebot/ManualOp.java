package com.buddyram.rosebot;

import com.buddyram.rframe.BaseLogger;
import com.buddyram.rframe.Broadcaster;
import com.buddyram.rframe.DefaultBroadcaster;
import com.buddyram.rframe.Logger;
import com.buddyram.rframe.Message;
import com.buddyram.rframe.MessageListener;
import com.buddyram.rframe.SmartLogWrapper;
import com.buddyram.rframe.drive.HolonomicDriveInstruction;
import com.buddyram.rframe.drive.KiwiDriveTrain;
import com.buddyram.rframe.ftc.Motor;
import com.buddyram.rosebot.head.Extension;
import com.buddyram.rosebot.head.Head;
import redis.clients.jedis.Jedis;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;


import org.firstinspires.ftc.robotcore.external.navigation.Orientation;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

@TeleOp(name = "manual", group = "Rosebot")
public class ManualOp extends LinearOpMode {
    public static final String REDIS_HOST = "redis://192.168.45.102:6379";
    KiwiDriveTrain drive;
    public Rosebot rosebot;
    Logger logger;
    NetworkCommunicator networkCommunicator;

    private final Broadcaster<BotMessage> broadcaster = new DefaultBroadcaster<>();

    public void run() throws InterruptedException {
        DcMotor backLeft = hardwareMap.get(DcMotor.class, "bl");
        DcMotor backRight = hardwareMap.get(DcMotor.class, "br");
        DcMotor front = hardwareMap.get(DcMotor.class, "f");
        BNO055IMU imu = hardwareMap.get(BNO055IMU.class, "imu");
        imu.initialize(new BNO055IMU.Parameters());
        drive = new KiwiDriveTrain(new Motor(front), new Motor(backLeft), new Motor(backRight), 0.20);

        DcMotor headMotor = hardwareMap.get(DcMotor.class, "hm");
        headMotor.setTargetPosition(0);
        headMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        headMotor.setPower(1);

        this.rosebot.init(drive, new Head(this.rosebot, new Extension(this.rosebot, headMotor)), this.logger);
        Gson gson = new GsonBuilder().create();

        waitForStart();
        while (opModeIsActive()) {
            double angle = Math.toDegrees(Math.atan2(-gamepad1.left_stick_y, gamepad1.left_stick_x));
            double speed = Math.sqrt(Math.pow(gamepad1.left_stick_x, 2) + Math.pow(gamepad1.left_stick_y, 2));
            telemetry.addData("angle", angle);
            Orientation rpy = imu.getAngularOrientation();
            if (this.gamepad1.right_bumper) {
                this.rosebot.getHead().extension.incrementTargetPosition(97);
            } else if (this.gamepad1.left_bumper) {
                this.rosebot.getHead().extension.incrementTargetPosition(-97);
            }
            telemetry.addData("hi", this.rosebot.getHead().extension.getPosition());

            telemetry.addData("r", Math.toDegrees(rpy.firstAngle));
            telemetry.addData("p", Math.toDegrees(rpy.secondAngle));
            telemetry.addData("y", Math.toDegrees(rpy.thirdAngle));
            Map<String, Double> map = new HashMap<String, Double>() {{
                put("r", Math.toDegrees(rpy.firstAngle));
                put("p", Math.toDegrees(rpy.secondAngle));
                put("y", Math.toDegrees(rpy.thirdAngle));
            }};
            Type type =  new TypeToken<HashMap<String, Double>>(){}.getType();
            String json = gson.toJson(map, type);
            broadcaster.broadcast(() -> json);

            //this.rosebot.getDrive().drive(new HolonomicDriveInstruction(gamepad1.right_stick_x, speed, angle));
            telemetry.update();
        }
        networkCommunicator.stop();
    }

    @Override
    public void runOpMode() throws InterruptedException {
        this.logger = new SmartLogWrapper(
            new BaseLogger() {
                public void log(String caption, Object value) {
                    telemetry.addData(caption, value);
                }

                public void flush() {
                    telemetry.update();
                }
            }
        );
        this.rosebot = new Rosebot();
        System.out.println("attempting to start");
        this.networkCommunicator = new NetworkCommunicator(
                new InetSocketAddress("0.0.0.0", 2222),
                this.broadcaster,
                (a) -> this.rosebot.handleAction(a)
        );
        try {
            networkCommunicator.start();
            this.run();
        } finally {
            this.networkCommunicator.stop();
        }
    }
}
