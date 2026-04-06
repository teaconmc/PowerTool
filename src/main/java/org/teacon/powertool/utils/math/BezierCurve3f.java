package org.teacon.powertool.utils.math;

import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class BezierCurve3f {
    
    public final int degree;
    public final int steps;
    public final List<Vector3f> controlPoints;
    private List<Vector3f> points;
    private float length = -1;
    
    public BezierCurve3f(int steps, List<Vector3f> controlPoints) {
        this(controlPoints.size() - 1, steps, controlPoints);
    }
    
    public BezierCurve3f(int degree, int steps, List<Vector3f> controlPoints) {
        assert steps > 0;
        assert controlPoints.size() > 1;
        assert degree == controlPoints.size() - 1;
        this.degree = degree;
        this.steps = steps;
        this.controlPoints = List.copyOf(controlPoints);
    }
    
    private void calculatePoints() {
        points = new ArrayList<>();
        double delta = 1d / (steps - 1);
        double t = 0;
        if (Double.isNaN(delta)) {
            points.add(controlPoints.getFirst());
            points.add(controlPoints.getLast());
            return;
        }
        for (int i = 0; i < steps; i++) {
            var result = new Vector3f();
            for (int n = 0; n < degree + 1; n++) {
                double scale = degree * Math.min(n, degree - n);
                if (scale == 0) scale = 1;
                scale *= Math.pow(1 - t, degree - n) * Math.pow(t, n);
                var pn = controlPoints.get(n);
                pn.mulAdd((float) scale, result, result);
            }
            points.add(result);
            t += delta;
        }
    }
    
    public List<Vector3f> getPoints() {
        if (points == null) {
            calculatePoints();
            points = Collections.unmodifiableList(points);
        }
        return points;
    }
    
    public float getLength() {
        if (length < 0) {
            var context = new Vector3f();
            for (var i = 0; i < getPoints().size() - 1; i++) {
                length += getPoints().get(i + 1).sub(getPoints().get(i), context).length();
            }
        }
        return length;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BezierCurve3f that)) return false;
        return degree == that.degree && steps == that.steps && Objects.equals(controlPoints, that.controlPoints);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(degree, steps, controlPoints);
    }
}
