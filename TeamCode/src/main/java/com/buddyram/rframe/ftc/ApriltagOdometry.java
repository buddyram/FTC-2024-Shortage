package com.buddyram.rframe.ftc;


import com.buddyram.rframe.Odometry;
import com.buddyram.rframe.Pose3D;
import com.buddyram.rframe.Vector3D;

import org.firstinspires.ftc.robotcore.external.navigation.Position;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;

import java.util.ArrayList;
import java.util.Set;

public class ApriltagOdometry implements Odometry<Pose3D> {
    private AprilTagProcessor aprilTagProcessor;
    private Set<String> positionalTags;
    private Pose3D offset;
    public ApriltagOdometry(AprilTagProcessor aprilTagProcessor, Set<String> positionalTags) {
        this(aprilTagProcessor, positionalTags, new Pose3D(
                new Vector3D(72, 72, 0),
                new Vector3D(),
                new Vector3D(),
                new Vector3D()
        ));
    }

    public ApriltagOdometry(AprilTagProcessor aprilTagProcessor, Set<String> positionalTags, Pose3D offset) {
        this.offset = offset;
        this.aprilTagProcessor = aprilTagProcessor;
        this.positionalTags = positionalTags;
    }

    @Override
    public Pose3D get() {
        ArrayList<AprilTagDetection> detections = this.aprilTagProcessor.getDetections();
        Vector3D totalReportedPositions = new Vector3D();
        Vector3D totalReportedRotations = new Vector3D();
        int validNum = 0;
        System.out.println(detections.size());
        for (AprilTagDetection detection : detections) {
            if (detection.metadata != null && this.positionalTags.contains(detection.metadata.name)) {
                Position pos = detection.robotPose.getPosition();
                YawPitchRollAngles rot = detection.robotPose.getOrientation();
                totalReportedPositions = totalReportedPositions.add(new Vector3D(
                        pos.y,
                        -pos.x,
                        pos.z
                ));
                totalReportedRotations = totalReportedRotations.add(new Vector3D(
                        rot.getRoll(),
                        -rot.getPitch(),
                        rot.getYaw() - 90
                ));
                        //pos.y / detections.size(),
                //                                        -pos.x / detections.size(),
                //                                        pos.z / detections.size() // TODO: There might be detection that dont match but it wouldnt work

                validNum++;
                System.out.println();
            }
        }
        if (validNum == 0) {
            return null;
        }
        return new Pose3D(
                totalReportedPositions.mul((double) 1 / validNum),
                totalReportedRotations.mul((double) 1 / validNum),
                new Vector3D(),
                new Vector3D()
        ).add(offset);
    }

    @Override
    public boolean init() {
        return true;
    }

    @Override
    public void setPosition(Pose3D pos) {
        throw new UnsupportedOperationException("setPosition not supported for ApriltagOdometry");
    }

    @Override
    public void cleanup() {

    }
}
