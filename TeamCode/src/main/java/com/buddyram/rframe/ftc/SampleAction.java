//package com.buddyram.rframe.ftc;
//
//package com.buddyram.rframe.ftc;
//
//import com.buddyram.rframe.ftc.intothedeep.BaseArmAction;
//import com.qualcomm.robotcore.hardware.DcMotor;
//import com.qualcomm.robotcore.hardware.Servo;
//
//
//class SampleAction extends ArmPositionalAction {
//    public SampleAction(int claw, double wrist, int extension, int angle) {
//        super(claw, wrist, extension, angle);
//    }
//
//    public boolean runArm(RobotArm arm) {
//        arm.angle.setTargetPosition(-6);
//        this.waitForCompletion();
//        arm.extension.setTargetPosition(3080);
//    }
//}