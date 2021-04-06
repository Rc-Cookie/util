package com.github.rccookie.common.util;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

//import jline.TerminalFactory;
import com.diogonunes.jcolor.Ansi;
import com.diogonunes.jcolor.Attribute;
import com.github.rccookie.common.util.Grid.GridElement;

/**
 * A console utility class.
 */
public final class Console {
    private Console() { }



    private static final HashMap<String, OutputFilter> FILTERS = new HashMap<>();
    static {
        new OutputFilter("");
    }



    private static final char T_H = '\u2500';
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

    private static final int PROGRESS_BAR_WIDTH = 10;
    private static final char PROB_START = '[';
    private static final char PROB_END = ']';
    private static final char PROB_ON = '.';

    /**
     * Configuration for the console output.
     */
    public static final class Config {
        private Config() { }

        /**
         * Weather the output should be colored. Should be disabled
         * if the console does not support coloring.
         */
        public static boolean coloredOutput = true;

        /**
         * Weather a print intp the console should include the line
         * number and class name that calls it.
         */
        public static boolean includeLineNumber = true;

        /**
         * If {@code != null} this will be used instead of the
         * measured width of the console. Note that the width is
         * used for the line number and the first measurement will
         * (currently) break the console input.
         */
        public static Integer manualConsoleWidth = null;
    }


    /**
     * A BufferedReader for the System.in input stream. Should not be closed.
     */
    public static final BufferedReader READER = new BufferedReader(new InputStreamReader(System.in));

    private static final String ERROR_BLOCK = new StringBuilder("[").append(colored(OutputFilter.ERROR.toUpperCase(), Attribute.RED_TEXT())).append("] ").toString();
    private static final String ERROR_BLOCK_PLAIN = "[ERROR] ";
    private static final String WARN_BLOCK = new StringBuilder("[").append(colored(OutputFilter.WARN.toUpperCase(), Attribute.YELLOW_TEXT())).append("] ").toString();
    private static final String WARN_BLOCK_PLAIN = "[WARN] ";
    private static final String INFO_BLOCK = new StringBuilder("[").append(colored(OutputFilter.INFO.toUpperCase(), Attribute.BLUE_TEXT())).append("] ").toString();
    private static final String INFO_BLOCK_PLAIN = "[INFO] ";
    private static final String INPUT_BLOCK = new StringBuilder("[").append(colored("INPUT", Attribute.MAGENTA_TEXT())).append("] ").toString();
    private static final String INPUT_BLOCK_PLAIN = "[INPUT] ";

    /**
     * This print stream prints all information info the normal System.out
     * output stream marked with {@code [ERROR]}.
     */
    public static final PrintStream CONSOLE_ERROR_STREAM = new NamedStream() {
        @Override
        String getLineStart() {
            return getErrorBlock();
        };
    };

    /**
     * This print stream prints all information info the normal System.out
     * output stream marked with {@code [WARN]}.
     */
    public static final PrintStream CONSOLE_WARN_STREAM = new NamedStream() {
        @Override
        String getLineStart() {
            return getWarnBlock();
        };
    };

    /**
     * This print stream prints all information info the normal System.out
     * output stream marked with {@code [WARN]}.
     */
    public static final PrintStream CONSOLE_LOG_STREAM = new NamedStream() {
        @Override
        String getLineStart() {
            return getInfoBlock();
        };
    };

    private static abstract class NamedStream extends PrintStream {

        public NamedStream() {
            super(System.out);
        }

        private boolean isLineStart = true;
        @Override
        public void print(String s) {
            if(isLineStart) {
                super.print(getLineStart() + s);
                isLineStart = false;
            }
            else super.print(s);
        };
        abstract String getLineStart();
        @Override
        public void println() {
            super.println();
            isLineStart = true;
        };
        @Override
        public void println(Object x) {
            super.println(x);
            isLineStart = true;
        };
        @Override
        public void println(String x) {
            super.println();
            isLineStart = true;
        };
        @Override
        public void println(boolean x) {
            super.println();
            isLineStart = true;
        };
        @Override
        public void println(char x) {
            super.println();
            isLineStart = true;
        };
        @Override
        public void println(char[] x) {
            super.println();
            isLineStart = true;
        };
        @Override
        public void println(double x) {
            super.println();
            isLineStart = true;
        };
        @Override
        public void println(float x) {
            super.println();
            isLineStart = true;
        };
        @Override
        public void println(int x) {
            super.println();
            isLineStart = true;
        };
        @Override
        public void println(long x) {
            super.println();
            isLineStart = true;
        };
    };

