package com.buddyram.rframe.ftc;

import com.buddyram.rframe.ftc.intothedeep.BaseArmAction;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;


class SpecimenHangAction extends ArmPositionalAction {
    public SpecimenHangAction(int claw, double wrist, int extension, int angle) {
        super(claw, wrist, extension, angle);
    }

    public boolean runArm(RobotArm arm) {
        super.runArm(arm);
        this.claw = 1;
        arm.claw.setPosition(this.claw);
        this.waitForCompletion();
        return true;
    }
}

public class RobotArm {
    public final ArmShoulder angle;
    public final DcMotor extension;
    public final Servo wrist;
    public final Servo claw;

    public RobotArm(Servo claw, Servo wrist, DcMotor extension, ArmShoulder angle){
        this.claw = claw;
        this.wrist = wrist;
        this.extension = extension;
        this.angle = angle;
    }
}

