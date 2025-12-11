package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class ShooterSubsystem {
    private final DcMotorEx shooter_motor_p;
    private final DcMotorEx shooter_motor_s;

    public ShooterSubsystem(HardwareMap hw) {
        shooter_motor_p = hw.get(DcMotorEx.class, "motor_shooter_p");
        shooter_motor_s = hw.get(DcMotorEx.class, "motor_shooter_s");

        shooter_motor_p.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        shooter_motor_s.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);

        shooter_motor_p.setDirection(DcMotorSimple.Direction.REVERSE);
        shooter_motor_s.setDirection(DcMotorSimple.Direction.FORWARD);

    }

    public void forward(double power) {
        shooter_motor_p.setPower(Math.abs(power));
        shooter_motor_s.setPower(Math.abs(power));
    }
    public void reverse(double power) {
        shooter_motor_p.setPower(-Math.abs(power));
        shooter_motor_s.setPower(-Math.abs(power));
    }
    public void stop() {
        shooter_motor_p.setPower(0);
        shooter_motor_s.setPower(0);
    }
}