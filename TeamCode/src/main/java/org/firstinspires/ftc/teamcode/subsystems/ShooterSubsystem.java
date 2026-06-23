package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class ShooterSubsystem {
    private final DcMotorEx shooter_motor_p;
    private final DcMotorEx shooter_motor_s;
    private final Servo shooter_servo;


    // Servo Constants & Variables
    private static final double POS_LOW = 0.0;   // minimum angle
    private static final double POS_HIGH = 1.0;  // maximum angle
    private static final double SERVO_SPEED = 0.005;
    private double currentTargetPosition = POS_LOW;



    private boolean enabled = false;


    public ShooterSubsystem(HardwareMap hw) {
        shooter_motor_p = hw.get(DcMotorEx.class, "shootersol");
        shooter_motor_s = hw.get(DcMotorEx.class, "shootersag");

        shooter_motor_p.setDirection(DcMotorSimple.Direction.REVERSE);
        shooter_motor_s.setDirection(DcMotorSimple.Direction.FORWARD);

        shooter_motor_p.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        shooter_motor_s.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);

        shooter_motor_p.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        shooter_motor_s.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        shooter_motor_p.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        shooter_motor_s.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        shooter_servo = hw.get(Servo.class, "servo_hood");
        shooter_servo.setPosition(POS_LOW);
    }

    public void forward(double velocity) {
        shooter_motor_p.setVelocity(Math.abs(velocity));
        shooter_motor_s.setVelocity(Math.abs(velocity));
    }
    public void reverse(double velocity) {
        shooter_motor_p.setVelocity(-Math.abs(velocity));
        shooter_motor_s.setVelocity(-Math.abs(velocity));
    }
    public void stop() {
        shooter_motor_p.setVelocity(0);
        shooter_motor_s.setVelocity(0);
    }

    public void moveHoodManually(boolean up, boolean down) {
        if (up && currentTargetPosition < POS_HIGH) {
            currentTargetPosition = Math.min(POS_HIGH, currentTargetPosition + SERVO_SPEED);
            shooter_servo.setPosition(currentTargetPosition);
        } else if (down && currentTargetPosition > POS_LOW) {
            currentTargetPosition = Math.max(POS_LOW, currentTargetPosition - SERVO_SPEED);
            shooter_servo.setPosition(currentTargetPosition);
        }
    }


    // Telemetry
    public void setEnabled(boolean on) { this.enabled = on; if (!on) stop(); }
    public boolean isEnabled() { return enabled; }
    public double getVelocity() { return shooter_motor_p.getVelocity(); }
    public double getHoodPosition() { return currentTargetPosition; }
}