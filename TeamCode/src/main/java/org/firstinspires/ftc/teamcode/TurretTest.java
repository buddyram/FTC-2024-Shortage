package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.AnalogInput;
import com.qualcomm.robotcore.hardware.PwmControl;
import com.qualcomm.robotcore.hardware.ServoImplEx;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

@TeleOp(name = "Turret Test", group = "Test")
public class TurretTest extends LinearOpMode {

    // ---- Calibrate these ----
    static final double SERVO_RANGE_DEG  = 355.0;        // servo travel across 500–2500 µs
    static final double GEAR_RATIO       = 64.0 / 70.0;  // turret deg per servo deg (≈0.914)
    static final double TURRET_RANGE_DEG = SERVO_RANGE_DEG * GEAR_RATIO;  // ≈324°
    static final double CENTER_OFFSET_DEG = 0.0;         // trim so 0° = turret pointing straight ahead
    static final double SAFE_MIN_DEG = -150.0;           // stay inside wiring / hard stops
    static final double SAFE_MAX_DEG =  150.0;
    static final double MAX_SPEED_DEG_PER_S = 180.0;     // gentle slew so 37% power isn't overwhelmed

    private ServoImplEx turret;
    private AnalogInput feedback;   // optional; null if not wired

    @Override
    public void runOpMode() {
        turret = hardwareMap.get(ServoImplEx.class, "turret");
        turret.setPwmRange(new PwmControl.PwmRange(500, 2500));  // Axon uses the full 500–2500 µs

        try { feedback = hardwareMap.get(AnalogInput.class, "turretFeedback"); }
        catch (Exception e) { feedback = null; }

        double targetDeg = 0.0;     // where we want to be
        double commandDeg = 0.0;    // where we've commanded so far (slewed toward target)
        setTurretDeg(commandDeg);   // centers on INIT — check the season manual allows servo motion in INIT

        telemetry.addLine("Turret centered. Press START.");
        telemetry.update();
        waitForStart();

        ElapsedTime timer = new ElapsedTime();
        while (opModeIsActive()) {
            double dt = timer.seconds();
            timer.reset();

            // Presets
            if (gamepad1.dpad_up)    targetDeg = 0;
            if (gamepad1.dpad_left)  targetDeg = -90;
            if (gamepad1.dpad_right) targetDeg =  90;

            // Manual nudge with the left stick (deg/s)
            targetDeg += -gamepad1.left_stick_x * 120.0 * dt;
            targetDeg = Range.clip(targetDeg, SAFE_MIN_DEG, SAFE_MAX_DEG);

            // Slew-rate limit: move the command toward the target at a capped speed
            double maxStep = MAX_SPEED_DEG_PER_S * dt;
            commandDeg += Range.clip(targetDeg - commandDeg, -maxStep, maxStep);
            setTurretDeg(commandDeg);

            // A = go limp (servo releases), B = re-enable
            if (gamepad1.a) turret.setPwmDisable();
            if (gamepad1.b) turret.setPwmEnable();

            telemetry.addData("target (deg)", "%.1f", targetDeg);
            telemetry.addData("command (deg)", "%.1f", commandDeg);
            telemetry.addData("servo pos", "%.3f", turret.getPosition());
            if (feedback != null) {
                telemetry.addData("feedback V", "%.3f / %.2f", feedback.getVoltage(), feedback.getMaxVoltage());
            }
            telemetry.update();
        }
    }

    /** Turret angle (deg, 0 = forward) -> servo position 0..1 */
    private void setTurretDeg(double turretDeg) {
        double servoDeg = (turretDeg + CENTER_OFFSET_DEG) / GEAR_RATIO;   // turret -> servo degrees
        double pos = 0.5 + servoDeg / SERVO_RANGE_DEG;                    // 0.5 = servo center
        turret.setPosition(Range.clip(pos, 0.0, 1.0));
    }
}
