package de.rccookie.util.text;

import java.util.Comparator;
import java.util.function.Function;
import java.util.stream.Stream;

import de.rccookie.math.Mathf;
import de.rccookie.util.Arguments;
import de.rccookie.util.Cloneable;
import de.rccookie.util.ListStream;
import de.rccookie.util.Tuples;
import de.rccookie.util.Utils;
import org.jetbrains.annotations.NotNull;

public class StringSearch implements Cloneable<StringSearch> {

    private CharSequence query;
    private PenaltyMode skipPenaltyMode = PenaltyMode.LINEAR;
    private int skipPenalty = 2;
    private PenaltyMode offsetPenaltyMode = PenaltyMode.CONSTANT;
    private int offsetPenalty = 5;
    private PenaltyMode offsetButSpacePenaltyMode = PenaltyMode.CONSTANT;
    private int offsetButSpacePenalty = 3;
    private PenaltyMode casePenaltyMode = PenaltyMode.LINEAR;
    private int casePenalty = 1;
    private PenaltyMode longerPenaltyMode = PenaltyMode.CONSTANT;
    private int longerPenalty = 7;
    private PenaltyMode longerButSpacePenaltyMode = PenaltyMode.CONSTANT;
    private int longerButSpacePenalty = 3;
    private boolean dashIsSpace = true;


    public StringSearch() {
        this(null);
    }

    public StringSearch(CharSequence query) {
        this.query = query;
    }


    @NotNull
    @Override
    public StringSearch clone() {
        StringSearch s = new StringSearch(query);
        s.skipPenaltyMode = skipPenaltyMode;
        s.skipPenalty = skipPenalty;
        s.offsetPenaltyMode = offsetPenaltyMode;
        s.offsetPenalty = offsetPenalty;
        s.offsetButSpacePenaltyMode = offsetButSpacePenaltyMode;
        s.offsetButSpacePenalty = offsetButSpacePenalty;
        s.casePenaltyMode = casePenaltyMode;
        s.casePenalty = casePenalty;
        s.longerPenaltyMode = longerPenaltyMode;
        s.longerPenalty = longerPenalty;
        s.longerButSpacePenaltyMode = longerButSpacePenaltyMode;
        s.longerButSpacePenalty = longerButSpacePenalty;
        s.dashIsSpace = dashIsSpace;
        return s;
    }

    public StringSearch withQuery(CharSequence query) {
        return clone().query(query);
    }



    public CharSequence query() {
        return query;
    }

    public PenaltyMode skipPenaltyMode() {
        return skipPenaltyMode;
    }

    public int skipPenalty() {
        return skipPenalty;
    }

    public PenaltyMode offsetPenaltyMode() {
        return offsetPenaltyMode;
    }

    public int offsetPenalty() {
        return offsetPenalty;
    }

    public PenaltyMode offsetButSpacePenaltyMode() {
        return offsetButSpacePenaltyMode;
    }

    public int offsetButSpacePenalty() {
        return offsetButSpacePenalty;
    }

    public PenaltyMode casePenaltyMode() {
        return casePenaltyMode;
    }

    public int casePenalty() {
        return casePenalty;
    }

    public PenaltyMode longerPenaltyMode() {
        return longerPenaltyMode;
    }

    public int longerPenalty() {
        return longerPenalty;
    }

    public PenaltyMode longerButSpacePenaltyMode() {
        return longerButSpacePenaltyMode;
    }

    public int longerButSpacePenalty() {
        return longerButSpacePenalty;
    }

    public boolean dashIsSpace() {
        return dashIsSpace;
    }

    public StringSearch query(CharSequence query) {
        this.query = query;
        return this;
    }

    public StringSearch skipPenaltyMode(PenaltyMode skipPenaltyMode) {
        this.skipPenaltyMode = Arguments.checkNull(skipPenaltyMode, "skipPenaltyMode");
        return this;
    }

    public StringSearch skipPenalty(int skipPenalty) {
        this.skipPenalty = skipPenalty;
        return this;
    }

    public StringSearch offsetPenaltyMode(PenaltyMode offsetPenaltyMode) {
        this.offsetPenaltyMode = Arguments.checkNull(offsetPenaltyMode, "offsetPenaltyMode");
        return this;
    }

    public StringSearch offsetPenalty(int offsetPenalty) {
        this.offsetPenalty = offsetPenalty;
        return this;
    }

    public StringSearch offsetButSpacePenaltyMode(PenaltyMode offsetButSpacePenaltyMode) {
        this.offsetButSpacePenaltyMode = Arguments.checkNull(offsetButSpacePenaltyMode, "offsetButSpacePenaltyMode");
        return this;
    }

    public StringSearch offsetButSpacePenalty(int offsetButSpacePenalty) {
        this.offsetButSpacePenalty = offsetButSpacePenalty;
        return this;
    }

    public StringSearch casePenaltyMode(PenaltyMode casePenaltyMode) {
        this.casePenaltyMode = Arguments.checkNull(casePenaltyMode, "casePenaltyMode");
        return this;
    }

    public StringSearch casePenalty(int casePenalty) {
        this.casePenalty = casePenalty;
        return this;
    }

