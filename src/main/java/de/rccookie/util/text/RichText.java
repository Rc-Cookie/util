//package de.rccookie.util.text;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collection;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Map;
//import java.util.Objects;
//import java.util.Set;
//import java.util.function.Function;
//import java.util.stream.Collectors;
//
//import de.rccookie.math.Mathf;
//import de.rccookie.util.Arguments;
//import de.rccookie.util.Utils;
//import org.jetbrains.annotations.Contract;
//import org.jetbrains.annotations.NotNull;
//
///**
// * Represents a text, where subsequences of the text can have formatting applied to it.
// * Additionally to the format for every character in the string, the rich text also has
// * a formatting state for the next string appended. This format is <b>not</b> compared
// * by {@link #equals(Object)}, it only compares whether the visual output is identical.
// * Rich text is always immutable.
// */
//@SuppressWarnings("unchecked")
//public class RichText implements CharSequence {
//
//    /**
//     * An empty, unformatted text.
//     */
//    public static final RichText EMPTY = new RichText(new String[] { "" }, new Map[0], Map.of());
//
//    /**
//     * The text split into segments such that each segment has the same format,
//     * and subsequent segments differ in format. Only the first element of this
//     * array can be an empty string (iff the text starts with unformatted text),
//     * and this array is never empty.
//     */
//    private final String[] segments;
//    /**
//     * The format <b>after</b> each segment. For example, the formats at index 4
//     * would represent the change of format between segment 4 and 5. Thus, this
//     * array is always one element short of {@link #segments}. Also, none of the
//     * sets in this array are empty (because each segment is formatted differently).
//     */
//    private final Map<String, Format>[] formats;
//    private final Map<String, Format> currentFormat;
//    /**
//     * The format change between the last segment and text possibly append later.
//     */
//    private final Map<String, Format> nextFormat;
//
//    private RichText(String[] segments, Map<String, Format>[] formats, Map<String, Format> currentFormat) {
//        this(segments, formats, currentFormat, Map.of());
//    }
//    private RichText(String[] segments, Map<String, Format>[] formats, Map<String, Format> currentFormat, Map<String, Format> nextFormat) {
//        this.segments = Arguments.deepCheckNull(segments);
//        this.formats = Arguments.checkNull(formats);
//        this.nextFormat = Arguments.checkNull(nextFormat, "nextFormat");
//        this.currentFormat = Arguments.checkNull(currentFormat, "currentFormat");
//        if(segments.length != formats.length + 1)
//            throw new RuntimeException("Assertion failed");
//    }
//
//    public RichText(String text, Format... format) {
//        RichText prefab = EMPTY.format(text, format);
//        segments = prefab.segments;
//        formats = prefab.formats;
//        nextFormat = prefab.nextFormat;
//        currentFormat = prefab.currentFormat;
//    }
//
//
//    /**
//     * Returns the unformatted string content of this rich text.
//     *
//     * @return The string of this rich text
//     */
//    @Override
//    @NotNull
//    @Contract(pure = true)
//    public String toString() {
//        return String.join("", segments);
//    }
//
//    /**
//     * Tests whether this rich text is equal to the given object. An object is
//     * equal to this rich text, if it is also a rich text, and would produce the
//     * same visual output. This method does <b>not</b> compare the order in which
//     * different formats were applied, as long as they are applied to the same
//     * characters in both cases. It also <b>does not</b> compare the trailing format
//     * that would affect text append to the rich text.
//     *
//     * @param o The object to compare for equality
//     * @return Whether the given object is equal to this rich text
//     */
//    @Override
//    public boolean equals(Object o) {
//        if(this == o) return true;
//        if(o == null || getClass() != o.getClass()) return false;
//        RichText richText = (RichText) o;
//        return Arrays.equals(segments, richText.segments) && Arrays.equals(formats, richText.formats);
//    }
//
//    @Override
//    public int hashCode() {
//        return 31 * Arrays.hashCode(segments) + Arrays.hashCode(formats);
//    }
//
//    @Override
//    @Contract(pure = true)
//    public int length() {
//        return Mathf.sum(segments, String::length);
//    }
//
//    @Contract(pure = true)
//    public boolean isEmpty() {
//        return segments.length == 1 && segments[0].isEmpty();
//    }
//
//    @Contract(pure = true)
//    public boolean isBlank() {
//        for(String s : segments)
//            if(!s.isBlank()) return false;
//        return true;
//    }
//
//    @Override
//    @Contract(pure = true)
//    public char charAt(int index) {
//        for(String s : segments) {
//            if(index < s.length())
//                return s.charAt(index);
//            index -= s.length();
//        }
//        throw new IndexOutOfBoundsException(index + length());
//    }
//
//    /**
//     * Returns a subsequence of this rich text, such that that subsequence has
//     * the same formatting as that segment has in this rich text. In other words,
//     * the implicit formatting from the segment before the beginning of the sequence
//     * will also be included in the subsequence.
//     * <p>The format state for subsequent operations will not be set (<b>except</b>
//     * <code>start == end</code>, in which case the format state will be the format
//     * of the start character). This means that, in general, <code>subSequence(0, length())</code>
//     * will be equal to this rich text, but appending strings to them might result in
//     * different results.</p>
//     *
//     * @param start the start index, inclusive
//     * @param end the end index, exclusive
//     * @return A rich text representing the specified subsequence of this rich text
//     */
//    @NotNull
//    @Override
//    @Contract(pure = true)
//    public RichText subSequence(int start, int end) {
//        Arguments.checkRange(start, 0, null);
//        Arguments.checkRange(end, start, null);
//
//        Map<String, Format> startFormat = getFormatMap(start);
//
//        if(start == end && end < length())
//            return new RichText(new String[] { "" }, new Map[0], Map.of(), startFormat);
//
//        int startSegment = getSegmentIndex(start);
//        start = getIndexInSegment(start);
//        int endSegment = getSegmentIndex(end - 1); // Get index of last included
//        end = getIndexInSegment(end);
//
//        int offset = startFormat.isEmpty() ? 0 : 1;
//
//        String[] subSegments = new String[endSegment - startSegment + 1 + offset]; // +1: endSegment is inclusive
//        subSegments[0] = ""; // Might be overridden later
//        Map<String, Format>[] subFormats = new Map[subSegments.length - 1];
//        subFormats[0] = startFormat;
//
//        if(startSegment != endSegment) {
//            subSegments[offset] = segments[startSegment].substring(start);
//            System.arraycopy(segments, startSegment + 1, subSegments, offset + 1, endSegment - startSegment - 1);
//            subSegments[subSegments.length - 1] = segments[endSegment].substring(0, end);
//        }
//        else subSegments[subSegments.length - 1] = segments[startSegment].substring(start, end);
//        System.arraycopy(formats, startSegment, subFormats, offset, endSegment - startSegment);
//
//        return new RichText(subSegments, subFormats, getFormatMap(Math.max(0, end - 1)));
//    }
//
//    /**
//     * Returns a subsequence of this rich text, but without the implicit formatting caused
//     * by formatting before the beginning of the subsequence. This means that at least the first
//     * character of the subsequence will be unformatted. In other words, only the <i>change</i>
//     * of format will be reserved, but not the initial state.
//     *
//     * @param start the start index, inclusive
//     * @param end the end index, exclusive
//     * @return A rich text representing the specified subsequence of this rich text, without implicit formatting
//     */
//    @Contract(pure = true)
//    public RichText subSequenceWithoutPrevFormat(int start, int end) {
//        Arguments.checkRange(start, 0, null);
//        Arguments.checkRange(end, start, null);
//
//        if(start == end && start < length())
//            return EMPTY;
//
//        int startSegment = getSegmentIndex(start);
//        start = getIndexInSegment(start);
//        int endSegment = getSegmentIndex(end - 1); // Get index of last included
//        end = getIndexInSegment(end);
//
//        if(startSegment == endSegment)
//            return new RichText(new String[] { segments[startSegment].substring(start, end) }, new Map[0], Map.of());
//
//        Map<String, Format> resetted = new HashMap<>();
//        List<String> subSequences = new ArrayList<>(endSegment - startSegment + 1);
//        List<Map<String, Format>> subFormat = new ArrayList<>(endSegment - startSegment + 1);
//        subSequences.add(segments[startSegment].substring(start));
//
//        for(int i=startSegment; i<endSegment; i++) {
//            String str = (i == endSegment - 1) ? segments[i].substring(0, end) : segments[i];
//            Map<String, Format> remaining = formats[i].values().stream().filter(f -> !(f instanceof Format.Reset) || resetted.put(f.formatGroup(), ((Format.Reset) f).resetted) == null).collect(Collectors.toMap(Format::formatGroup, Function.identity()));
//            if(remaining.isEmpty()) {
//                // Format is all a reset from a format that was applied before the subsequence -> ignore and append string to previous segment
//                int index = subSequences.size() - 1;
//                subSequences.set(index, subSequences.get(index) + str);
//            }
//            else {
//                subSequences.add(str);
//                subFormat.add(formats[i]);
//            }
//        }
//
//        return new RichText(subSequences.toArray(new String[0]), subFormat.toArray(new Set[0]));
//    }
//
//    /**
//     * Returns all format modifications applied to the character at the specified index
//     *
//     * @param index The index of the character to get the format of
//     * @return The format of the character at that index
//     */
//    @Contract(pure = true)
//    public Set<Format> getFormat(int index) {
//        return Set.copyOf(getFormatMap(index).values());
//    }
//
//    /**
//     * Returns all format modifications applied to the character at the specified index
//     *
//     * @param index The index of the character to get the format of
//     * @return The format of the character at that index
//     */
//    private Map<String, Format> getFormatMap(int index) {
//        Map<String, Format> format = new HashMap<>();
//        for(int i=0, stop=getSegmentIndex(index); i<stop; i++)
//            for(Format f : formats[i].values())
//                format.put(f.formatGroup(), f);
//        format.values().removeIf(f -> f.getClass() == Format.Reset.class);
//        return format;
//    }
//
//    /**
//     * Returns the change in format between the last character in this rich text and the format
//     * that will apply for any appended text.
//     *
//     * @return The change in format for potentially appended text
//     */
//    @Contract(pure = true)
//    public Set<Format> getNextFormatChange() {
//        return Utils.view(nextFormat);
//    }
//
//    /**
//     * Returns all the format modifiers that would be applied to appended text. This may or may
//     * not be the same format as applied to the last character in the text; it is the same iff
//     * {@link #getNextFormatChange()} returns an empty set.
//     *
//     * @return The format that would be applied to any appended text
//     */
//    @Contract(pure = true)
//    public Set<Format> currentFormat() {
//        Set<Format> format = getLastSegmentFormat();
//        for(Format f : nextFormat) {
//            if(f instanceof Format.Reset)
//                format.remove(((Format.Reset) f).resetted);
//            else format.add(f);
//        }
//        return Utils.view(format);
//    }
//
//    private Set<Format> getLastSegmentFormat() {
//        Set<Format> format = new HashSet<>();
//        for(int i=0; i<formats.length; i++) {
//            for(Format f : formats[i]) {
//                if(f instanceof Format.Reset)
//                    format.remove(((Format.Reset) f).resetted);
//                else format.add(f);
//            }
//        }
//        return format;
//    }
//
//    private int getSegmentIndex(int charIndex) {
//        return getIndex(charIndex, true);
//    }
//
//    private int getIndexInSegment(int charIndex) {
//        return getIndex(charIndex, false);
//    }
//
//    private int getIndex(int charIndex, boolean segment) {
//        if(charIndex < 0)
//            throw new IndexOutOfBoundsException(charIndex);
//        for(int i=0; i<segments.length; i++) {
//            if(charIndex < segments[i].length())
//                return segment ? i : charIndex;
//            charIndex -= segments[i].length();
//        }
//        throw new IndexOutOfBoundsException(charIndex + length());
//    }
//
//
//    /**
//     * Appends the given object as its {@link Object#toString()} representation to this rich text.
//     *
//     * @param toString The object to append to this text as its string representation
//     * @return A rich text representing this text with the given text appended
//     */
//    @SuppressWarnings("unchecked")
//    @Contract(pure = true)
//    public RichText append(Object toString) {
//        String str = Objects.toString(toString);
//        if(str.isEmpty()) return this;
//
//        if(nextFormat.isEmpty()) {
//            // Format hasn't changed, simply append it to the end of the previous string which is in the same format
//            // (the arrays always have at least one element)
//            String[] segments = this.segments.clone();
//            segments[segments.length - 1] += str;
//            return new RichText(segments, formats);
//        }
//        String[] segments = new String[this.segments.length + 1];
//        Set<Format>[] formats = new Set[this.formats.length + 1];
//        System.arraycopy(this.segments, 0, segments, 0, this.segments.length);
//        System.arraycopy(this.formats, 0, formats, 0, this.formats.length);
//
//        segments[segments.length - 1] = str;
//        formats[formats.length - 1] = nextFormat;
//
//        return new RichText(segments, formats);
//    }
//
//    /**
//     * Adds the given format modifiers, which will be used when text will be appended later.
//     *
//     * @param format The format modifiers to add. If a {@link Format.Reset} is passed, the format
//     *               will be removed instead (if it was present)
//     * @return A rich text representing the same text as this rich text, with the given formats
//     *         marked for subsequent text
//     */
//    @Contract(pure = true)
//    public RichText format(Format... format) {
//        return format(Arrays.asList(Arguments.deepCheckNull(format, "format")), false);
//    }
//
//    /**
//     * Shorthand for <code>format(format).append(text).reset(format)</code>. Note that the given text will
//     * already receive the given format, as opposed to calling <code>append(text).format(format)</code>
//     * where the text would not receive the format. Also, subsequently appended text will not have the
//     * additional format modifiers applied as the added modifiers are reset at the end.
//     *
//     * @param text The text to appended with the given formats applied
//     * @param format The formats to (additionally) apply to the given text
//     * @return A rich text representing this text with the given format and text appended
//     */
//    @Contract(pure = true)
//    public RichText format(String text, Format... format) {
//        return format(format).append(text).reset(format);
//    }
//
//    /**
//     * Removes the given format modifiers, which will be not used when text will be appended later,
//     * if they were currently present.
//     *
//     * @param format The format modifiers to remove. If a {@link Format.Reset} is passed, the format
//     *               will be added instead (if it wasn't already present)
//     * @return A rich text representing the same text as this rich text, with the given formats
//     *         unmarked for subsequent text
//     */
//    @Contract(pure = true)
//    public RichText reset(Format... format) {
//        return format(Arrays.asList(Arguments.deepCheckNull(format, "format")), true);
//    }
//
//    @Contract(pure = true)
//    private RichText format(Collection<? extends Format> format, boolean reset) {
//        if(format.isEmpty())
//            return this;
//
//        Map<String, Format> nextFormat = new HashMap<>(this.nextFormat);
//        for(Format f : format) {
//            if(reset) f = f.reset();
//
//            if(nextFormat.containsValue(f)) continue;
//            if(currentFormat.containsValue(f))
//                nextFormat.remove(f.formatGroup()); // The previous text is already formatted this way, we only need to prevent that it gets changed
//            else nextFormat.put(f.formatGroup(), f); // Set this as the next format and remove and formats that would be immediately overridden
//        }
//
//        return nextFormat.equals(this.nextFormat) ? this : new RichText(segments, formats, currentFormat, nextFormat);
//    }
//
//    /**
//     * Resets all formatting for subsequently appended text.
//     *
//     * @return A rich text with the same format and content, but with subsequent text being marked as
//     *         unformatted
//     */
//    @Contract(pure = true)
//    public RichText reset() {
//        return new RichText(segments, formats, currentFormat, currentFormat().stream().map(Format.Reset::new).collect(Collectors.toMap(Format::formatGroup, f->f)));
//    }
//
//    /**
//     * Appends the given rich text to this rich text, such that the appended part has exactly the same
//     * format as the given rich text.
//     *
//     * @param text The text to append
//     * @return A rich text representing the concatenation of this and the given rich text
//     */
//    @Contract(pure = true)
//    public RichText append(RichText text) {
//        return append(text, true);
//    }
//
//    /**
//     * Appends the given rich text to this rich text.
//     *
//     * @param text The text to append
//     * @param preserveUnformattedFormat Whether to remove any formatting for the appended text that is currently
//     *                                  active in this rich text that is not active in the given rich text
//     * @return A rich text representing the concatenation of this and the given rich text
//     */
//    @Contract(pure = true)
//    public RichText append(RichText text, boolean preserveUnformattedFormat) {
//        if(preserveUnformattedFormat)
//            return reset().append(text, false);
//
//        if(text.isEmpty())
//            return format(text.nextFormat.values(), false);
//
//        if(!text.segments[0].isEmpty()) {
//            String[] segments = new String[this.segments.length + text.segments.length];
//            Set<Format>[] formats = new Set[this.formats.length + 1 /* nextFormat */ + text.formats.length];
//
//            System.arraycopy(this.segments, 0, segments, 0, this.segments.length);
//            System.arraycopy(text.segments, 0, segments, this.segments.length, text.segments.length);
//
//            System.arraycopy(this.formats, 0, formats, 0, this.formats.length);
//            formats[this.formats.length] = Set.copyOf(nextFormat.values());
//            System.arraycopy(text.formats, 0, formats, this.formats.length+1, text.formats.length);
//
//            Map<String, Format> currentFormat = new HashMap<>(this.currentFormat);
//            currentFormat.putAll(nextFormat);
//
//            return new RichText(segments, formats, text.nextFormat);
//        }
//
//        RichText t = format(text.nextFormat, false);
//
//        if(t.nextFormat.isEmpty()) {
//            String[] segments = new String[t.segments.length + text.segments.length - 2];
//            Set<Format>[] formats = new Set[t.formats.length + text.formats.length - 1];
//
//            System.arraycopy(t.segments, 0, segments, 0, t.segments.length);
//            segments[t.segments.length - 1] += text.segments[1];
//            System.arraycopy(text.segments, 2, segments, t.segments.length, text.segments.length - 2);
//
//            System.arraycopy(t.formats, 0, formats, 0, t.formats.length);
//            System.arraycopy(text.formats, 1, formats, this.formats.length, text.formats.length - 1);
//
//            return new RichText(segments, formats, text.nextFormat);
//        }
//
//        String[] segments = new String[t.segments.length + text.segments.length - 1];
//        Set<Format>[] formats = new Set[t.formats.length + text.formats.length];
//
//        System.arraycopy(t.segments, 0, segments, 0, t.segments.length);
//        System.arraycopy(text.segments, 1, segments, t.segments.length, text.segments.length - 1);
//
//        System.arraycopy(t.formats, 0, formats, 0, t.formats.length);
//        formats[t.formats.length] = t.nextFormat;
//        System.arraycopy(text.formats, 1, formats, this.formats.length+1, text.formats.length - 1);
//
//        return new RichText(segments, formats, text.nextFormat);
//    }
//
//
//    /**
//     * Shorthand for <code>format(Format.BOLD)</code>.
//     *
//     * @return A rich text with the same content as this rich text, but subsequently
//     *         appended text will be bold.
//     */
//    public RichText bold() {
//        return format(Format.BOLD);
//    }
//
//    /**
//     * Shorthand for <code>reset(Format.BOLD)</code>.
//     *
//     * @return A rich text with the same content as this rich text, but subsequently
//     *         appended text will not be bold.
//     */
//    public RichText resetBold() {
//        return reset(Format.BOLD);
//    }
//
//    /**
//     * Appends the given text formatted bold. The format for subsequent text will be the
//     * same as the format before the bold text, it would be bold iff this text is already
//     * formatted bold.
//     *
//     * @param text The text to append while being formatted bold
//     * @return A rich text with the given text appended as bold text, and the same next
//     *         format as this rich text
//     */
//    public RichText bold(Object text) {
//        if(currentFormat().contains(Format.BOLD))
//            return append(text);
//        return bold().append(text).resetBold();
//    }
//
//
//
//    public String encodeToAnsi() {
//
//        StringBuilder str = new StringBuilder();
//        str.append(segments[0]);
//
//        Set<Format> active = new HashSet<>();
//        for(int i=1; i<segments.length; i++) {
//            if(!formats[i-1].isEmpty()) {
//                str.append("\u001b[");
//                for(Format f : formats[i - 1]) {
//                    str.append(f.beginAnsi());
//                    if(f instanceof Format.Reset)
//                        active.remove(((Format.Reset) f).resetted);
//                    else active.add(f);
//                    str.append(';');
//                }
//                str.setCharAt(str.length() - 1, 'm');
//            }
//            str.append(segments[i]);
//        }
//        if(!active.isEmpty())
//            str.append("\u001b[0m");
//        return str.toString();
//    }
//
//
//
//    public static void main(String[] args) {
//        System.out.println(RichText.EMPTY.append("Hello").format(Format.BOLD).append(" wo").format(Format.ITALIC).append("rld").reset(Format.BOLD).append("!").encodeToAnsi());
//    }
//}
