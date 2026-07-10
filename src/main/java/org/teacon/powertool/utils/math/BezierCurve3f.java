package org.teacon.powertool.utils.math;

import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;
import org.teacon.powertool.annotation.NonNullByDefault;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@NonNullByDefault
public class BezierCurve3f {
    
    public final int degree;
    public final int steps;
    public final List<Vector3f> controlPoints;
    @Nullable
    private List<Vector3f> points;
    private float length = Float.NaN;
    
    public BezierCurve3f(int steps, List<Vector3f> controlPoints) {
        this(Math.min(controlPoints.size() - 1, 3), steps, controlPoints);
    }
    
    public BezierCurve3f(int degree, int steps, List<Vector3f> controlPoints) {
        assert steps > 0;
        assert controlPoints.size() > 1;
        assert degree == Math.min(controlPoints.size() - 1, 3);
        this.degree = degree;
        this.steps = steps;
        this.controlPoints = List.copyOf(controlPoints);
    }
    
    private void calculatePoints() {
        var calculatedPoints = new ArrayList<Vector3f>();
        if (controlPoints.size() <= 4) {
            appendSegment(calculatedPoints, controlPoints, false);
            points = calculatedPoints;
            return;
        }
        appendSegment(calculatedPoints, controlPoints.subList(0, 4), false);
        var previousEnd = controlPoints.get(3);
        int nextControlPoint = 4;
        while (nextControlPoint < controlPoints.size()) {
            int segmentEnd = Math.min(nextControlPoint + 3, controlPoints.size());
            var segmentControlPoints = new ArrayList<Vector3f>(4);
            segmentControlPoints.add(previousEnd);
            segmentControlPoints.addAll(controlPoints.subList(nextControlPoint, segmentEnd));
            appendSegment(calculatedPoints, segmentControlPoints, true);
            previousEnd = segmentControlPoints.getLast();
            nextControlPoint = segmentEnd;
        }
        points = calculatedPoints;
    }

    private void appendSegment(List<Vector3f> result, List<Vector3f> segmentControlPoints, boolean skipFirst) {
        int firstStep = skipFirst ? 1 : 0;
        for (int step = firstStep; step < steps; step++) {
            float progress = steps == 1 ? 0 : (float) step / (steps - 1);
            var workingPoints = segmentControlPoints.stream().map(Vector3f::new).toList();
            for (int level = 1; level < segmentControlPoints.size(); level++) {
                for (int i = 0; i < segmentControlPoints.size() - level; i++) {
                    workingPoints.get(i).lerp(workingPoints.get(i + 1), progress);
                }
            }
            result.add(workingPoints.getFirst());
        }
    }
    
    public List<Vector3f> getPoints() {
        if (points == null) {
            calculatePoints();
        }
        return Objects.requireNonNull(points);
    }
    
    public float getLength() {
        if (Float.isNaN(length)) {
            length = 0;
            var context = new Vector3f();
            for (var i = 0; i < getPoints().size() - 1; i++) {
                length += getPoints().get(i + 1).sub(getPoints().get(i), context).length();
            }
        }
        return length;
    }
    
    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (!(o instanceof BezierCurve3f that)) return false;
        return degree == that.degree && steps == that.steps && Objects.equals(controlPoints, that.controlPoints);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(degree, steps, controlPoints);
    }
}