    private static final String getErrorBlock() {
        return Config.coloredOutput ? ERROR_BLOCK : ERROR_BLOCK_PLAIN;
    }

    private static final String getWarnBlock() {
        return Config.coloredOutput ? WARN_BLOCK : WARN_BLOCK_PLAIN;
    }

    private static final String getInfoBlock() {
        return Config.coloredOutput ? INFO_BLOCK : INFO_BLOCK_PLAIN;
    }

    private static final String getInputBlock() {
        return Config.coloredOutput ? INPUT_BLOCK : INPUT_BLOCK_PLAIN;
    }



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



    public static final void newLine() {
        newLine(1);
    }

    public static final void newLine(int count) {
        newLine(count, System.out);
    }

    public static final void newLine(int count, PrintStream out) {
        for(int i=0; i<count; i++) savelyPrintln("");
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
        return Arrays.deepToString((Object[])o);
    }


    /**
     * Clears the console.
     */
    public static final void clear() {
        savelyPrintln(String.format("\033[2J"));
    }



    private static boolean progressBarActive = false;
    private static int lastOn = -1;
    private static int lastPercentage = -1;
    private static String lastProgressBarType = null;

    /**
     * Sets the given progress on the console progress bar. If there
     * is no progress bar yet there will be created one.
     * 
     * @param progress The progress to set, a value between {@code 0}
     *                 and {@code 1}, inslusive
     */
    public static void setProgress(double progress) {
        internalSetProgress(OutputFilter.INFO, progress);
    }

    /**
     * Sets the given progress on the console progress bar. If there
     * is no progress bar yet there will be created one.
     * 
     * @param progress The progress to set, a value between {@code 0}
     *                 and {@code 1}, inslusive
     */
    public static void setProgress(String type, double progress) {
        internalSetProgress(type, progress);
    }

