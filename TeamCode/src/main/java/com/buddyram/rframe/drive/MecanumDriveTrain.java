package com.buddyram.rframe.drive;

import com.buddyram.rframe.Motor;

public class MecanumDriveTrain implements HolonomicDriveTrain {
    private final Motor fl;
    private final Motor fr;
    private final Motor bl;
    private final Motor br;
    public final double powerLimit;

    /**
     *
     * @param fl front left motor
     * @param fr front right motor
     * @param bl back left motor
     * @param br back right motor
     */
    public MecanumDriveTrain(Motor fl, Motor fr, Motor bl, Motor br) {
        this(fl, fr, bl, br, 1);
    }

    public MecanumDriveTrain(Motor fl, Motor fr, Motor bl, Motor br, double powerLimit) {
        this.fl = fl;
        this.fr = fr;
        this.bl = bl;
        this.br = br;
        this.powerLimit = powerLimit;
    }

    public void drive(HolonomicDriveInstruction instruction) {
        // Motors can only get powers from -1 to 1 so this distributes the power between rotation and movement
        double maxSpeed = Math.max(Math.abs(instruction.speed + instruction.rotation) / this.powerLimit, 1);
        double speed = instruction.speed / maxSpeed;
        double rotation = instruction.rotation / maxSpeed;
        // This corresponds to the front left motor and the back right motor
        double leftCross = Math.sin(Math.toRadians(instruction.direction) + Math.PI / 4) * speed;
        // This corresponds to the front right motor and the back left motor
        double rightCross = Math.sin(Math.toRadians(instruction.direction) - Math.PI / 4) * speed;
        fl.setPower(leftCross - rotation);
        fr.setPower(rightCross + rotation);
        bl.setPower(rightCross - rotation);
        br.setPower(leftCross + rotation);
    }
}
