package com.buddyram.rframe;

public class HolonomicDriveInstruction {
    public final double rotation;
    public final double speed;
    public final double direction;

    public HolonomicDriveInstruction(double rotation, double speed, double direction) {
        this.rotation = rotation;
        this.speed = speed;
        this.direction = direction;
    }
}