    private static final void internalSetProgress(String type, double progress) {
        type = type.toLowerCase();
        if(!isAllowedWrapped(type)) return;

        if(progress < 0) progress = 0;
        else if(progress > 1) progress = 1;

        int percentage = (int)Math.round(progress * 100);
        if(percentage == lastPercentage) return;

        int on = (int)Math.round(PROGRESS_BAR_WIDTH * progress);

        StringBuilder dif = new StringBuilder();

        if(!progressBarActive || !type.equals(lastProgressBarType)) {
            if(progressBarActive) System.out.println();
            lastProgressBarType = type;
            dif.append('[').append(colored(type.equals(OutputFilter.LOG) ? timeStamp() : type.toUpperCase(), getColor(type))).append(']');
            dif.append(' ').append(PROB_START);
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

    private static Attribute getColor(String type) {
        if(type.equals(OutputFilter.INFO)) return Attribute.BLUE_TEXT();
        if(type.equals(OutputFilter.DEBUG)) return Attribute.CYAN_TEXT();
        if(type.equals(OutputFilter.WARN)) return Attribute.YELLOW_TEXT();
        if(type.equals(OutputFilter.ERROR)) return Attribute.RED_TEXT();
        if(type.equals(OutputFilter.LOG)) return Attribute.GREEN_TEXT();
        return Attribute.CYAN_TEXT();
    }

    private static boolean isAllowedWrapped(String messageType) {
        return isAllowed(messageType);
    }


    /**
     * Prints the current stack trace until this method call into the console.
     */
    public static final void printStackTrace() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        StackTraceElement[] reducedStackTrace = new StackTraceElement[stackTrace.length - 3];
        System.arraycopy(stackTrace, 2, reducedStackTrace, 0, reducedStackTrace.length);
        String stackString = Arrays.stream(reducedStackTrace).map(e -> e.toString()).collect(Collectors.joining("\n\t"));
        savelyPrintln(">>>" + '\t' + stackString);
    }


    /**
     * Prints a splitting line with the given title in the console.
     * 
     * @param main The title of the split
     */
    private static final void internalSplit(String type, String main, Object... arguments) {
        if(!isAllowedWrapped(type)) return;

        String string = assembly(main, arguments);
        string = string.replace("\n", " ");

        int consoleWidth = getConsoleWidth();

        StringBuilder out = new StringBuilder(consoleWidth);
        out.append('[').append(colored(type.equals(OutputFilter.LOG) ? timeStamp() : type.toUpperCase(), getColor(type))).append("] ");

        int lineLength = consoleWidth - (length(string) + 4);
        int firstHalf = lineLength / 2;
        firstHalf -= length(out.toString());

        out.append('-'); // To have at least one
        for(int i=0; i<firstHalf; i++) out.append('-');
        out.append("< ").append(string).append(Ansi.RESET).append(" >");
        while(length(out.toString()) < consoleWidth) out.append('-');

        savelyPrintln(out);
    }

    public static void split(String main, Object... arguments) {
        internalSplit(OutputFilter.INFO, main, arguments);
    }

    public static void split(String title) {
        internalSplit(OutputFilter.INFO, title, new Object[0]);
    }

    public static void splitCustom(String type, String main, Object... arguments) {
        internalSplit(type, main, arguments);
    }

    public static void splitCustom(String type, String title) {
        internalSplit(type, title, new Object[0]);
    }



    public static void line() {
        internalLine(OutputFilter.INFO);
    }

    public static void line(String type) {
        internalLine(type);
    }

    /**
     * Prints a splitting line in the console;
     */
    private static final void internalLine(String type) {
        if(!isAllowedWrapped(type)) return;

        int consoleWidth = getConsoleWidth();
        StringBuilder line = new StringBuilder(consoleWidth);
        line.append('[').append(colored(type.equals(OutputFilter.LOG) ? timeStamp() : type.toUpperCase(), getColor(type))).append("] ");
        while(length(line.toString()) < consoleWidth) line.append('-');
        savelyPrintln(line);
    }



    /**
     * Prints the given information in the console.
     * 
     * @param x The information to print
     */
    public static final void info(Object x) {
        internalInfo(x);
    }

    /**
     * Prints the given information in the console.
     * 
     * @param main The information to print
     * @param arguments Additional information to print. If {@code main} is a
     *                  {@code String} and not {@code null} each "{}" will be
     *                  replaced with the corresponding argument. Additional
     *                  arguments will be comma-seperated appended.
     */
    public static final void info(Object main, Object... arguments) {
        internalInfo(main, arguments);
    }



    /**
     * Prints the given information in the console.
     * 
     * @param x The information to print
     */
    public static final void debug(Object x) {
        internalDebug(x, new Object[0]);
    }

    /**
     * Prints the given information in the console.
     * 
     * @param main The information to print
     * @param arguments Additional information to print. If {@code main} is a
     *                  {@code String} and not {@code null} each "{}" will be
     *                  replaced with the corresponding argument. Additional
     *                  arguments will be comma-seperated appended.
     */
    public static final void debug(Object main, Object... arguments) {
        internalDebug(main, arguments);
    }



    /**
     * Prints the given object as a message of the given type (which will be
     * both title and filter type) into the console. If the type is one of the
     * default types it will also be threated as such.
     * 
     * @param type The title and filter type of this message
     * @param x The object to print
     */
    public static final void custom(String type, Object x) {
        if(type.toLowerCase().equals(OutputFilter.INFO)) internalInfo(x);
        else if(type.toLowerCase().equals(OutputFilter.DEBUG)) internalDebug(x);
        else if(type.toLowerCase().equals(OutputFilter.WARN)) internalWarn(x);
        else if(type.toLowerCase().equals(OutputFilter.ERROR)) internalError(x);
        else if(type.toLowerCase().equals(OutputFilter.LOG)) internalLog(x);
        else internalCustom(type, x);
    }

    /**
     * Prints the given object as a message of the given type (which will be
     * both title and filter type) into the console. If the type is one of the
     * default types it will also be threated as such.
     * 
     * @param type The title and filter type of this message
     * @param x The object to print
     */
    public static final void custom(String type, Object main, Object... arguments) {
        if(type.toLowerCase().equals(OutputFilter.INFO)) internalInfo(main, arguments);
        else if(type.toLowerCase().equals(OutputFilter.DEBUG)) internalDebug(main, arguments);
        else if(type.toLowerCase().equals(OutputFilter.WARN)) internalWarn(main, arguments);
        else if(type.toLowerCase().equals(OutputFilter.ERROR)) internalError(main, arguments);
        else if(type.toLowerCase().equals(OutputFilter.LOG)) internalLog(main, arguments);
        else internalCustom(type, main, arguments);
    }

    /**
     * Prints the given warning in the console.
     * 
     * @param x The warning to print
     */
    public static final void warn(Object x) {
        internalWarn(x);
    }

    /**
     * Prints the given warnings in the console.
     * 
     * @param x A warning to print
     * @param y Another warning to print
     * @param more More warnings to print
     */
    public static final void warn(Object main, Object... arguments) {
        internalWarn(main, arguments);
    }

    /**
     * Prints the given exception as a warning message into the console
     * 
     * @param exception The exception to print
     */
    public static final void warn(Exception exception) {
        exception.printStackTrace(CONSOLE_WARN_STREAM);
    }

    /**
     * Prints the given error as a warning message into the console
     * 
     * @param error The error to print
     */
    public static final void warn(Error error) {
        error.printStackTrace(CONSOLE_WARN_STREAM);
    }

    /**
     * Prints the given error in the console.
     * 
     * @param x The error to print
     */
    public static final void error(Object x) {
        internalError(x);
    }

    /**
     * Prints the given errors in the console.
     * 
     * @param x A error to print
     * @param y Another error to print
     * @param more More errors to print
     */
    public static final void error(Object main, Object... arguments) {
        internalError(main, arguments);
    }

    /**
     * Prints the given exception as an error message into the console
     * 
     * @param exception The exception to print
     */
    public static final void error(Exception exception) {
        exception.printStackTrace(CONSOLE_ERROR_STREAM);
    }

    /**
     * Prints the given error as an error message into the console
     * 
     * @param error The error to print
     */
    public static final void error(Error error) {
        error.printStackTrace(CONSOLE_ERROR_STREAM);
    }

    /**
     * Logs the current time in the console.
     * <p>This has the same effect as logging {@code ""}.
     */
    public static final void log() {
        internalLog("");
    }

    /**
     * Logs the given information together with the current time
     * in the console.
     * 
     * @param x The information to print
     */
    public static final void log(Object x) {
        internalLog(x);
    }

    /**
     * Logs the given information together with the current time
     * in the console.
     * 
     * @param x An information to print
     * @param y Another information to print
     * @param more More information to print
     */
    public static final void log(Object main, Object... arguments) {
        internalLog(main, arguments);
    }

    /**
     * Prints the two objects as key-value pair as information in
     * the console.
     * <p>For example,
     * <pre>map("Hello", "World");</pre>
     * will result in the output {@code [INFO] Hello: World}
     * 
     * @param key The key to map the value to
     * @param value The value to map
     */
    public static final void map(final Object key, final Object value) {
        internalInfo("{}: {}", key, value);
    }

    /**
     * Prints the two objects as key-value pair as information in
     * the console.
     * <p>For example,
     * <pre>map("Hello", "World");</pre>
     * will result in the output {@code [INFO] Hello: World}
     * 
     * @param key The key to map the value to
     * @param value The value to map
     */
    public static final void map(final Object key, final Object value, Object...arguments) {
        internalInfo(stringFor(key) + ": " + stringFor(value), arguments);
    }

    /**
     * Prints the two objects as key-value pair as information in
     * the console.
     * <p>For example,
     * <pre>map("Hello", "World");</pre>
     * will result in the output {@code [DEBUG] Hello: World}
     * 
     * @param key The key to map the value to
     * @param value The value to map
     */
    public static final void mapDebug(final Object key, final Object value) {
        internalDebug("{}: {}", key, value);
    }

    /**
     * Prints the two objects as key-value pair as information in
     * the console.
     * <p>For example,
     * <pre>map("Hello", "World");</pre>
     * will result in the output {@code [DEBUG] Hello: World}
     * 
     * @param key The key to map the value to
     * @param value The value to map
     */
    public static final void mapDebug(final Object key, final Object value, Object...arguments) {
        internalDebug(stringFor(key) + ": " + stringFor(value), arguments);
    }

    public static final String input(String prompt) {
        if(prompt == null) prompt = "null";

        StringBuilder out = new StringBuilder();

        out.append(getInputBlock());
        out.append(prompt).append(" ");

        savelyPrint(out);

        String result;
        try {
            result = READER.readLine();
        } catch(Exception e) {
            error(e);
            result = null;
        }
        return result;
    }



    private static final void internalInfo(Object format, Object... arguments) {
        internalPrint(OutputFilter.INFO, OutputFilter.INFO, format, arguments, getColor(OutputFilter.INFO));
    }

    private static final void internalDebug(Object format, Object... arguments) {
        internalPrint(OutputFilter.DEBUG, OutputFilter.DEBUG, format, arguments, getColor(OutputFilter.DEBUG));
    }

    private static final void internalWarn(Object format, Object... arguments) {
        internalPrint(OutputFilter.WARN, OutputFilter.WARN, format, arguments, getColor(OutputFilter.WARN));
    }

    private static final void internalError(Object format, Object... arguments) {
        internalPrint(OutputFilter.ERROR, OutputFilter.ERROR, format, arguments, getColor(OutputFilter.ERROR));
    }

    private static final void internalLog(Object format, Object... arguments) {
        internalPrint(timeStamp(), OutputFilter.LOG, format, arguments, getColor(OutputFilter.LOG));
    }

    private static String timeStamp() {
        Calendar c = Calendar.getInstance();
        StringBuilder time = new StringBuilder(8);
        time.append(String.format("%02d", c.get(Calendar.HOUR_OF_DAY)));
        time.append(':');
        time.append(String.format("%02d", c.get(Calendar.MINUTE)));
        time.append(':');
        time.append(String.format("%02d", c.get(Calendar.SECOND)));
        return time.toString();
    }

    private static void internalCustom(String type, Object format, Object... arguments) {
        internalPrint(type.toUpperCase(), type.toLowerCase(), format, arguments, Attribute.CYAN_TEXT());
    }



    private static final void internalPrint(String title, String messageType, Object format, Object[] arguments, Attribute... titleColor) {

        if(!isAllowed(messageType)) return;

        if(arguments == null)
            arguments = new Object[] {null};
            // Will print 'null'

        StringBuilder out = new StringBuilder();

        out.append('[').append(colored(title, titleColor)).append(']');
        out.append(' ');
        String titleBlock = out.toString();

        String string = assembly(format, arguments);
        string = string.replace("\n", " ");
        if(Config.coloredOutput) string += Ansi.RESET;

        final int consoleWidth = getConsoleWidth();

        if(Config.includeLineNumber) {

            String classAndLineString = ' ' + classAndLineString(5);
            
            if(Config.coloredOutput) {
                int remainingLength = consoleWidth - length(titleBlock);
                int requiredRemainingLength = length(string) + classAndLineString.length();

                if(requiredRemainingLength <= remainingLength) {
                    out.append(string);
                    int tabLength = remainingLength - requiredRemainingLength;
                    for(int i=0; i<tabLength; i++) out.append(' ');
                    out.append(classAndLineString);
                }
                else {
                    StringBuilder remainingString = new StringBuilder(string);
                    while(length(remainingString.toString()) > remainingLength) {
                        String cutString = remainingString.substring(0, remainingLength);
                        for(int i=1; length(cutString) < remainingLength; i++) {
                            cutString = remainingString.substring(0, remainingLength + i);
                        }
                        remainingString.delete(0, cutString.length());
                        out.append(cutString).append(Ansi.RESET).append('\n').append(titleBlock);
                    }
                    if(length(remainingString.toString()) <= remainingLength - classAndLineString.length()) {
                        out.append(remainingString);
                        int tabLength = remainingLength - (length(remainingString.toString()) + classAndLineString.length());
                        for(int i=0; i<tabLength; i++) out.append(' ');
                        out.append(classAndLineString);
                    }
                    else {
                        out.append(remainingString).append('\n');
                        out.append(titleBlock);
                        int tabLength = remainingLength - classAndLineString.length();
                        for(int i=0; i<tabLength; i++) out.append(' ');
                        out.append(classAndLineString);
                    }
                }
            }
            else {
                string = plain(string);

                int remainingLength = consoleWidth - titleBlock.length();
                int requiredRemainingLength = string.length() + classAndLineString.length();

                if(requiredRemainingLength <= remainingLength) {
                    out.append(string);
                    int tabLength = remainingLength - requiredRemainingLength;
                    for(int i=0; i<tabLength; i++) out.append(' ');
                    out.append(classAndLineString);
                }
                else {
                    StringBuilder remainingString = new StringBuilder(string);
                    while(remainingString.length() > remainingLength) {
                        String cutString = remainingString.substring(0, remainingLength);
                        remainingString.delete(0, remainingLength);
                        out.append(cutString).append('\n').append(titleBlock);
                    }
                    if(remainingString.length() <= remainingLength - classAndLineString.length()) {
                        out.append(remainingString);
                        int tabLength = remainingLength - (remainingString.length() + classAndLineString.length());
                        for(int i=0; i<tabLength; i++) out.append(' ');
                        out.append(classAndLineString);
                    }
                    else {
                        out.append(remainingString).append('\n');
                        out.append(titleBlock);
                        int tabLength = remainingLength - classAndLineString.length();
                        for(int i=0; i<tabLength; i++) out.append(' ');
                        out.append(classAndLineString);
                    }
                }
            }

            savelyPrintln(out);


            int usedWidth = consoleWidth - ((out.length() + classAndLineString.length() - (Config.coloredOutput ? colored("", titleColor).length() : 0)) % consoleWidth);
            for(int i=0; i!=usedWidth; i++) out.append(' ');
            out.append(classAndLineString);
        }
        else {

            out.append(string);

            if(Config.coloredOutput) {
                while(length(out.toString()) > consoleWidth) {
                    String line = out.substring(0, consoleWidth);
                    for(int i=1; length(line) < consoleWidth; i++) {
                        line = out.substring(0, consoleWidth + i);
                    }
                    savelyPrintln(line + Ansi.RESET);
                    out.delete(0, line.length());
                    out.insert(0, titleBlock);
                }
            }
            else {
                while(out.length() > consoleWidth) {
                    savelyPrintln(out.substring(0, consoleWidth));
                    out.delete(0, consoleWidth);
                    out.insert(0, titleBlock);
                }
            }
            savelyPrintln(out);
        }
    }



    private static String plain(String x) {
        return x.replaceAll("\u001b\\[\\d+(;\\d+)*m", "");
    }

    private static int length(String x) {
        return plain(x).length();
    }



    private static boolean isAllowed(String messageType) {
        try {
            StackTraceElement[] elements = Thread.currentThread().getStackTrace();
            int index = elements.length > 5 ? 5 : elements.length - 1;
            String cls = elements[index].getClassName();
            return isEnabled(messageType, cls);
        } catch(Exception e) {
            return true;
        }
    }

    private static String assembly(Object format, Object[] arguments) {
        if(arguments != null && arguments.length == 0) return stringFor(format);
        if(format == null || !(format instanceof String)) {
            if(arguments == null) return stringFor(format) + ", null";
            return stringFor(format) + ", " + Arrays.stream(arguments).map(o -> stringFor(o)).collect(Collectors.joining(", "));
        }
        StringBuilder out = new StringBuilder((String)format);
        for(Object argument : arguments) {
            int index = out.indexOf("{}");
            if(index != -1)
                out = out.replace(index, index + 2, stringFor(argument));
            else out.append(", ").append(stringFor(argument));
        }
        return out.toString();
    }



    private static final void savelyPrintln(Object x) {
        savelyPrint(x);
        System.out.println();
    }

    private static final void savelyPrint(Object x) {
        if(progressBarActive) {
            System.out.println();
            progressBarActive = false;
        }
        System.out.print(x);
    }




    /**
     * Returns the given string colored in the specified color. If {@link coloredOutput}
     * is {@code false} or the color is {@code null}, the input string will be returned.
     * Note that the size of the string will increase by {@code 9} if the text actually
     * gets colored.
     * 
     * @param string The string to color
     * @param color The color to paint the string in
     * @return The painted string
     */
    public static final String colored(String string, Attribute...attributes) {
        if(!Config.coloredOutput || attributes == null) return string;
        return Ansi.colorize(string, attributes);
    }

    private static final String classAndLineString(int off) {
        try {
            final StackTraceElement[] elements = Thread.currentThread().getStackTrace();
            final int index = elements.length > off ? off : elements.length - 1;
            return elements[index].getFileName() + ':' + elements[index].getLineNumber();
        } catch(Exception e) { }
        return "";
    }

    private static final int getConsoleWidth() {
        if(Config.manualConsoleWidth != null) return Config.manualConsoleWidth;
        try {
            //return Math.max(TerminalFactory.get().getWidth(), 100);
        } catch(Exception e) { }
        return 100;
    }

    /**
     * A set of colors that can be chosen to color text in using {@code colored(String, Colors)}.
     */
    public static enum Colors {
        WHITE,
        YELLOW,
        RED,
        PURPLE,
        BLUE,
        CYAN,
        GREEN,
        BLACK
    }



    /**
     * A filter for output.
     */
    public static class OutputFilter {

        public static final String INFO = "info";
        public static final String DEBUG = "debug";
        public static final String WARN = "warn";
        public static final String ERROR = "error";
        public static final String LOG = "log";

        private final HashMap<String, Boolean> settings = new HashMap<>();
        public final String clsOrPkg;

        /**
         * By default, every filter behaves exactly like its super filter by simply
         * requesting all its results.
         * 
         * @param clsOrPkg This filter's full class or package name
         */
        private OutputFilter(String clsOrPkg) {
            this.clsOrPkg = clsOrPkg;
            FILTERS.put(clsOrPkg, this);
        }

        /**
         * Returns weather the given message type is currently enabled. If not specifically
         * set this will return the result of the same call on the super-filter of this
         * filter. If this filter is responsible for the default package, it will return some
         * default values.
         * 
         * @param messageType The type of message to check for (case-insensitive)
         * @return Weather the given message type is currently enabled by this filter
         */
        public boolean isEnabled(String messageType) {
            messageType = messageType.toLowerCase();
            Boolean enabled = settings.get(messageType);
            if(enabled == null) return getSuperOrDefaultEnabled(messageType);
            return enabled;
        }

        /**
         * Sets weather this filter should allow messages of the given message type. Passing
         * {@code null} will make the filter return the super filter's enabled state, or for
         * the default package filter some default values.
         * 
         * @param messageType The type of message to set the filter state for
         * @param enabled Weather the filter should allow, disallow or use the super filter's
         *                enabled state for the messages of that type
         */
        public void setEnabled(String messageType, Boolean enabled) {
            settings.put(messageType.toLowerCase(), enabled);
        }

        private boolean getSuperOrDefaultEnabled(String messageType) {
            if(clsOrPkg.length() > 0)
                return getFilter(clsOrPkg.substring(0, Math.max(clsOrPkg.lastIndexOf("."), 0))).isEnabled(messageType);

            if(OutputFilter.DEBUG.equals(messageType.toLowerCase())) return false;
            return true;
        }
    }



    /**
     * Returns weather messages of the given type posted from the specified class or package
     * will be displayed. This returns exactly the same as calling
     * <pre>getFilter(clsOrPkg).isEnabled(messageType);</pre>
     * 
     * @param messageType The type of message, for example {@code OutputFilter.INFO} for info messages.
     *                    Case-insensitive.
     * @param clsOrPkg The full name of the class or package to check for
     * @return Weather messages from the given class or package of the specified type will be
     *         displayed
     */
    public static boolean isEnabled(String messageType, String clsOrPkg) {
        return getFilter(clsOrPkg).isEnabled(messageType);
    }

    /**
     * Returns weather messages of the given type posted from the specified class will be
     * displayed. This returns exactly the same as calling
     * <pre>getFilter(cls).isEnabled(messageType);</pre>
     * 
     * @param messageType The type of message, for example {@code OutputFilter.INFO} for info messages.
     *                    Case-insensitive.
     * @param cls The class to check for
     * @return Weather messages from the given class of the specified type will be displayed
     */
    public static boolean isEnabled(String messageType, Class<?> cls) {
        return isEnabled(messageType, cls.getName());
    }

    /**
     * Returns the {@link OutputFilter} for the specified class or package. If the exact class
     * or package did not have a specific filter applied before a new one will be created,
     * behaving exactly as the previously effective filter.
     * 
     * @param clsOrPkg The full name of the class or package to get or create the filter of
     * @return The filter for exactly that class or package
     */
    public static OutputFilter getFilter(String clsOrPkg) {
        OutputFilter filter = FILTERS.get(clsOrPkg);
        if(filter != null) return filter;
        return new OutputFilter(clsOrPkg);
    }

    /**
     * Returns the {@link OutputFilter} for the specified class. If the exact class did not
     * have a specific filter applied before a new one will be created, behaving exactly as
     * the previously effective filter.
     * 
     * @param cls The class to get or create the filter of
     * @return The filter for exactly that class
     */
    public static OutputFilter getFilter(Class<?> cls) {
        return getFilter(cls.getName());
    }

    /**
     * Returns the filter of the default package that is the filter for all output, unless
     * there was a filter for the specific class or package specified.
     * 
     * @return The default output filter
     */
    public static OutputFilter getDefaultFilter() {
        return getFilter("");
    }

    /**
     * Removes the filter that applies for exactly that class or package, if there was one.
     * 
     * @param clsOrPkg The full name of the class or package to remove the filter of
     * @return Weather a filter was removed
     * @throws IllegalArgumentException If an attempt was made to remove the default package
     *                                  filter
     */
    public static boolean removeFilter(String clsOrPkg) {
        if(clsOrPkg.length() == 0) throw new IllegalArgumentException("Cannot remove the filter of the default package");
        return FILTERS.remove(clsOrPkg) != null;
    }

    /**
     * Removes the filter that applies for exactly that class, if there was one.
     * 
     * @param cls The class to remove the filter of
     * @return Weather a filter was removed
     */
    public static boolean removeFilter(Class<?> cls) {
        return removeFilter(cls.getName());
    }



    public static void main(String[] args) {
        System.setErr(CONSOLE_WARN_STREAM);
        test();
    }


    private static void test() {
        Console.getDefaultFilter().setEnabled(OutputFilter.DEBUG, true);
        Console.split("Hello World!");
        Console.split("Hello {}!", "World");
        Console.splitCustom(OutputFilter.DEBUG, "Hello World!");
        Console.splitCustom(OutputFilter.DEBUG, "Hello {}!", "World");
        Console.splitCustom(OutputFilter.WARN, "Hello World!");
        Console.splitCustom(OutputFilter.WARN, "Hello {}!", "World");
        Console.splitCustom(OutputFilter.ERROR, "Hello World!");
        Console.splitCustom(OutputFilter.ERROR, "Hello {}!", "World");
        Console.splitCustom(OutputFilter.LOG, "Hello World!");
        Console.splitCustom(OutputFilter.LOG, "Hello {}!", "World");
        Console.splitCustom("custom", colored("Hello World!", Attribute.RED_TEXT()));
        Console.splitCustom("A stupidly long type of message that can still be handled", "Hello {}!", "World");
        Console.line();
    }
}
