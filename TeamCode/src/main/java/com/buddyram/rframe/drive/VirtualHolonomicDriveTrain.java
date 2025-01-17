package com.buddyram.rframe.drive;

public class VirtualHolonomicDriveTrain implements HolonomicDriveTrain {
    final double maxSpeedInchesPerSecond;
    final double maxRotationsPerSecond;
    private HolonomicDriveInstruction lastInstruction;

    public VirtualHolonomicDriveTrain(double maxSpeedInchesPerSecond, double maxRotationsPerSecond) {
        this.maxSpeedInchesPerSecond = maxSpeedInchesPerSecond;
        this.maxRotationsPerSecond = maxRotationsPerSecond;
        this.lastInstruction = new HolonomicDriveInstruction(0, 0, 0);
    }

    public void drive(HolonomicDriveInstruction instruction) {
        this.lastInstruction = instruction;
    }

    public HolonomicDriveInstruction getLastInstruction() {
        return lastInstruction;
    }
}
