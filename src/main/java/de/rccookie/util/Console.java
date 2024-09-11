package de.rccookie.util;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.diogonunes.jcolor.Ansi;
import com.diogonunes.jcolor.Attribute;
import com.sun.jna.AltCallingConvention;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.win32.W32APIOptions;
import de.rccookie.math.Mathf;
import de.rccookie.util.function.ThrowingRunnable;
import de.rccookie.util.function.ThrowingSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

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
     * kernel32.dll library wrapper.
     */
    private interface Kernel32 extends Library, AltCallingConvention {
        Kernel32 INSTANCE = Native.load("kernel32", Kernel32.class, W32APIOptions.DEFAULT_OPTIONS);

        boolean SetConsoleOutputCP(int wCodePageID);
    }


    /**
     * The output stream used by the console by default. This may be {@link System#out},
     * but may also be a different stream to fix Windows encoding issues.
     */
    public static final PrintStream stdOut;
    /**
     * The output stream used by the console by default. This may be {@link System#err},
     * but may also be a different stream to fix Windows encoding issues.
     */
    public static final PrintStream stdErr;
    static {
        PrintStream out = System.out, err = System.err;
        if(System.console() != null && System.getProperty("os.name").toLowerCase().contains("win")) {
            try {
                if(Kernel32.INSTANCE.SetConsoleOutputCP(65001)) {
                    out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
                    err = new PrintStream(System.err, true, StandardCharsets.UTF_8);
                }
            } catch(Throwable t) {
                out = System.out;
                err = System.err;
            }
        }
        stdOut = out;
        stdErr = err;
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

    private static final Object WRITE_LOCK = new Object();

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
    private static final int DEFAULT_PROGRESS_BAR_WIDTH = 10;
    /**
     * Left border of the progress bar.
     */
    private static final String DEFAULT_PROGRESS_BAR_LEFT_BOUND = "[";
    /**
     * Char for a full bar segment.
     */
    private static final char DEFAULT_PROGRESS_BAR_ON = '.';
    /**
     * Char for an empty bar segment.
     */
    private static final char DEFAULT_PROGRESS_BAR_OFF = ' ';
    /**
     * Right border of the progress bar.
     */
    private static final String DEFAULT_PROGRESS_BAR_RIGHT_BOUND = "]";
    /**
     * Line prefix for rendering the scope from which the message was sent.
     */
    private static final char SCOPE_PREFIX = '|';
    private static final Attribute[] RANDOM_SCOPE_COLORS = {
            Attribute.RED_TEXT(),
            Attribute.GREEN_TEXT(),
            Attribute.YELLOW_TEXT(),
            Attribute.BLUE_TEXT(),
            Attribute.MAGENTA_TEXT(),
            Attribute.CYAN_TEXT(),
            Attribute.TEXT_COLOR(255, 128, 0)
    };

    private static final Pattern ANSI_SEQ_PAT = Pattern.compile("\u001b\\[\\d+(;\\d+)*m");

    private static final boolean IS_TERMINAL = System.console() != null;


    /**
     * Contains configuration options for the console class.
     */
    public static final class Config {

        /**
         * The output stream of the console. Can be adjusted to change the output target without
         * affecting {@link System#out}.
         */
        public static final TransferPrintStream out = new TransferPrintStream(stdOut);

        /**
         * The maximum width of console output, in chars.
         */
        public static int width = 1000;
        /**
         * Whether color codes should be printed.
         */
        public static boolean colored = true;
        /**
         * Whether all prints should include a time stamp.
         */
        public static boolean logTime = false;
        /**
         * Whether all date prefixes should also contain the day, month and year.
         */
        public static boolean logDateWithTime = false;
        /**
         * Whether the file and line number where the print takes place should be printed. Useful
         * for finding unwanted logs.
         */
        public static boolean includeLineNumber = false;
        /**
         * Which of the leading [TAG] prefixes to show. This does not affect the visibility of the
         * message content itself.
         */
        public static TagMode tagMode = TagMode.ALL;
        /**
         * If and how to show the console scopes.
         */
        public static ScopeMode scopeMode = ScopeMode.REGULAR;

        private Config() {
            throw new UnsupportedOperationException();
        }

        /**
         * Controls which of the leading [TAG] prefixes are shown with the messages.
         */
        public enum TagMode {
            /**
             * Show all tags.
             */
            ALL {
                @Override
                boolean show(String type) {
                    return true;
                }
            },
            /**
             * Show all tags except [INPUT].
             */
            NO_INPUT {
                @Override
                boolean show(String type) {
                    return !type.equals("INPUT");
                }
            },
            /**
             * Only show [WARN] and [ERROR] tags.
             */
            WARN_ONLY {
                @Override
                boolean show(String type) {
                    return type.equals("WARN") || type.equals("ERROR");
                }
            },
            /**
             * Only show [ERROR] tags.
             */
            ERROR_ONLY {
                @Override
                boolean show(String type) {
                    return type.equals("ERROR");
                }
            },
            /**
             * Don't show any tags.
             */
            NONE {
                @Override
                boolean show(String type) {
                    return false;
                }
            };

            abstract boolean show(String type);
        }

        public enum ScopeMode {
            REGULAR,
            WITHOUT_COLOR,
            HIDDEN
        }
    }

    private static final CompactIdGenerator SCOPE_GENERATOR = new SynchronizedCompactIdGenerator();
    private static final ThreadLocal<ThreadScopes> SCOPES = ThreadLocal.withInitial(ThreadScopes::new);
    private static final List<Integer> lastScopes = new ArrayList<>();
    private static final int[] RAND_COLOR_USE_COUNT = new int[RANDOM_SCOPE_COLORS.length];
    private static final Deque<Integer> RAND_COLOR_USE_ORDER = IntStream.range(0, RANDOM_SCOPE_COLORS.length).boxed().collect(Collectors.toCollection(ArrayDeque::new));

    /**
     * If the progress bar is active, this describes its inner width. Otherwise,
     * this is <= 0.
     */
    private static int currentProgressBarWidth = -1;
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
    private static String lastProgressBarType = OutputFilter.LOG;
    private static String lastProgressBarLeftBound, lastProgressBarRightBound;
    private static char[] lastProgressBarOnSegment, lastProgressBarOffSegment;
    private static boolean lastProgressBarShowPercentage;

    private static String prompt = null;

    private static int currentLine = 0;


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
            String messagePrefix = getMessagePrefix(messageType);
            int typeLength = length(messagePrefix);
            String[] lines = message.toString().split("\n");

            synchronized(Console.class) {
                writeLine(messagePrefix, typeLength, "--- Stack Trace ---", false, callDepth + 1, true);
                for(int i=1; i<lines.length; i++)
                    writeLine(messagePrefix, typeLength, lines[i].substring(4), false, callDepth + 1, true);
            }
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
        out.append(getMessagePrefix(messageType));

        int lineLength = Config.width - (length(string) + 4);
        int firstHalf = lineLength / 2;
        firstHalf -= length(out.toString());

        out.append('-'); // To have at least one
        for(int i=0; i<firstHalf; i++)
            out.append('-');
        out.append("< ").append(string).append(Config.colored ? Ansi.RESET : "").append(" >");
        while(length(out.toString()) < Config.width) out.append('-');

        print(out.toString(), true);
    }

    /**
     * Enters a visual scope local to the thread. All messages printed by this thread will
     * then be preceded by a vertical bar (in the specified colors). Other scopes (from this or
     * other threads) will be visualized with vertical bars offset to the left or right. The
     * scope can be terminated using {@link #exitScope()} or using {@link Scope#exit()}.
     *
     * <p>The returned object implements the {@link AutoCloseable}</p> interface. Thus, it
     * is possibly to use try-with-resource statements for the scope:
     * <pre>
     * try(var ignored = enterScope()) {
     *     Console.log("This is scoped");
     *     throw new RuntimeException();
     *     // Scope will still be closed
     * } catch(Exception e) { }
     * Console.log("Not scoped anymore");
     * </pre>
     *
     * @param colors Optionally, the color scheme to simplify distinguishing scopes
     * @return The created scope, can be used to later exit the scope
     */
    public static Scope enterScope(Attribute... colors) {
        return new Scope(SCOPES.get().add(new ScopeData(SCOPE_GENERATOR.allocate(), colors)).index, colors);
    }

    /**
     * Similar to {@link #enterScope(Attribute...)}, but the scope will be assigned a "random"
     * color. The color will be chosen in a way to decrease the chance of using the same color
     * twice shortly after another.
     *
     * @return The created scope, can be used to later exit the scope and to receive information
     *         about the selected color
     */
    public static Scope enterRandomScope() {
        int color;
        synchronized(RAND_COLOR_USE_ORDER) {
            color = RAND_COLOR_USE_ORDER.getFirst();
            int minUseCount = Integer.MAX_VALUE;
            for(int i : RAND_COLOR_USE_ORDER) {
                if(RAND_COLOR_USE_COUNT[i] < minUseCount) {
                    minUseCount = RAND_COLOR_USE_COUNT[i];
                    color = i;
                }
            }
            RAND_COLOR_USE_ORDER.remove(color);
            RAND_COLOR_USE_ORDER.addLast(color);
            RAND_COLOR_USE_COUNT[color]++;
        }
        return new Scope(SCOPES.get().add(new ScopeData(SCOPE_GENERATOR.allocate(), color)).index, RANDOM_SCOPE_COLORS[color]);
    }

    /**
     * Exists the innermost visual scope of the current thread. This is usually the desired behaviour, however,
     * if you want to exit a scope different from the last created scope, use {@link #exitScope(int)}.
     */
    public static void exitScope() {
        finalizeScopeExit(SCOPES.get().pop());
    }

    /**
     * Exists a specific visual scope of the current thread.
     *
     * @param scopeId The id of the scope to close
     */
    private static void exitScope(int scopeId) {
        finalizeScopeExit(SCOPES.get().remove(scopeId));
    }

    private static void finalizeScopeExit(ScopeData scope) {
        if(scope.randomColor != null) {
            synchronized(RAND_COLOR_USE_ORDER) {
                RAND_COLOR_USE_COUNT[scope.randomColor]--;
            }
        }
        synchronized(SCOPE_GENERATOR) {
            if(scope.everUsed)
                lastScopes.add(scope.index);
            else SCOPE_GENERATOR.free(scope.index);
        }
    }

    /**
     * Executes the given code while within a new scope block, as if created with {@link #enterScope(Attribute...)}.
     * After the action is finished (possibly with an exception thrown), the scope will be closed again (by id).
     *
     * @param action The code to execute within a new scope
     * @param colors Optionally, the color scheme to simplify distinguishing scopes
     * @throws E Exceptions thrown by the executed code
     */
    public static <E extends Throwable> void scoped(ThrowingRunnable<E> action, Attribute... colors) throws E {
        try(Scope ignored = enterScope(colors)) {
            action.run();
        }
    }

    /**
     * Executes the given code while within a new scope block, as if created with {@link #enterScope(Attribute...)}.
     * After the action is finished (possibly with an exception thrown), the scope will be closed again (by id).
     *
     * @param action The code to execute within a new scope
     * @param colors Optionally, the color scheme to simplify distinguishing scopes
     * @return The value returned by the code executed
     * @throws E Exceptions thrown by the executed code
     */
    public static <R, E extends Throwable> R scoped(ThrowingSupplier<R,E> action, Attribute... colors) throws E {
        try(Scope ignored = enterScope(colors)) {
            return action.get();
        }
    }

    /**
     * Executes the given code while within a new scope block, as if created with {@link #enterRandomScope()}.
     * After the action is finished (possibly with an exception thrown), the scope will be closed again (by id).
     *
     * @param action The code to execute within a new scope
     * @throws E Exceptions thrown by the executed code
     */
    public static <E extends Throwable> void scopedRandomly(ThrowingRunnable<E> action) throws E {
        try(Scope ignored = enterRandomScope()) {
            action.run();
        }
    }

    /**
     * Executes the given code while within a new scope block, as if created with {@link #enterRandomScope()}.
     * After the action is finished (possibly with an exception thrown), the scope will be closed again (by id).
     *
     * @param action The code to execute within a new scope
     * @return The value returned by the code executed
     * @throws E Exceptions thrown by the executed code
     */
    public static <R, E extends Throwable> R scopedRandomly(ThrowingSupplier<R,E> action) throws E {
        try(Scope ignored = enterRandomScope()) {
            return action.get();
        }
    }

    /**
     * Returns the current time stamp, in the format {@code HH:mm:SS} or {@code DD:MM:YY HH:mm:SS}.
     *
     * @return The current time stamp
     */
    @NotNull
    private static String getTimeStamp() {
        Calendar c = Calendar.getInstance();
        String tod = String.format("%02d:%02d:%02d", c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), c.get(Calendar.SECOND));
        if(!Config.logDateWithTime) return tod;
        return String.format("%02d.%02d.%02d %s", c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.MONTH), c.get(Calendar.YEAR) % 100, tod);
    }

    /**
     * Writes a message into the console.
     *
     * @param messageType The message tag
     * @param message The message
     * @param callDepth Call depth since last public method
     */
    private static void write0(String messageType, Object[] message, int callDepth) {
        write0(messageType, message, true, callDepth + 1);
    }

    /**
     * Writes a message into the console.
     *
     * @param messageType The message tag
     * @param message The message
     * @param callDepth Call depth since last public method
     * @param finalNewline Whether the final line should be terminated by a newline
     */
    private static int write0(String messageType, Object[] message, boolean finalNewline, int callDepth) {
        if(hideMessageType(messageType, callDepth)) return 0;

        String prefix = getMessagePrefix(messageType);
        int prefixLength = length(prefix);
        String stringMessage = assembleMessage(message);

        String[] lines = stringMessage.split("\n");
        int remaining = 0;
        synchronized(Console.class) {
            for(int i = 0; i < lines.length; i++)
                remaining = writeLine(prefix, prefixLength, lines[i], Config.includeLineNumber && i == lines.length - 1, callDepth + 1, i != lines.length - 1 || finalNewline);
        }
        return remaining;
    }

    /**
     * Prints a message string that does not include newlines into the console.
     *
     * @param messagePrefix Stuff to prepend to any line written
     * @param prefixLength The length of messagePrefix, but only visible characters
     * @param message The message string
     * @param includeLineNumber Whether to include the line number
     * @param callDepth Call depth since last public method
     */
    private static int writeLine(String messagePrefix, int prefixLength, String message, boolean includeLineNumber, int callDepth, boolean finalNewline) {
        StringBuilder remaining = new StringBuilder(message);
        int maxMessageLength = Math.max(1, Config.width - prefixLength);

        if(includeLineNumber) {
            String lineNumber = ' ' + getLineNumberString(callDepth + 1);
            int messageLength = length(remaining.toString()) + lineNumber.length();
            while(messageLength > maxMessageLength) messageLength -= maxMessageLength;
            int spaces = maxMessageLength - messageLength;
            for(int i=0; i<spaces; i++) remaining.append(' ');
            remaining.append(lineNumber);
        }
        while(true) {
            int len = length(remaining.toString());
            if(len > maxMessageLength) {
                StringBuilder line = new StringBuilder(remaining.substring(0, maxMessageLength));
                remaining.delete(0, maxMessageLength);
                while (length(line.toString()) < maxMessageLength) {
                    line.append(remaining.charAt(0));
                    remaining.deleteCharAt(0);
                }
                if(Config.colored) line.append(Ansi.RESET);
                print(messagePrefix + line, true);
            } else {
                print(messagePrefix + remaining, finalNewline);
                return maxMessageLength - len;
            }
        }
    }

    private static String getMessagePrefix(String type) {
        String typePrefix = getTypePrefix(type);
        String scopePrefix = getScopePrefix();
        if(typePrefix.isEmpty() || scopePrefix.isEmpty())
            return typePrefix + scopePrefix;
        // Pad type prefix such that at least all regular types (those with maximum length 5)
        // show the scope aligned properly
        return typePrefix + Utils.blank(Math.max(0, 8 - length(typePrefix))) + scopePrefix;
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

        Config.TagMode mode = Config.tagMode;
        String tag = mode == null || mode.show(type) ? '[' + colored(type, colors) + "] " : "";

        if(!Config.logTime) return tag;
        String timeStamp = '[' + colored(getTimeStamp(), colors) + "] ";
        if(type.equals("LOG")) return timeStamp;
        return timeStamp + tag;
    }

    private static String getScopePrefix() {
        if(Config.scopeMode == Config.ScopeMode.HIDDEN)
            return "";

        int totalScopes;
        synchronized(SCOPE_GENERATOR) {
            totalScopes = SCOPE_GENERATOR.range();
            if(totalScopes > 0 && SCOPE_GENERATOR.usedCount() > lastScopes.size())
                totalScopes = Math.max(totalScopes, Mathf.max(lastScopes));
            else totalScopes = 0;

            for(int scope : lastScopes)
                SCOPE_GENERATOR.free(scope);
            lastScopes.clear();

            if(totalScopes == 0)
                return "";
        }

        StringBuilder prefix = new StringBuilder();
        ThreadScopes threadScopes = SCOPES.get();
        threadScopes.markAsUsed();
        for(int i=0; i<totalScopes; i++) {
            ScopeData scope = threadScopes.getOrNull(i);
            prefix.append(scope != null && Config.scopeMode == Config.ScopeMode.REGULAR ? colored(SCOPE_PREFIX, scope.colors) : " ");
        }
        return prefix.append(' ').toString();
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
        if(o instanceof String) return (String) o;
        if(o instanceof Throwable) {
            StringWriter str = new StringWriter();
            ((Throwable) o).printStackTrace(new PrintWriter(str));
            return str.toString();
        }
        if(o instanceof Color) {
            Color c = (Color) o;
            String str = c.getAlpha() != 255 ? String.format("#%08x", c.getRGB()) : String.format("#%06x", c.getRGB() & 0xFFFFFF);
            if(!Config.colored)
                return str;
            return colored(str + " \u25A0", Attribute.TEXT_COLOR(c.getRed(), c.getGreen(), c.getBlue()));
        }
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
     * Returns the given object as string colored in the given colors. Does nothing if
     * colored output is disabled.
     *
     * @param message The message to colorize
     * @param colors The colors and formatting for the message
     * @return The colored message
     */
    public static String colored(Object message, Attribute... colors) {
        String msg = stringFor(message);
        return colors != null && colors.length != 0 && Config.colored ? Ansi.colorize(msg, colors) : msg;
    }

    /**
     * Removes all ANSI codes from the given message.
     *
     * @param message The message to de-colorize
     * @return The plain message string
     */
    private static String removeColors(@NotNull String message) {
        return Config.colored ? ANSI_SEQ_PAT.matcher(message).replaceAll("") : message;
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
     * Safely prints the given message and a newline into the configured
     * output stream.
     *
     * @param line The message to print
     */
    private static synchronized void print(String line, boolean newline) {
        if(prompt == null) {
            if(currentProgressBarWidth > 0) {
                Config.out.println();
                currentLine++;
                currentProgressBarWidth = -1;
            }
            if(newline) {
                Config.out.println(line);
                currentLine++;
            }
            else Config.out.print(line);
        }
        else if(newline) {
            Config.out.println();
            currentLine++;
            Config.out.print(line);
        }
        else {
            Config.out.print(Utils.repeat('\b', prompt.length()));
            Config.out.println(line);
            currentLine++;
            Config.out.print(prompt);
        }
        Config.out.flush();
    }

    private static String prompt(String line) {
        synchronized(Console.class) {
            if(currentProgressBarWidth > 0 || prompt != null) {
                Config.out.println();
                currentLine++;
                currentProgressBarWidth = -1;
            }
            prompt = line;
            Config.out.print(line);
            Config.out.flush();
        }
        try {
            return in.readLine();
        } catch(IOException e) {
            throw Utils.rethrow(e);
        } finally {
            synchronized(Console.class) {
                prompt = null;
            }
        }
    }

    private static void output(Runnable outputAction) {
        synchronized(WRITE_LOCK) {
            outputAction.run();
        }
        Config.out.flush(); // TODO: Use lock, consider stack trace
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


    public static boolean supportsLine() {
        return IS_TERMINAL;
    }

    public static Line logLine(Object... message) {
        return line0(OutputFilter.LOG, 1, message);
    }

    public static Line debugLine(Object... message) {
        return line0(OutputFilter.DEBUG, 1, message);
    }

    public static Line line(String messageType, Object... message) {
        return line0(messageType, 1, message);
    }

    private static Line line0(String messageType, int callDepth, Object... message) {
        if(hideMessageType(messageType, callDepth))
            return HIDDEN_LINE;
        String prefix = getMessagePrefix(messageType);
        String content = assembleMessage(message);
        if(!supportsLine())
            return new CompatabilityLine(prefix, content);
        return new RealLine(prefix, content);
    }


    /**
     * Sets the given progress on the console progress bar. If there
     * is no progress bar yet there will be created one with the default design.
     *
     * @param progress The progress to set, a value between {@code 0}
     *                 and {@code 1}, inclusive
     */
    public static void setProgress(double progress) {
        internalSetProgress(null, -1, null, null, null, null, null, progress, null, 1);
    }

    /**
     * Returns a builder to create a new progress bar with progress 0, which will terminate any existing
     * progress bar once started. The progress bar will first be printed when
     * {@link ProgressBarBuilder#start()} is called. The builder can safely be reused.
     * <p>
     * The newly created progress bar can then subsequently be updated using
     * {@link #setProgress(double)}.
     * </p>
     *
     * @param message The message to prepend before
     * @return A new progress bar builder
     */
    public static ProgressBarBuilder newProgress(Object... message) {
        return new ProgressBarBuilder(message);
    }

    /**
     * Returns a builder to create a new progress bar with debug message type, which will
     * terminate any existing progress bar once started. The progress bar will first be printed when
     * {@link ProgressBarBuilder#start()} is called. The builder can safely be reused.
     * <p>
     * The newly created progress bar can then subsequently be updated using
     * {@link #setProgress(double)}.
     * </p>
     * <p>
     * This method is equivalent to calling <code>newProgressBar(message).type({@value OutputFilter#DEBUG})</code>.
     * </p>
     *
     * @param message The message to prepend before
     * @return A new progress bar builder
     */
    public static ProgressBarBuilder newDebugProgress(Object... message) {
        return newProgress(message).type(OutputFilter.DEBUG);
    }

    /**
     * Terminates any existing progress bar, such that a subsequent call to
     * {@link #setProgress(double)} will definitively create a new progress bar.
     * If {@link #setProgress(double)} is called with progress = 1, the progress
     * bar will automatically be ended.
     */
    public static synchronized void endProgress() {
        if(currentProgressBarWidth > 0) {
            Config.out.println();
            currentLine++;
            currentProgressBarWidth = -1;
        }
    }

    /**
     * Sets the progress bar to the 100% and ends it. Equivalent to <code>endProgress(1)</code>.
     * Note that this will create a new progress bar if none existed.
     */
    public static synchronized void finishProgress() {
        internalSetProgress(null, -1, null, null, null, null, null, 1, null, 1);
        endProgress();
    }

    /**
     * Sets the progress bar to the given value and ends it. Equivalent to
     * <pre>
     * setProgress(progress);
     * endProgress();
     * </pre>
     * Note that this will create a new progress bar if none existed.
     *
     * @param progress The progress to set as the final value
     */
    public static synchronized void endProgress(double progress) {
        internalSetProgress(null, -1, null, null, null, null, null, progress, null, 1);
        endProgress();
    }

    /**
     * Internal method to set the progress of the progress bar.
     *
     * @param messageType The message tag
     * @param progress The progress of the progress bar
     * @param callDepth Call depth since last public method
     */
    private static synchronized void internalSetProgress(@Nullable String messageType,
                                                         int width,
                                                         @Nullable String leftBound, @Nullable String rightBound,
                                                         char[] onSegment, char[] offSegment,
                                                         Boolean showPercentage,
                                                         double progress,
                                                         Object[] message,
                                                         int callDepth) {

        String actualType = currentProgressBarWidth > 0 ? lastProgressBarType : messageType != null ? messageType : OutputFilter.LOG;
        if(hideMessageType(actualType, callDepth)) return;

        if(prompt != null) {
            Config.out.println();
            currentLine++;
            prompt = null;
        }

        progress = Mathf.clamp(progress, 0, 1);
        if(width <= 0)
            width = currentProgressBarWidth > 0 ? currentProgressBarWidth : DEFAULT_PROGRESS_BAR_WIDTH;
        if(leftBound == null)
            leftBound = currentProgressBarWidth > 0 ? lastProgressBarLeftBound : DEFAULT_PROGRESS_BAR_LEFT_BOUND;
        if(rightBound == null)
            rightBound = currentProgressBarWidth > 0 ? lastProgressBarRightBound : DEFAULT_PROGRESS_BAR_RIGHT_BOUND;
        if(onSegment == null)
            onSegment = currentProgressBarWidth > 0 ? lastProgressBarOnSegment : new char[] { DEFAULT_PROGRESS_BAR_ON };
        if(offSegment == null)
            offSegment = currentProgressBarWidth > 0 ? lastProgressBarOffSegment : new char[] { DEFAULT_PROGRESS_BAR_OFF };
        if(showPercentage == null)
            showPercentage = currentProgressBarWidth <= 0 || lastProgressBarShowPercentage;
        if(currentProgressBarWidth > 0)
            message = new Object[0];

        int totalWidth = 1 + leftBound.length() + width + rightBound.length() + (showPercentage ? 5 : 0);

        int percentage = (int) Math.round(progress * 100);
        int on = (int) Math.round(width * progress);

        StringBuilder dif = new StringBuilder();

        if(messageType != null || currentProgressBarWidth <= 0) {

            // Don't set width yet, let write / print insert newline it was set
            lastProgressBarType = actualType;
            lastProgressBarLeftBound = leftBound;
            lastProgressBarRightBound = rightBound;
            lastProgressBarOnSegment = onSegment;
            lastProgressBarOffSegment = offSegment;
            lastProgressBarShowPercentage = showPercentage;

            if(message == null || message.length != 0) {
                if(write0(actualType, message, false, callDepth + 1) < totalWidth) {
                    Config.out.println();
                    currentLine++;
                    write0(actualType, new Object[0], false, callDepth + 1);
                }
                else dif.append(' '); // Space after non-empty message
            }
            else write0(actualType, message, false, callDepth + 1);

            currentProgressBarWidth = width;

            dif.append(leftBound);
            for(int i=0; i<width; i++)
                dif.append(i < on ? onSegment[i % onSegment.length] : offSegment[i % offSegment.length]);
        }
        else {
            if(percentage == lastPercentage) return;

            int deleteBoundCount = rightBound.length() + (showPercentage ? 2 + (lastPercentage+"").length() : 0);
            for(int i=0; i<deleteBoundCount; i++)
                dif.append('\b');

            if(on != lastOn) {
                int min = Math.min(on, lastOn);
                for(int i=0; i<width-min; i++)
                    dif.append('\b');

                for(int i=min; i<width; i++)
                    dif.append(i < on ? onSegment[i % onSegment.length] : offSegment[i % offSegment.length]);
            }
        }

        dif.append(rightBound);
        if(showPercentage)
            dif.append(' ').append(percentage).append("%");

        Config.out.print(dif);

        lastOn = on;
        lastPercentage = percentage;
        if(percentage == 100) {
            Config.out.println();
            currentLine++;
            currentProgressBarWidth = -1;
        }
        else currentProgressBarWidth = width;
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
        try {
            return prompt(getMessagePrefix("input") + (string.isEmpty() ? "" : string + ' '));
        } catch(Exception e) {
            error(e);
            return "";
        }
    }

    /**
     * Displays the given message and {@code "_(Y/N)?_"} into the console and returns {@code true}
     * if the user entered "Y"/"y" and {@code false} on "N"/"n", and repeats the question otherwise.
     *
     * @param message The question elements
     * @return Whether the user selected "y" or "n"
     */
    public static boolean inputYesNo(Object... message) {
        Object[] yesNoMsg = appendToMessage(message, getYesNoEnd(false));

        String result;
        try {
            result = input(yesNoMsg);
            if(result.equalsIgnoreCase("y") || result.equalsIgnoreCase("j")) return true;
            if(result.equalsIgnoreCase("n")) return false;

            yesNoMsg[yesNoMsg.length - 1] = getYesNoEnd(true);

            while(true) {
                result = input(yesNoMsg);
                if(result.equalsIgnoreCase("y") || result.equalsIgnoreCase("j")) return true;
                if(result.equalsIgnoreCase("n")) return false;
            }
        } catch(Exception e) {
            error(e);
            return false;
        }
    }

    /**
     * Returns {@code (Y/N)?}, where "Y/N" is colored if desired.
     *
     * @param inWarnColor Whether "Y/N" should be colored
     * @return The y/n string
     */
    private static String getYesNoEnd(boolean inWarnColor) {
        return inWarnColor ? "(" + colored("Y/N", Attribute.RED_TEXT()) + ")?" : "(Y/N)?";
    }

    /**
     * Displays the given message and waits for user input, repeating the prompt while the
     * user enters input that cannot be interpreted as an integer.
     *
     * @param message The message prompt to show
     * @return The number entered by the user
     */
    public static BigInteger inputInteger(Object... message) {
        String msg = assembleMessage(message)+" >";
        while(true) {
            String input = input(msg);
            try {
                return new BigInteger(input);
            } catch(NumberFormatException e) {
                if(input.matches("[+-]?\\d*[.,]\\d*"))
                    Console.error("Please input an integer value");
                else Console.error("Please input a number");
            }
        }
    }

    /**
     * Displays the given message and the spedified range and waits for user input, repeating the
     * prompt while the user enters input which cannot be interpreted as an integer or is out of range.
     *
     * @param min The (inclusive) lower bound for the input, or <code>null</code> for no lower bound
     * @param max The (exclusive) upper bound for the input, or <code>null</code> for no upper bound
     * @param message The message to show in front of the range
     * @return The number entered by the user
     */
    public static BigInteger inputIntegerInRange(@Nullable BigInteger min, @Nullable BigInteger max, Object... message) {
        if(min != null && max != null && min.compareTo(max) >= 0)
            throw new ArgumentOutOfRangeException("min >= max ("+min+" >= "+max+")");
        if(min == null && max == null)
            return inputInteger(assembleMessage(message));

        Object[] rangeMsg = appendToMessage(message, getRangeEnd(min, max, false));
        while(true) {
            String input = input(rangeMsg);
            try {
                BigInteger x = new BigInteger(input);
                if((min == null || x.compareTo(min) >= 0) && (max == null || x.compareTo(max) < 0))
                    return x;
                rangeMsg = appendToMessage(message, getRangeEnd(min, max, true));
            } catch(NumberFormatException e) {
                rangeMsg = appendToMessage(message, getRangeEnd(min, max, false));
                if(input.matches("[+-]?\\d*[.,]\\d*"))
                    Console.error("Please input an integer value");
                else Console.error("Please input a number");
            }
        }
    }

    /**
     * Displays the given message and waits for user input, repeating the prompt while the
     * user enters input that cannot be interpreted as an integer or is out of long range.
     *
     * @param message The message prompt to show
     * @return The number entered by the user
     */
    public static long inputLong(Object... message) {
        String msg = assembleMessage(message)+" >";
        while(true) {
            String input = input(msg);
            try {
                return Long.parseLong(input);
            } catch(NumberFormatException e) {
                if(input.matches("[+]?\\d+"))
                    Console.error("Number too large");
                else if(input.matches("-\\d+"))
                    Console.error("Number too large");
                else if(input.matches("[+-]?\\d*[.,]\\d*"))
                    Console.error("Please input an integer value");
                else Console.error("Please input a number");
            }
        }
    }

    /**
     * Displays the given message and waits for user input, repeating the prompt while the
     * user enters input that cannot be interpreted as an integer or is out of int range.
     *
     * @param message The message prompt to show
     * @return The number entered by the user
     */
    public static int inputInt(Object... message) {
        String msg = assembleMessage(message)+" >";
        while(true) {
            String input = input(msg);
            try {
                return Integer.parseInt(input);
            } catch(NumberFormatException e) {
                if(input.matches("[+]?\\d+"))
                    Console.error("Number too large");
                else if(input.matches("-\\d+"))
                    Console.error("Number too large");
                else if(input.matches("[+-]?\\d*[.,]\\d*"))
                    Console.error("Please input an integer value");
                else Console.error("Please input a number");
            }
        }
    }

    /**
     * Displays the given message and the spedified range and waits for user input, repeating the
     * prompt while the user enters input which cannot be interpreted as an integer or is out of range.
     *
     * @param min The (inclusive) lower bound for the input, or <code>null</code> for no lower bound
     * @param max The (exclusive) upper bound for the input, or <code>null</code> for no upper bound
     * @param message The message to show in front of the range
     * @return The number entered by the user
     */
    public static int inputIntInRange(@Nullable Integer min, @Nullable Integer max, Object... message) {
        if(min != null && max != null && min >= max)
            throw new ArgumentOutOfRangeException("min >= max ("+min+" >= "+max+")");
        if(min == null && max == null)
            return inputInt(assembleMessage(message));

        Object[] rangeMsg = appendToMessage(message, getRangeEnd(min, max, false));
        while(true) {
            String input = input(rangeMsg);
            boolean rangeError = false;
            try {
                int x = Integer.parseInt(input);
                if((min == null || x >= min) && (max == null || x < max))
                    return x;
                rangeError = true;
            } catch(NumberFormatException e) {
                if(input.matches("[+-]?\\d+"))
                    rangeError = true;
                else if(input.matches("[+-]?\\d*[.,]\\d*"))
                    Console.error("Please input an integer value");
                else Console.error("Please input a number");
            }
            rangeMsg = appendToMessage(message, getRangeEnd(min, max, rangeError));
        }
    }

    /**
     * Displays the given message and waits for user input, repeating the prompt while the
     * user enters input that cannot be interpreted as a number.
     *
     * @param message The message prompt to show
     * @return The number entered by the user
     */
    public static BigDecimal inputDecimal(Object... message) {
        String msg = assembleMessage(message)+" >";
        while(true) {
            String input = input(msg);
            try {
                return new BigDecimal(input);
            } catch(NumberFormatException e) {
                Console.error("Please input a number");
            }
        }
    }

    /**
     * Displays the given message and the spedified range and waits for user input, repeating the
     * prompt while the user enters input which cannot be interpreted as a numer or is out of range.
     *
     * @param min The (inclusive) lower bound for the input, or <code>null</code> for no lower bound
     * @param max The (exclusive) upper bound for the input, or <code>null</code> for no upper bound
     * @param message The message to show in front of the range
     * @return The number entered by the user
     */
    public static BigDecimal inputDecimalInRange(@Nullable BigDecimal min, @Nullable BigDecimal max, Object... message) {
        if(min != null && max != null && min.compareTo(max) > 0)
            throw new ArgumentOutOfRangeException("min > max ("+min+" > "+max+")");
        if(min == null && max == null)
            return inputDecimal(assembleMessage(message));

        Object[] rangeMsg = appendToMessage(message, getRangeEnd(min, max, false));
        while(true) {
            String input = input(rangeMsg);
            try {
                BigDecimal x = new BigDecimal(input);
                if((min == null || x.compareTo(min) >= 0) && (max == null || x.compareTo(max) <= 0))
                    return x;
                rangeMsg = appendToMessage(message, getRangeEnd(min, max, true));
            } catch(NumberFormatException e) {
                rangeMsg = appendToMessage(message, getRangeEnd(min, max, false));
                Console.error("Please input a number");
            }
        }
    }

    /**
     * Displays the given message and waits for user input, repeating the prompt while the
     * user enters input that cannot be interpreted as a number or would resolve to +- infinity.
     *
     * @param message The message prompt to show
     * @return The number entered by the user
     */
    public static double inputDouble(Object... message) {
        String msg = assembleMessage(message)+" >";
        while(true) {
            String input = input(msg);
            try {
                double x = Double.parseDouble(input);
                if(Double.isFinite(x))
                    return x;
                if(Double.isNaN(x))
                    Console.error("Please input a number");
                else if(x > 0)
                    Console.error("Number too large");
                else Console.error("Number too small");
            } catch(NumberFormatException e) {
                Console.error("Please input a number");
            }
        }
    }

    /**
     * Displays the given message and the spedified range and waits for user input, repeating the
     * prompt while the user enters input which cannot be interpreted as a numer or is out of range.
     *
     * @param min The (inclusive) lower bound for the input, or <code>null</code> for no lower bound
     * @param max The (exclusive) upper bound for the input, or <code>null</code> for no upper bound
     * @param message The message to show in front of the range
     * @return The number entered by the user
     */
    public static double inputDoubleInRange(@Nullable Double min, @Nullable Double max, Object... message) {
        if(min != null && max != null && min > max)
            throw new ArgumentOutOfRangeException("min > max ("+min+" > "+max+")");
        if(min == null && max == null)
            return inputInt(assembleMessage(message));

        Object[] rangeMsg = appendToMessage(message, getRangeEnd(min, max, false));
        while(true) {
            String input = input(rangeMsg);
            boolean rangeError = false;
            try {
                double x = Double.parseDouble(input);
                if((min == null || x >= min) && (max == null || x <= max)) {
                    if(Double.isFinite(x))
                        return x;
                    if(x > 0)
                        Console.error("Number too large");
                    else Console.error("Number too small");
                }
                else rangeError = true;
            } catch(NumberFormatException e) {
                if(input.matches("[+-]?\\d+"))
                    rangeError = true;
                else if(input.matches("[+-]?\\d*[.,]\\d*"))
                    Console.error("Please input an integer value");
                else Console.error("Please input a number");
            }
            rangeMsg = appendToMessage(message, getRangeEnd(min, max, rangeError));
        }
    }

    /**
     * Displays the given message and waits for user input, repeating the prompt while the
     * user enters input that cannot be interpreted as a number or would resolve to +- infinity.
     *
     * @param message The message prompt to show
     * @return The number entered by the user
     */
    public static float inputFloat(Object... message) {
        while(true) {
            float x = (float) inputDouble(message);
            if(Float.isFinite(x))
                return x;
            if(x > 0)
                Console.error("Number too large");
            else Console.error("Number too small");
        }
    }

    /**
     * Displays the given message and the spedified range and waits for user input, repeating the
     * prompt while the user enters input which cannot be interpreted as a numer or is out of range.
     *
     * @param min The (inclusive) lower bound for the input, or <code>null</code> for no lower bound
     * @param max The (exclusive) upper bound for the input, or <code>null</code> for no upper bound
     * @param message The message to show in front of the range
     * @return The number entered by the user
     */
    public static float inputFloatInRange(@Nullable Float min, @Nullable Float max, Object... message) {
        while(true) {
            float x = (float) inputDoubleInRange(min != null ? (double) min : null, max != null ? (double) max : null, message);
            if(Float.isFinite(x))
                return x;
            if(x > 0)
                Console.error("Number too large");
            else Console.error("Number too small");
        }
    }

    /**
     * Displays the given message followed by a colon ':' (if any message is given), displays the
     * given options and prompts the user to select one of the options by index.
     *
     * @param options The options to select from, at least one is required
     * @param message The message to show
     * @return The index of the option selected by the user
     */
    public static int inputSelect(Object[] options, Object... message) {
        if(Arguments.checkNull(options, "options").length == 0)
            throw new IllegalArgumentException("At least one option is required");
        synchronized(Console.class) {
            if(message == null || message.length != 0) {
                String msg = assembleMessage(message);
                if(!msg.isBlank())
                    print(getMessagePrefix("input") + msg + ':', true);
            }
            for(int i = 0; i < options.length; i++)
                print(getMessagePrefix("input") + '[' + (i + 1) + "]  " + stringFor(options[i]), true);
        }
        return inputIntInRange(1, options.length + 1) - 1;
    }

    /**
     * Displays the given message followed by a colon ':' (if any message is given), displays the
     * given options and prompts the user to select one of the options by index. Differs from
     * {@link #inputSelect(Object[], Object...)} in that it returns the selected value instead
     * of the index.
     *
     * @param options The options to select from, at least one is required
     * @param message The message to show
     * @return The option selected by the user
     */
    public static <T> T inputSelectValue(T[] options, Object... message) {
        return options[inputSelect(options, message)];
    }

    /**
     * Displays the given message followed by a colon ':' (if any message is given), displays the
     * given enum options and prompts the user to select one of them by index.
     *
     * @param enumType The enum type which represents the options
     * @param message The message to show
     * @return The enum constant selected by the user
     */
    public static <E extends Enum<E>> E inputSelect(Class<E> enumType, Object... message) {
        return inputSelectValue(Arguments.checkNull(enumType, "enumType").getEnumConstants(), message);
    }

    private static String getRangeEnd(Integer min, Integer max, boolean inWarnColor) {
        return getRangeEnd(min, min != null ? min-1L : null, max != null ? max-1L : null, max, inWarnColor);
    }

    private static String getRangeEnd(BigInteger min, BigInteger max, boolean inWarnColor) {
        return getRangeEnd(min, min != null ? min.subtract(BigInteger.ONE) : null, max != null ? max.subtract(BigInteger.ONE) : null, max, inWarnColor);
    }

    private static String getRangeEnd(BigDecimal min, BigDecimal max, boolean inWarnColor) {
        return getRangeEnd(min, min != null ? min.subtract(BigDecimal.ONE) : null, max != null ? max.subtract(BigDecimal.ONE) : null, max, inWarnColor);
    }

    private static String getRangeEnd(Double min, Double max, boolean inWarnColor) {
        return getRangeEnd(min, min != null ? min-1 : null, max != null ? max-1 : null, max, inWarnColor);
    }

    private static String getRangeEnd(Object minIncl, Object minExcl, Object maxIncl, Object maxExcl, boolean inWarnColor) {
        String range;

        if(minIncl == null) {

            range = stringFor(maxIncl).matches("0([.,]0)?") ? "<= 0" : "< " + stringFor(maxExcl);
        }
        else if(maxExcl == null) {
            range = stringFor(minIncl).matches("0([.,]0)?") ? ">= 0" : "> " + stringFor(minExcl);
        }
        else {
            String a = stringFor(minIncl), b = stringFor(maxIncl);
            if(a.length() + b.length() > 10 || b.startsWith("-"))
                range = a+" - "+b;
            else range = a+"-"+b;
        }

        return inWarnColor ? "(" + colored(range, Attribute.RED_TEXT()) + ") >" : "("+range+") >";
    }

    private static Object[] appendToMessage(Object[] message, Object... more) {
        return Utils.joinArray(message != null ? message : new Object[] { null }, more);
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
            if(!clsOrPkg.isEmpty())
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
        if(clsOrPkg.isEmpty())
            throw new IllegalArgumentException("Cannot remove the filter of the default package");
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



    /**
     * Sets {@link #stdOut} and {@link #stdErr} as {@link System#out} and {@link System#err}
     * to potentially fix Windows encoding issues and support UTF-8. The {@link Console} class
     * uses these streams by default, use this method to use the streams everywhere.
     */
    public static void useFixedStdOut() {
        System.setOut(stdOut);
        System.setErr(stdErr);
    }


    /**
     * A builder class to configure the style of a progress bar shown in the console.
     */
    public static final class ProgressBarBuilder {

        private double progress = 0;
        private int width;
        private String type = OutputFilter.LOG;
        private Object[] message;
        private String leftBound = DEFAULT_PROGRESS_BAR_LEFT_BOUND, rightBound = DEFAULT_PROGRESS_BAR_RIGHT_BOUND;
        private char[] on = new char[] { DEFAULT_PROGRESS_BAR_ON }, off = new char[] { DEFAULT_PROGRESS_BAR_OFF };
        private boolean showPercentage = true;

        private ProgressBarBuilder(Object[] message) {
            this.message = message;
        }

        /**
         * The initial progress of the progress bar, between 0 and 1. The default value is 0.
         */
        @Range(from = 0, to = 1)
        public double progress() {
            return progress;
        }

        /**
         * @param progress The initial progress of the progress bar, between 0 and 1
         * @return This builder
         */
        public ProgressBarBuilder progress(@Range(from = 0, to = 1)double progress) {
            this.progress = Mathf.clamp(progress, 0, 1);
            return this;
        }

        /**
         * The message to show before the progress bar.
         */
        public Object[] message() {
            return message;
        }

        /**
         * @param message The message to show before the progress bar
         * @return This builder
         */
        public ProgressBarBuilder message(Object... message) {
            this.message = message;
            return this;
        }

        /**
         * The inner width of the progress bar; the length between the left and the right bound.
         * The default width is {@value DEFAULT_PROGRESS_BAR_WIDTH}.
         */
        @Range(from = 1)
        public int width() {
            return width;
        }

        /**
         * @param width The inner width of the progress bar; the length between the left and the right bound.
         * @return This builder
         */
        public ProgressBarBuilder width(@Range(from = 1) int width) {
            //noinspection ConstantValue
            this.width = width > 0 ? width : DEFAULT_PROGRESS_BAR_WIDTH;
            return this;
        }

        /**
         * The message type of the progress bar, the default value is "log".
         */
        @NotNull
        public String type() {
            return type;
        }

        /**
         * @param type The message type of the progress bar
         * @return This builder
         */
        public ProgressBarBuilder type(@NotNull String type) {
            this.type = Arguments.checkNull(type, "type");
            return this;
        }

        /**
         * The left bound of the progress bar. The default value is "{@value DEFAULT_PROGRESS_BAR_LEFT_BOUND}".
         */
        public String leftBound() {
            return leftBound;
        }

        /**
         * The right bound of the progress bar. The default value is "{@value DEFAULT_PROGRESS_BAR_RIGHT_BOUND}".
         */
        public String rightBound() {
            return rightBound;
        }

        /**
         * Sets the left and right bound of the progress bar.
         *
         * @param left The string before the progress bar content
         * @param right The string after the progress bar content
         * @return This builder
         */
        public ProgressBarBuilder bounds(String left, String right) {
            this.leftBound = left != null ? left : "";
            this.rightBound = right != null ? right : "";
            return this;
        }

        /**
         * The char used to visualize the part of the progress bar that is filled.
         * Fails if the on style was set to a string with more than 1 character.
         * The default value for this is '{@value DEFAULT_PROGRESS_BAR_ON}'.
         */
        public char onChar() {
            if(on.length != 1)
                throw new IllegalStateException("Different chars for on");
            return on[0];
        }

        /**
         * The char used to visualize the part of the progress bar that is not filled.
         * Fails if the off style was set to a string with more than 1 character.
         * The default value for this is the whitespace character ' '.
         */
        public char offChar() {
            if(off.length != 1)
                throw new IllegalStateException("Different chars for off");
            return off[0];
        }

        /**
         * The pattern to show in the part where the progress bar is filled. If the
         * progress bar is wider than the pattern is long, it will be repeated. Never empty.
         * The default value for this is "{@value DEFAULT_PROGRESS_BAR_ON}".
         */
        @NotNull
        public String on() {
            return new String(on);
        }

        /**
         * The pattern to show in the part where the progress bar is not filled. If the
         * progress bar is wider than the pattern is long, it will be repeated. Never empty.
         * The default value for this is a string with a whitespace " ".
         */
        @NotNull
        public String off() {
            return new String(off);
        }

        /**
         * Sets the char used to visualize the filled area of the bar to the given char and
         * the char used to visualize the empty part to the whitespace character ' '.
         *
         * @param on The char to use for the filled area
         * @return This builder
         */
        public ProgressBarBuilder segment(char on) {
            this.on = new char[] { on };
            this.off = new char[] { ' ' };
            return this;
        }

        /**
         * Sets the chars used to visualize the filled and not filled area of the progress bar.
         *
         * @param on The char to use for the filled area
         * @param off The char to use for the not filled area
         * @return This builder
         */
        public ProgressBarBuilder segment(char on, char off) {
            this.on = new char[] { on };
            this.off = new char[] { off };
            return this;
        }

        /**
         * Sets the pattern used to visualize the filled area to the given pattern and sets
         * the width to the length of the pattern. The not filled area pattern gets set to a
         * blank string. The pattern may not be empty (but can of course be blank).
         *
         * @param on The pattern to use for the filled area
         * @return This builder
         */
        public ProgressBarBuilder bar(String on) {
            if(Arguments.checkNull(on, "on").isEmpty())
                throw new ArgumentOutOfRangeException("Bar may not be empty");
            this.on = Arguments.checkNull(on, "on").toCharArray();
            this.off = Utils.blank(on.length()).toCharArray();
            this.width = on.length();
            return this;
        }

        /**
         * Sets the pattern used to visualize the filled and not filled area to the given pattern,
         * and sets the width to the length of the patterns. The patterns must have the same length
         * any may not be empty (but can of course be blank).
         *
         * @param on The pattern to use for the filled area
         * @return This builder
         */
        public ProgressBarBuilder bar(String on, String off) {
            if(Arguments.checkNull(on, "on").length() != Arguments.checkNull(off, "off").length())
                throw new IllegalArgumentException("on.length != off.length");
            if(on.isEmpty())
                throw new ArgumentOutOfRangeException("Bar may not be empty");
            this.on = on.toCharArray();
            this.off = off.toCharArray();
            this.width = on.length();
            return this;
        }

        /**
         * Whether to show the percentage after the progress bar.
         * This is on by default.
         */
        public boolean showPercentage() {
            return showPercentage;
        }

        /**
         * @param show Whether to show the percentage after the progress bar
         * @return This builder
         */
        public ProgressBarBuilder showPercentage(boolean show) {
            this.showPercentage = show;
            return this;
        }

        /**
         * Sets to not show the percentage, equivalent to <code>showPercentage(false)</code>.
         *
         * @return This builder
         */
        public ProgressBarBuilder hidePercentage() {
            this.showPercentage = false;
            return this;
        }

        /**
         * Prints the progress bar as configured into the console. The progress can
         * then subsequently be adjusted using {@link #setProgress(double)}, until the
         * progress is set to 1 (or the progress bar is terminated using
         * {@link #endProgress()}). If a progress bar is already active in the console,
         * it will be terminated and this new progress bar will be the active one.
         * This builder can be reused at any later point.
         */
        public void start() {
            internalSetProgress(type, width, leftBound, rightBound, on, off, showPercentage, progress, message, 1);
        }
    }


//    public static constInt2 tryGetConsoleSize() {
//        try {
//            if(System.getProperty("os.name").toLowerCase().contains("win")) {
//                WinNT.HANDLE stdOutHandle = com.sun.jna.platform.win32.Kernel32.INSTANCE.GetStdHandle(-11);
//                Wincon.CONSOLE_SCREEN_BUFFER_INFO info = new Wincon.CONSOLE_SCREEN_BUFFER_INFO();
//                if(com.sun.jna.platform.win32.Kernel32.INSTANCE.GetConsoleScreenBufferInfo(stdOutHandle, info))
//                    return new int2(info.srWindow.Right - info.srWindow.Left + 1, info.srWindow.Bottom - info.srWindow.Top + 1);
//                else throw new RuntimeException(Kernel32Util.getLastErrorMessage());
//            }
//        } catch(Throwable t) {
//            Console.debug(t);
//        }
//        return null;
//    }


    public static final class Scope implements AutoCloseable {
        private int index;
        private final Attribute[] colors;

        private Scope(int index, Attribute... colors) {
            this.index = index;
            this.colors = colors != null ? colors.clone() : new Attribute[0];
        }

        @NotNull
        public Attribute[] colors() {
            return colors.clone();
        }

        public void exit() {
            if(index < 0) return;
            try {
                exitScope(index);
            } finally {
                index = -1;
            }
        }

        @Override
        public void close() {
            exit();
        }
    }


    private static final class ScopeData {
        final int index;
        final Attribute[] colors;
        final Integer randomColor;
        boolean everUsed = false;

        ScopeData(int index, Attribute[] colors) {
            this.index = index;
            this.colors = colors != null ? colors.clone() : new Attribute[0];
            this.randomColor = null;
        }

        ScopeData(int index, int randomColor) {
            this.index = index;
            this.colors = new Attribute[] { RANDOM_SCOPE_COLORS[randomColor] };
            this.randomColor = randomColor;
        }
    }

    private static final class ThreadScopes {

        final List<ScopeData> scopes = new ArrayList<>();

        ScopeData add(ScopeData scope) {
            scopes.add(scope);
            return scope;
        }

        ScopeData pop() {
            if(scopes.isEmpty())
                throw new IllegalStateException("Current thread is not in any console scope");
            return scopes.remove(scopes.size() - 1);
        }

        int indexInThread(int index) {
            for(int i=0; i<scopes.size(); i++)
                if(scopes.get(i).index == index)
                    return i;
            throw new IllegalStateException("The scope that is trying to be exited has either already been exited, or belongs to a different thread");
        }

        ScopeData remove(int index) {
            return scopes.remove(indexInThread(index));
        }

        ScopeData getOrNull(int index) {
            for(ScopeData s : scopes)
                if(s.index == index)
                    return s;
            return null;
        }

        void markAsUsed() {
            for(ScopeData s : scopes)
                s.everUsed = true;
        }
    }


    public interface Line {

        boolean isVisible();

        boolean isHidden();

        void append(Object... message);

        void set(Object... message);

        void overwrite(Object... message);

        void setWithPrefix(String prefix, Object... message);

        void overwriteWithPrefix(String prefix, Object... message);
    }

    private static final Line HIDDEN_LINE = new Line() {
        @Override
        public boolean isVisible() {
            return false;
        }

        @Override
        public boolean isHidden() {
            return true;
        }

        @Override
        public void append(Object... message) { }

        @Override
        public void set(Object... message) { }

        @Override
        public void overwrite(Object... message) { }

        @Override
        public void setWithPrefix(String prefix, Object... message) { }

        @Override
        public void overwriteWithPrefix(String prefix, Object... message) { }
    };

    private static final class RealLine implements Line {

        private static final String ESC = "\u001b";
        private static final String SAVE_CURSOR = ESC+"7";
        private static final String RESTORE_CURSOR = ESC+"8";
        private static final String CLEAR_LINE = ESC+"[K";
        private static final String CLEAR_LINE_FROM_CURSOR = ESC+"[0K";

        private final int line;
        private int prefixLen;
        private int length;

        private RealLine(String prefix, String initial) {
            synchronized(Console.class) {
                this.line = currentLine;
                print(prefix + initial, true);
            }
            prefixLen = length(prefix);
            length = prefixLen + length(initial);
        }

        @Override
        public boolean isVisible() {
            return true;
        }

        @Override
        public boolean isHidden() {
            return false;
        }

        @Override
        public void append(Object... message) {
            String msg = assembleMessage(message);
            synchronized(Console.class) {
                Config.out.print(SAVE_CURSOR+ESC+"["+(currentLine - line)+"A"+ESC+"["+(length + 1)+"G");
                Config.out.print(msg);
                Config.out.print(RESTORE_CURSOR);
                Config.out.flush();
                length += length(msg);
            }
        }

        @Override
        public void set(Object... message) {
            String msg = assembleMessage(message);
            synchronized(Console.class) {
                Config.out.print(SAVE_CURSOR+ESC+"["+(currentLine - line)+"A"+ESC+"["+(prefixLen + 1)+"G"+CLEAR_LINE_FROM_CURSOR);
                Config.out.print(msg);
                Config.out.print(RESTORE_CURSOR);
                Config.out.flush();
            }
        }

        @Override
        public void overwrite(Object... message) {
            String msg = assembleMessage(message);
            synchronized(Console.class) {
                Config.out.print(SAVE_CURSOR+ESC+"["+(currentLine - line)+"A"+ESC+"["+(prefixLen + 1)+"G");
                Config.out.print(msg);
                Config.out.print(RESTORE_CURSOR);
                Config.out.flush();
            }
        }

        @Override
        public void setWithPrefix(String prefix, Object... message) {
            if(prefix == null)
                prefix = "null";
            String msg = assembleMessage(message);
            synchronized(Console.class) {
                Config.out.print(SAVE_CURSOR+ESC+"["+(currentLine - line)+"A"+CLEAR_LINE);
                Config.out.print(prefix + msg);
                Config.out.print(RESTORE_CURSOR);
                Config.out.flush();
                prefixLen = length(prefix);
                length = prefixLen + length(msg);
            }
        }

        @Override
        public void overwriteWithPrefix(String prefix, Object... message) {
            if(prefix == null)
                prefix = "null";
            String msg = assembleMessage(message);
            synchronized(Console.class) {
                Config.out.print(SAVE_CURSOR+ESC+"["+(currentLine - line)+"A");
                Config.out.print(prefix + msg);
                Config.out.print(RESTORE_CURSOR);
                Config.out.flush();
                prefixLen = length(prefix);
                length = prefixLen + length(msg);
            }
        }
    }

    private static final class CompatabilityLine implements Line {

        private String prefix;
        private String content;

        private CompatabilityLine(String prefix, String initial) {
            this.prefix = prefix;
            this.content = initial;
            print();
        }

        @Override
        public boolean isVisible() {
            return true;
        }

        @Override
        public boolean isHidden() {
            return false;
        }

        private void print() {
            Console.print(prefix + content, true);
        }

        @Override
        public void append(Object... message) {
            content += assembleMessage(message);
            print();
        }

        @Override
        public void set(Object... message) {
            setWithPrefix(prefix, message);
        }

        @Override
        public void overwrite(Object... message) {
            String over = assembleMessage(message);
            content = over.length() < content.length() ? over + content.substring(0, over.length()) : over;
            print();
        }

        @Override
        public void setWithPrefix(String prefix, Object... message) {
            this.prefix = prefix == null ? "null" : prefix;
            this.content = assembleMessage(message);
            print();
        }

        @Override
        public void overwriteWithPrefix(String prefix, Object... message) {

        }
    }


    public static void main(String[] args) {
        Console.log("Hello");
        Line l = Console.logLine("--- Dynamic line ---");
        Console.log("World!");
        l.set("abc");
//        String ESC = "\u001b";
//        System.out.println("abc\n"+ESC+"[Ad");
    }
}
