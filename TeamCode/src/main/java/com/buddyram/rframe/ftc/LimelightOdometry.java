package com.buddyram.rframe.ftc;

import com.buddyram.rframe.Odometry;
import com.buddyram.rframe.Pose3D;
import com.buddyram.rframe.Utils;
import com.buddyram.rframe.Vector3D;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLStatus;
import com.qualcomm.hardware.limelightvision.Limelight3A;

public class LimelightOdometry implements Odometry<Pose3D> {
    public static final int INIT_SAMPLE_COUNT = 10000;
    Limelight3A limelight;
    Odometry<Vector3D> imu;
    Double yaw = null;

    public LimelightOdometry(Limelight3A limelight) {
        this(limelight, null);
    }

    public LimelightOdometry(Limelight3A limelight, Odometry<Vector3D> imu) {
        limelight.pipelineSwitch(0);
        this.limelight = limelight;
        this.imu = imu;
    }

    public void updateOrientation(double yaw) {
        this.yaw = yaw;
        this.limelight.updateRobotOrientation(yaw);
    }

    public Pose3D getMT1() {
        LLResult result = limelight.getLatestResult();
        org.firstinspires.ftc.robotcore.external.navigation.Pose3D botPose = result.getBotpose();

        if (botPose == null || !result.isValid()) {
            return null;
        }
        return new Pose3D(
                new Vector3D(botPose.getPosition().y * 39.3701 + 72,
                        -botPose.getPosition().x * 39.3701 + 72,
                        botPose.getPosition().z + 0),
                new Vector3D(0, 0, Utils.normalizeAngle(botPose.getOrientation().getYaw()+180)),
                new Vector3D(),
                new Vector3D()
        );
    }

    @Override
    public Pose3D get() {
        LLResult result = limelight.getLatestResult();
        org.firstinspires.ftc.robotcore.external.navigation.Pose3D botPose = result.getBotpose_MT2();

        if (botPose == null || !result.isValid()) {
            return null;
        }
        return new Pose3D(
                new Vector3D(botPose.getPosition().y * 39.3701 + 72,
                             -botPose.getPosition().x * 39.3701 + 72,
                             botPose.getPosition().z + 0),
                new Vector3D(0, 0, Utils.normalizeAngle(botPose.getOrientation().getYaw()+180)),
                new Vector3D(),
                new Vector3D()
        );
    }

    @Override
    public boolean init() {
        this.limelight.start();
        if (!this.limelight.isRunning()) {
            throw new RuntimeException("Not Running!!");
        }
        try {
            this.updateOrientation(calculateStartingYaw());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    public boolean initWithBackup(double yawBackup) {
        this.limelight.start();
        if (!this.limelight.isRunning()) {
            throw new RuntimeException("Not Running!!");
        }
        try {
            try {
                this.updateOrientation(calculateStartingYaw());
                return true;
            } catch (RuntimeException e) {
                this.updateOrientation(yawBackup);
                return false;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public double calculateStartingYaw() throws Exception {
        double total = 0;
        int samples = 0;
        long time = System.currentTimeMillis();
        double min = 500;
        double max = -500;
        for (int i = 0; i < INIT_SAMPLE_COUNT; i++) {
            LLResult result = limelight.getLatestResult();
            org.firstinspires.ftc.robotcore.external.navigation.Pose3D botPose = result.getBotpose();
            if(result.isValid()) {
                double yaw = botPose.getOrientation().getYaw();
                total += yaw;
                min = Math.min(min, yaw);
                max = Math.max(max, yaw);
                samples += 1;
            }

        }
        if (samples <= INIT_SAMPLE_COUNT / 4) {
            throw new RuntimeException("Failed to set start! Sample count: " + samples);
        }
        System.out.println("time: " + (System.currentTimeMillis() - time));
        System.out.println(min);
        System.out.println(max);
        System.out.println("average: " + total / samples);
        return total / samples;
    }

    @Override
    public void setPosition(Pose3D pos) {
    }

    @Override
    public void cleanup() {
        this.limelight.stop();
    }
}
