package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class IntakeSubsystem {
    private final DcMotorEx intake_motor;

    public IntakeSubsystem(HardwareMap hw) {
        intake_motor = hw.get(DcMotorEx.class, "intake");
        intake_motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    }

    public void forward(double power) { intake_motor.setPower(Math.abs(power)); }
    public void reverse(double power) { intake_motor.setPower(-Math.abs(power)); }
    public void stop() { intake_motor.setPower(0); }
}