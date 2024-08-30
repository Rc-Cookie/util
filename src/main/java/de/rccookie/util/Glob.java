package de.rccookie.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.jetbrains.annotations.Nullable;

/**
 * A glob is a file pattern, for example <code>*.java</code>. They are simpler to use
 * than regular expressions and commonly found in command line tools. This implementation
 * supports regular unix syntax:
 * <ul>
 *     <li><code>*</code> matches 0 or more characters other than <code>/</code> or <code>\</code> (and also not <code>".."</code>).</li>
 *     <li><code>**</code> matches 0 or more characters including <code>/</code> or <code>\</code> (but still not <code>".."</code>).</li>
 *     <li><code>?</code> matches exactly one character other than <code>/</code> or <code>\</code>.</li>
 *     <li><code>[...]</code> and <code>[!...]</code> match one character in or not in the selection,
 *     respectively, with the same syntax as regular expressions: <code>0-9</code> matches any character
 *     which' index is between the inclusive range, all other patterns like <code>abcde</code> match exactly</li>
 *     these characters.
 * </ul>
 */
public final class Glob {

    /**
     * The glob pattern split around '/' and '\'
     */
    private final String[] parts;
    /**
     * The number of leading '..'s
     */
    private final int rootOffset;
    /**
     * The regex patterns for each path segment until the first occurrence of '**'.
     * The part with '**' is also included, but the regex only checks for the part
     * before the '**' and ends with '[/\\]*'.
     */
    private final Pattern[] beforeWildcard;
    /**
     * The regex pattern for all path segments joined, beginning with the first segment
     * that contains '**'. If no part contains '**', this is null.
     */
    @Nullable
    private final Pattern fromWildcard;
    /**
     * Whether this is a concrete path.
     */
    private final boolean concrete;

    private Glob(String src) {
        parts = normalize(src);

        int i = 0;
        while(i < parts.length && parts[i].equals("..")) i++;
        this.rootOffset = i;

        BoolWrapper concrete = new BoolWrapper(true);
        List<Pattern> beforeWildcard = new ArrayList<>();

        for(; i<parts.length && !parts[i].contains("**"); i++)
            beforeWildcard.add(Pattern.compile(parseRegexFromPathPart(parts[i], true, concrete)));

        if(i != parts.length) {
            concrete.value = false;

            int dwIndex = parts[i].indexOf("**");
            String before = parts[i].substring(0, dwIndex);
            beforeWildcard.add(Pattern.compile(parseRegexFromPathPart(before, true, concrete) + "[^/\\\\]*"));

            StringBuilder regex = new StringBuilder(parseRegexFromPathPart(parts[i] /* includes before and '**' */, false, concrete));
            for(i++; i<parts.length; i++)
                regex.append("[/\\\\]").append(parseRegexFromPathPart(parts[i], false, concrete));
            fromWildcard = Pattern.compile(regex.toString());
        }
        else fromWildcard = null;

        this.beforeWildcard = beforeWildcard.toArray(new Pattern[0]);
        this.concrete = concrete.value;
    }

    /**
     * Removes unnecessary 'xy/..' and 'abc/./def' like parts. Also adds an empty
     * string in front / at the back if the path starts / ends with '/' or '\'.
     *
     * @param path The path to normalize
     * @return The normalized path parts
     */
    private static String[] normalize(String path) {
        if(path.isEmpty()) return new String[] { "" };

        List<String> parts = new ArrayList<>(Arrays.asList(path.split("[/\\\\]+")));

        // Starts with /...?
        if((path.charAt(0) == '/' || path.charAt(0) == '\\') && (parts.isEmpty() || !parts.get(0).isEmpty()))
            parts.add(0, "");
        // Ends with .../ ?
        if((path.charAt(path.length()-1) == '/' || path.charAt(path.length()-1) == '\\') &&
                (parts.isEmpty() || !parts.get(parts.size()-1).isEmpty() || (parts.size() == 1 && parts.get(0).isEmpty() /* '/' should result in ["",""] */)))
            parts.add("");

        for(int i=0; i<parts.size(); i++) {
            String part = parts.get(i);
            if(part.equals("."))
                parts.remove(i--);
            else if(part.equals("..") && i != 0 && !parts.get(i-1).equals("..") && !parts.get(i-1).contains("**") /* removing ** would change semantics */) {
                parts.remove(i);
                parts.remove(i-1);
                i -= 2;
            }
        }

        // Current directory?
        if(parts.isEmpty()) return new String[] { "" };

        return parts.toArray(new String[0]);
    }

