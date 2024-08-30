//package de.rccookie.util;
//
//import java.io.PrintWriter;
//import java.io.StringWriter;
//import java.time.Instant;
//import java.time.temporal.ChronoField;
//import java.util.Arrays;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.function.Predicate;
//import java.util.regex.Pattern;
//
//import com.diogonunes.jcolor.Ansi;
//import com.diogonunes.jcolor.Attribute;
//import org.jetbrains.annotations.NotNull;
//import org.jetbrains.annotations.Nullable;
//
//public final class Console2 {
//
//    /**
//     * Pattern that matches ANSI escape sequences.
//     */
//    private static final Pattern ANSI_PATTERN = Pattern.compile("\u001b\\[\\d+(;\\d+)*m");
//
//    /**
//     * Delimiter between console parameters.
//     */
//    private static final String DELIMITER = " ";
//
//    /**
//     * Best guess whether the standard output stream is connected to a user terminal, rather
//     * than being piped to some other process.
//     */
//    public static final boolean IS_USER_TERMINAL = System.console() != null;
//
//    /**
//     * Stream to write the console output to.
//     */
//    public static final TransferPrintStream OUT = new TransferPrintStream(System.out);
//
//    /**
//     * Whether console output should be colored. Defaults to {@link Console2#IS_USER_TERMINAL}.
//     */
//    private static boolean colored = IS_USER_TERMINAL;
//
//    /**
//     * Whether to print <code>[TYPE]</code> in front of every message, e.g. <code>[LOG]</code>
//     * or <code>[DEBUG]</code>.
//     */
//    private static boolean printMessageType = true;
//
//    /**
//     * Whether to print the current timestamp with every log message.
//     */
//    private static boolean printTime = !IS_USER_TERMINAL;
//
//    /**
//     * Whether to print day, month and year with every log message.
//     */
//    private static boolean printDateWithTime = false;
//
//    /**
//     * Whether to print file name and line number at the end of every message.
//     */
//    private static boolean printLineNumber = false;
//
//    /**
//     * Predicate to determine, for a given message type, whether to print the call stack with the message.
//     */
//    private static Predicate<String> printStackTrace = $ -> false;
//
//    /**
//     * Maximum width of a single output line, after which the output will be wrapped to the next line.
//     */
//    private static Integer maxWidth = null;
//
//
//
//    public static void write(String type, Object... msg) {
//
//    }
//
//    private static String[] renderMessage(Message message) {
//        String prefix = renderPrefix(message);
//    }
//
//    private static String renderPrefix(Message message) {
//        StringBuilder prefix = new StringBuilder();
//        if(message.printTime) {
//            prefix.append('[');
//            prefix.append(colored(renderTimestamp(message), getColors(message.type)));
//            prefix.append("] ");
//        }
//        if(message.printType) {
//            prefix.append('[');
//            prefix.append(colored(message.type, getColors(message.type)));
//            prefix.append(']');
//        }
//        return prefix.toString();
//    }
//
//    /**
//     * Returns the colors for the given message type.
//     *
//     * @param type The message type
//     * @return The colors for the type
//     */
//    private static Attribute[] getColors(@NotNull String type) {
//        type = type.toLowerCase();
//        if(type.equals(Console2.OutputFilter.LOG))   return new Attribute[] { Attribute.GREEN_TEXT() };
//        if(type.equals(Console2.OutputFilter.DEBUG)) return new Attribute[] { Attribute.CYAN_TEXT() };
//        if(type.equals(Console2.OutputFilter.WARN))  return new Attribute[] { Attribute.YELLOW_TEXT() };
//        if(type.equals(Console2.OutputFilter.ERROR)) return new Attribute[] { Attribute.RED_TEXT() };
//        if(type.equals(Console2.OutputFilter.INFO))  return new Attribute[] { Attribute.BLUE_TEXT() };
//        if(type.equals("input"))            return new Attribute[] { Attribute.MAGENTA_TEXT() };
//        return new Attribute[] { Attribute.CYAN_TEXT() };
//    }
//
//    private static String renderTimestamp(Message message) {
//        String tod = String.format(
//                "%02d:%02d:%02d",
//                message.timestamp.get(ChronoField.HOUR_OF_DAY),
//                message.timestamp.get(ChronoField.MINUTE_OF_HOUR),
//                message.timestamp.get(ChronoField.SECOND_OF_MINUTE)
//        );
//        if(!message.printDateWithTime)
//            return tod;
//        return String.format(
//                "%02d.%02d.%02d %s",
//                message.timestamp.get(ChronoField.DAY_OF_MONTH),
//                message.timestamp.get(ChronoField.MONTH_OF_YEAR),
//                message.timestamp.get(ChronoField.YEAR) % 100,
//                tod
//        );
//    }
//
//    private static void writeMessage(Message message) {
//        if(message == null) return;
//        synchronized(OUT) {
//
//        }
//    }
//
//
//
//    private static Message createMessage(String type, @Nullable Object[] message, boolean forceTime) {
//        StackTraceElement[] stackTrace;
//        try {
//            stackTrace = Thread.currentThread().getStackTrace();
//            // Skip stack trace in thread class and in console class
//            int start = Math.min(2, stackTrace.length);
//            for(; start < stackTrace.length; start++)
//                if(!stackTrace[start].getClassName().equals(Console2.class.getName()))
//                    break;
//            stackTrace = Arrays.copyOfRange(stackTrace, start, stackTrace.length);
//        } catch(Exception e) {
//            stackTrace = new StackTraceElement[0];
//        }
//        if(stackTrace.length != 0 && !isEnabled(type, stackTrace[0].getClassName())
//            return null;
//
//        return new Message(
//                type,
//                assembleMessageContent(message),
//                Instant.now(),
//                printMessageType,
//                printTime || forceTime,
//                printDateWithTime,
//                stackTrace.length != 0 && stackTrace[0].getFileName() != null ? stackTrace[0].getFileName() + ":" + stackTrace[0].getLineNumber() : null,
//                stackTrace
//        );
//    }
//
//    /**
//     * Assembles the given message elements to a message string.
//     *
//     * @param message The message elements
//     * @return The message string
//     */
//    @NotNull
//    private static String assembleMessageContent(@Nullable Object[] message) {
//        if(message == null) return "null";
//        if(message.length == 0) return "";
//
//        StringBuilder string = new StringBuilder(stringFor(message[0]));
//        for(int i=1; i<message.length; i++) {
//            int index = string.toString().indexOf("{}");
//            String nextString = stringFor(message[i]);
//            if(index != -1) string.replace(index, index + 2, nextString);
//            else string.append(DELIMITER).append(nextString);
//        }
//        return string.toString();
//    }
//
//    /**
//     * Returns a string representation for the given object.
//     *
//     * @param o The object to get the string representation of
//     * @return The string representation
//     */
//    @NotNull
//    private static String stringFor(@Nullable Object o) {
//        if(o == null) return "null";
//        if(o instanceof Throwable) {
//            StringWriter str = new StringWriter();
//            ((Throwable) o).printStackTrace(new PrintWriter(str));
//            return str.toString();
//        }
//        if(!(o.getClass().isArray())) return o.toString();
//        if(o instanceof boolean[]) return Arrays.toString((boolean[])o);
//        if(o instanceof double[]) return Arrays.toString((double[])o);
//        if(o instanceof float[]) return Arrays.toString((float[])o);
//        if(o instanceof long[]) return Arrays.toString((long[])o);
//        if(o instanceof int[]) return Arrays.toString((int[])o);
//        if(o instanceof short[]) return Arrays.toString((short[])o);
//        if(o instanceof char[]) return Arrays.toString((char[])o);
//        if(o instanceof byte[]) return Arrays.toString((byte[])o);
//        return Arrays.deepToString((Object[])o);
//    }
//
//
//
//    /**
//     * Returns the given string colored in the given colors. Does nothing if
//     * colored output is disabled.
//     *
//     * @param message The message to colorize
//     * @param colors The colors and formatting for the message
//     * @return The colored messages
//     */
//    public static String colored(String message, Attribute... colors) {
//        return colored(message, colored, colors);
//    }
//
//    /**
//     * Returns the given string colored in the given colors. Does nothing if the
//     * colored flag is disabled.
//     *
//     * @param message The message to colorize
//     * @param colored Whether to actually color the given message. If false, this simply
//     *                returns <code>message</code>.
//     * @param colors The colors and formatting for the message
//     * @return The colored messages
//     */
//    private static String colored(String message, boolean colored, Attribute... colors) {
//        return colored ? Ansi.colorize(message, colors) : message;
//    }
//
//    /**
//     * Removes all ANSI codes from the given message.
//     *
//     * @param message The message to de-colorize
//     * @return The plain message string
//     */
//    private static String removeColors(@NotNull String message) {
//        return ANSI_PATTERN.matcher(message).replaceAll("");
//    }
//
//
//
//    public static final class Message {
//
//        @NotNull
//        public final String type;
//        @NotNull
//        public final String message;
//        @NotNull
//        public final Instant timestamp;
//        public final boolean printType;
//        public final boolean printTime;
//        public final boolean printDateWithTime;
//        @Nullable
//        public final String caller;
//        @NotNull
//        public final StackTraceElement[] stackTrace;
//
//        public Message(@NotNull String type, @NotNull String message, @NotNull Instant timestamp, boolean printType, boolean printTime, boolean printDateWithTime, @Nullable String caller, @NotNull StackTraceElement[] stackTrace) {
//            this.type = Arguments.checkNull(type, "type").toUpperCase();
//            this.message = Arguments.checkNull(message, "message");
//            this.timestamp = Arguments.checkNull(timestamp, "timestamp");
//            this.printType = printType;
//            this.printTime = printTime;
//            this.printDateWithTime = printDateWithTime;
//            this.caller = caller;
//            this.stackTrace = Arguments.checkNull(stackTrace, "stackTrace");
//        }
//    }
//
//
//    /**
//     * Configured filters, and the default filter.
//     */
//    private static final Map<String, Console2.OutputFilter> FILTERS = new ConcurrentHashMap<>();
//    static {
//        new Console2.OutputFilter("", true);
//        if("true".equals(System.getProperty("intellij.debug.agent"))) {
//            Console2.getDefaultFilter().setEnabled(Console2.OutputFilter.DEBUG, true);
//        }
//    }
//
//    /**
//     * A filter for output.
//     */
//    public static class OutputFilter {
//
//        public static final String INFO = "info";
//        public static final String DEBUG = "debug";
//        public static final String WARN = "warn";
//        public static final String ERROR = "error";
//        public static final String LOG = "log";
//
//        private final Map<String, Boolean> settings = new ConcurrentHashMap<>();
//        @NotNull
//        public final String clsOrPkg;
//
//        /**
//         * By default, every filter behaves exactly like its super filter by simply
//         * requesting all its results.
//         *
//         * @param clsOrPkg This filter's full class or package name
//         */
//        private OutputFilter(@NotNull String clsOrPkg, boolean register) {
//            this.clsOrPkg = clsOrPkg;
//            if(register) FILTERS.put(clsOrPkg, this);
//        }
//
//        /**
//         * Returns weather the given message type is currently enabled. If not specifically
//         * set this will return the result of the same call on the super-filter of this
//         * filter. If this filter is responsible for the default package, it will return some
//         * default values.
//         *
//         * @param messageType The type of message to check for (case-insensitive)
//         * @return Weather the given message type is currently enabled by this filter
//         */
//        public boolean isEnabled(@NotNull String messageType) {
//            messageType = messageType.toLowerCase();
//            Boolean enabled = settings.get(messageType);
//            if(enabled == null) return getSuperOrDefaultEnabled(messageType);
//            return enabled;
//        }
//
//        /**
//         * Sets weather this filter should allow messages of the given message type. Passing
//         * {@code null} will make the filter return the super filter's enabled state, or for
//         * the default package filter some default values.
//         *
//         * @param messageType The type of message to set the filter state for
//         * @param enabled Weather the filter should allow, disallow or use the super filter's
//         *                enabled state for the messages of that type
//         */
//        public void setEnabled(@NotNull String messageType, @Nullable Boolean enabled) {
//            settings.put(messageType.toLowerCase(), enabled);
//        }
//
//        /**
//         * Sets weather this filter should allow messages of the given message type. If this
//         * filter was specified to allow, disallow or default this message type, this will
//         * have no effect.
//         *
//         * @param messageType The type of message to set the filter state for
//         * @param enabled Weather this filter should allow or disallow messages of the given
//         *                type, if never specified before
//         */
//        public void setDefault(@NotNull String messageType, boolean enabled) {
//            if(settings.containsKey(messageType.toLowerCase())) return;
//            settings.put(messageType.toLowerCase(), enabled);
//        }
//
//        private boolean getSuperOrDefaultEnabled(@NotNull String messageType) {
//            if(!clsOrPkg.isEmpty())
//                return getFilter(clsOrPkg.substring(0, Math.max(Math.max(clsOrPkg.lastIndexOf("."), clsOrPkg.lastIndexOf("$")), 0))).isEnabled(messageType);
//
//            return !DEBUG.equalsIgnoreCase(messageType);
//        }
//    }
//
//
//
//    /**
//     * Returns weather messages of the given type posted from the specified class or package
//     * will be displayed. This returns exactly the same as calling
//     * <pre>getFilter(clsOrPkg).isEnabled(messageType);</pre>
//     *
//     * @param messageType The type of message, for example {@code OutputFilter.INFO} for info messages.
//     *                    Case-insensitive.
//     * @param clsOrPkg The full name of the class or package to check for
//     * @return Weather messages from the given class or package of the specified type will be
//     *         displayed
//     */
//    public static boolean isEnabled(@NotNull String messageType, @NotNull String clsOrPkg) {
//        return getFilter(clsOrPkg).isEnabled(messageType);
//    }
//
//    /**
//     * Returns weather messages of the given type posted from the specified class will be
//     * displayed. This returns exactly the same as calling
//     * <pre>getFilter(cls).isEnabled(messageType);</pre>
//     *
//     * @param messageType The type of message, for example {@code OutputFilter.INFO} for info messages.
//     *                    Case-insensitive.
//     * @param cls The class to check for
//     * @return Weather messages from the given class of the specified type will be displayed
//     */
//    public static boolean isEnabled(@NotNull String messageType, @NotNull Class<?> cls) {
//        return isEnabled(messageType, cls.getName());
//    }
//
//    /**
//     * Returns weather messages of the given type posted from the calling class will be
//     * displayed. This returns exactly the same as calling
//     * <pre>getFilter().isEnabled(messageType);</pre>
//     *
//     * @param messageType The type of message, for example {@code OutputFilter.INFO} for info messages.
//     *                    Case-insensitive.
//     * @return Weather messages from the class that calls this method of the specified type will
//     *         be displayed
//     */
//    public static boolean isEnabled(@NotNull String messageType) {
//        return getFilter().isEnabled(messageType);
//    }
//
//    /**
//     * Returns the {@link Console2.OutputFilter} for the specified class or package. If the exact class
//     * or package did not have a specific filter applied before a new one will be created,
//     * behaving exactly as the previously effective filter.
//     *
//     * @param clsOrPkg The full name of the class or package to get or create the filter of
//     * @return The filter for exactly that class or package
//     */
//    @NotNull
//    public static Console2.OutputFilter getFilter(@NotNull String clsOrPkg) {
//        Console2.OutputFilter filter = FILTERS.get(Arguments.checkNull(clsOrPkg));
//        if(filter != null) return filter;
//        return new Console2.OutputFilter(clsOrPkg, true);
//    }
//
//    /**
//     * Returns the {@link Console2.OutputFilter} for the specified class. If the exact class did not
//     * have a specific filter applied before a new one will be created, behaving exactly as
//     * the previously effective filter.
//     *
//     * @param cls The class to get or create the filter of
//     * @return The filter for exactly that class
//     */
//    @NotNull
//    public static Console2.OutputFilter getFilter(@NotNull Class<?> cls) {
//        return getFilter(cls.getName());
//    }
//
//    /**
//     * Returns the {@link Console2.OutputFilter} for the calling class. If the exact class did not
//     * have a specific filter applied before a new one will be created, behaving exactly as
//     * the previously effective filter.
//     *
//     * @return The filter exactly for the class that calls this method
//     */
//    @NotNull
//    public static Console2.OutputFilter getFilter() {
//        try {
//            StackTraceElement[] elements = Thread.currentThread().getStackTrace();
//            int index = elements.length > 2 ? 2 : elements.length - 1;
//            String cls = elements[index].getClassName();
//            return getFilter(cls);
//        } catch(Exception e) {
//            return new Console2.OutputFilter("", false) {
//                @Override
//                public boolean isEnabled(@NotNull String messageType) {
//                    return true;
//                }
//            };
//        }
//    }
//
//    /**
//     * Returns the filter of the default package that is the filter for all output, unless
//     * there was a filter for the specific class or package specified.
//     *
//     * @return The default output filter
//     */
//    @NotNull
//    public static Console2.OutputFilter getDefaultFilter() {
//        return getFilter("");
//    }
//
//    /**
//     * Removes the filter that applies for exactly that class or package, if there was one.
//     *
//     * @param clsOrPkg The full name of the class or package to remove the filter of
//     * @return Weather a filter was removed
//     * @throws IllegalArgumentException If an attempt was made to remove the default package
//     *                                  filter
//     */
//    public static boolean removeFilter(@NotNull String clsOrPkg) {
//        if(clsOrPkg.isEmpty())
//            throw new IllegalArgumentException("Cannot remove the filter of the default package");
//        return FILTERS.remove(clsOrPkg) != null;
//    }
//
//    /**
//     * Removes the filter that applies for exactly that class, if there was one.
//     *
//     * @param cls The class to remove the filter of
//     * @return Weather a filter was removed
//     */
//    public static boolean removeFilter(@NotNull Class<?> cls) {
//        return removeFilter(cls.getName());
//    }
//}
