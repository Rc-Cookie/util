package com.github.rccookie.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;

import com.diogonunes.jcolor.Ansi;
import com.diogonunes.jcolor.Attribute;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Utility class to log messages into the console or a selected output stream.
 * <p>The messages can contain a variable number of elements. Each element will first
 * be converted to a string. Arrays will automatically be converted to a readable
 * format equivalently to {@link Arrays#deepToString(Object[])}. Then one element
 * after the other gets appended with the delimiter {@value DELIMITER}, unless the
 * already created message contains the literal {@code {}}. If it does, the {} will
 * instead be replaced with the next message element.</p>
 * <p>Every message has a tag. Using {@link Console.OutputFilter} specific tags can
 * be hidden or shown, for all classes or for specific calling classes or packages.</p>
 */
@SuppressWarnings({"StringRepeatCanBeUsed", "SameParameterValue", "DuplicatedCode"})
public final class Console {

    private Console() {
        throw new UnsupportedOperationException();
    }

    /**
     * A BufferedReader on {@link System#in}. Cannot be closed.
     */
    public static final BufferedReader in = new BufferedReader(new InputStreamReader(System.in)) {
        @Override
        public void close() {
            throw new UnsupportedOperationException("The console reader cannot be closed.");
        }
    };

    /**
     * Delimiter between console parameters
     */
    private static final String DELIMITER = " ";
    /**
     * Colors for the [INPUT] tag.
     */
    private static final Attribute[] INPUT_COLORS = { Attribute.MAGENTA_TEXT() };

    /**
     * Width of the progress bar's inner bar.
     */
    private static final int PROGRESS_BAR_WIDTH = 10;
    /**
     * Left border of the progress bar.
     */
    private static final char PROGRESS_BAR_START = '[';
    /**
     * Char for a full bar segment.
     */
    private static final char PROGRESS_BAR_ON = '.';
    /**
     * Right border of the progress bar.
     */
    private static final char PROGRESS_BAR_END = ']';


    /**
     * Contains configuration options for the console class.
     */
    public static final class Config {

        /**
         * The output stream of the console. Can be adjusted to change the output target without
         * affecting {@link System#out}.
         */
        public static final TransferPrintStream out = new TransferPrintStream(System.out);


        /**
         * The maximum width of console output, in chars.
         */
        public static int width = 100;
        /**
         * Whether color codes should be printed.
         */
        public static boolean colored = true;
        /**
         * Whether all prints should include a time stamp.
         */
        public static boolean logTime = false;
        /**
         * Whether the file and line number where the print takes place should be printed. Useful
         * for finding unwanted logs.
         */
        public static boolean includeLineNumber = false;

        private Config() {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Is the progress bar currently active and not done?
     */
    private static boolean progressBarActive = false;
    /**
     * Last progress displayed by the progress bar, in chars.
     */
    private static int lastOn = -1;
    /**
     * Last progress percentage displayed.
     */
    private static int lastPercentage = -1;
    /**
     * Type of the last progress bar.
     */
    private static String lastProgressBarType = null;


    /**
     * Prints an INFO message into the console.
     *
     * @param message The elements to display
     */
    public static void info(Object... message) {
        write0(OutputFilter.INFO, message, 1);
    }

    /**
     * Prints a LOG message into the console.
     *
     * @param message The elements to display
     */
    public static void log(Object... message) {
        write0(OutputFilter.LOG, message, 1);
    }

    /**
     * Prints a LOG message into the console using the current time stamp
     * as type, independent of {@link Config#logTime}.
     *
     * @param message The elements to display
     */
    public static void logTime(Object... message) {
        boolean oldState = Config.logTime;
        Config.logTime = true;
        write0(OutputFilter.LOG, message, 1);
        Config.logTime = oldState;
    }

    /**
     * Prints a DEBUG message into the console.
     *
     * @param message The elements to display
     */
    public static void debug(Object... message) {
        write0(OutputFilter.DEBUG, message, 1);
    }

    /**
     * Prints a WARN message into the console.
     *
     * @param message The elements to display
     */
    public static void warn(Object... message) {
        write0(OutputFilter.WARN, message, 1);
    }

    /**
     * Prints the given throwable as WARN message into the console,
     * with its stacktrace.
     *
     * @param t The throwable to print
     */
    public static void warn(Throwable t) {
        printStackTrace0(OutputFilter.WARN, true, t, 1);
    }

    /**
     * Prints an ERROR message into the console.
     *
     * @param message The elements to display
     */
    public static void error(Object... message) {
        write0(OutputFilter.ERROR, message, 1);
    }

    /**
     * Prints the given throwable as ERROR message into the console,
     * with its stacktrace.
     *
     * @param t The throwable to print
     */
    public static void error(Throwable t) {
        printStackTrace0(OutputFilter.ERROR, true, t, 1);
    }

    /**
     * Prints a message with a custom tag into the console.
     *
     * @param messageType The tag of the message
     * @param message The elements to print
     */
    public static void write(String messageType, Object... message) {
        write0(messageType, message, 1);
    }

    /**
     * Logs a {@code key: value} pair into the console.
     *
     * @param key The key to print
     * @param value The value to print
     * @param more More elements to print
     */
    public static void map(Object key, Object value, Object... more) {
        map0(OutputFilter.LOG, 1, key, value, more);
    }

    /**
     * Logs a {@code key: value} pair as debug message into the console.
     *
     * @param key The key to print
     * @param value The value to print
     * @param more More elements to print
     */
    public static void mapDebug(Object key, Object value, Object... more) {
        map0(OutputFilter.DEBUG, 1, key, value, more);
    }

    /**
     * Internal map method.
     *
     * @param messageType Message tag
     * @param callDepth Call depth since last public method
     * @param key Key to print
     * @param value Value to print
     * @param more More elements to print
     */
    @SuppressWarnings("SameParameterValue")
    private static void map0(String messageType, int callDepth, Object key, Object value, Object... more) {
        Object[] message = new Object[more != null ? more.length + 1 : 1];
        message[0] = stringFor(key) + ": " + stringFor(value);
        if(more != null)
            System.arraycopy(more, 0, message, 1, more.length);
        write0(messageType, message, callDepth+1);
    }

    /**
     * Internal method to print the stack trace of an exception.
     *
     * @param messageType The message tag to use
     * @param asException Whether the exception should be printed as exception,
     *                    or only its stack trace without the exception name and message
     * @param t The exception
     * @param callDepth Call depth since last public method
     */
    private static void printStackTrace0(String messageType, boolean asException, Throwable t, int callDepth) {
        if(hideMessageType(messageType, callDepth)) return;

        StringWriter message = new StringWriter();
        if(t != null) t.printStackTrace(new PrintWriter(message));
        else message.append("null");

        if(asException)
            write0(messageType, new Object[] { message }, callDepth+1);
        else {
            String typePrefix = getTypePrefix(messageType);
            int typeLength = length(typePrefix);
            writeLine(typePrefix, typeLength, "--- Stack Trace ---", false, callDepth+1);

            String[] lines = message.toString().split("\n");
            for(int i=1; i<lines.length; i++)
                writeLine(typePrefix, typeLength, lines[i].substring(4), false, callDepth+1);
        }
    }

    /**
     * Logs a line with the given title into the console.
     *
     * @param message The title in the center of the line
     */
    public static void split(Object... message) {
        split0(OutputFilter.LOG, 1, message);
    }

    /**
     * Prints a line with a custom tag into the console.
     *
     * @param messageType The message tag
     * @param message The title in the center of the line
     */
    public static void splitCustom(String messageType, Object... message) {
        split0(messageType, 1, message);
    }

    /**
     * Internal method to print a line with a title into the console.
     *
     * @param messageType The message tag
     * @param callDepth Call depth since last public method
     * @param message The elements to use as title
     */
    @SuppressWarnings("SameParameterValue")
    private static void split0(String messageType, int callDepth, Object... message) {
        if(hideMessageType(messageType, callDepth)) return;

        String string = assembleMessage(message);
        string = string.replace("\n", " ");

        StringBuilder out = new StringBuilder(Config.width);
        out.append(getTypePrefix(messageType));

        int lineLength = Config.width - (length(string) + 4);
        int firstHalf = lineLength / 2;
        firstHalf -= length(out.toString());

        out.append('-'); // To have at least one
        for(int i=0; i<firstHalf; i++)
            out.append('-');
        out.append("< ").append(string).append(Config.colored ? Ansi.RESET : "").append(" >");
        while(length(out.toString()) < Config.width) out.append('-');

        println(out.toString());
    }

    /**
     * Returns the current time stamp, in the format {@code HH:MM:SS}.
     *
     * @return The current time stamp
     */
    @NotNull
    private static String getTimeStamp() {
        Calendar c = Calendar.getInstance();
        return String.format("%02d", c.get(Calendar.HOUR_OF_DAY)) + ':' +
                String.format("%02d", c.get(Calendar.MINUTE)) + ':' +
                String.format("%02d", c.get(Calendar.SECOND));
    }

    /**
     * Writes a message into the console.
     *
     * @param messageType The message tag
     * @param message The message
     * @param callDepth Call depth since last public method
     */
    private static void write0(String messageType, Object[] message, int callDepth) {
        if(hideMessageType(messageType, callDepth)) return;

        String type = getTypePrefix(messageType);
        int typeLength = length(type);
        String stringMessage = assembleMessage(message);

        String[] lines = stringMessage.split("\n");
        for(int i=0; i<lines.length; i++)
            writeLine(type, typeLength, lines[i], Config.includeLineNumber && i == lines.length-1, callDepth+1);
    }

    /**
     * Prints a message string that does not include newlines into the console.
     *
     * @param typePrefix The tag name, as {@code [TAG]_} or {@code [HH:MM:SS]_[TAG]_}
     * @param typeLength The length of typePrefix, but only visible characters
     * @param message The message string
     * @param includeLineNumber Whether to include the line number
     * @param callDepth Call depth since last public method
     */
    private static void writeLine(String typePrefix, int typeLength, String message, boolean includeLineNumber, int callDepth) {
        StringBuilder remaining = new StringBuilder(message);
        int maxMessageLength = Math.max(1, Config.width - typeLength);

        if(includeLineNumber) {
            String lineNumber = ' ' + getLineNumberString(callDepth + 1);
            int messageLength = length(remaining.toString()) + lineNumber.length();
            while(messageLength > maxMessageLength) messageLength -= maxMessageLength;
            int spaces = maxMessageLength - messageLength;
            for(int i=0; i<spaces; i++) remaining.append(' ');
            remaining.append(lineNumber);
        }
        while (true) {
            if(length(remaining.toString()) > maxMessageLength) {
                print(typePrefix);
                StringBuilder line = new StringBuilder(remaining.substring(0, maxMessageLength));
                remaining.delete(0, maxMessageLength);
                while (length(line.toString()) < maxMessageLength) {
                    line.append(remaining.charAt(0));
                    remaining.deleteCharAt(0);
                }
                if(Config.colored) line.append(Ansi.RESET);
                println(line.toString());
            } else {
                println(typePrefix + remaining);
                return;
            }
        }
    }

    /**
     * Returns the type prefix for the given message tag, with time stamp if configured.
     *
     * @param type The message tag
     * @return The tag prefix as {@code [TAG]_} or {@code [HH:MM:SS]_[TAG]_}
     */
    private static String getTypePrefix(String type) {
        type = type.toUpperCase();
        Attribute[] colors = getColors(type);
        if(!Config.logTime) return '[' + colored(type, colors) + "] ";
        String timeStamp = '[' + colored(getTimeStamp(), colors) + "] ";
        if(type.equals("LOG")) return timeStamp;
        return timeStamp + '[' + colored(type, colors) + "] ";
    }

    /**
     * Returns whether the given message is configured to be hidden.
     *
     * @param messageType The message tag
     * @param callDepth Call depth since last public method
     * @return Whether the message should not be printed
     */
    private static boolean hideMessageType(String messageType, int callDepth) {
        try {
            String cls = Thread.currentThread().getStackTrace()[callDepth + 3].getClassName();
            return !isEnabled(messageType, cls);
        } catch(Exception e) {
            return false;
        }
    }

    /**
     * Returns the current file and line number, as {@code File.java:line}.
     *
     * @param callDepth Call depth since last public method
     * @return The current line number
     */
    @NotNull
    private static String getLineNumberString(int callDepth) {
        try {
            final StackTraceElement[] elements = Thread.currentThread().getStackTrace();
            return elements[callDepth+2].getFileName() + ':' + elements[callDepth+2].getLineNumber();
        } catch(Exception ignored) { }
        return "";
    }

    /**
     * Assembles the given message elements to a message string.
     *
     * @param message The message elements
     * @return The message string
     */
    @NotNull
    private static String assembleMessage(@Nullable Object[] message) {
        if(message == null) return "null";
        if(message.length == 0) return "";

        StringBuilder string = new StringBuilder(stringFor(message[0]));
        for(int i=1; i<message.length; i++) {
            int index = string.toString().indexOf("{}");
            String nextString = stringFor(message[i]);
            if(index != -1) string.replace(index, index + 2, nextString);
            else string.append(DELIMITER).append(nextString);
        }
        return string.toString();
    }

    /**
     * Returns a string representation for the given object.
     *
     * @param o The object to get the string representation of
     * @return The string representation
     */
    @NotNull
    private static String stringFor(@Nullable Object o) {
        if(o == null) return "null";
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
     * Returns the given string colored in the given colors. Does nothing if
     * colored output is disabled.
     *
     * @param message The message to colorize
     * @param colors The colors and formatting for the message
     * @return The colored messages
     */
    public static String colored(String message, Attribute... colors) {
        return Config.colored ? Ansi.colorize(message, colors) : message;
    }

    /**
     * Removes all ANSI codes from the given message.
     *
     * @param message The message to de-colorize
     * @return The plain message string
     */
    private static String removeColors(@NotNull String message) {
        return Config.colored ? message.replaceAll("\u001b\\[\\d+(;\\d+)*m", "") : message;
    }

    /**
     * Returns the colors for the given message tag.
     *
     * @param type The message tag
     * @return The colors for the tag
     */
    private static Attribute[] getColors(@NotNull String type) {
        type = type.toLowerCase();
        if(type.equals(OutputFilter.LOG))   return new Attribute[] { Attribute.GREEN_TEXT() };
        if(type.equals(OutputFilter.DEBUG)) return new Attribute[] { Attribute.CYAN_TEXT() };
        if(type.equals(OutputFilter.WARN))  return new Attribute[] { Attribute.YELLOW_TEXT() };
        if(type.equals(OutputFilter.ERROR)) return new Attribute[] { Attribute.RED_TEXT() };
        if(type.equals(OutputFilter.INFO))  return new Attribute[] { Attribute.BLUE_TEXT() };
        if(type.equals("input"))            return new Attribute[] { Attribute.MAGENTA_TEXT() };
        return new Attribute[] { Attribute.CYAN_TEXT() };
    }

    /**
     * Returns the visual length of the given message string.
     *
     * @param message The (potentially colored) message string
     * @return The length of the message's visual part
     */
    private static int length(String message) {
        return removeColors(message).length();
    }

    /**
     * Safely prints the given message into the configured output stream.
     *
     * @param message The message to print
     */
    private static void print(String message) {
        if(progressBarActive) {
            Config.out.println();
            progressBarActive = false;
        }
        Config.out.print(message);
        Config.out.flush();
    }

    /**
     * Safely prints the given message and a newline into the configured
     * output stream.
     *
     * @param line The message to print
     */
    private static void println(String line) {
        print(line);
        Config.out.println();
        Config.out.flush();
    }

    /**
     * Logs the current stack trace into the console.
     */
    public static void printStackTrace() {
        if(hideMessageType(OutputFilter.LOG, 1)) return; // Don't create unused exception
        printStackTrace0(OutputFilter.LOG, false, new Exception(), 1);
    }

    /**
     * Prints the current stack trace into the console with the given message tag.
     *
     * @param messageType The message tag
     */
    public static void printStackTrace(String messageType) {
        if(hideMessageType(messageType, 1)) return; // Don't create unused exception
        printStackTrace0(messageType, false, new Exception(), 1);
    }


    /**
     * Sets the given progress on the console progress bar. If there
     * is no progress bar yet there will be created one.
     *
     * @param progress The progress to set, a value between {@code 0}
     *                 and {@code 1}, inclusive
     */
    public static void setProgress(double progress) {
        internalSetProgress(OutputFilter.LOG, progress, 1);
    }

    /**
     * Sets the given progress on the console progress bar. If there
     * is no progress bar yet there will be created one.
     *
     * @param progress The progress to set, a value between {@code 0}
     *                 and {@code 1}, inclusive
     */
    public static void setProgress(@NotNull String type, double progress) {
        internalSetProgress(type, progress, 1);
    }

    /**
     * Internal method to set the progress of the progress bar.
     *
     * @param messageType The message tag
     * @param progress The progress of the progress bar
     * @param callDepth Call depth since last public method
     */
    private static void internalSetProgress(@NotNull String messageType, double progress, int callDepth) {
        messageType = messageType.toLowerCase();
        if(hideMessageType(messageType, callDepth)) return;

        if(progress < 0) progress = 0;
        else if(progress > 1) progress = 1;

        int percentage = (int)Math.round(progress * 100);
        if(percentage == lastPercentage) return;

        int on = (int)Math.round(PROGRESS_BAR_WIDTH * progress);

        StringBuilder dif = new StringBuilder();

        if(!progressBarActive || !messageType.equals(lastProgressBarType)) {
            if(progressBarActive) System.out.println();
            lastProgressBarType = messageType;
            dif.append(getTypePrefix(messageType));
            dif.append(PROGRESS_BAR_START);
            for(int i=0; i<PROGRESS_BAR_WIDTH; i++) dif.append(i < on ? PROGRESS_BAR_ON : ' ');
        }
        else {
            int percWidth = (lastPercentage + "").length();
            for(int i=0; i<percWidth+3; i++)
                dif.append('\b');

            if(on != lastOn) {
                int min = Math.min(on, lastOn);
                for(int i=0; i<PROGRESS_BAR_WIDTH-min; i++)
                    dif.append('\b');

                for(int i=min; i<PROGRESS_BAR_WIDTH; i++) dif.append(i < on ? PROGRESS_BAR_ON : ' ');
            }
        }

        dif.append(PROGRESS_BAR_END).append(' ').append(percentage).append("%");

        Config.out.print(dif);

        lastOn = on;
        lastPercentage = percentage;
        if(percentage == 100) {
            Config.out.println();
            progressBarActive = false;
        }
        else progressBarActive = true;
    }

    /**
     * Displays the given message into the console and returns the next entered line from
     * {@link System#in}.
     *
     * @param message The message elements
     * @return The entered line, without newline
     */
    public static String input(Object... message) {
        String string = assembleMessage(message);

        print(getTypePrefix("input") + (string.isEmpty() ? "" : string + ' '));

        String result;
        try {
            result = in.readLine();
        } catch(Exception e) {
            error(e);
            result = null;
        }
        return result;
    }

    /**
     * Displays the given message and {@code "_(Y/N)?_"} into the console and returns {@code true}
     * if the user entered "Y"/"y" and {@code false} on "N"/"n", and repeats the question otherwise.
     *
     * @param message The question elements
     * @return Whether the user selected "y" or "n"
     */
    public static boolean inputYesNo(Object... message) {
        String string = assembleMessage(message);
        String messageWithoutYN = colored("INPUT", INPUT_COLORS) + (string.isEmpty() ? "" : string + ' ');

        print(messageWithoutYN + getYesNoEnd(false));

        String result;
        try {
            result = strip(in.readLine());
            if(result.equalsIgnoreCase("y") || result.equalsIgnoreCase("j")) return true;
            if(result.equalsIgnoreCase("n")) return false;

            String messageWithYN = messageWithoutYN + getYesNoEnd(true);

            while(true) {
                print(messageWithYN);
                result = strip(in.readLine());
                if(result.equalsIgnoreCase("y") || result.equalsIgnoreCase("j")) return true;
                if(result.equalsIgnoreCase("n")) return false;
            }
        } catch(Exception e) {
            error(e);
            return false;
        }
    }

    /**
     * Returns {@code (Y/N)?_}, where "Y/N" is colored if wanted.
     *
     * @param inWarnColor Whether "Y/N" should be colored
     * @return The y/n string
     */
    private static String getYesNoEnd(boolean inWarnColor) {
        return inWarnColor ? "(" + colored("Y/N", Attribute.RED_TEXT()) + ")? " : "(Y/N)? ";
    }

    /**
     * Manual implementation of {@link String#strip()}.
     *
     * @param str The string to strip
     * @return The stripped string
     */
    private static String strip(String str) {
        StringBuilder out = new StringBuilder(str);
        while(Character.isWhitespace(out.charAt(0)))
            out.deleteCharAt(0);
        while(Character.isWhitespace(out.charAt(out.length()-1)))
            out.deleteCharAt(out.length()-1);
        return out.toString();
    }


    /**
     * Configured filters, and the default filter.
     */
    private static final HashMap<String, OutputFilter> FILTERS = new HashMap<>();
    static {
        new Console.OutputFilter("", true);
        if("true".equals(System.getProperty("intellij.debug.agent"))) {
            Console.getDefaultFilter().setEnabled(OutputFilter.DEBUG, true);
            Config.width = 243;
        }
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
        @NotNull
        public final String clsOrPkg;

        /**
         * By default, every filter behaves exactly like its super filter by simply
         * requesting all its results.
         *
         * @param clsOrPkg This filter's full class or package name
         */
        private OutputFilter(@NotNull String clsOrPkg, boolean register) {
            this.clsOrPkg = clsOrPkg;
            if(register) FILTERS.put(clsOrPkg, this);
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
        public boolean isEnabled(@NotNull String messageType) {
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
        public void setEnabled(@NotNull String messageType, @Nullable Boolean enabled) {
            settings.put(messageType.toLowerCase(), enabled);
        }

        /**
         * Sets weather this filter should allow messages of the given message type. If this
         * filter was specified to allow, disallow or default this message type, this will
         * have no effect.
         *
         * @param messageType The type of message to set the filter state for
         * @param enabled Weather this filter should allow or disallow messages of the given
         *                type, if never specified before
         */
        public void setDefault(@NotNull String messageType, boolean enabled) {
            if(settings.containsKey(messageType.toLowerCase())) return;
            settings.put(messageType.toLowerCase(), enabled);
        }

        private boolean getSuperOrDefaultEnabled(@NotNull String messageType) {
            if(clsOrPkg.length() > 0)
                return getFilter(clsOrPkg.substring(0, Math.max(Math.max(clsOrPkg.lastIndexOf("."), clsOrPkg.lastIndexOf("$")), 0))).isEnabled(messageType);

            return !DEBUG.equalsIgnoreCase(messageType);
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
    public static boolean isEnabled(@NotNull String messageType, @NotNull String clsOrPkg) {
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
    public static boolean isEnabled(@NotNull String messageType, @NotNull Class<?> cls) {
        return isEnabled(messageType, cls.getName());
    }

    /**
     * Returns weather messages of the given type posted from the calling class will be
     * displayed. This returns exactly the same as calling
     * <pre>getFilter().isEnabled(messageType);</pre>
     *
     * @param messageType The type of message, for example {@code OutputFilter.INFO} for info messages.
     *                    Case-insensitive.
     * @return Weather messages from the class that calls this method of the specified type will
     *         be displayed
     */
    public static boolean isEnabled(@NotNull String messageType) {
        return getFilter().isEnabled(messageType);
    }

    /**
     * Returns the {@link OutputFilter} for the specified class or package. If the exact class
     * or package did not have a specific filter applied before a new one will be created,
     * behaving exactly as the previously effective filter.
     *
     * @param clsOrPkg The full name of the class or package to get or create the filter of
     * @return The filter for exactly that class or package
     */
    @NotNull
    public static OutputFilter getFilter(@NotNull String clsOrPkg) {
        OutputFilter filter = FILTERS.get(Arguments.checkNull(clsOrPkg));
        if(filter != null) return filter;
        return new OutputFilter(clsOrPkg, true);
    }

    /**
     * Returns the {@link OutputFilter} for the specified class. If the exact class did not
     * have a specific filter applied before a new one will be created, behaving exactly as
     * the previously effective filter.
     *
     * @param cls The class to get or create the filter of
     * @return The filter for exactly that class
     */
    @NotNull
    public static OutputFilter getFilter(@NotNull Class<?> cls) {
        return getFilter(cls.getName());
    }

    /**
     * Returns the {@link OutputFilter} for the calling class. If the exact class did not
     * have a specific filter applied before a new one will be created, behaving exactly as
     * the previously effective filter.
     *
     * @return The filter exactly for the class that calls this method
     */
    @NotNull
    public static OutputFilter getFilter() {
        try {
            StackTraceElement[] elements = Thread.currentThread().getStackTrace();
            int index = elements.length > 2 ? 2 : elements.length - 1;
            String cls = elements[index].getClassName();
            return getFilter(cls);
        } catch(Exception e) {
            return new OutputFilter("", false) {
                @Override
                public boolean isEnabled(@NotNull String messageType) {
                    return true;
                }
            };
        }
    }

    /**
     * Returns the filter of the default package that is the filter for all output, unless
     * there was a filter for the specific class or package specified.
     *
     * @return The default output filter
     */
    @NotNull
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
    public static boolean removeFilter(@NotNull String clsOrPkg) {
        if(clsOrPkg.length() == 0) throw new IllegalArgumentException("Cannot remove the filter of the default package");
        return FILTERS.remove(clsOrPkg) != null;
    }

    /**
     * Removes the filter that applies for exactly that class, if there was one.
     *
     * @param cls The class to remove the filter of
     * @return Weather a filter was removed
     */
    public static boolean removeFilter(@NotNull Class<?> cls) {
        return removeFilter(cls.getName());
    }
}