    /**
     * Converts a glob pattern to a regex string.
     *
     * @param part The path
     * @param allowParentDir Whether part is allowed to be '..'
     * @param concrete Will be set to false if a wildcard pattern is found
     * @return The regex pattern
     */
    private static String parseRegexFromPathPart(String part, boolean allowParentDir, BoolWrapper concrete) {
        if(!allowParentDir && part.equals(".."))
            throw new IllegalArgumentException("'..' not allowed after **");

        StringBuilder regex = new StringBuilder();
        StringBuilder quote = new StringBuilder();

        boolean singleWildcard = false, doubleWildcard = false;
        int questionMarks = 0;

        for(int j=0; j<part.length(); j++) {
            char c = part.charAt(j);
            int index;
            if(c == '?') {
                questionMarks++;
                concrete.value = false;
            }
            else if(c == '*') {
                if(j != part.length() - 1 && part.charAt(j+1) == '*') {
                    doubleWildcard = true;
                    j++;
                }
                else singleWildcard = true;
                concrete.value = false;
            }
            else if(c == '[' && (index = part.indexOf(']', j+1)) != -1) {
                if(quote.length() != 0) {
                    regex.append(Pattern.quote(quote.toString()));
                    quote.setLength(0);
                }
                appendSimplifiedWildcards(regex, singleWildcard, doubleWildcard, questionMarks);
                singleWildcard = doubleWildcard = false;
                questionMarks = 0;

                regex.append('[');
                boolean neg = part.charAt(j+1) == '!';
                if(neg) {
                    j++;
                    regex.append('^');
                }
                regex.append(part, j+1, index+1);
                concrete.value = false;
                j = index;
            }
            else if(c == '/' || c == '\\') {
                if(quote.length() != 0) {
                    regex.append(Pattern.quote(quote.toString()));
                    quote.setLength(0);
                }
                appendSimplifiedWildcards(regex, singleWildcard, doubleWildcard, questionMarks);
                singleWildcard = doubleWildcard = false;
                questionMarks = 0;

                regex.append("[/\\\\]");
            }
            else {
                if(singleWildcard || doubleWildcard || questionMarks != 0) {
                    if(quote.length() != 0) {
                        regex.append(Pattern.quote(quote.toString()));
                        quote.setLength(0);
                    }
                    appendSimplifiedWildcards(regex, singleWildcard, doubleWildcard, questionMarks);
                    singleWildcard = doubleWildcard = false;
                    questionMarks = 0;
                }
                quote.append(c);
            }
        }
        if(quote.length() != 0)
            regex.append(Pattern.quote(quote.toString()));
        appendSimplifiedWildcards(regex, singleWildcard, doubleWildcard, questionMarks);

        return regex.toString();
    }

    /**
     * Appends an as concise as possible regex pattern for the given combination of wildcard patterns.
     *
     * @param regex The string builder to write to
     * @param singleWildcard Whether a single wildcard was found
     * @param doubleWildcard Whether a double wildcard was found
     * @param questionMarks The number of question marks found
     */
    private static void appendSimplifiedWildcards(StringBuilder regex, boolean singleWildcard, boolean doubleWildcard, int questionMarks) {
        singleWildcard &= !doubleWildcard;
        if(doubleWildcard)
            regex.append(".*");

        if(singleWildcard && questionMarks == 0)
            regex.append("[^/\\\\]*");
        else if(questionMarks == 1) {
            if(singleWildcard)
                regex.append("[^/\\\\]+");
            else regex.append("[^/\\\\]");
        }
        else if(questionMarks != 0) {
            regex.append("[^/\\\\]{").append(questionMarks);
            if(singleWildcard) regex.append(',');
            regex.append('}');
        }
    }


