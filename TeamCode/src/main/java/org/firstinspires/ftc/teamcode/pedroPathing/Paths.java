package org.firstinspires.ftc.teamcode.pedroPathing;

import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.BezierCurve;
import com.pedropathing.geometry.BezierLine;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.PathChain;

public class Paths {

    public PathChain Path1;
    public PathChain Path2;
    public PathChain Path3;
    public PathChain Path4;

    public Paths(Follower follower) {
        Path1 = follower
                .pathBuilder()
                .addPath(
                        new BezierCurve(
                                new Pose(19.531, 117.011),
                                new Pose(54.866, 109.199),
                                new Pose(55.398, 96.947)
                        )
                )
                .setLinearHeadingInterpolation(Math.toRadians(0), Math.toRadians(-180))
                .build();

        Path2 = follower
                .pathBuilder()
                .addPath(
                        new BezierCurve(
                                new Pose(55.398, 96.947),
                                new Pose(56.464, 82.920),
                                new Pose(44.567, 83.453)
                        )
                )
                .setConstantHeadingInterpolation(Math.toRadians(180))
                .build();

        Path3 = follower
                .pathBuilder()
                .addPath(
                        new BezierLine(new Pose(44.567, 83.453), new Pose(23.970, 83.453))
                )
                .setTangentHeadingInterpolation()
                .build();

        Path4 = follower
                .pathBuilder()
                .addPath(
                        new BezierCurve(
                                new Pose(23.970, 83.453),
                                new Pose(36.400, 82.387),
                                new Pose(51.847, 93.573)
                        )
                )
                .setTangentHeadingInterpolation()
                .setReversed()
                .build();
    }

    public PathChain getPath(int i) {
        try {
            return new PathChain[]{Path1, Path2, Path3, Path4}[i - 1];
        } catch (RuntimeException e) {
            return null;
        }
    }
}

