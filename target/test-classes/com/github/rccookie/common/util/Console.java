package com.github.rccookie.common.util;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.github.rccookie.common.util.Grid.GridElement;

import jline.TerminalFactory;

/**
 * Description: A console utility class.
 */
public final class Console {
    private Console() { }

    private static final char T_H = '-';//'\u2500'; // For Greenfoot console
    private static final char T_V = '\u2502';
    private static final char T_C = '\u253C';
    private static final char T_TL = '\u250C';
    private static final char T_TR = '\u2510';
    private static final char T_BL = '\u2514';
    private static final char T_BR = '\u2518';
    private static final char T_L = '\u251C';
    private static final char T_R = '\u2524';
    private static final char T_T = '\u252C';
    private static final char T_B = '\u2534';

    private static final int DEFAULT_CELL_WIDTH = 20;

    private static final char C_WHITE = ' ';
    private static final char C_LIGHT = '\u2591';
    private static final char C_MEDIUM = '\u2592';
    private static final char C_DARK = '\u2593';
    private static final char C_BLACK = '\u2588';

    private static final String LOG_ID = ">>";

    private static final int PROGRESS_BAR_WIDTH = 10;
    private static final char PROB_START = '[';
    private static final char PROB_END = ']';
    private static final char PROB_ON = '.';



    public static final void table(final Map<?,?> map) {
        table(map, DEFAULT_CELL_WIDTH);
    }

    public static final void table(final Map<?,?> map, final int maxCellWidth) {
        final Table<Object> table = new Table<>(2, map.size() + 1);

        table.set(0, 0, "KEYS");
        table.set(1, 0, "VALUES");

        final Entry<?,?>[] entrys = map.entrySet().toArray(new Entry[0]);
        for(int i=0; i<map.size(); i++) {
            table.set(0, i + 1, entrys[i].getKey());
            table.set(1, i + 1, entrys[i].getValue());
        }
        table(table, 1, maxCellWidth);
    }


    public static final void table(final Table<?> table) {
        table(table, 1, DEFAULT_CELL_WIDTH);
    }

    public static final void table(final Table<?> table, final int minCellWidth, final int maxCellWidth) {
        table(table, minCellWidth, maxCellWidth, System.out);
    }

    public static final void table(final Table<?> table, final int minCellWidth, final int maxCellWidth, final PrintStream out) {
        if(table == null) return;
        final int r = table.rowCount(), c = table.columnCount();
        if(r == 0) return;

        final Table<String> sTable = new Table<>(r, c, loc -> {
            Object element = table.get(loc.row(), loc.column());
            return element != null ? element.toString() : null;}
        );

        final int[] cellWidths = new int[c];
        for(int i=0; i<c; i++) {
            cellWidths[i] = minCellWidth;
            cellLoop:
            for(final String cell : sTable.column(i)) {
                final int length = cell != null ? cell.length() : String.valueOf((String)null).length();
                if(length <= cellWidths[i]) continue cellLoop;
                if(length > maxCellWidth) {
                    cellWidths[i] = maxCellWidth;
                    break cellLoop;
                }
                cellWidths[i] = length;
            }
        }

        out.println(line(c, cellWidths, -1));
        for(int i=0; i<r; i++) {
            out.println(row(sTable.row(i), cellWidths));
            out.println(line(c, cellWidths, i+1 < r ? 0 : 1));
        }
    }

    private static final String line(final int cells, final int[] cellWidths, final int pos) {
        final StringBuilder string = new StringBuilder();
        
        if(pos < 0) string.append(T_TL);
        else if(pos > 0) string.append(T_BL);
        else string.append(T_L);

        for(int i=0; i<cells; i++) {
            string.append(T_H).append(T_H);
            for(int j=0; j<cellWidths[i]; j++) string.append(T_H);
            if(i+1 < cells) {
                if(pos < 0) string.append(T_T);
                else if(pos > 0) string.append(T_B);
                else string.append(T_C);
            }
            else if(pos < 0) string.append(T_TR);
            else if(pos > 0) string.append(T_BR);
            else string.append(T_R);
        }
        return string.toString();
    }

