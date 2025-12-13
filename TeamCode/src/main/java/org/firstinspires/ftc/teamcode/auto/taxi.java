import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.util.ElapsedTime; // A useful class for timing

@Autonomous(name = "taxi")
public class taxi extends LinearOpMode {

    // Define motor variables
    private DcMotorEx fl, bl, fr, br;
    // Define timer
    private ElapsedTime runtime = new ElapsedTime();

    @Override
    public void runOpMode() {
        // --- 1. HARDWARE INITIALIZATION ---
        // Your existing motor configurations:
        fl = hardwareMap.get(DcMotorEx.class, "front_left");
        bl = hardwareMap.get(DcMotorEx.class, "back_left");
        fr = hardwareMap.get(DcMotorEx.class, "front_right");
        br = hardwareMap.get(DcMotorEx.class, "back_right");

        // Motor Directions (as provided by you)
        fl.setDirection(DcMotorSimple.Direction.REVERSE);
        bl.setDirection(DcMotorSimple.Direction.REVERSE);
        fr.setDirection(DcMotorSimple.Direction.FORWARD);
        br.setDirection(DcMotorSimple.Direction.FORWARD);

        // Motor Zero Power Behavior (Brake)
        fl.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        bl.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        fr.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        br.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        // Ensure motors are in a standard mode (e.g., Run without Encoders for simple power control)
        fl.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        bl.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        fr.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        br.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        // Send a message to the Driver Station that the robot is initialized
        telemetry.addData("Status", "Initialized");
        telemetry.update();

        // Wait for the game to start (Driver Station ON button pressed)
        waitForStart();
        runtime.reset(); // Reset the timer once the OpMode starts

        // --- 2. MOVEMENT LOGIC ---
        if (opModeIsActive()) {

            final double DRIVE_SPEED = 0.8;
            final long MOVE_TIME_MS = 120; // 120 milliseconds

            telemetry.addData("Status", "Driving Forward at %f for %d ms", DRIVE_SPEED, MOVE_TIME_MS);
            telemetry.update();

            // Set all motors to the desired power for forward movement
            // For forward motion, all motors should run in the same *logical* direction
            fl.setPower(DRIVE_SPEED);
            bl.setPower(DRIVE_SPEED);
            fr.setPower(DRIVE_SPEED);
            br.setPower(DRIVE_SPEED);

            // Wait for the specified time
            // The `sleep()` method is the simplest way to introduce a pause
            sleep(MOVE_TIME_MS);

            // --- 3. STOPPING THE ROBOT ---
            telemetry.addData("Status", "Stopping");
            telemetry.update();

            // Set all motor powers to zero to stop
            fl.setPower(0);
            bl.setPower(0);
            fr.setPower(0);
            br.setPower(0);

            // Optional: Keep the OpMode running until STOP is pressed
            while (opModeIsActive()) {
                telemetry.addData("Status", "Done. Time: %f", runtime.seconds());
                telemetry.update();
            }
        }
    }
}