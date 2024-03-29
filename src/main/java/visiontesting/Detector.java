package visiontesting;

import com.kylecorry.frc.vision.Range;
import com.kylecorry.frc.vision.camera.CameraSettings;
import com.kylecorry.frc.vision.camera.FOV;
import com.kylecorry.frc.vision.camera.Resolution;
import com.kylecorry.frc.vision.contourFilters.ContourFilter;
import com.kylecorry.frc.vision.contourFilters.StandardContourFilter;
import com.kylecorry.frc.vision.filters.HSVFilter;
import com.kylecorry.frc.vision.filters.TargetFilter;
import com.kylecorry.frc.vision.targetConverters.TargetGrouping;
import com.kylecorry.frc.vision.targetConverters.TargetUtils;
import com.kylecorry.frc.vision.targeting.Target;
import com.kylecorry.frc.vision.targeting.TargetFinder;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Detector {
    private boolean SHOULD_DRAW;

    public Detector(boolean should_draw) {
        this.SHOULD_DRAW = should_draw;
    }

    public Detector() {
        this.SHOULD_DRAW = true;
    }

    public List<Target> detect2019Targets(Mat image){
        // Adjust these parameters for your team's needs
      
        // Target filter parameters
        double minBrightness = 200;
        double maxBrightness = 255;
      
        // Contour filter parameters
        // Range area = new Range(0.03, 100);
        // Range fullness = new Range(0, 100);
        // Range aspectRatio = new Range(0.2, 4);
        Range area = new Range(0, 100);
        Range fullness = new Range(0, 100);
        Range aspectRatio = new Range(0, 100);
      
        // Camera settings
        CameraSettings cameraSettings;
        TargetFilter targetFilter;
        ContourFilter contourFilter;
        if (!App.cameraActive) {
            Resolution resolution = new Resolution(720, 478);

            cameraSettings = new CameraSettings(false, new FOV(50, 40), resolution);
            targetFilter = new HSVFilter(new Range(50, 70), new Range(100, 255), new Range(100, 255));
            contourFilter = new StandardContourFilter(new Range(0.03, 100), new Range(0, 100), new Range(0.2, 4), resolution.getArea());
        } else {
            FOV fov = new FOV(50, 40);
            Resolution resolution = new Resolution(640, 480);
            boolean cameraInverted = false;
            int imageArea = resolution.getArea();

            targetFilter = new HSVFilter(new Range(70, 180), new Range(80, 242), new Range(193, 255));
            contourFilter = new StandardContourFilter(area, fullness, aspectRatio, imageArea);
            cameraSettings = new CameraSettings(cameraInverted, fov, resolution);

        }
        TargetFinder targetFinder = new TargetFinder(cameraSettings, targetFilter, contourFilter, TargetGrouping.SINGLE);
      
        // Find the targets
        List<Target> targets = targetFinder.findTargets(image);

        if (this.SHOULD_DRAW) {
            for (Target target : targets) {
                Imgproc.drawMarker(image, target.getBoundary().center, new Scalar(0, 0, 255), Core.TYPE_GENERAL, 12, 2);
            }
        }
      
        // Sort the targets by x coordinates
        targets.sort(Comparator.comparingDouble(target -> target.getBoundary().center.x));
      
        List<Target> bays = new ArrayList<>();
        // If the current target is a left and the next is a right, make it a pair
        for (int i = 0; i < targets.size() - 1; i++) {
            Target current = targets.get(i);
            Target next = targets.get(i + 1);
      
            // Determine if the targets are a left and right pair
            if (isLeftTarget(current) && isRightTarget(next)){
                // Combine the targets
                Target bay = TargetUtils.combineTargets(current, next, cameraSettings);
                bays.add(bay);
                // Skip the next target
                i++;
            }
        }

        if (this.SHOULD_DRAW) {
            for (Target bay : bays) {
                drawBay(image, bay);
            }
        }
      
        return bays;
      }
      
      /**
       * Determines if a target is a left vision target.
       * @param target The target.
       * @return True if it is a left target.
       */
      private boolean isLeftTarget(Target target){
          return target.getSkew() < 0;
      }
      
      /**
       * Determines if a target is a right vision target.
       * @param target The target.
       * @return True if it is a right target.
       */
      private boolean isRightTarget(Target target){
          return target.getSkew() > 0;
      }

      public void drawBay(Mat image, Target bay){
        RotatedRect boundary = bay.getBoundary();

        double height = boundary.boundingRect().height;
        // double verticalPPI = height / TARGET_HEIGHT;
        double verticalPPI = height / 5.5;
        double holeYDist = 8.25;
        double centerY = boundary.center.y + holeYDist * verticalPPI;

        Imgproc.circle(image, new Point(boundary.center.x, centerY), boundary.boundingRect().width/2, new Scalar(255, 0, 255), 4);
        Imgproc.drawMarker(image, new Point(boundary.center.x, centerY), new Scalar(255, 0, 255), Core.TYPE_GENERAL, 30, 2);
    }
      
}