package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.sparkfun.SparkFunOTOS;
import com.qualcomm.robotcore.hardware.I2cDeviceSynch;
import com.qualcomm.robotcore.hardware.configuration.annotations.DeviceProperties;
import com.qualcomm.robotcore.hardware.configuration.annotations.I2cDeviceType;


@I2cDeviceType
@DeviceProperties(
        name = "Much Better SparkFun OTOS",
        xmlTag = "BetterSparkFunOTOS",
        description = "Better SparkFun Qwiic Optical Tracking Odometry Sensor"
)
public class BetterSparkFunOTOS extends SparkFunOTOS {
    public BetterSparkFunOTOS(I2cDeviceSynch deviceClient) {
        super(deviceClient);
    }

    @Override
    public boolean setLinearScalar(double scalar) {
        // Check if the scalar is out of bounds


//        if (scalar < MIN_SCALAR || scalar > MAX_SCALAR)
//            return false;

        // Convert to integer, multiples of 0.1% (+0.5 to round instead of truncate)
        byte rawScalar = (byte) ((scalar - 1.0) * 1000 + 0.5);

        // Write the scalar to the device
        deviceClient.write8(REG_SCALAR_LINEAR, rawScalar);

        // Done!
        return true;
    }
}
