package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class IntakeSubsystem {
    private final DcMotorEx intake;

    public IntakeSubsystem(HardwareMap hw) {
        intake = hw.get(DcMotorEx.class, "motor_intake");
        intake.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    }

    public void forward(double power) { intake.setPower(Math.abs(power)); }
    public void reverse(double power) { intake.setPower(-Math.abs(power)); }
    public void stop() { intake.setPower(0); }
}