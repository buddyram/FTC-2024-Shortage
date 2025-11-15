package com.buddyram.rframe.ftc.decode;

import com.buddyram.rframe.Vector3D;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;


public class DecodeGameMap {


    private final Polygon closeLaunch;
    private final Polygon farLaunch;
    private final Polygon humanPlayerBlue;
    private final Polygon secretTunnelBlue;
    public DecodeGameMap() throws ParseException {
        WKTReader reader = new WKTReader();
        this.closeLaunch = (Polygon) reader.read("POLYGON ((0 144, 72 72, 144 144))");
        this.farLaunch = (Polygon) reader.read("POLYGON ((48 0, 72 24, 96 0))");
        this.humanPlayerBlue = (Polygon) reader.read("POLYGON ((0 24, 24 24, 24 0, 0 0))");
        this.secretTunnelBlue = (Polygon) reader.read("POLYGON ((0 24, 24 24, 24 0, 0 0))");
    }

    public enum Zones {
        CLOSE_LAUNCH,
        FAR_LAUNCH,
        HUMAN_PLAYER,
        SECRET_TUNNEL,
        PARK,
        OTHER
    }


    public enum Sides {
        RED_GOAL,
        BLUE_GOAL
    }

    public Zones getZone(Vector3D position) {
        return null;
    }
}
