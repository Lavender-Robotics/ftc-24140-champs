package org.firstinspires.ftc.teamcode.opmodes.test;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.subsystems.FeederSubsystem;
@TeleOp(name = "Kickerr")
public class KickerTest extends OpMode {
    private FeederSubsystem feeder;

    private static final double STEP = 0.005;   // Her döngüde değişecek miktar (yavaşlık için küçült)
    private double targetPosition = 0.0;

    @Override
    public void init() {
        feeder = new FeederSubsystem(hardwareMap);
        targetPosition = feeder.getFeederPosition();
    }

    @Override
    public void loop() {
        // Y basılı tutuldukça pozisyonu sürekli yavaşça artır
        if (gamepad1.y) {
            targetPosition = Math.min(1.0, targetPosition + STEP);
            feeder.setPosition(targetPosition);
        }

        // A basılı tutuldukça pozisyonu sürekli yavaşça azalt
        if (gamepad1.a) {
            targetPosition = Math.max(0.0, targetPosition - STEP);
            feeder.setPosition(targetPosition);
        }

        // X = bir uca git (0.0) -> hornu burada tak, bu senin referansın
        if (gamepad1.x) {
            targetPosition = 0.0;
            feeder.setPosition(targetPosition);
        }

        // B = diğer uca git (1.0)
        if (gamepad1.b) {
            targetPosition = 1.0;
            feeder.setPosition(targetPosition);
        }

        telemetry.addData("X = uc (0.0) | B = uc (1.0) | Y/A = ince ayar", "");
        telemetry.addData("Target Position:", targetPosition);
        telemetry.addData("Servo Position:", feeder.getFeederPosition());
        telemetry.update();
    }
}