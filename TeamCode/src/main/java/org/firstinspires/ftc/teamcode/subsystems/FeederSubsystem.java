package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;


public class FeederSubsystem {
    private Servo feeder_servo;
    private boolean isTransformReady;



    private static final double START_POS = 0.57;      // En alt pozisyon (başlangıç)
    private static final double HIT_POS = 0.81;       // Top vuruş pozisyonu (0.2 5-0.30 arası)
    private boolean enabled = false;


    private final ElapsedTime timer = new ElapsedTime(); // The timer object
    // Time constants (in milliseconds)
    private static final double EXTEND_TIME_MS = 700.0; // Wait 1 second after extending
    private static final double RETRACT_TIME_MS = 1000.0; // Wait 2 seconds after retracting
    private FeederState currentState = FeederState.RETRACTED_IDLE;

    public enum FeederState {
        RETRACTED_IDLE, // Servo is retracted, waiting 2s
        EXTENDING,      // Action: Setting servo to HIT_POS
        EXTENDED_IDLE,  // Servo is extended, waiting 1s
        RETRACTING      // Action: Setting servo to START_POS
    }
    private boolean PosToggle = false;

    public FeederSubsystem(HardwareMap hw) {
        feeder_servo = hw.get(Servo.class, "servo_feeder");
        feeder_servo.setPosition(0.71);
        timer.reset(); // Start the timer
    }


    // Controls
    public void extendServo()   { feeder_servo.setPosition(HIT_POS); }
    public void retractServo()  { feeder_servo.setPosition(START_POS); }
    public void manualServo(double target)   { feeder_servo.setPosition(target);}

    public void periodicServo(){
        if (!enabled) {
            return; // Exit if the periodic behavior is not enabled
        }

        // State Machine logic
        switch (currentState) {
            case RETRACTED_IDLE:
                // Check if 2 seconds have passed since the servo was retracted
                if (timer.milliseconds() >= RETRACT_TIME_MS) {
                    if (!isTransformReadyCheck()) { setTransformReady(true);}
                    // Time to extend
                    extendServo();
                    timer.reset(); // Reset timer for the next state's wait
                    currentState = FeederState.EXTENDED_IDLE; // Move to the extended wait state
                }
                break;

            case EXTENDED_IDLE:
                // Check if 1 second has passed since the servo was extended
                if (timer.milliseconds() >= EXTEND_TIME_MS) {
                    // Time to retract
                    //  if (!isTransformReadyCheck()) { setTransformReady(true);}
                    retractServo();
                    timer.reset(); // Reset timer for the next state's wait
                    currentState = FeederState.RETRACTED_IDLE; // Move back to the retracted wait state
                }
                break;

            // The EXTENDING and RETRACTING states are typically only needed for
            // slow transitions, but for immediate servo actions, they are often
            // combined with the IDLE state transitions as shown above.
            // If you wanted to run the servo action once and then wait,
            // the above structure is the most efficient.
        }
    }
//    public void toggleServoPos(){
//        if (PosToggle){
//            runPeriodic(1000,setPosition(1));
//        }
//        if (!PosToggle){
//
//        }
//    }

    public void setPosition(double position) {
        double clampedPos = Math.max(0.0, Math.min(1.0, position));
        feeder_servo.setPosition(clampedPos);
    }


    // Toggles
    public void setEnabled(boolean on) { this.enabled = on; }
    public boolean isEnabled() { return enabled; }

    public void setTransformReady(boolean on) { this.isTransformReady = on;}
    public boolean isTransformReadyCheck() {return isTransformReady;}



    // Telemetry
    public double getFeederPosition() { return feeder_servo.getPosition(); }
}