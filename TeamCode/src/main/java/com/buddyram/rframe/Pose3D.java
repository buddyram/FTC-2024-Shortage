package com.buddyram.rframe;

public class Pose3D {
    public final Vector3D position;
    public final Vector3D rotation;
    public final Vector3D positionVelocity;
    public final Vector3D rotationVelocity;

    public Pose3D(Vector3D position, Vector3D rotation, Vector3D positionVelocity, Vector3D rotationVelocity) {
        this.position = position;
        this.rotation = rotation;
        this.positionVelocity = positionVelocity;
        this.rotationVelocity = rotationVelocity;
    }

    public Pose3D() {
        this(new Vector3D(), new Vector3D(), new Vector3D(), new Vector3D());
    }


    public Pose3D add(Pose3D other) {
        return new Pose3D(this.position.add(other.position), this.rotation.add(other.rotation), this.positionVelocity.add(other.positionVelocity), this.rotationVelocity.add(other.rotationVelocity));
    }
}
