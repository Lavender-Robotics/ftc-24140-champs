package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

public class FeederSubsystem {
    private Servo feeder_servo;


    private static final double START_POS = 0.57;      // En alt pozisyon (başlangıç)
    private static final double HIT_POS = 0.8;       // Top vuruş pozisyonu (0.25-0.30 arası)
    private boolean enabled = false;


    public FeederSubsystem(HardwareMap hw) {
        feeder_servo = hw.get(Servo.class, "servo_feeder");
        feeder_servo.setPosition(START_POS);
    }


    // Controls
    public void extendServo()   { feeder_servo.setPosition(HIT_POS); }
    public void retractServo()  { feeder_servo.setPosition(START_POS); }
    public void setPosition(double position) {
        double clampedPos = Math.max(0.0, Math.min(1.0, position));
        feeder_servo.setPosition(clampedPos);
    }


    // Toggles
    public void setEnabled(boolean on) { this.enabled = on; }
    public boolean isEnabled() { return enabled; }


    // Telemetry
    public double getFeederPosition() { return feeder_servo.getPosition(); }
}