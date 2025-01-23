package com.buddyram.rframe;

import java.io.PrintStream;

public class PrintLogger extends BaseLogger {
    PrintStream out;

    public PrintLogger(PrintStream out) {
        this.out = out;
    }

    public void log(String caption, Object value) {
        this.out.print("\r" + caption + ": " + value.toString() + "                  ");
    }

    public void flush() {
        //this.out.flush();
    }
}
