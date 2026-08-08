package com.buddyram.rframe.ftc.decode;

import com.buddyram.rframe.Pose3D;
import com.buddyram.rframe.ftc.decode.indexer.ColorSensor;

public class Globals {
    public static Pose3D POSITION = null;
    public static Boolean DID_RUN_AUTO = false;
    public static ColorSensor.ColorMatch[] INDEXER = new ColorSensor.ColorMatch[]{
            ColorSensor.ColorMatch.MATCH_PURPLE,
            ColorSensor.ColorMatch.MATCH_PURPLE,
            ColorSensor.ColorMatch.MATCH_GREEN
    };
    public static Boolean IS_RED = null;
}
