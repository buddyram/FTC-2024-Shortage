package com.buddyram.rframe.ftc.decode.indexer;

import com.buddyram.rframe.BaseComponent;
import com.buddyram.rframe.ftc.decode.DecodeBot;
import com.buddyram.rframe.ftc.decode.indexer.ColorSensor.ColorMatch;
import com.qualcomm.robotcore.hardware.DcMotor;

public class Indexer extends BaseComponent<DecodeBot> {
    private final DcMotor motor;
    private final double tickPerRotation;
    private int currentSlot = 0;
    private ColorMatch[] slots;
    private ColorSensor colorSensor;

    public Mode getCurrentMode() {
        return currentMode;
    }

    public void setCurrentMode(Mode currentMode) {
        this.currentMode = currentMode;
    }

    private Mode currentMode = Mode.INTAKING;
    public enum Mode {
        INTAKING,
        OUTTAKING
    }


    public Indexer(DecodeBot robot, DcMotor motor, double ticksPerRotation, ColorMatch[] slots) {
        super(robot);
        this.motor = motor;
        this.slots = slots;
        this.tickPerRotation = ticksPerRotation; // 28 * 2.89 * 5.23
    }

    private void goToAngle(double angle) {
        this.motor.setTargetPosition((int) Math.floor(angle / 360.0 * tickPerRotation));
    }

    public void goToSlot(int newSlot) {
        this.currentSlot = newSlot;
        if (this.currentMode == Mode.INTAKING) {
            this.goToAngle(currentSlot * 120);
        } else if (this.currentMode == Mode.OUTTAKING) {
            this.goToAngle(currentSlot * 120 + 180);
        }
    }

    private void fillSlot(ColorMatch color) {
        slots[currentSlot] = color;
    }

    private void emptySlot() {
        slots[currentSlot] = ColorMatch.NONE;
    }

    public boolean isFull() {
        return (slots[0] != ColorMatch.NONE && slots[1] != ColorMatch.NONE && slots[2] != ColorMatch.NONE);
    }

    public int getNearestEmpty() throws Exception {
        if (this.isFull()) {
            throw new Exception("Indexer is full");
        } else {
            if (slots[0] != ColorMatch.NONE) {
                return 0;
            } else if (slots[1] != ColorMatch.NONE) {
                return 1;
            } else if (slots[2] != ColorMatch.NONE) {
                return 2;
            }
        }
        throw new Exception("Error occurred. Indexer is not full, but could not find empty slot.");
    }

    public void ifFullGoToNext () {
        // TODO implement function
    }
}
