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
        this.goToSlot(currentSlot);
    }

    private Mode currentMode = Mode.INTAKING;
    public enum Mode {
        INTAKING,
        OUTTAKING
    }


    public Indexer(DecodeBot robot, DcMotor motor, double ticksPerRotation, ColorMatch[] slots, ColorSensor sensor) {
        super(robot);
        this.motor = motor;
        this.slots = slots;
        this.tickPerRotation = ticksPerRotation; // 28 * 2.89 * 5.23
        this.colorSensor = sensor;
    }

    private void goToAngle(double angle) {
        this.motor.setTargetPosition((int) Math.floor(angle / 360.0 * tickPerRotation));
    }

    public boolean isReady() {
        return Math.abs(motor.getCurrentPosition() - motor.getTargetPosition()) < 20;
    }

    public void goToSlot(int newSlot) {
        this.currentSlot = newSlot;
        if (this.currentMode == Mode.INTAKING) {
            this.goToAngle(currentSlot * 120);
        } else if (this.currentMode == Mode.OUTTAKING) {
            this.goToAngle(currentSlot * 120 + 210);
        }
    }

    public void fillSlot(ColorMatch color) {
        slots[currentSlot] = color;
    }

    public void emptySlot() {
        slots[currentSlot] = ColorMatch.NONE;
    }

    public boolean isFull() {
        return (slots[0] != ColorMatch.NONE && slots[1] != ColorMatch.NONE && slots[2] != ColorMatch.NONE);
    }

    public boolean isEmpty() {
        return (slots[0] == ColorMatch.NONE && slots[1] == ColorMatch.NONE && slots[2] == ColorMatch.NONE);
    }

    public int getNearestEmpty() throws Exception {
        if (this.isFull()) {
            throw new Exception("Indexer is full");
        } else {
            if (slots[0] == ColorMatch.NONE) {
                return 0;
            } else if (slots[1] == ColorMatch.NONE) {
                return 1;
            } else if (slots[2] == ColorMatch.NONE) {
                return 2;
            }
        }
        throw new Exception("Error occurred. Indexer is not full, but could not find empty slot.");
    }
    public int getNearestFull() throws Exception {
        if (this.isEmpty()) {
            throw new Exception("Indexer is empty");
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

    public void ifFullGoToNext() throws Exception {
        if (this.colorSensor.indexerBall() != ColorMatch.NONE && isReady()) {
            if (isFull()) {
                throw new Exception("full!!!");
            } else {
                fillSlot(this.colorSensor.indexerBall() );
                goToSlot(getNearestEmpty());
            }
        }
    }
}
