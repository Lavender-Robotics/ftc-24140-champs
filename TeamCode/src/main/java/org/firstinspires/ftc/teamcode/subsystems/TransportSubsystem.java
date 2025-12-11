package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.CRServo;

public class TransportSubsystem {
    private CRServo transport_servo_p;
    private CRServo transport_servo_s;


    public TransportSubsystem(HardwareMap hw) {
        transport_servo_p = hw.get(CRServo.class, "servo_transport");
        transport_servo_s = hw.get(CRServo.class, "servo_transport1");
    }

    public void forward(double power) {
        transport_servo_p.setPower(Math.abs(power));
        transport_servo_s.setPower(-Math.abs(power));
    }
    public void reverse(double power) {
        transport_servo_p.setPower(-Math.abs(power));
        transport_servo_s.setPower(Math.abs(power));
    }
    public void stop() {
        transport_servo_p.setPower(0);
        transport_servo_s.setPower(0);
    }
}