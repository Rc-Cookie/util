package com.github.rccookie.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;

import com.github.rccookie.util.redirect.Redirector;

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
    private boolean allowArgs = true;


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

    public boolean isAllowArgs() {
        return allowArgs;
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

    public void allowArgs(boolean allowArgs) {
        this.allowArgs = allowArgs;
    }


    public void addOptionDebug() {
        addOption(null, "debug", null, "Enables debug output for all or a specific class")
                .action(() -> Console.getDefaultFilter().setEnabled(Console.OutputFilter.DEBUG, true))
                .action(c -> Console.getFilter(c).setEnabled(Console.OutputFilter.DEBUG, true));
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
        addOptionRedirect();
        addOptionShowLineNumber();
        addOptionOutputFilter();
        addOptionLogTime();
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

        for(Option option : options) {
            String left = (option.shortName != null ? "-" + option.shortName : "")
                    + (option.shortName != null && option.longName != null ? ", " :"")
                    + (option.longName != null ? "--" + option.longName : "")
                    + (option.hasParameter == null ? " <x?>" : option.hasParameter ? " <x>" : "");
            left = left + " ".repeat(Math.max(1, 30 - left.length()));
            System.out.println(left + option.help);
        }
    }

    @NotNull
    public Args parse() {
        return parse(Utils.getArgs());
    }

    @NotNull
    public Args parse(@NotNull String @NotNull[] args) {
        Arguments.checkNull(args);
        Map<String, String> optionsOut = new HashMap<>();
        String[] remaining = new String[0];
        for(int i=0; i<args.length; i++) {
            Option[] optionsSelected = null;
            if(args[i].length() > 2 && args[i].startsWith("--")) {
                String name = args[i].substring(2);
                optionsSelected = new Option[] { options.stream().filter(o -> name.equals(o.longName)).findAny().orElse(null) };
                if(optionsSelected[0] == null) {
                    errorProcessor.accept("Unknown option: " + args[i]);
                    continue;
                }
            }
            else if(args[i].startsWith("-") && args[i].length() > 1) {
                optionsSelected = new Option[args[i].length() - 1];
                for(int j=1,off=1; j<args[i].length(); j++) {
                    char name = args[i].charAt(j);
                    optionsSelected[j-off] = options.stream().filter(o -> o.shortName != null && name == o.shortName).findAny().orElse(null);
                    if(optionsSelected[j-off] == null) {
                        errorProcessor.accept("Unknown option: -" + name);
                        optionsSelected = Arrays.copyOfRange(optionsSelected, 0, optionsSelected.length-1);
                        off++;
                    }
                }
            }

            if(optionsSelected == null) {
                if(!allowArgs || (args[i].length() != 1 && args[i].startsWith("-"))) {
                    errorProcessor.accept("Unknown option: " + args[i]);
                    continue;
                }
                remaining = Arrays.copyOfRange(args, i, args.length);
                break;
            }

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
        return new Args(optionsOut, remaining);
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
            return (shortName != null && shortName == option.shortName) ||
                    (longName != null && Objects.equals(longName, option.longName));
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
}
