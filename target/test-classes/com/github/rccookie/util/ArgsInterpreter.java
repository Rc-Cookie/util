package com.github.rccookie.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.github.rccookie.util.Console.OutputFilter;

public final class ArgsInterpreter {

    private ArgsInterpreter() {
        throw new UnsupportedOperationException();
    }



    public static String[] interprete(String[] args, Predicate<String> commands, BiPredicate<String, String> properties) {

        if(Objects.requireNonNull(args, "args was null, it must have meen modified").length == 0) return args;

        List<String> otherArgs = new ArrayList<>();

        for(String arg : args) {
            if(!arg.startsWith("-")) {
                otherArgs.add(arg);
                continue;
            }

            if(arg.startsWith("--")) {
                if(arg.replace("=", "").length() != arg.length() - 1) {
                    otherArgs.add(arg);
                    continue;
                }
                String[] property = arg.substring(2).split("=");
                Console.mapDebug("Args property", "'{}' = '{}'", property[0], property[1]);
                if(!properties.test(property[0], property[1])) otherArgs.add(arg);
            }
            else {
                String cmd = arg.substring(1);
                Console.mapDebug("Args option", "'{}'", cmd);
                if(!commands.test(cmd)) otherArgs.add(arg);
            }
        }

        return otherArgs.toArray(new String[otherArgs.size()]);
    }

    public static String[] interprete(String[] args, Consumer<String> commands, BiConsumer<String, String> properties) {
        return interprete(args, v -> {
            commands.accept(v);
            return true;
        }, (k,v) -> {
            properties.accept(k, v);
            return true;
        });
    }

    public static String[] intoProperties(String[] args) {
        return interprete(args, s -> System.setProperty(s, "true"), System::setProperty);
    }

    public static String[] interpreteWithDefaults(String[] args, Predicate<String> commands, BiPredicate<String, String> properties) {
        return interprete(args, cmd -> {
            if(cmd.strip().equalsIgnoreCase("debug")) {
                Console.getDefaultFilter().setEnabled(OutputFilter.DEBUG, true);
                return true;
            }
            if(cmd.endsWith(":debug")) {
                Console.getFilter(cmd.substring(0, cmd.length() - 6)).setEnabled(OutputFilter.DEBUG, true);
                return true;
            }
            if(cmd.strip().equalsIgnoreCase("nocolor")) {
                Console.Config.coloredOutput = false;
                Console.debug("No colored output");
                return true;
            }
            if(cmd.strip().equalsIgnoreCase("showLineNumber")) {
                Console.Config.includeLineNumber = true;
                return true;
            }
            return commands.test(cmd);
        }, (k,v) -> {
            if(k.strip().toLowerCase().startsWith("filter:")) {
                try {
                    String filterCmd = k.substring(k.indexOf(':') + 1);
                    String clsOrPkg = filterCmd.substring(0, filterCmd.indexOf(':'));
                    String messageType = filterCmd.substring(filterCmd.indexOf(':') + 1);
                    Boolean enabled = v.equals("null") ? null : Boolean.parseBoolean(v);
                    Console.getFilter(clsOrPkg).setEnabled(messageType, enabled);
                } catch(Exception e) {
                    Console.error("Illegal filter command '--{}={}', should be like '--filter:<clsOrPkg>:<messageType>=<true/false/null>'", k, v);
                }
                return true;
            }
            if(k.strip().equalsIgnoreCase("colored")) {
                Console.Config.coloredOutput = Boolean.parseBoolean(v.strip());
                return true;
            }
            if(k.strip().equalsIgnoreCase("showLineNumber")) {
                Console.Config.includeLineNumber = Boolean.parseBoolean(v.strip());
                return true;
            }
            if(k.strip().equalsIgnoreCase("consoleWidth")) {
                try {
                    Console.Config.manualConsoleWidth = Integer.parseInt(v.strip());
                } catch(NumberFormatException e) {
                    Console.error("Failed to parse console width from '{}'", v.strip());
                }
                return true;
            }
            return properties.test(k, v);
        });
    }

    public static String[] interpreteWithDefaults(String[] args, Consumer<String> commands, BiConsumer<String, String> properties) {
        return interpreteWithDefaults(args, v -> {
            commands.accept(v);
            return true;
        }, (k,v) -> {
            properties.accept(k, v);
            return true;
        });
    }
}
