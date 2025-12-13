package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

public class FeederSubsystem {
    private Servo feeder_servo;

    private static final double START_POS = 0.57;
    private static final double HIT_POS = 0.8;
    private boolean enabled = false;


    public FeederSubsystem(HardwareMap hw) {
        feeder_servo = hw.get(Servo.class, "servo_feeder");
        feeder_servo.setPosition(START_POS);
    }

    public void extendServo()   { feeder_servo.setPosition(HIT_POS); }
    public void retractServo()  { feeder_servo.setPosition(START_POS); }

    // Telemetry
    public void setEnabled(boolean on) { this.enabled = on; }
    public boolean isEnabled() { return enabled; }

}