    public StringSearch longerPenaltyMode(PenaltyMode longerPenaltyMode) {
        this.longerPenaltyMode = Arguments.checkNull(longerPenaltyMode, "longerPenaltyMode");
        return this;
    }

    public StringSearch longerPenalty(int longerPenalty) {
        this.longerPenalty = longerPenalty;
        return this;
    }

    public StringSearch longerButSpacePenaltyMode(PenaltyMode longerButSpacePenaltyMode) {
        this.longerButSpacePenaltyMode = Arguments.checkNull(longerButSpacePenaltyMode, "longerButSpacePenaltyMode");
        return this;
    }

    public StringSearch longerButSpacePenalty(int longerButSpacePenalty) {
        this.longerButSpacePenalty = longerButSpacePenalty;
        return this;
    }

    public StringSearch dashIsSpace(boolean dashIsSpace) {
        this.dashIsSpace = dashIsSpace;
        return this;
    }



    public <S extends CharSequence> ListStream<S> find(Iterable<? extends S> items) {
        return find(items, checkQuery());
    }

    public <S extends CharSequence> ListStream<S> find(Iterable<? extends S> items, CharSequence query) {
        return find(Utils.stream(items), query);
    }

    public <S extends CharSequence> ListStream<S> find(Stream<? extends S> items) {
        return find(items, checkQuery());
    }

    public <S extends CharSequence> ListStream<S> find(Stream<? extends S> items, CharSequence query) {
        return find(items, query, Function.identity());
    }

    public <T> ListStream<T> find(Iterable<? extends T> items, Function<? super T, ? extends CharSequence> selector) {
        return find(items, checkQuery(), selector);
    }

    public <T> ListStream<T> find(Iterable<? extends T> items, CharSequence query, Function<? super T, ? extends CharSequence> selector) {
        return find(Utils.stream(items), query, selector);
    }

    public <T> ListStream<T> find(Stream<? extends T> items, Function<? super T, ? extends CharSequence> selector) {
        return find(items, checkQuery(), selector);
    }

    public <T> ListStream<T> find(Stream<? extends T> items, CharSequence query, Function<? super T, ? extends CharSequence> selector) {
        return ListStream.of(items.filter(i -> matchQuality(query, selector.apply(i)) != Integer.MAX_VALUE));
    }

    public <T> ListStream<T> findMultipleKeys(Iterable<? extends T> items, Function<? super T, ? extends Iterable<? extends CharSequence>> selector) {
        return findMultipleKeys(items, checkQuery(), selector);
    }

    public <T> ListStream<T> findMultipleKeys(Iterable<? extends T> items, CharSequence query, Function<? super T, ? extends Iterable<? extends CharSequence>> selector) {
        return findMultipleKeys(Utils.stream(items), query, selector);
    }

    public <T> ListStream<T> findMultipleKeys(Stream<? extends T> items, Function<? super T, ? extends Iterable<? extends CharSequence>> selector) {
        return findMultipleKeys(items, checkQuery(), selector);
    }

    public <T> ListStream<T> findMultipleKeys(Stream<? extends T> items, CharSequence query, Function<? super T, ? extends Iterable<? extends CharSequence>> selector) {
        return ListStream.of(items.filter(i -> Mathf.min(selector.apply(i), s -> matchQuality(query, s)) != Integer.MAX_VALUE));
    }

    public <S extends CharSequence> ListStream<S> sort(Iterable<? extends S> items) {
        return sort(items, checkQuery());
    }

    public <S extends CharSequence> ListStream<S> sort(Iterable<? extends S> items, CharSequence query) {
        return sort(Utils.stream(items), query);
    }

    public <S extends CharSequence> ListStream<S> sort(Stream<? extends S> items) {
        return sort(items, checkQuery());
    }

    public <S extends CharSequence> ListStream<S> sort(Stream<? extends S> items, CharSequence query) {
        return sort(items, query, Function.identity());
    }

    public <T> ListStream<T> sort(Iterable<? extends T> items, Function<? super T, ? extends CharSequence> selector) {
        return sort(items, checkQuery(), selector);
    }

    public <T> ListStream<T> sort(Iterable<? extends T> items, CharSequence query, Function<? super T, ? extends CharSequence> selector) {
        return sort(Utils.stream(items), query, selector);
    }

    public <T> ListStream<T> sort(Stream<? extends T> items, Function<? super T, ? extends CharSequence> selector) {
        return sort(items, checkQuery(), selector);
    }

    public <T> ListStream<T> sort(Stream<? extends T> items, CharSequence query, Function<? super T, ? extends CharSequence> selector) {
        return ListStream.of(items
                .map(i -> Tuples.t(i, matchQuality(query, selector.apply(i))))
                .filter(t -> t.b != Integer.MAX_VALUE)
                .sorted(Comparator.comparingInt(t -> t.b))
                .map(t -> t.a));
    }

    public <T> ListStream<T> sortMultipleKeys(Iterable<? extends T> items, Function<? super T, ? extends Iterable<? extends CharSequence>> selector) {
        return sortMultipleKeys(items, checkQuery(), selector);
    }

