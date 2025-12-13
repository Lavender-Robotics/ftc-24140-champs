package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class IntakeSubsystem {
    private final DcMotorEx intake_motor;
    private final CRServo intake_servo_p;
    private final CRServo intake_servo_s;

    private boolean enabled = false;

    public IntakeSubsystem(HardwareMap hw) {
        intake_motor = hw.get(DcMotorEx.class, "motor_intake");
        intake_servo_p = hw.get(CRServo.class, "servo_transport");
        intake_servo_s = hw.get(CRServo.class, "servo_transport1");

        intake_motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    }

    public void motor_forward(double power) { intake_motor.setPower(Math.abs(power)); }
    public void motor_reverse(double power) { intake_motor.setPower(-Math.abs(power)); }
    public void motor_stop() { intake_motor.setPower(0); }

    public void servo_forward(double power) {
        intake_servo_p.setPower(Math.abs(power));
        intake_servo_s.setPower(-Math.abs(power));
    }
    public void servo_reverse(double power) {
        intake_servo_p.setPower(-Math.abs(power));
        intake_servo_s.setPower(Math.abs(power));
    }
    public void servo_stop() {
        intake_servo_p.setPower(0);
        intake_servo_s.setPower(0);
    }


    // Telemetry
    public void setEnabled(boolean on) { this.enabled = on; }
    public boolean isEnabled() { return enabled; }

}