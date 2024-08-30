package de.rccookie.util.text;

import java.util.Arrays;
import java.util.function.IntFunction;

import de.rccookie.math.IRect;
import de.rccookie.math.Mathf;
import de.rccookie.math.constInt2;
import de.rccookie.math.int2;
import de.rccookie.util.Arguments;
import org.jetbrains.annotations.Contract;

import static de.rccookie.util.Utils.blank;
import static de.rccookie.util.text.AsciiArt.KeepCenter.*;

public final class AsciiArt {

    public static final AsciiArt EMPTY = new AsciiArt(new String[0]);

    private final String[] lines;
    private final constInt2 center;
    private final int width;

    public AsciiArt(String value) {
        lines = value.lines().toArray(String[]::new);
        pad();
        width = calcWidth();
        center = new int2(lines.length == 0 ? 0 : length(lines[0]) / 2, lines.length / 2);
    }

    public AsciiArt(int height, IntFunction<String> lineGenerator) {
        lines = new String[height];
        for(int i=0; i<lines.length; i++)
            lines[i] = Arguments.checkNull(lineGenerator.apply(i), "lineGenerator.apply("+i+")");
        pad();
        width = calcWidth();
        center = new int2(lines.length == 0 ? 0 : length(lines[0]) / 2, lines.length / 2);
    }

    public AsciiArt(String[] lines) {
        this.lines = Arguments.checkNull(lines, "lines").clone();
        pad();
        width = calcWidth();
        center = new int2(lines.length == 0 ? 0 : length(lines[0]) / 2, lines.length / 2);
    }

    private AsciiArt(String[] lines, constInt2 center) {
        this.lines = lines;
        this.center = center;
        width = calcWidth();
    }

    private void pad() {
        if(lines.length == 0) return;
        int[] lengths = new int[lines.length];
        Arrays.setAll(lengths, i -> length(lines[i]));
        int w = Mathf.max(lengths);
        for(int i=0; i<lines.length; i++)
            if(lengths[i] != w)
                lines[i] = lines[i] + blank(w - lengths[i]);
    }

//    private static final Pattern ANSI_ESCAPE_SEQUENCE_PAT = Pattern.compile("\u001b\\[\\d+(;\\d+)*m");
    private static int length(String str) {
        return str.length();
//        return ANSI_ESCAPE_SEQUENCE_PAT.matcher(str).replaceAll("").length();
    }

    public int2 size() {
        return new int2(width(), height());
    }

    private int calcWidth() {
        return lines.length == 0 ? 0 : lines[0].length();
    }

    public int width() {
        return width;
    }

    public int height() {
        return lines.length;
    }

    public constInt2 center() {
        return center;
    }

    public String getLine(int index) {
        return lines[index];
    }

    @Override
    public String toString() {
        return String.join("\n", lines);
    }

    public AsciiArt setCenter(constInt2 center) {
        Arguments.checkRange(center.x(), 0, Math.max(1, width()));
        Arguments.checkRange(center.y(), 0, Math.max(1, lines.length));
        return new AsciiArt(lines, center.toConst());
    }

    public AsciiArt setCenterX(int centerX) {
        return setCenter(new int2(centerX, center.y()));
    }

    public AsciiArt setCenterY(int centerY) {
        return setCenter(new int2(center.x(), centerY));
    }

    public AsciiArt recalculateCenterX() {
        return setCenterX(width() / 2);
    }

    public AsciiArt recalculateCenterY() {
        return setCenterY(lines.length / 2);
    }

    public AsciiArt recalculateCenter() {
        return setCenter(size().div(2));
    }

    @Contract(pure = true)
    public AsciiArt appendTop(AsciiArt a) {
        return appendTop(a, AVERAGE, KEEP);
    }

    @Contract(pure = true)
    public AsciiArt appendTop(AsciiArt a, KeepCenter keepCenterX, KeepCenter keepCenterY) {
        String[] lines = new String[Math.max(this.lines.length, a.lines.length)];
        for(int i=0; i<lines.length; i++) {
            String line = i < this.lines.length ? this.lines[i] : blank(width());
            lines[i] = line + (i < a.lines.length ? a.lines[i] : blank(a.width()));
        }
        return new AsciiArt(lines, new int2(
                keepCenterX == KEEP ? center.x() : keepCenterX == KEEP_OTHER ? a.center.x() + width : width / 2,
                keepCenterY == KEEP ? center.y() : keepCenterY == KEEP_OTHER ? a.center.y() : lines.length / 2
        ));
    }

    @Contract(pure = true)
    public AsciiArt appendBottom(AsciiArt a) {
        return appendBottom(a, AVERAGE, KEEP);
    }

