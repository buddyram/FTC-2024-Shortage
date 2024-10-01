package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;


public class BaseRobot {

    double speed = 0;
    double movementDirection = 0;
    double robotSpinSpeed = 0;
    double FlBrSpeed = 0;
    double FrBlSpeed = 0;
    DcMotor[] motors;
    double rightSideChange = 0;
    double leftSideChange = 0;
    double[] errorCorrectionMultipliers = new double[4];

    /**
     * @param fl front left motor
     * @param fr front right motor
     * @param bl back left motor
     * @param br back right motor
     */
    public BaseRobot(DcMotor fl, DcMotor fr, DcMotor bl, DcMotor br) {
        this.motors = new DcMotor[] {fl, fr, bl, br};
    }

    public void setPowers(double FlBr, double FrBl) {
        this.FlBrSpeed = FlBr;
        this.FrBlSpeed = FrBl;
    }

    public void updateMotors() {
        this.motors[0].setPower((this.FlBrSpeed + this.leftSideChange) * this.errorCorrectionMultipliers[0]);
        this.motors[1].setPower((this.FrBlSpeed + this.rightSideChange) * this.errorCorrectionMultipliers[1]);
        this.motors[2].setPower((this.FrBlSpeed + this.leftSideChange) * this.errorCorrectionMultipliers[2]);
        this.motors[3].setPower((this.FlBrSpeed + this.rightSideChange) * this.errorCorrectionMultipliers[3]);
    }

    public void update() {
        this.setPowers(Math.sin(Math.toRadians(this.movementDirection) + Math.PI / 4) * this.speed, Math.sin(Math.toRadians(this.movementDirection) - Math.PI / 4) * this.speed);
        this.rightSideChange = this.robotSpinSpeed;
        this.leftSideChange = -this.robotSpinSpeed;
        this.updateMotors();
    }

    public void setDirection(double dir) {
        this.movementDirection = dir;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public void setRobotDirection(double dir) {
        this.robotSpinSpeed = dir;
    }

    public void setErrorCorrectionMultipliers(double[] newErrorCorrectionMultipliers) {
        this.errorCorrectionMultipliers = newErrorCorrectionMultipliers;
    }
}

