package org.firstinspires.ftc.teamcode.util;

import com.qualcomm.robotcore.util.ElapsedTime;
import java.util.function.BooleanSupplier;

public class Timer {
    private ElapsedTime timer;
    private boolean isRunning;

    public Timer() {
        this.timer = new ElapsedTime();
        this.isRunning = false;
    }

    /**
     * The Magic Function.
     * @param delayMs       How often to run the action (in milliseconds).
     * @param action        The code to run (e.g., () -> servo.setPosition(1)).
     * @param stopCondition A condition to stop the timer (e.g., () -> sensor.isPressed()).
     */
    public void runPeriodic(double delayMs, Runnable action, BooleanSupplier stopCondition) {
        // 1. Check if we should stop entirely
        if (stopCondition.getAsBoolean()) {
            stop();
            return;
        }

        // 2. If we aren't running, start now
        if (!isRunning) {
            start();
        }

        // 3. Check time
        if (timer.milliseconds() >= delayMs) {
            action.run();  // Execute the provided function
            timer.reset(); // Reset clock
        }
    }

    // --- Standard Helpers ---
    private void start() {
        isRunning = true;
        timer.reset();
    }

    private void stop() {
        isRunning = false;
    }
}