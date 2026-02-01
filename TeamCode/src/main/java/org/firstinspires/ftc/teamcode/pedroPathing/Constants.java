package org.firstinspires.ftc.teamcode.pedroPathing;

import com.pedropathing.control.FilteredPIDFCoefficients;
import com.pedropathing.control.PIDFCoefficients;
import com.pedropathing.follower.Follower;
import com.pedropathing.follower.FollowerConstants;
import com.pedropathing.ftc.FollowerBuilder;
import com.pedropathing.ftc.drivetrains.MecanumConstants;
import com.pedropathing.ftc.localization.constants.OTOSConstants;
import com.pedropathing.paths.PathConstraints;
import com.qualcomm.hardware.sparkfun.SparkFunOTOS;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

public class Constants {
    public static FollowerConstants followerConstants = new FollowerConstants()
            .mass(11.29)
//            .mass(9.93)
            .forwardZeroPowerAcceleration(-31.77)
            .lateralZeroPowerAcceleration(-63.5)
            .translationalPIDFCoefficients(new PIDFCoefficients(0.3, 0, 0.05, 0.05))
            .headingPIDFCoefficients(new PIDFCoefficients(1.2, 0, 0.00, 0.03))
            .drivePIDFCoefficients(new FilteredPIDFCoefficients(0.025, 0, 0.00001, 0.36, 0.01));
////            .drivePIDFCoefficients(new FilteredPIDFCoefficients(0.5,0.0,0.00001,0.1,0.01))
//            .centripetalScaling(0.0005)
//            .holdPointTranslationalScaling(0.25)
//            .holdPointHeadingScaling(0.2);

    public static PathConstraints pathConstraints = new PathConstraints(
            0.99,
            0.5,
            0.5,
            0.02,
            150,
            1.2,
            10,
            1);

    public static MecanumConstants driveConstants = new MecanumConstants()
            .maxPower(1)
            .rightFrontMotorName("dfr")
            .rightRearMotorName("dbr")
            .leftRearMotorName("dbl")
            .leftFrontMotorName("dfl")
            .leftFrontMotorDirection(DcMotorSimple.Direction.REVERSE)
            .leftRearMotorDirection(DcMotorSimple.Direction.REVERSE)
            .rightFrontMotorDirection(DcMotorSimple.Direction.FORWARD)
            .rightRearMotorDirection(DcMotorSimple.Direction.FORWARD)
            .xVelocity(71.003)
            .yVelocity(55.002);

    public static OTOSConstants localizerConstants = new OTOSConstants()
            .hardwareMapName("otos")
            .linearUnit(DistanceUnit.INCH)
            .angleUnit(AngleUnit.RADIANS)
            .offset(new SparkFunOTOS.Pose2D(4.28, -3.84, -Math.PI / 2))
            .linearScalar(0.8883)
            .angularScalar(1.0101010101);

    public static Follower createFollower(HardwareMap hardwareMap) {
        return new FollowerBuilder(followerConstants, hardwareMap)
                .OTOSLocalizer(localizerConstants)
                .pathConstraints(pathConstraints)
                .mecanumDrivetrain(driveConstants)
                .build();
    }
}
