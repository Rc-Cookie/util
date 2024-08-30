package de.rccookie.util.text;

final class SimpleFormat implements Format {

    private final String name;
    private final String beginAnsi;
    private final String endAnsi;

    SimpleFormat(String name, int beginAnsi, int endAnsi) {
        this(name, ""+beginAnsi, ""+endAnsi);
    }

    SimpleFormat(String name, String beginAnsi, String endAnsi) {
        this.name = name;
        this.beginAnsi = beginAnsi;
        this.endAnsi = endAnsi;
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
        return endAnsi;
    }

    @Override
    public String formatGroup() {
        return name;
    }
}
