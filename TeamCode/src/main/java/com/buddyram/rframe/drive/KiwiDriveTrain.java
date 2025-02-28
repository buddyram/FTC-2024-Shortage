package com.buddyram.rframe.drive;

import com.buddyram.rframe.Motor;

public class KiwiDriveTrain implements HolonomicDriveTrain {
    private final Motor f;
    private final Motor bl;
    private final Motor br;
    public final double powerLimit;

    /**
     *
     * @param f front left motor   _
     * @param bl back left motor  /
     * @param br back right motor   \
     */
    public KiwiDriveTrain(Motor f, Motor bl, Motor br) {
        this(f, bl, br, 1);
    }

    public KiwiDriveTrain(Motor f, Motor bl, Motor br, double powerLimit) {
        this.f = f;
        this.bl = bl;
        this.br = br;
        this.powerLimit = powerLimit;
    }

    public void drive(HolonomicDriveInstruction instruction) {
        // Motors can only get powers from -1 to 1 so this distributes the power between rotation and movement
        double maxSpeed = Math.max(Math.abs(instruction.speed + instruction.rotation) / this.powerLimit, 1);
        double speed = instruction.speed / maxSpeed;
        double rotation = instruction.rotation / maxSpeed;
        double incX = Math.cos(Math.toRadians(instruction.direction)) * speed;
        double incY = Math.sin(Math.toRadians(instruction.direction)) * speed;
        double front = incX;
        double backLeft = -0.5 * incX + Math.sqrt(3) / 2 * incY;
        double backRight = -0.5 * incX - Math.sqrt(3) / 2 * incY;
        f.setPower(front + rotation);
        bl.setPower(backLeft + rotation);
        br.setPower(backRight + rotation);
    }
}
