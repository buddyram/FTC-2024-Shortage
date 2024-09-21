package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;


public class Robot {

    double speed = 0;
    double direction = 0;
    double FlBrSpeed = 0;
    double FrBlSpeed = 0;
    DcMotor[] motors;

    public Robot(DcMotor Fl, DcMotor Fr, DcMotor Bl, DcMotor Br) {
        this.motors = new DcMotor[] {Fl, Fr, Bl, Br};
    }

    public void setPowers(double FlBr, double FrBl) {
        this.FlBrSpeed = FlBr;
        this.FrBlSpeed = FrBl;
    }

    public void updateMotors() {
        this.motors[0].setPower(FlBrSpeed);
        this.motors[1].setPower(FrBlSpeed);
        this.motors[2].setPower(FlBrSpeed);
        this.motors[3].setPower(FrBlSpeed);
    }

    public void update() {
        this.setPowers(Math.sin(Math.toRadians(direction) + Math.PI / 4) * speed, Math.sin(Math.toRadians(direction) - Math.PI / 4) * speed);
        updateMotors();
    }

    public setDir

}

