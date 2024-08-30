package de.rccookie.util.text;

final class ColorFormat implements Format {

    private final String name;
    private final String beginAnsi;
    private final boolean foreground;

    ColorFormat(String name, int beginAnsi, boolean foreground) {
        this.name = name;
        this.beginAnsi = "" + beginAnsi;
        this.foreground = foreground;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public String beginAnsi() {
        return beginAnsi;
    }

    @Override
    public String endAnsi() {
        return foreground ? "39" : "49";
    }

    @Override
    public String formatGroup() {
        return foreground ? "color" : "background color";
    }
}