    private static final String row(final List<String> row, final int[] cellWidths) {
        final StringBuilder string = new StringBuilder();
        string.append(T_V);
        for(int i=0; i<row.size(); i++) {
            string.append(' ');
            String content = row.get(i);
            if(content == null) content = String.valueOf((String)null);
            for(int j=0; j<cellWidths[i]; j++) {
                if(j < content.length()) string.append(content.charAt(j));
                else string.append(' ');
            }
            string.append(' ').append(T_V);
        }
        return string.toString();
    }




    /**
     * Width should not be bigger the 90 pixels.
     */
    public static final void paint(final BufferedImage image) {
        paint(image, 1);
    }

    /**
     * Width should not be bigger the 90 pixels.
     */
    public static final void paint(final BufferedImage image, double scale) {
        paint(image, true, scale);
    }

    /**
     * Width should not be bigger the 90 pixels.
     */
    public static final void paint(final BufferedImage image, final boolean negative, double scale) {
        paint(image, negative, scale, System.out);
    }

    public static final void paint(final BufferedImage image, final boolean negative, double scale, final PrintStream out) {
        AffineTransform at = new AffineTransform();
        at.scale(2.4 * scale, scale);
        final BufferedImage scaledImage = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR).filter(image, new BufferedImage((int)(2.4 * image.getWidth() * scale), (int)(image.getHeight() * scale), BufferedImage.TYPE_INT_ARGB));
        paint(new Table<Integer>(scaledImage.getHeight(), scaledImage.getWidth(), loc -> toBrightness(scaledImage, loc, negative)), out);
    }

    private static final int toBrightness(final BufferedImage image, final GridElement<?> loc, final boolean negative) {
        final Color c = new Color(image.getRGB(loc.column(), loc.row()));
        final int brightness = (int)Math.sqrt(
            0.299 * c.getRed() * c.getRed() +
            0.587 * c.getGreen() * c.getGreen() +
            0.114 * c.getBlue() * c.getBlue()
        );
        return negative ? 255 - brightness : brightness;
    }

    public static final void paint(final Table<Integer> table) {
        paint(table, System.out);
    }

    public static final void paint(final Table<Integer> table, final PrintStream out) {
        if(table == null) return;
        for(int i=0; i<table.rowCount(); i++) {
            StringBuilder row = new StringBuilder();
            for(final int color : table.row(i)) row.append(colorChar(color));
            out.println(row);
        }
    }

    private static final char colorChar(final int color) {
        if(color < 32) return C_BLACK;
        if(color < 96) return C_DARK;
        if(color < 159) return C_MEDIUM;
        if(color < 223) return C_LIGHT;
        return C_WHITE;
    }



    public static final void info(final String title, final String content) {
        info(title, content, System.out);
    }

    public static final void info(final String title, final String content, final PrintStream out) {
        out.println(new StringBuilder().append('[').append(title).append("]: ").append(content));
    }



    public static final void newLine() {
        newLine(1);
    }

    public static final void newLine(int count) {
        newLine(count, System.out);
    }

    public static final void newLine(int count, PrintStream out) {
        for(int i=0; i<count; i++) out.println();
    }



    public static final void map(final Object key, final Object value) {
        internalLog(new String[] { key + ": " + stringFor(value) });
    }


    public static final void log(final Object object, final Object... moreObjects) {
        final Object[] objects;
        if(moreObjects == null) objects = new Object[] { object };
        else {
            objects = new Object[moreObjects.length + 1];
            objects[0] = object;
            System.arraycopy(moreObjects, 0, objects, 1, moreObjects.length);
        }
        internalLog(objects);
    }

    public static final void log() {
        internalLog(new Object[0]);
    }



    private static final String stringFor(Object o) {
        if(o == null) return String.valueOf((String)null);
        if(!(o.getClass().isArray())) return o.toString();
        if(o instanceof boolean[]) return Arrays.toString((boolean[])o);
        if(o instanceof double[]) return Arrays.toString((double[])o);
        if(o instanceof float[]) return Arrays.toString((float[])o);
        if(o instanceof long[]) return Arrays.toString((long[])o);
        if(o instanceof int[]) return Arrays.toString((int[])o);
        if(o instanceof short[]) return Arrays.toString((short[])o);
        if(o instanceof char[]) return Arrays.toString((char[])o);
        if(o instanceof byte[]) return Arrays.toString((byte[])o);
        return '[' + Arrays.stream((Object[])o).map(e -> stringFor(e)).collect(Collectors.joining(", ")) + ']';
    }

    private static final String removeBrackets(String arrayString) {
        if(arrayString.startsWith("[") && arrayString.endsWith("]"))
            return arrayString.substring(1, arrayString.length() - 1);
        return arrayString;
    }

    /**
     * <b>This method should be called <i>directly</i> and <i>exclusively</i> by all of the internal <i>log</i> methods!</b>
     * <p>Prints the given objects and a information about the current method into the given print stream.
     * 
     * @param out The print stream to write into
     * @param objects The objects to print
     */
    private static final void internalLog(Object[] objects) {
        if(progressBarActive) {
            System.out.println();
            progressBarActive = false;
        }
        if(objects == null) objects = new Object[] {null};
        if(objects.length == 0) {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            StackTraceElement[] reducedStackTrace = new StackTraceElement[stackTrace.length - 3];
            System.arraycopy(stackTrace, 3, reducedStackTrace, 0, reducedStackTrace.length);
            String stackString = Arrays.asList(reducedStackTrace).stream().map(e -> e.toString()).collect(Collectors.joining("\n\t"));
            System.out.println(LOG_ID + '\t' + stackString); // Stack trace contains line numbers
        }
        else {
            StringBuilder out = new StringBuilder().append(LOG_ID).append(' ');
            out.append(removeBrackets(stringFor(objects))).append(' ');
            String classAndLineString = classAndLineString();
            final int width = TerminalFactory.get().getWidth();
            int usedWidth = width - ((out.length() + classAndLineString.length()) % width);
            for(int i=0; i!=usedWidth; i++) out.append(' ');
            System.out.println(out.append(classAndLineString));
        }
    }

    private static final String classAndLineString() {
        final StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        final int index = elements.length > 4 ? 4 : elements.length - 1;
        return elements[index].getFileName() + ':' + elements[index].getLineNumber();
    }

    /**
     * Clears the console.
     */
    public static final void clear() {
        System.out.print(String.format("\033[2J"));
    }



    private static boolean progressBarActive = false;
    private static int lastOn = -1;
    private static int lastPercentage = -1;

    public static final void setProgress(double progress) {
        if(progress < 0) progress = 0;
        if(progress > 1) progress = 1;

        int percentage = (int)Math.round(progress * 100);
        if(percentage == lastPercentage) return;

        int on = (int)Math.round(PROGRESS_BAR_WIDTH * progress);

        StringBuilder dif = new StringBuilder();

        if(!progressBarActive) {
            dif.append(LOG_ID).append(' ').append(PROB_START);
            for(int i=0; i<PROGRESS_BAR_WIDTH; i++) dif.append(i < on ? PROB_ON : ' ');
        }
        else {
            int percWidth = (lastPercentage + "").length();
            for(int i=0; i<percWidth+3; i++) dif.append('\b');
            
            if(on != lastOn) {
                int min = Math.min(on, lastOn);
                for(int i=PROGRESS_BAR_WIDTH; i>min; i--) dif.append('\b');

                for(int i=min; i<PROGRESS_BAR_WIDTH; i++) dif.append(i < on ? PROB_ON : ' ');
            }
        }

        dif.append(PROB_END + " " + percentage + "%");

        System.out.print(dif);

        lastOn = on;
        lastPercentage = percentage;
        if(percentage == 100) {
            System.out.println();
            progressBarActive = false;
        }
        else progressBarActive = true;
    }
}
