package com.buddyram.rframe.ftc.decode;

import com.buddyram.rframe.Logger;
import com.buddyram.rframe.Odometry;
import com.buddyram.rframe.Pose3D;
import com.buddyram.rframe.RobotException;
import com.buddyram.rframe.Vector3D;
import com.buddyram.rframe.actions.RobotAction;
import com.buddyram.rframe.drive.HolonomicDriveInstruction;
import com.buddyram.rframe.drive.HolonomicDriveTrain;
import com.buddyram.rframe.drive.Navigatable;
import com.buddyram.rframe.ftc.decode.action.ShootAction;
import com.buddyram.rframe.ftc.decode.intake.Intake;
import com.buddyram.rframe.ftc.decode.launcher.Flywheel;
import com.buddyram.rframe.ftc.decode.launcher.Launcher;
import com.buddyram.rframe.ftc.decode.launcher.Sweeper;
import com.buddyram.rframe.ftc.intothedeep.ShortageBot;
import com.buddyram.rframe.ftc.intothedeep.actions.RobotActions;

import java.util.ArrayList;

public class DecodeBot implements Navigatable<HolonomicDriveTrain> {
    public Logger logger;
    public Odometry<Pose3D> odometry;
    public HolonomicDriveTrain drive;

    public Intake getIntake() {
        return intake;
    }

    public Intake intake;

    public Launcher getLauncher() {
        return launcher;
    }

    public Launcher launcher;

    public DecodeBot() {
        this(null, null, null, null, null);
    }
    public DecodeBot(Logger logger, Odometry<Pose3D> odometry, HolonomicDriveTrain drive, Launcher launcher, Intake intake) {
        this.init(logger, odometry, drive, launcher, intake);
    }

    public void init(Logger logger, Odometry<Pose3D> odometry, HolonomicDriveTrain drive, Launcher launcher, Intake intake) {
        this.logger = logger;
        this.odometry = odometry;
        this.drive = drive;
        this.launcher = launcher;
        this.intake = intake;
    }

    @Override
    public Odometry<Pose3D> getOdometry() {
        return this.odometry;
    }

    @Override
    public HolonomicDriveTrain getDrive() {
        return this.drive;
    }

    @Override
    public Logger getLogger() {
        return this.logger;
    }

    @Override
    public boolean isActive() {
        return true;
    }

    public HolonomicDriveInstruction calculateRelativeDriveInstruction(Vector3D relativeTarget, double speed) {
        return this.calculateDriveInstruction(relativeTarget.add(this.odometry.get().position), speed);
    }

    public HolonomicDriveInstruction calculateDriveInstruction(Vector3D target, double speed) {
        double rotationInstruction = 0, driveSpeedInstruction = 0, driveAngleInstruction = 0;
        Pose3D pos = this.odometry.get();
        driveSpeedInstruction = speed;
        driveAngleInstruction = pos.position.calculateRotation(target).z;

        return new HolonomicDriveInstruction(rotationInstruction, driveSpeedInstruction, driveAngleInstruction);
    }

    public void runAutonomous() throws RobotException, InterruptedException {
        ArrayList<RobotAction<DecodeBot>> actions = new ArrayList<>();
        actions.add(Sweeper.moveTo(-1));
        actions.add(Flywheel.setRPMTo(3100));
        actions.add(BotUtils.wait(600));
        actions.add(BotUtils.rotateTo(45));
        actions.add(new ShootAction());
        actions.add(BotUtils.rotateTo(90));
        actions.add(BotUtils.driveTo(new Vector3D(29.95366666666666, 88.38416666666666, 0), false));
        actions.add(BotUtils.wait(600));
        actions.add(BotUtils.driveTo(new Vector3D(60, 84, 0), false));
        actions.add(BotUtils.rotateTo(45));
        actions.add(new ShootAction());
        actions.add(BotUtils.rotateTo(90));
        actions.add(BotUtils.driveTo(new Vector3D(45, 64.38416666666666, 0), false));
        actions.add(BotUtils.driveTo(new Vector3D(29.95366666666666, 64.38416666666666, 0), false));
        actions.add(BotUtils.wait(600));
        actions.add(BotUtils.driveTo(new Vector3D(60, 84, 0), false));
        actions.add(BotUtils.rotateTo(45));
        actions.add(new ShootAction());
        actions.add(BotUtils.rotateTo(90));
        actions.add(BotUtils.driveTo(new Vector3D(45, 40.38416666666666, 0), false));
        actions.add(BotUtils.driveTo(new Vector3D(29.95366666666666, 40.38416666666666, 0), false));
        actions.add(BotUtils.wait(600));
        actions.add(BotUtils.driveTo(new Vector3D(60, 84, 0), false));
        actions.add(BotUtils.rotateTo(45));
        actions.add(Flywheel.setRPMTo(3100));
        actions.add(new ShootAction());
        actions.add(BotUtils.rotateTo(0));

        while (this.isActive() && !actions.isEmpty()) {
            if (actions.get(0).run(this)) {
                actions.remove(0);
                System.out.println("next!! " + actions.size());
            }
        }
    }

}
