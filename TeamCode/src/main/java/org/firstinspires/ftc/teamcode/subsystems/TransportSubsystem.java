package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.CRServo;

public class TransportSubsystem {
    private CRServo transport_servo;
    private CRServo transport_servo1;


    public TransportSubsystem(HardwareMap hw) {
        transport_servo = hw.get(CRServo.class, "servo_transport");
        transport_servo1 = hw.get(CRServo.class, "servo_transport1");
    }

    public void forward(double power) {
        transport_servo.setPower(Math.abs(power));
        transport_servo1.setPower(-Math.abs(power));
    }
    public void reverse(double power) {
        transport_servo.setPower(-Math.abs(power));
        transport_servo1.setPower(Math.abs(power));
    }
    public void stop() {
        transport_servo.setPower(0);
        transport_servo1.setPower(0);
    }
}