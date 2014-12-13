package utils.validator;

import com.google.common.collect.Lists;
import cornerfinders.core.shapes.TPoint;
import cornerfinders.core.shapes.TStroke;

import java.util.List;
import java.util.Optional;

/**
 * Created by jaideepray on 12/12/14.
 */
public class SketchDataValidator {

    public static Integer MIN_POINTS = 7;

    public static boolean isValidPoint(TPoint point) {
        if (point == null)
            return false;

        try {
            long t = point.getTime();
            if (t < 0) return false;
            point.getX();
            point.getY();
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    public static boolean isValidStroke(TStroke stroke) {
        if (stroke == null || stroke.getPoints().size() < 7)
            return false;
        for (TPoint point : stroke.getPoints()) {
            if (!isValidPoint(point)) {
                return false;
            }
        }
        return true;
    }

    public static TStroke cleanStroke(TStroke stroke) {
        for (TPoint point : stroke.getPoints()) {
            if (!SketchDataValidator.isValidPoint(point)) {
                stroke.deleteInvalidPoint(point);
            }
        }
        return stroke;
    }

    public static Optional<List<TStroke>> validateSketch(List<TStroke> strokeList) {
        if (strokeList.isEmpty())
            return Optional.empty();
        List<TStroke> validatedStrokes = Lists.newArrayList();
        for (TStroke stroke : strokeList) {
            if (!isValidStroke(stroke)) {
                continue;
            }
            validatedStrokes.add(stroke);
        }
        if (validatedStrokes.size() > 0)
            return Optional.of(validatedStrokes);
        return Optional.empty();
    }
}