    public <T> ListStream<T> sortMultipleKeys(Iterable<? extends T> items, CharSequence query, Function<? super T, ? extends Iterable<? extends CharSequence>> selector) {
        return sortMultipleKeys(Utils.stream(items), query, selector);
    }

    public <T> ListStream<T> sortMultipleKeys(Stream<? extends T> items, Function<? super T, ? extends Iterable<? extends CharSequence>> selector) {
        return sortMultipleKeys(items, checkQuery(), selector);
    }

    public <T> ListStream<T> sortMultipleKeys(Stream<? extends T> items, CharSequence query, Function<? super T, ? extends Iterable<? extends CharSequence>> selector) {
        return ListStream.of(items
                .map(i -> Tuples.t(i, Mathf.min(selector.apply(i), s -> matchQuality(query, s))))
                .filter(t -> t.b != Integer.MAX_VALUE)
                .sorted(Comparator.comparingInt(t -> t.b))
                .map(t -> t.a));
    }

    private CharSequence checkQuery() {
        if(query == null)
            throw new IllegalStateException("No query specified");
        return query;
    }



    /**
     * Calculates how well the given item matches the query previously
     * specified using #query().
     * Higher numbers indicate that the item matches the query <b>less</b>.
     * If the item doesn't match the query at all, {@link Integer#MAX_VALUE}
     * will be returned.
     *
     * @param item The item to test against the search query
     * @return An integer describing how poor the item matches the query
     */
    public int matchQuality(CharSequence item) {
        if(query == null)
            throw new IllegalStateException("No query specified");
        return matchQuality(query, item);
    }

    /**
     * Calculates how well the given item matches the specified query string.
     * Higher numbers indicate that the item matches the query <b>less</b>.
     * If the item doesn't match the query at all, {@link Integer#MAX_VALUE}
     * will be returned.
     *
     * @param query The search query
     * @param item The item to test against the search query
     * @return An integer describing how poor the item matches the query
     */
    public int matchQuality(CharSequence query, CharSequence item) {

        Arguments.checkNull(query, "query");
        CharSequence lowerItem = normalize(Arguments.checkNull(item, "item"));

        int start = 0;
        int quality = 1; // Higher is worse
        for(int i=0; i<query.length(); i++) {
            char c = query.charAt(i);
            int index;

            // Upper case characters only match upper case, lower case matches both upper and lower case chars
            if(casePenaltyMode != PenaltyMode.FAIL && c == Character.toLowerCase(c))
                index = indexOf(lowerItem, normalize(c), start);
            else index = indexOf(item, c, start);

            if(index < start)
                return Integer.MAX_VALUE; // Not found
            else if(item.charAt(index) != c) // char is lower case but matched upper case
                quality += penalty(casePenaltyMode, casePenalty, 1);

            if(index != start) {
                PenaltyMode penaltyMode;
                int penalty;
                if(start != 0) {
                    penaltyMode = skipPenaltyMode;
                    penalty = skipPenalty;
                }
                else if(Character.isWhitespace(lowerItem.charAt(index - 1))) {
                    penaltyMode = offsetButSpacePenaltyMode;
                    penalty = offsetButSpacePenalty;
                }
                else {
                    penaltyMode = offsetPenaltyMode;
                    penalty = offsetPenalty;
                }
                if(penaltyMode == PenaltyMode.FAIL)
                    return Integer.MAX_VALUE;
                quality += penalty(penaltyMode, penalty, index - start); // Add number of characters skipped
            }

            start = index + 1; // If replaced by an alias skip all of it
        }
        int itemLen = item.length();
        if(start != itemLen) {
            boolean nextIsSpace = Character.isWhitespace(normalize(item.charAt(start)));
            PenaltyMode penaltyMode = nextIsSpace ? longerButSpacePenaltyMode : longerPenaltyMode;
            if(penaltyMode == PenaltyMode.FAIL)
                return Integer.MAX_VALUE;
            quality += penalty(penaltyMode, nextIsSpace ? longerButSpacePenalty : longerPenalty, itemLen - start); // If you type the full word it should be a better match
        }

        return quality;
    }

    private CharSequence normalize(CharSequence str) {
        int len = str.length();
        StringBuilder normalized = new StringBuilder(len);
        for(int i=0; i<len; i++)
            normalized.append(normalize(str.charAt(i)));
        return normalized;
    }

    private char normalize(char c) {
        if(dashIsSpace && c == '_' || c == '-')
            return ' ';
        return Character.toLowerCase(c);
    }

    private static int indexOf(CharSequence str, char c, int start) {
        if(str instanceof String)
            return ((String) str).indexOf(c, start);
        int len = str.length();
        for(; start < len; start++)
            if(str.charAt(start) == c)
                return start;
        return -1;
    }

    private static int penalty(PenaltyMode mode, int penalty, int count) {
        switch(mode) {
            case OFF: return 0;
            case CONSTANT: return penalty;
            case LINEAR: return penalty * count;
            default: throw new AssertionError();
        }
    }


    public enum PenaltyMode {
        OFF,
        CONSTANT,
        LINEAR,
        FAIL
    }
}
