//package de.rccookie.util.text;
//
//import java.util.Arrays;
//import java.util.Map;
//import java.util.Objects;
//
//import org.jetbrains.annotations.NotNull;
//
//public class RichText2 implements CharSequence {
//
//    public static final RichText2 EMPTY = new RichText2(new Segment[0], Map.of());
//
//    private final Segment[] segments;
//    private final Map<String, Format> nextFormat;
//
//    private RichText2(Segment[] segments, Map<String, Format> nextFormat) {
//        this.segments = segments;
//        this.nextFormat = nextFormat;
//    }
//
//    @Override
//    @NotNull
//    public String toString() {
//        StringBuilder str = new StringBuilder();
//        for(Segment segment : segments)
//            str.append(segment.value);
//        return str.toString();
//    }
//
//    @Override
//    public boolean equals(Object obj) {
//        if(obj == this) return true;
//        if(!(obj instanceof RichText2)) return false;
//        return Arrays.equals(segments, ((RichText2) obj).segments);
//    }
//
//    @Override
//    public int hashCode() {
//        return Arrays.hashCode(segments);
//    }
//
//    @Override
//    public int length() {
//        int length = 0;
//        for(Segment segment : segments)
//            length += segment.value.length();
//        return length;
//    }
//
//    @Override
//    public char charAt(int index) {
//        long i = getIndex(index, false);
//        return segments[(int) i].value.charAt((int) (i >> 32));
//    }
//
//    @NotNull
//    @Override
//    public RichText2 subSequence(int start, int end) {
//        if(start > end)
//            throw new IndexOutOfBoundsException("start > end");
//
//        long startIndex = getIndex(start, true);
//        long endIndex = getIndex(end, true);
//        if(startIndex == endIndex)
//            return EMPTY;
//
//        int startSegment = (int) startIndex, endSegment = (int) endIndex;
//
//        if(startIndex == 0 && endSegment == segments.length + 1) {
//            if(segments.length == 0)
//                return EMPTY;
//            if(segments[segments.length - 1].format.equals(nextFormat))
//                return this;
//            return new RichText2(segments, segments[segments.length - 1].format);
//        }
//
//        start = (int) (startIndex >> 32);
//        end = (int) (endIndex >> 32);
//
//        int newSegmentCount = endSegment - startSegment;
//        if(end == 0) newSegmentCount--;
//        Segment[] newSegments = new Segment[newSegmentCount];
//        if(start == 0)
//            newSegments[0] = segments[startSegment];
//        else newSegments[0] = new Segment(segments[startSegment].value.substring(start), segments[startSegment].format);
//        System.arraycopy(segments, startSegment + 1, newSegments, 1, endSegment - startSegment - 2);
//        if(end != 0)
//            newSegments[newSegmentCount-1] = new Segment(segments[endSegment].value.substring(0, end), segments[endSegment].format);
//        return new RichText2(newSegments, newSegments[newSegmentCount - 1].format);
//    }
//
//    private long getIndex(int charIndex, boolean inclusive) {
//        if(charIndex < 0)
//            throw new IndexOutOfBoundsException(charIndex);
//        for(int i=0; i<segments.length; i++) {
//            if(charIndex < segments[i].value.length())
//                return i | ((long) charIndex << 32);
//            charIndex -= segments[i].value.length();
//        }
//        if(inclusive && charIndex == 0)
//            return segments.length + 1;
//        throw new IndexOutOfBoundsException(charIndex + length());
//    }
//
//
//    private static final class Segment {
//
//        final String value;
//        final Map<String, Format> format;
//
//        private Segment(String value, Map<String, Format> format) {
//            this.value = value;
//            this.format = format;
//        }
//
//        @Override
//        public boolean equals(Object obj) {
//            if(obj == this) return true;
//            if(!(obj instanceof Segment)) return false;
//            return value.equals(((Segment) obj).value) && format.equals(((Segment) obj).format);
//        }
//
//        @Override
//        public int hashCode() {
//            return Objects.hash(value, format);
//        }
//    }
//}
