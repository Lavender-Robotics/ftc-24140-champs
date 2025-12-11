package org.firstinspires.ftc.teamcode.util;

// Basit “edge” tespit eden toggle: buton "false->true" yaptığında true döner.
public class Toggle {
    private boolean last = false;
    // update() true dönerse "bir kez" tetikle.
    public boolean update(boolean current) {
        boolean edge = current && !last;
        last = current;
        return edge;
    }
}