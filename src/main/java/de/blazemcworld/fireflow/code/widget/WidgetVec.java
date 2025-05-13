package de.blazemcworld.fireflow.code.widget;

import de.blazemcworld.fireflow.code.CodeEditor;
import de.blazemcworld.fireflow.code.CodeWorld;
import net.minecraft.util.math.Vec3d;

public record WidgetVec(CodeEditor editor, double x, double y) {

    public Vec3d vec() {
        return new Vec3d(x, y, 15.999);
    }

    public CodeWorld world() {
        return editor.world;
    }

    public WidgetVec withX(double x) {
        return new WidgetVec(editor, x, y);
    }

    public WidgetVec withY(double y) {
        return new WidgetVec(editor, x, y);
    }

    public WidgetVec add(double x, double y) {
        return new WidgetVec(editor, this.x + x, this.y + y);
    }

    public WidgetVec div(double v) {
        return new WidgetVec(editor, this.x / v, this.y / v);
    }

    public WidgetVec add(WidgetVec v) {
        return new WidgetVec(editor, this.x + v.x, this.y + v.y);
    }

    public WidgetVec sub(WidgetVec pos) {
        return new WidgetVec(editor, this.x - pos.x, this.y - pos.y);
    }

    public WidgetVec gridAligned() {
        return new WidgetVec(editor, Math.ceil(x * 8.0) / 8.0, Math.ceil(y * 8.0) / 8.0);
    }

    public WidgetVec sub(double x, double y) {
        return new WidgetVec(editor, this.x - x, this.y - y);
    }

    public double distance(WidgetVec other) {
        return Math.sqrt(distanceSquared(other));
    }

    public double distanceSquared(WidgetVec other) {
        return (other.x() - x) * (other.x() - x) + (other.y() - y) * (other.y() - y);
    }

    public WidgetVec min(WidgetVec other) {
        return new WidgetVec(editor, Math.min(x, other.x()), Math.min(y, other.y()));
    }

    public WidgetVec max(WidgetVec other) {
        return new WidgetVec(editor, Math.max(x, other.x()), Math.max(y, other.y()));
    }

    public WidgetVec max(double x, int y) {
        return new WidgetVec(editor, Math.max(this.x, x), Math.max(this.y, y));
    }

    public WidgetVec mul(double x, double y) {
        return new WidgetVec(editor, this.x * x, this.y * y);
    }
}
