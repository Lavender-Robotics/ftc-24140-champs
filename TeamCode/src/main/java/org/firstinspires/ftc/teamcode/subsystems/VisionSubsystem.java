package org.firstinspires.ftc.teamcode.subsystems;

import android.util.Size;
import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;
import org.firstinspires.ftc.vision.apriltag.AprilTagGameDatabase;

import java.util.List;


public class VisionSubsystem {

    private static final double CAMERA_HEIGHT = 12.992126; // Kameranın yerden yüksekliği
    private static final double CAMERA_ANGLE = 10.0;  // Kameranın açısı
    private static final double TAG_HEIGHT = 6.0;     // AprilTag'in merkez yüksekliği (örnek)

    private AprilTagProcessor aprilTag;
    private VisionPortal visionPortal;
    private HardwareMap hardwareMap;

    public VisionSubsystem(HardwareMap hardwareMap) {
        this.hardwareMap = hardwareMap;
    }

    public void init() {
        aprilTag = new AprilTagProcessor.Builder()
                .setDrawAxes(true)
                .setDrawCubeProjection(true)
                .setDrawTagOutline(true)
                .setTagLibrary(AprilTagGameDatabase.getCenterStageTagLibrary())
                .build();

        VisionPortal.Builder builder = new VisionPortal.Builder();
        builder.setCamera(hardwareMap.get(WebcamName.class, "Webcam 1"));
        builder.setCameraResolution(new Size(640, 480));
        builder.addProcessor(aprilTag);

        visionPortal = builder.build();
    }


    public List<AprilTagDetection> getDetections() { return aprilTag.getDetections(); }


    public double getDistanceToTag(AprilTagDetection detection){
        if (detection == null || detection.ftcPose == null) return 0;

        double deltaHeight = TAG_HEIGHT - CAMERA_HEIGHT;

        double detectionAngle = detection.ftcPose.elevation;
        double totalAngleDeg = CAMERA_ANGLE + detectionAngle;
        double totalAngleRad = Math.toRadians(totalAngleDeg);

        if (Math.abs(Math.tan(totalAngleRad)) < 0.001) return 0; // Çok küçük açı hatası
        double distance = deltaHeight / Math.tan(totalAngleRad);

        return Math.abs(distance);
    }


    public void close() { if (visionPortal != null) visionPortal.close(); }




}