    /**
     * Returns the glob pattern for this glob. This must not necessarily
     * be identical to the pattern that this glob was parsed from, but the
     * returned pattern has the same semantic.
     *
     * @return A string representation of this glob
     */
    @Override
    public String toString() {
        return parts.length == 1 && parts[0].isEmpty() ? "." : String.join(File.separator, parts);
    }

    /**
     * Returns a regular expression which matches exactly the paths that this glob
     * matches.
     *
     * @param withParentSection Whether to include leading <code>".."</code>s in the regular expression
     * @return An equivalent regular expression
     */
    public Pattern regex(boolean withParentSection) {
        StringBuilder regex = new StringBuilder();

        if(withParentSection)
            for(int i=0; i<rootOffset; i++)
                regex.append("\\.\\.[/\\\\]");

        for(int i=0; i<beforeWildcard.length; i++) {
            if(fromWildcard == null || i != beforeWildcard.length - 1)
                regex.append(beforeWildcard[i]);
            if(i != beforeWildcard.length - 1)
                regex.append("[/\\\\]");
        }

        if(fromWildcard != null)
            regex.append(fromWildcard);

        return Pattern.compile(regex.toString());
    }

    /**
     * Returns whether this glob can match a variable number of path segments, which is true
     * iff the glob pattern contains a double wildcard.
     *
     * @return Whether the pattern contains <code>"**"</code>
     */
    public boolean isVariableLength() {
        return fromWildcard != null;
    }

    /**
     * Returns the number of leading <code>".."</code> path segments.
     *
     * @return The root offset
     */
    public int getRootOffset() {
        return rootOffset;
    }

    /**
     * Returns <code>true</code> iff this pattern only matches directories, that is,
     * it ends with a slash or describes the current directory (<code>"."</code>).
     *
     * @return Whether this pattern only matches directories
     */
    public boolean isDefinitelyDirectory() {
        return parts[parts.length-1].isEmpty();
    }

    /**
     * Returns whether this pattern describes a concrete path without any wildcards
     * or other patterns.
     *
     * @return Whether this glob matches exactly one path
     */
    public boolean isConcrete() {
        return concrete;
    }

    /**
     * Returns whether this pattern describes the current directory, that is, it is the
     * pattern <code>"."</code>.
     *
     * @return Whether this glob describes the root
     */
    public boolean isCurrent() {
        return parts.length == 1 && parts[0].isEmpty();
    }

    /**
     * Returns whether this pattern describes an absolute path, that is, it starts with
     * <code>"/"</code> or <code>"\"</code>, or with a <code>"C:"</code> like name.
     *
     * @return Whether this glob describes an absolute path pattern
     */
    public boolean isAbsolute() {
        return parts[0].endsWith(":") || (parts.length != 1 /* not current */ && parts[0].isEmpty());
    }

    /**
     * Resolves all files matching this glob within the current directory. Throws an exception
     * if the glob pattern ends with a '/' or '\' (which indicates that a directory is expected).
     *
     * @return All matching files within the current directory
     */
    public List<Path> resolveFiles() {
        return resolveFiles(Path.of("."));
    }

    /**
     * Resolves all files matching this glob.  Throws an exception if the glob pattern ends with
     * a '/' or '\' (which indicates that a directory is expected).
     *
     * @param root The root directory to match from
     * @return All matching paths within the given directory
     */
    public List<Path> resolveFiles(Path root) {
        return resolve(root, true, false);
    }

    /**
     * Resolves files and directories matching this glob within the current directory.
     *
     * @param files Whether to match files
     * @param dirs Whether to match directories
     * @return All matching paths within the current directory
     */
    public List<Path> resolve(boolean files, boolean dirs) {
        return resolve(Path.of("."), files, dirs);
    }

