package de.rccookie.util.text;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.rccookie.util.Arguments;

public final class LineWrapper {

    private static final Pattern WHITESPACES = Pattern.compile("\\s+");

    private LineWrapper() { }

    public static String wrapLines(String str, int maxLineLength) {
        return streamLines(str, maxLineLength).collect(Collectors.joining("\n"));
    }

    public static String[] splitLines(String str, int maxLineLength) {
        return streamLines(str, maxLineLength).toArray(String[]::new);
    }

    public static Stream<String> streamLines(String str, int maxLineLength) {
        Arguments.checkNull(str, "str");
        Arguments.checkRange(maxLineLength, 1, null);

        return str.lines().flatMap(line -> {
            List<String> lines = new ArrayList<>();
            StringBuilder curLine = new StringBuilder();
            String[] words = WHITESPACES.split(line);
            int lineLength = 0;
            for(int i = 0; i < words.length; i++) {
                if(lineLength == 0) {
                    if(words[i].length() <= maxLineLength) {
                        curLine.append(words[i]);
                        lineLength += words[i].length();
                    } else {
                        curLine.append(words[i], 0, maxLineLength);
                        lines.add(curLine.toString());
                        curLine.setLength(0);
                        words[i] = words[i].substring(maxLineLength);
                        i--;
                    }
                } else {
                    if(words[i].length() + lineLength + 1 <= maxLineLength) {
                        curLine.append(' ').append(words[i]);
                        lineLength += words[i].length() + 1;
                    } else {
                        lines.add(curLine.toString());
                        curLine.setLength(0);
                        lineLength = 0;
                        i--;
                    }
                }
            }
            lines.add(curLine.toString());
            return lines.stream();
        });
    }
}
