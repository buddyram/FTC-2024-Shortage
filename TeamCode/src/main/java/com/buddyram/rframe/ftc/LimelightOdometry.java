package com.buddyram.rframe.ftc;

import com.buddyram.rframe.Odometry;
import com.buddyram.rframe.Pose3D;
import com.buddyram.rframe.Vector3D;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.Limelight3A;

public class LimelightOdometry implements Odometry<Pose3D> {
    Limelight3A limelight;
    public LimelightOdometry(Limelight3A limelight) {
        limelight.pipelineSwitch(0);
        this.limelight = limelight;
    }

    @Override
    public Pose3D get() {
        LLResult result = limelight.getLatestResult();
        org.firstinspires.ftc.robotcore.external.navigation.Pose3D botPose = result.getBotpose();
        if (botPose == null || !result.isValid()) {
            return null;
        }
        return new Pose3D(
                new Vector3D(botPose.getPosition().y * 39.3701 + 72,
                             -botPose.getPosition().x * 39.3701 + 72,
                             botPose.getPosition().z + 0),
                new Vector3D(0, 0, botPose.getOrientation().getYaw() - 90),
                new Vector3D(),
                new Vector3D()
        );
    }

    @Override
    public boolean init() {
        this.limelight.start();
        return true;
    }

    @Override
    public void setPosition(Pose3D pos) {
    }
}
