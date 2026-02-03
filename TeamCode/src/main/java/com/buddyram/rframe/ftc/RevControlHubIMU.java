package com.buddyram.rframe.ftc;

import com.buddyram.rframe.Odometry;
import com.buddyram.rframe.Vector3D;

public class RevControlHubIMU implements Odometry<Vector3D> {
    @Override
    public Vector3D get() {
        return null;
    }

    @Override
    public boolean init() {
        return false;
    }

    @Override
    public void setPosition(Vector3D pos) {

    }

    @Override
    public void cleanup() {

    }
}