    @Contract(pure = true)
    public AsciiArt appendBottom(AsciiArt a, KeepCenter keepCenterX, KeepCenter keepCenterY) {
        String[] lines = new String[Math.max(this.lines.length, a.lines.length)];
        for(int i=0; i<lines.length; i++) {
            String line = i < this.lines.length ? this.lines[this.lines.length - i - 1] : blank(width());
            lines[lines.length - i - 1] = line + (i < a.lines.length ? a.lines[a.lines.length - i - 1] : blank(a.width()));
        }
        return new AsciiArt(lines, new int2(
                keepCenterX == KEEP ? center.x() : keepCenterX == KEEP_OTHER ? a.center.x() + width : width / 2,
                keepCenterY == KEEP ? center.y() + Math.max(0, a.lines.length-lines.length) : keepCenterY == KEEP_OTHER ? a.center.y() + Math.max(0, lines.length-a.lines.length) : lines.length / 2
        ));
    }

    @Contract(pure = true)
    public AsciiArt appendCenter(AsciiArt a) {
        return appendCenter(a, AVERAGE);
    }

    @Contract(pure = true)
    public AsciiArt appendCenter(AsciiArt a, KeepCenter keepCenterX) {
        return draw(a, new int2(width(), center.y() - a.center.y()), keepCenterX, KEEP);
    }

    public AsciiArt appendBelowCenter(AsciiArt a, KeepCenter keepCenterY) {
        return draw(a, new int2(center.x() - a.center.x(), height()), KEEP, keepCenterY);
    }

    @Contract(pure = true)
    public AsciiArt append(AsciiArt a, Alignment alignment, KeepCenter keepCenterX, KeepCenter keepCenterY) {
        switch(Arguments.checkNull(alignment, "alignment")) {
            case LEFT /* = TOP */:     return appendTop(a, keepCenterX, keepCenterY);
            case CENTER:               return appendCenter(a, keepCenterX);
            case RIGHT /* = BOTTOM */: return appendBottom(a, keepCenterX, keepCenterY);
            default: throw new AssertionError();
        }
    }

    @Contract(pure = true)
    public AsciiArt draw(AsciiArt a, constInt2 position) {
        return draw(a, position, AVERAGE, KEEP);
    }

    @Contract(pure = true)
    public AsciiArt draw(AsciiArt a, constInt2 position, KeepCenter keepCenterX, KeepCenter keepCenterY) {
        int2 min = int2.min(int2.zero, position);
        IRect selfArea = new IRect(min.negated(), min.negated().add(size()));
        IRect aArea = new IRect(position.subed(min), position.subed(min).add(a.size()));

        int w = Math.max(selfArea.max().x(), aArea.max().x());
        String[] lines = new String[Math.max(selfArea.max().y(), aArea.max().y())];

        for(int i=0; i<lines.length; i++) {
            if(i >= selfArea.min().y() && i < selfArea.max().y()) {
                String line;
                if(i >= aArea.min().y() && i < aArea.max().y()) {
                    if(selfArea.min().x() == 0) {
                        if(aArea.min().x() < selfArea.max().x())
                            line = this.lines[i-selfArea.min().y()].substring(0, aArea.min().x());
                        else line = this.lines[i-selfArea.min().y()] + blank(aArea.min().x() - selfArea.max().x());
                        line += a.lines[i-aArea.min().y()];
                        if(w != aArea.max().x())
                            line += this.lines[i-selfArea.min().y()].substring(length(line));
                    }
                    else {
                        line = a.lines[i-aArea.min().y()];
                        if(w != aArea.max().x()) {
                            if(selfArea.min().x() < aArea.max().x())
                                line += this.lines[i - selfArea.min().y()].substring(aArea.max().x() - selfArea.min().x());
                            else line += blank(selfArea.min().x() - aArea.max().x()) + this.lines[i - selfArea.min().y()];
                        }
                    }
                }
                else line = blank(selfArea.min().x()) + this.lines[i - selfArea.min().y()] + blank(w - selfArea.max().x());
                lines[i] = line;
            }
            else if(i >= aArea.min().y() && i < aArea.max().y())
                lines[i] = blank(aArea.min().x()) + a.lines[i - aArea.min().y()] + blank(w - aArea.max().x());
            else lines[i] = blank(w);
        }

        int2 newCenter = int2.max(int2.zero, position.negated()).add(center);
        int2 newACenter = int2.max(int2.zero, position).add(a.center);
        return new AsciiArt(lines, new int2(
                keepCenterX == KEEP ? newCenter.x() : keepCenterX == KEEP_OTHER ? newACenter.x() : lines.length == 0 ? 0 : width / 2,
                keepCenterY == KEEP ? newCenter.y() : keepCenterY == KEEP_OTHER ? newACenter.y() : lines.length / 2
        ));
    }

    public static AsciiArt empty(constInt2 size) {
        String line = blank(size.x());
        String[] lines = new String[size.y()];
        Arrays.fill(lines, line);
        return new AsciiArt(lines, size.dived(2));
    }

    public static AsciiArt empty(int width) {
        return new AsciiArt(new String[] { blank(width) }, int2.zero);
    }



    public enum KeepCenter {
        KEEP,
        KEEP_OTHER,
        AVERAGE;
    }
}
