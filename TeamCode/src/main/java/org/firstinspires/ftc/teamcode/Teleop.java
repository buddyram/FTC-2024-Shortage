package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

@Override
    public void init() {
        

        leftDrive1  = hardwareMap.get(DcMotor.class, "leftDrive1");
        leftDrive2  = hardwareMap.get(DcMotor.class, "leftDrive2");
        rightDrive1 = hardwareMap.get(DcMotor.class, "rightDrive1");
        rightDrive2 = hardwareMap.get(DcMotor.class, "rightDrive1");

        leftDrive1.setDirection(DcMotor.Direction.FORWARD);
        rightDrive2.setDirection(DcMotor.Direction.REVERSE);
        leftDrive2.setDirection(DcMotor.Direction.FORWARD);
        rightDrive2.setDirection(DcMotor.Direction.REVERSE);
        leftDrive1.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        rightDrive2.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        leftDrive1.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        rightDrive2.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        
    }

@Override
public void init_loop() {
}

@Override
    public void start() {
        runtime.reset();
    }


//Drive
@Override
public void loop(){
  double leftPower;
  double rightPower;
  double drive = -gamepad1.left_stick_y;
  double turn  =  gamepad1.right_stick_x;
  leftPower    = Range.clip(drive + turn, -1.0, 1.0) ;
  rightPower   = Range.clip(drive - turn, -1.0, 1.0) ;
  leftDrive.setPower(leftPower);
  rightDrive.setPower(rightPower);

        @Override
    public void stop() {
    }
}
