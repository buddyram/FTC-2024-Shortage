package com.buddyram.rframe.ftc.intothedeep;

import com.buddyram.rframe.Pose3D;
import com.buddyram.rframe.Vector3D;

public abstract class Robot {
    public enum AscentLevel {
        LEVEL_1,
        LEVEL_2,
        LEVEL_3
    }
    public enum BasketHeight {
        HIGH,
        LOW
    }
    public abstract void rotate(float degrees);
    public abstract void navigateTo(Pose3D position);
    public abstract void startAscent(AscentLevel level);
    public abstract void retrieveBlock(Pose3D blockPosition);
    public abstract void placeBlockInBasket(BasketHeight basketHeight);
    public abstract void placeBlockInNetZone();
    public abstract void park();
    public abstract void placeBlockInObservationZone();
}
