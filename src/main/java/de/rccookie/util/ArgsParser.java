package de.rccookie.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;

import de.rccookie.util.redirect.Redirector;
import de.rccookie.util.text.Alignment;
import de.rccookie.util.text.LineWrapper;
import de.rccookie.util.text.TableRenderer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ArgsParser {

    private final Set<Option> options = new TreeSet<>((o1, o2) -> String.CASE_INSENSITIVE_ORDER.compare(o1.key, o2.key));
    {
        addOption(null, "help", false, "Shows this help message").action((Runnable)this::showHelp);
    }
    private Consumer<String> errorProcessor = Console::warn;

    private String name;
    private String description;
    private boolean quitAfterHelp = true;
    private ArgsMode argsMode = ArgsMode.ANY_NON_OPTION;
    private UnknownOptionMode unknownOptionMode = UnknownOptionMode.IGNORE;
    private Consumer<String> argsListener = a -> { };


    public ArgsParser() {
        this(true);
    }

    public ArgsParser(boolean addDefaults) {
        if(addDefaults)
            addDefaults();
    }


    @Contract("null,null,_->fail;_,_,_->new")
    public Option addOption(Character shortName, String longName, @Nullable Boolean hasParameter) {
        Option option = new Option(shortName, longName, hasParameter);
        options.remove(option);
        options.add(option);
        return option;
    }

    @Contract("null,null,_,_->fail;_,_,_,_->new")
    public Option addOption(Character shortName, String longName, @Nullable Boolean hasParameter, String help) {
        return addOption(shortName, longName, hasParameter).help(help);
    }

    /**
     * Removes the option with the given name. If the option to remove has a long
     * name, pass the long name, otherwise the short name.
     *
     * @param name The long name of the option to remove if it has one, otherwise its short name
     * @return Whether an option was removed
     */
    public boolean removeOption(String name) {
        for(Option o : options) {
            if((name.equals(o.longName)) || (o.longName == null && Objects.equals(name.charAt(0), o.shortName))) {
                options.remove(o);
                return true;
            }
        }
        return false;
    }

    public Consumer<String> getErrorProcessor() {
        return errorProcessor;
    }

    public void setErrorProcessor(@NotNull Consumer<String> errorProcessor) {
        this.errorProcessor = Arguments.checkNull(errorProcessor);
    }

    public Set<Option> getOptions() {
        return Collections.unmodifiableSet(options);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isQuitAfterHelp() {
        return quitAfterHelp;
    }

    @Deprecated
    public boolean isAllowArgs() {
        return getArgsMode() != ArgsMode.NOT_ALLOWED;
    }

    public ArgsMode getArgsMode() {
        return argsMode;
    }

    public UnknownOptionMode getUnknownOptionMode() {
        return unknownOptionMode;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setQuitAfterHelp(boolean quitAfterHelp) {
        this.quitAfterHelp = quitAfterHelp;
    }

    /**
     * Sets the args mode to {@link ArgsMode#ANY_NON_OPTION} if allowed, otherwise to
     * {@link ArgsMode#NOT_ALLOWED}.
     *
     * @param allowArgs Whether to allow additional arguments to be passed
     * @deprecated Use {@link #setArgsMode(ArgsMode)} instead for finer control over how
     *             additional arguments are treated if allowed
     */
    @Deprecated
    public void allowArgs(boolean allowArgs) {
        setArgsMode(allowArgs ? ArgsMode.ANY_NON_OPTION : ArgsMode.NOT_ALLOWED);
    }

    /**
     * Sets how to deal with additional arguments which are not options or parameters of
     * options. The default value is {@link ArgsMode#ANY_NON_OPTION}.
     *
     * @param argsMode The mode to set
     */
    public void setArgsMode(ArgsMode argsMode) {
        this.argsMode = Arguments.checkNull(argsMode, "argsMode");
    }

    /**
     * Sets a listener which will be called for every regular argument which is neither an option
     * nor ones parameter. This will only be called if args are allowed at all, which may be forbidden
     * using {@link #setArgsMode(ArgsMode)}.
     *
     * @param argsListener The listener to call for every argument in proper order, or <code>null</code>
     */
    public void setArgsListener(@Nullable Consumer<String> argsListener) {
        this.argsListener = argsListener != null ? argsListener : a -> { };
    }

    /**
     * Sets how to deal with unknown options. The default value is {@link UnknownOptionMode#IGNORE}.
     *
     * @param unknownOptionMode The mode to set
     */
    public void setUnknownOptionMode(UnknownOptionMode unknownOptionMode) {
        this.unknownOptionMode = Arguments.checkNull(unknownOptionMode, "unknownOptionMode");
    }

    public void addOptionDebug() {
        addOption(null, "debug", null, "Enables debug output for all or a specific class")
                .action(() -> Console.getDefaultFilter().setEnabled(Console.OutputFilter.DEBUG, true))
                .action(c -> Console.getFilter(c).setEnabled(Console.OutputFilter.DEBUG, true));
    }

    public void addOptionLogPID() {
        addOption(null, "logPID", false, "Logs the process PID on startup. Convenient for killing the process later")
                .action(() -> Console.write("PID", ProcessHandle.current().pid()));
    }

    public void addOptionRedirect() {
        addOption(null, "redirect", null, "Redirects console in/output to an external program. Parameter may specify host:port.")
                .action((Runnable) Redirector::redirect)
                .action(hp -> Redirector.redirect(hp.substring(0, hp.indexOf(':')), Integer.parseInt(hp.substring(hp.indexOf(':')+1))));
    }

    public void addOptionShowLineNumber() {
        addOption(null, "showLineNumber", false, "Shows class and line number behind console messages")
                .action(() -> Console.Config.includeLineNumber = true);
    }

    public void addOptionOutputFilter() {
        addOption(null, "outputFilter", true,
                "Sets an output filter for a specific class or package. Argument should have the form <clsOrPkg>:<messageType>=<true/false/null>")
                .action(s -> {
                    try {
                        String clsOrPkg = s.substring(0, s.indexOf(':'));
                        String type = s.substring(clsOrPkg.length() + 1, s.indexOf(':', clsOrPkg.length() + 1));
                        String setting = s.substring((clsOrPkg + type).length() + 1);
                        Console.getFilter(clsOrPkg).setEnabled(type, setting.equals("null") ? null : Boolean.valueOf(setting));
                    } catch(Exception e) {
                        Console.error("Illegal filter parameter '{}', should be like '<clsOrPkg>:<messageType>=<true/false/null>'", s);
                    }
                });
    }

    public void addOptionLogTime() {
        addOption(null, "logTime", false, "Logs the time of log output")
                .action(() -> Console.Config.logTime = true);
    }

    public void addOptionLogDate() {
        addOption(null, "logDate", false, "Logs the date and time of log output")
                .action(() -> Console.Config.logTime = Console.Config.logDateWithTime = true);
    }

    public void addOptionConsoleWidth() {
        addOption(null, "consoleWidth", true, "Sets the console output max width")
                .action(w -> Console.Config.width = Integer.parseInt(w));
    }

    public void addOptionConsoleOutput() {
        addOption(null, "consoleOutput", true, "Sets the console output to the specified file")
                .action(f -> {
                    try {
                        new File(f).getParentFile().mkdirs();
                        Console.Config.out.setOut(new FileOutputStream(f, true));
                        Console.Config.out.println();
                        Console.Config.colored = false;
                    } catch(IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
    }

    public void addDefaults() {
        addOptionDebug();
        addOptionLogPID();
        addOptionRedirect();
        addOptionShowLineNumber();
        addOptionOutputFilter();
        addOptionLogTime();
        addOptionLogDate();
        addOptionConsoleWidth();
        addOptionConsoleOutput();
    }

    public void showHelp() {
        showHelp(System.out);
        if(quitAfterHelp) System.exit(0);
    }

    public void showHelp(PrintStream out) {
        if(name != null) out.println(name);
        if(description != null) out.println(description);
        if(name != null || description != null) out.println();

        TableRenderer table = new TableRenderer()
                .alignment(Alignment.LEFT, Alignment.TOP)
                .style(TableRenderer.Style.COMPACT_BLANK);

        boolean anyShort = options.stream().anyMatch(o -> o.shortName != null);
        boolean anyLong = options.stream().anyMatch(o -> o.longName != null);

        for(Option option : options) {
            String param = option.hasParameter == null ? " <x?>" : option.hasParameter ? " <x>" : "";
            String shortName = option.shortName != null ? "-"+option.shortName+param : null;
            String longName = option.longName != null ? "--"+option.longName+param : null;
            String help = option.help != null ? LineWrapper.wrapLines(option.help, 80) : null;
            if(anyShort && anyLong)
                table.addRow(shortName, longName, help);
            else table.addRow(anyShort ? shortName : longName, help);
        }
        out.println(table);
    }

    @NotNull
    public Options parse() {
        return parse(Utils.getArgs());
    }

    @NotNull
    public Options parse(@NotNull String @NotNull[] args) {
        Arguments.checkNull(args);
        Map<String, String> optionsOut = new HashMap<>();
        List<String> remaining = new ArrayList<>();

        for(int i=0; i<args.length; i++) {
            Option[] optionsSelected = null;

            // --options
            if(args[i].length() > 2 && args[i].startsWith("--")) {
                String name = args[i].substring(2);
                optionsSelected = new Option[] { options.stream().filter(o -> name.equals(o.longName)).findAny().orElse(null) };
                if(optionsSelected[0] == null) {
                    if(unknownOptionMode == UnknownOptionMode.IGNORE) {
                        errorProcessor.accept("Unknown option: "+args[i]);
                        continue;
                    }
                    else if(unknownOptionMode == UnknownOptionMode.SWITCH)
                        optionsSelected[0] = new Option(null, name, false);
                    else optionsSelected = null;
                }
            }
            // -o and -options
            else if(args[i].startsWith("-") && args[i].length() > 1 && !args[i].equals("--")) {
                optionsSelected = new Option[args[i].length() - 1];
                for(int j=1,off=1; j<args[i].length(); j++) {
                    char name = args[i].charAt(j);
                    optionsSelected[j-off] = options.stream().filter(o -> o.shortName != null && name == o.shortName).findAny().orElse(null);
                    if(optionsSelected[j-off] == null) {

                        if(unknownOptionMode == UnknownOptionMode.IGNORE) {
                            errorProcessor.accept("Unknown option: -" + name);
                            optionsSelected = Arrays.copyOfRange(optionsSelected, 0, optionsSelected.length-1);
                            off++;
                        }
                        else if(unknownOptionMode == UnknownOptionMode.SWITCH)
                            optionsSelected[j-off] = new Option(name, null, false);
                        else if(unknownOptionMode == UnknownOptionMode.ADDITIONAL_ARGS)
                            optionsSelected[j-off] = null;
                        else if(unknownOptionMode == UnknownOptionMode.ADDITIONAL_ARGS_IF_ANY_UNKNOWN) {
                            optionsSelected = null;
                            break;
                        }
                    }
                }
                if(unknownOptionMode == UnknownOptionMode.ADDITIONAL_ARGS) {
                    if(Utils.allNull(optionsSelected))
                        optionsSelected = null;
                    else {
                        int off = 0;
                        for(int j=0; j<optionsSelected.length; j++) {
                            if(optionsSelected[j-off] == null) {
                                errorProcessor.accept("Unknown option: -" + args[i].charAt(j+1));
                                off++;
                            } else optionsSelected[j-off] = optionsSelected[j];
                        }
                        if(off != 0)
                            optionsSelected = Arrays.copyOfRange(optionsSelected, 0, optionsSelected.length - off);
                    }
                }
            }

            if(optionsSelected == null) {
                if(argsMode == ArgsMode.NOT_ALLOWED)
                    errorProcessor.accept("Unknown option: " + args[i]);
                else if(argsMode == ArgsMode.ANY_NON_OPTION) {
                    remaining.add(args[i]);
                    argsListener.accept(args[i]);
                }
                else {
                    remaining.addAll(Arrays.asList(args).subList(i, args.length));
                    for(int j=i; j<args.length; j++)
                        argsListener.accept(args[j]);
                    break;
                }
                continue;
            }

            assert !Utils.anyNull(optionsSelected);
            //noinspection DataFlowIssue
            if(optionsSelected.length == 1 && ((optionsSelected[0].hasParameter == null && args.length > i + 1 && !args[i + 1].startsWith("-")) || optionsSelected[0].hasParameter == Boolean.TRUE)) {
                if(i == args.length - 1)
                    errorProcessor.accept("Option -" + optionsSelected[0].shortName + " requires parameter");
                else {
                    String param = i < args.length - 1 ? args[++i] : null;
                    optionsOut.put(optionsSelected[0].key, param);
                    optionsSelected[0].invokeAction(param);
                }
            }
            else for(Option option : optionsSelected) {
                if(option.hasParameter == Boolean.TRUE)
                    errorProcessor.accept("Option -" + option.shortName + " requires parameter");
                else {
                    optionsOut.put(option.key, "true");
                    option.invokeAction();
                }
            }
        }
        return new Options(optionsOut, remaining.toArray(new String[0]));
    }


    public <T> T parseTo(Class<T> type) {
        return parse().as(type);
    }

    public <T> T parseTo(String[] args, Class<T> type) {
        return parse(args).as(type);
    }



    public static class Option {

        @Nullable
        public final Character shortName;
        public final String longName;
        @NotNull
        public final String key;
        public final Boolean hasParameter;
        public String help;
        public String defaultValue;
        private Runnable action;
        private Consumer<String> paramAction;

        @Contract("null,null,_->fail")
        public Option(@Nullable Character shortName, @Nullable String longName, @Nullable Boolean hasParameter) {
            if(shortName == null && longName == null)
                throw new NullArgumentException("At least one of shortName and longName must be non-null");
            this.shortName = shortName;
            this.longName = longName;
            this.hasParameter = hasParameter;
            key = longName != null ? longName : (shortName+"");
        }

        @Override
        public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            Option option = (Option) o;
            if(longName != null) return longName.equals(option.longName);
            return Objects.equals(shortName, option.shortName);
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @NotNull
        @Contract("_->this")
        public Option help(@Nullable String help) {
            this.help = help;
            return this;
        }

        @NotNull
        @Contract("_->this")
        public Option defaultVal(@Nullable String defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        @NotNull
        public Option action(@NotNull Runnable action) {
            if(hasParameter == Boolean.TRUE) throw new IllegalStateException();
            this.action = Arguments.checkNull(action);
            return this;
        }

        @NotNull
        public Option action(@NotNull Consumer<String> action) {
            if(hasParameter == Boolean.FALSE) throw new IllegalStateException();
            paramAction = Arguments.checkNull(action);
            return this;
        }

        public void invokeAction() {
            if(hasParameter == Boolean.TRUE) throw new IllegalStateException();
            if(action != null)
                action.run();
        }

        public void invokeAction(@Nullable String parameter) {
            if(hasParameter == Boolean.FALSE) throw new IllegalStateException();
            if(paramAction != null)
                paramAction.accept(parameter);
        }
    }


    /**
     * Specifies how regular arguments are treated which are not options
     * (starting with '-' or '--' or being a parameter of one).
     */
    public enum ArgsMode {
        /**
         * Additional parameters are not permitted, passing any values which are not
         * an option or parameter of an option will cause a warning message and the parameter
         * will be ignored.
         */
        NOT_ALLOWED,
        /**
         * Starting from the first parameter that is not an option or an option parameter,
         * all arguments will be treated as additional parameters, <i>even if later arguments
         * start with '-' or '--'.</i> For example, the argument string <code>-a --b hello --world</code>
         * would treat <code>"hello"</code> and <code>"--world"</code> as additional parameters,
         * and <i>not</i> treat <code>"--world"</code> as the option "world".
         */
        FROM_FIRST_NON_OPTION,
        /**
         * Any parameters which are not an option or an option parameter will be treated as
         * additional parameters. These can be in any order, if subsequent arguments start
         * with "-" or "..", they will be treated as options again. For example, the argument
         * string <code>-a --b hello --world</code> would treat <code>"hello"</code> as additional
         * parameter, and trigger the options <code>"a"</code>, <code>"b"</code> and <code>"world"</code>.
         */
        ANY_NON_OPTION
    }

    /**
     * Defines how to handle parameters which have option syntax (leading "-" or "--")
     * but the option is not defined.
     */
    public enum UnknownOptionMode {
        /**
         * Ignore unknown options (and show a warning).
         */
        IGNORE,
        /**
         * Treat any unknown option as switch (a parameterless option) (and don't show a warning).
         */
        SWITCH,
        /**
         * Treat any unknown option as additional arguments (which will be handled as set using
         * {@link ArgsParser#setArgsMode(ArgsMode)}). For chained character switches (e.g. <code>-abc</code>
         * for options "a", "b" and "c") they will be treated as additional parameter only if none
         * of the options are known. Otherwise, a warning will be shown for the unknown part of
         * parameters and the known ones will be handles as usual.
         */
        ADDITIONAL_ARGS,
        /**
         * Treat any unknown option as additional arguments (which will be handled as set using
         * {@link ArgsParser#setArgsMode(ArgsMode)}). For chained character switches (e.g. <code>-abc</code>
         * for options "a", "b" and "c") they will be treated as additional parameter only if
         * <i>any</i> if the options are unknown.
         */
        ADDITIONAL_ARGS_IF_ANY_UNKNOWN
    }

    public static void main(String[] args) {
        ArgsParser parser = new ArgsParser();
        parser.addDefaults();
        parser.addOption('c', "command", true, "Some extremely useful help message");
        parser.showHelp();
    }
}
