//package com.buddyram.rframe;
//
//public class DefaultOdometrySensor extends DefaultBroadcaster<Vector3D> implements Odometry<Vector3D> {
//    private Vector3D point;
//
//    public DefaultOdometrySensor(Vector3D startingPoint) {
//        this.point = startingPoint;
//    }
//
//    public Vector3D get() {
//        return point;
//    }
//
//    public void update(Vector3D point) {
//        this.point = point;
//        this.broadcast(point);
//    }
//}
