package com.buddyram.rframe.ftc;

import com.buddyram.rframe.Odometry;
import com.buddyram.rframe.Pose3D;
import com.buddyram.rframe.Vector3D;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;

public class GoBildaPinpointOdometry implements Odometry<Pose3D> {
    GoBildaPinpointDriver odometry;
    public GoBildaPinpointOdometry(GoBildaPinpointDriver odometry) {
        this.odometry = odometry;
    }

    @Override
    public Pose3D get() {
        this.odometry.update();
        Pose2D pos = this.odometry.getPosition();
        return pinToMy(pos.getX(DistanceUnit.MM), pos.getY(DistanceUnit.MM), pos.getHeading(AngleUnit.DEGREES));
    }

    @Override
    public boolean init() {
        this.odometry.resetPosAndIMU();
        return true;
    }

    @Override
    public void setPosition(Pose3D pos) {
        this.odometry.setPosition(myToPin(pos.position.x, pos.position.y, pos.rotation.z));
    }

    @Override
    public void cleanup() {
    }

    private static final double HALF_FIELD_IN = 72.0;
    private static final double MM_TO_IN = 1.0 / 25.4;
    private static final double IN_TO_MM = 25.4;

    /**
     * Convert Pinpoint coordinates (mm, center origin)
     * to your coordinate system (inches, bottom-left origin)
     */
    public static Pose3D pinToMy(double pinXmm, double pinYmm, double pinHeading) {

        // convert mm -> inches
        double x = pinXmm * MM_TO_IN;
        double y = pinYmm * MM_TO_IN;

        // matrix transform
        double myX = y;
        double myY = -x;

        // shift origin (center -> bottom-left)
        myX += HALF_FIELD_IN;
        myY += HALF_FIELD_IN;

        // heading conversion
        double heading = normalize(pinHeading + 180);

        return new Pose3D(
                new Vector3D(myX, myY, 0),
                new Vector3D(0, 0, heading),
                new Vector3D(),
                new Vector3D()
        );
    }

    /**
     * Convert your coordinates (inches, bottom-left origin)
     * to Pinpoint coordinates (mm, center origin)
     */
    public static Pose2D myToPin(double myX, double myY, double myHeading) {

        // shift origin (bottom-left -> center)
        double x = myX - HALF_FIELD_IN;
        double y = myY - HALF_FIELD_IN;

        // inverse matrix transform
        double pinX = -y;
        double pinY = x;

        // convert inches -> mm
        pinX *= IN_TO_MM;
        pinY *= IN_TO_MM;

        // heading conversion
        double heading = normalize(myHeading - 180);

        return new Pose2D(DistanceUnit.MM, pinX, pinY, AngleUnit.DEGREES, heading);
    }

    /**
     * Normalize angle to [0,360)
     */
    public static double normalize(double angle) {
        return ((angle % 360) + 360) % 360;
    }
}