    /**
     * Resolves files and directories matching this glob.
     *
     * @param root The root directory to match from
     * @param files Whether to match files
     * @param dirs Whether to match directories
     * @return All matching paths within the given directory
     */
    public List<Path> resolve(Path root, boolean files, boolean dirs) {
        if(!Files.exists(Arguments.checkNull(root, "root")))
            throw new IllegalArgumentException("Root does not exist");

        if(!files && !dirs) return List.of();
        if(!dirs && isDefinitelyDirectory())
            throw new IllegalArgumentException("File(s) expected, got directory");

        if(concrete) {
            Path path = root.resolve(toString()); // Also works if path is absolute
            if(Files.isRegularFile(path))
                return files ? List.of(path) : List.of();
            if(Files.isDirectory(path))
                return dirs ? List.of(path) : List.of();
            return List.of();
        }

        root = root.resolve("../".repeat(rootOffset));
        if(!Files.exists(root)) return List.of();

        boolean absolute = isAbsolute();

        List<Pattern> beforeWildcard = new ArrayList<>(Arrays.asList(this.beforeWildcard));
        if(absolute)
            beforeWildcard.remove(0);
        if(isDefinitelyDirectory())
            beforeWildcard.remove(beforeWildcard.size() - 1);


        List<Path> result = new ArrayList<>();

        files &= !isDefinitelyDirectory();
        try {
            if(!absolute)
                resolve(root, beforeWildcard, fromWildcard, files, dirs, result);
            else for(Path r : FileSystems.getDefault().getRootDirectories()) {
                String str = r.toString();
                if(str.contains("\\")) str = str.substring(0, str.indexOf('\\'));
                else if(str.contains("/")) str = str.substring(0, str.indexOf('/'));
                if(this.beforeWildcard[0].matcher(str).matches())
                    resolve(r, beforeWildcard, fromWildcard, files, dirs, result);
            }
        } catch(IOException e) {
            throw Utils.rethrow(e);
        }

        return result;
    }

    private void resolve(Path root, List<Pattern> beforeWildcard, Pattern fromWildcard, boolean files, boolean dirs, List<Path> out) throws IOException {
        if(beforeWildcard.isEmpty()) { // Then fromWildcard must be null, otherwise the substring before the ** had to be present
            out.add(root);
            return;
        }
        Files.walkFileTree(root, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {

                Path rel = root.relativize(dir);
                int relDepth = root.equals(dir) ? 0 : rel.getNameCount();

                if(relDepth > beforeWildcard.size()) {
                    if(fromWildcard == null) return FileVisitResult.SKIP_SUBTREE;

                    if(dirs && fromWildcard.matcher(rel.subpath(beforeWildcard.size() - 1, rel.getNameCount()).toString()).matches())
                        out.add(dir);
                }
                else {
                    if(relDepth > 0 && !beforeWildcard.get(relDepth - 1).matcher(dir.getFileName().toString()).matches())
                        return FileVisitResult.SKIP_SUBTREE;

                    if(fromWildcard == null && relDepth == beforeWildcard.size()) {
                        if(dirs) out.add(dir);
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                }

                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if(!files) return FileVisitResult.CONTINUE;

                Path rel = root.relativize(file);
                int relDepth = rel.getNameCount();

                if(fromWildcard == null) {
                    if(relDepth > beforeWildcard.size()) return FileVisitResult.SKIP_SIBLINGS;
                    if(relDepth == beforeWildcard.size() && beforeWildcard.get(beforeWildcard.size() - 1).matcher(file.getFileName().toString()).matches())
                        out.add(file);
                }
                else {
                    if(relDepth >= beforeWildcard.size() && fromWildcard.matcher(rel.subpath(beforeWildcard.size() - 1, rel.getNameCount()).toString()).matches())
                        out.add(file);
                }

                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                if(exc instanceof AccessDeniedException)
                    return FileVisitResult.CONTINUE;
                return super.visitFileFailed(file, exc);
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                if(exc instanceof AccessDeniedException)
                    return FileVisitResult.CONTINUE;
                return super.postVisitDirectory(dir, exc);
            }
        });
    }

    /**
     * Parses the given glob pattern.
     *
     * @param pattern The pattern to parse
     * @return The parsed pattern
     */
    public static Glob parse(String pattern) {
        return new Glob(pattern);
    }
}
