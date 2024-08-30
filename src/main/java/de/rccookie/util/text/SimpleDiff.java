package de.rccookie.util.text;

import java.util.Objects;

import de.rccookie.util.Arguments;

public final class SimpleDiff<S extends CharSequence> {

    public final S prefix;
    public final S old;
    public final S replacement;
    public final S suffix;

    public SimpleDiff(S prefix, S old, S replacement, S suffix) {
        this.prefix = Arguments.checkNull(prefix, "prefix");
        this.old = Arguments.checkNull(old, "old");
        this.replacement = Arguments.checkNull(replacement, "replacement");
        this.suffix = Arguments.checkNull(suffix, "suffix");
    }

    @Override
    public String toString() {
        return "SimpleDiff{" +
               "prefix=" + prefix +
               ", old=" + old +
               ", replacement=" + replacement +
               ", suffix=" + suffix +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(!(o instanceof SimpleDiff)) return false;
        SimpleDiff<?> that = (SimpleDiff<?>) o;
        return Objects.equals(prefix, that.prefix) && Objects.equals(old, that.old) && Objects.equals(replacement, that.replacement) && Objects.equals(suffix, that.suffix);
    }

    @Override
    public int hashCode() {
        return Objects.hash(prefix, old, replacement, suffix);
    }

    public boolean identical() {
        return old.length() == 0 && replacement.length() == 0;
    }



    @SuppressWarnings({"unchecked", "rawtypes"})
    public static SimpleDiff<String> compute(String a, String b) {
        return (SimpleDiff<String>) (SimpleDiff) compute((CharSequence) a, b);
    }

    public static SimpleDiff<CharSequence> compute(CharSequence a, CharSequence b) {
        int prefix = 0, suffix = 0;
        int aLen = a.length(), bLen = b.length(), len = Math.min(aLen, bLen);

        for(; prefix<len; prefix++)
            if(a.charAt(prefix) != b.charAt(prefix)) break;
        for(; suffix<len-prefix; suffix++)
            if(a.charAt(aLen - suffix - 1) != b.charAt(bLen - suffix - 1)) break;

        return new SimpleDiff<>(a.subSequence(0, prefix), a.subSequence(prefix, aLen - suffix), b.subSequence(prefix, bLen - suffix), a.subSequence(aLen - suffix, aLen));
    }
}
