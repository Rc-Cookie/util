package de.rccookie.util.text;

import java.util.Objects;

import de.rccookie.util.Arguments;

public interface Format {

    Format BOLD = new SimpleFormat("bold", 1, 22);
    Format ITALIC = new SimpleFormat("italic", 3, 23);
    Format UNDERLINE = new SimpleFormat("underline", 4, 24);
    Format BLINKING = new SimpleFormat("blinking", 5, 25);
    Format INVERSE = new SimpleFormat("inverse", 7, 27);
    Format INVISIBLE = new SimpleFormat("invisible", 8, 28);
    Format STRIKETHROUGH = new SimpleFormat("strikethrough", 9, 29);

    Format BLACK = new ColorFormat("black", 30, true);
    Format RED = new ColorFormat("red", 31, true);
    Format GREEN = new ColorFormat("green", 32, true);
    Format YELLOW = new ColorFormat("yellow", 33, true);
    Format BLUE = new ColorFormat("blue", 34, true);
    Format MAGENTA = new ColorFormat("magenta", 35, true);
    Format CYAN = new ColorFormat("cyan", 36, true);
    Format WHITE = new ColorFormat("white", 37, true);

    Format BLACK_BACK = new ColorFormat("black", 40, false);
    Format RED_BACK = new ColorFormat("red", 41, false);
    Format GREEN_BACK = new ColorFormat("green", 42, false);
    Format YELLOW_BACK = new ColorFormat("yellow", 43, false);
    Format BLUE_BACK = new ColorFormat("blue", 44, false);
    Format MAGENTA_BACK = new ColorFormat("magenta", 45, false);
    Format CYAN_BACK = new ColorFormat("cyan", 46, false);
    Format WHITE_BACK = new ColorFormat("white", 47, false);


    String beginAnsi();

    String endAnsi();

    String formatGroup();

    default Format reset() {
        return new Reset(this);
    }


    final class Reset implements Format {

        public final Format resetted;

        Reset(Format resetted) {
            if(Arguments.checkNull(resetted, "resetted") instanceof Reset)
                throw new IllegalArgumentException("Cannot reset a format reset");
            this.resetted = resetted;
        }

        @Override
        public String toString() {
            return "reset "+resetted;
        }

        @Override
        public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            Reset reset = (Reset) o;
            return Objects.equals(resetted, reset.resetted);
        }

        @Override
        public int hashCode() {
            return Objects.hash(resetted);
        }

        @Override
        public String beginAnsi() {
            return resetted.endAnsi();
        }

        @Override
        public String endAnsi() {
            return resetted.beginAnsi();
        }

        @Override
        public String formatGroup() {
            return resetted.formatGroup();
        }

        @Override
        public Format reset() {
            return resetted;
        }
    }

    static Format reset(Format format) {
        return format instanceof Reset ? ((Reset) format).resetted : new Reset(format);
    }
}
