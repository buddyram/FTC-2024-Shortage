package com.buddyram.rosebot.head;

import com.buddyram.rframe.BaseComponent;
import com.buddyram.rframe.Robot;

public class Head extends BaseComponent<Robot> {
    public Extension extension;

    public Head(Robot robot, Extension extension) {
        super(robot);
        this.extension = extension;
    }
}
