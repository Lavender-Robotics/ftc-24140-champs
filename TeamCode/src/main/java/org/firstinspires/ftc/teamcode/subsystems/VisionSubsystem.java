package org.firstinspires.ftc.teamcode.subsystems;



import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes.FiducialResult;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.hardware.HardwareMap;


import java.util.List;

public class VisionSubsystem {

    public static final int RED_GOAL_TAG_ID = 24;
    public static final double ALIGN_KP = 0.025;
    public static final int BLUE_GOAL_TAG_ID = 20;
    public static final double ALIGN_DEADZONE_DEG = 1.0;
    public static final double ALIGN_MAX_TURN = 0.4;
    public static final double GOAL_HEIGHT_CM = 74.95;
    public static final double CAMERA_HEIGHT_CM = 28.0;
    public static final double CAMERA_ANGLE_DEG = 10.0;

    public static final double TARGET_DISTANCE = 115.0;
    public static final double DISTANCE_KP = 0.04;
    public static final double DISTANCE_DEADZONE_CM = 0.5;
    public static final double DISTANCE_MAX_POWER = 0.4;

    private Limelight3A limelight;
    private double lastValidDistance = 0.0;


        public void init(HardwareMap hw){
        limelight = hw.get(Limelight3A.class, "limelight");
        limelight.pipelineSwitch(0);
    }

    public void start() {
        limelight.start();
    }

    public void stop() {
        limelight.stop();
    }

    /** tx (yatay hata, derece) → dönüş gücü. Hizalıysa 0, değilse P kontrolcü + clamp. */
    private double headingCorrectionFromTx(double tx) {
        if (Math.abs(tx) < ALIGN_DEADZONE_DEG) return 0.0;   // hizalı → dönme

        double power = tx * ALIGN_KP;
        power = Math.max(-ALIGN_MAX_TURN, Math.min(ALIGN_MAX_TURN, power));
        return power;
    }

        private boolean isGoalTag ( int id){
            return id == BLUE_GOAL_TAG_ID || id == RED_GOAL_TAG_ID;
        }

        /** Görülen goal tag'ini döndürür (Blue 20 / Red 24); yoksa null. Obelisk yok sayılır. */
        private FiducialResult getGoalFiducial () {
            LLResult result = limelight.getLatestResult();
            if (result == null || !result.isValid()) return null;
            List<FiducialResult> fiducials = result.getFiducialResults();
            if (fiducials == null) return null;
            for (FiducialResult f : fiducials) {
                if (isGoalTag(f.getFiducialId())) {
                    return f;
                }
            }
            return null;
        }

        /** OpMode'un çağıracağı metot: goal'a hizalanmak için dönüş gücü. Goal yoksa 0. */
        public double getGoalHeadingCorrection () {
            FiducialResult goal = getGoalFiducial();
            if (goal == null) return 0.0;
            return headingCorrectionFromTx(goal.getTargetXDegrees());
        }
        public double getGoalDistanceCorrection () {
            FiducialResult goal = getGoalFiducial();
            if (goal == null) return 0.0;
            return distanceCorrection();
        }


        /** Goal tag'i şu an görünüyor mu? (telemetri/tetik için) */
        public boolean hasGoalTarget () {
            return getGoalFiducial() != null;
        }
        /**
         * Calculates distance to the tracked target in cm.
         */
        public double getDistance () {
            LLResult result = limelight.getLatestResult();
            if (result != null && result.isValid()) {
                lastValidDistance = calculateDistance(result.getTy());
            }
            return lastValidDistance;
        }
        private double distanceCorrection() {
            double error = getDistance() - TARGET_DISTANCE;
            if (Math.abs(error) < DISTANCE_DEADZONE_CM) return 0.0;
            double powerd = error * DISTANCE_KP;
            double MIN_POWER = 0.12;
            if (Math.abs(powerd) < MIN_POWER) powerd = Math.copySign(MIN_POWER, powerd);
            powerd = Math.max(-DISTANCE_MAX_POWER, Math.min(DISTANCE_MAX_POWER, powerd));
            return powerd;
        }
        /**
         * Returns true if ANY AprilTag is visible in the frame.
         */
        public boolean hasAnyTarget () {
            LLResult result = limelight.getLatestResult();
            if (result != null && result.isValid()) {
                List<FiducialResult> fiducials = result.getFiducialResults();
                return fiducials != null && !fiducials.isEmpty();
            }
            return false;
        }

        /**
         * Calculates heading correction power for whichever tag is currently visible.
         */
        public double getAnyTagHeadingCorrection () {
            LLResult result = limelight.getLatestResult();
            if (result != null && result.isValid()) {
                List<FiducialResult> fiducials = result.getFiducialResults();

                if (fiducials != null && !fiducials.isEmpty()) {
                    // Pick the first AprilTag seen in the frame
                    FiducialResult targetTag = fiducials.get(0);
                    double tx = targetTag.getTargetXDegrees();

                    double kP = 0.04;        // Proportional gain (adjust if alignment oscillates)
                    double deadzone = 0.5;   // Acceptable error threshold in degrees

                    if (Math.abs(tx) > deadzone) {
                        return tx * kP;
                    }
                }
            }
            return 0.0;
        }

        private double calculateDistance (double ty){
                double angleToTarget = this.CAMERA_ANGLE_DEG + ty;
                double heightDiff = this.GOAL_HEIGHT_CM - this.CAMERA_HEIGHT_CM;
                return heightDiff / Math.tan(Math.toRadians(angleToTarget));
            }
    }