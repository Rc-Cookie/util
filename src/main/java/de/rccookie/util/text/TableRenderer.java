package de.rccookie.util.text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.IntFunction;
import java.util.regex.Pattern;

import com.diogonunes.jcolor.Ansi;
import com.diogonunes.jcolor.Attribute;
import de.rccookie.math.Mathf;
import de.rccookie.util.Arguments;
import de.rccookie.util.Table;
import de.rccookie.util.Utils;
import org.jetbrains.annotations.Nullable;

public class TableRenderer {

    private static final int TOP = 0;
    private static final int ABOVE_LABEL = 1;
    private static final int IN_LABEL = 2;
    private static final int BELOW_LABEL = 3;
    private static final int IN_CELL = 4;
    private static final int BETWEEN_CELLS = 5;
    private static final int BOTTOM = 6;
    @SuppressWarnings("SuspiciousNameCombination")
    private static final int LEFT = TOP;
    private static final int BEFORE_LABEL = ABOVE_LABEL;
    private static final int AFTER_LABEL = BELOW_LABEL;
    @SuppressWarnings("SuspiciousNameCombination")
    private static final int RIGHT = BOTTOM;


    public TableRenderer() { }

    public TableRenderer(Table<?,?> table) {
        table(table);
    }


    private String title = "";
    private List<String> rowLabels = List.of();
    private List<String> columnLabels = List.of();
    private final List<List<String>> rows = new ArrayList<>();

    private boolean identicalCellWidths = false;
    private boolean identicalCellHeights = false;
    private int[] maxCellWidths = { Integer.MAX_VALUE };
    private Integer maxRowLabelWidth = null;
    private boolean unicodeSymbols = true;
    private Style style = Style.DOUBLE_LINES_AFTER_LABELS_NO_BORDER;
    private Alignment horizontalAlignment = Alignment.LEFT;
    private Alignment verticalAlignment = Alignment.TOP;
    private boolean realignMultilineStrings = true;


    @Override
    public String toString() {
        return render().toString();
    }



    public List<String> rowLabels() {
        return rowLabels;
    }

    public List<String> columnLabels() {
        return columnLabels;
    }

    public String title() {
        return title;
    }

    public List<String> labels() {
        return columnLabels();
    }

    public List<List<String>> rows() {
        return Utils.view(rows);
    }

    public boolean identicalCellWidths() {
        return identicalCellWidths;
    }

    public boolean identicalCellHeights() {
        return identicalCellHeights;
    }

    public Integer maxRowLabelWidth() {
        return maxRowLabelWidth;
    }

    public int[] maxCellWidths() {
        return maxCellWidths.clone();
    }

    public boolean useUnicodeSymbols() {
        return unicodeSymbols;
    }

    public Style style() {
        return style;
    }

    public Alignment horizontalAlignment() {
        return horizontalAlignment;
    }

    public Alignment verticalAlignment() {
        return verticalAlignment;
    }

    public boolean realignMultilineStrings() {
        return realignMultilineStrings;
    }

    public TableRenderer rowLabels(@Nullable List<?> rowLabels) {
        this.rowLabels = copyAndNormalize(rowLabels);
        return this;
    }

    public TableRenderer rowLabels(Object... rowLabels) {
        return rowLabels(Arrays.asList(rowLabels));
    }

    public TableRenderer title(@Nullable Object title) {
        String str = Objects.toString(title);
        this.title = title == null || str == null ? "" : str;
        return this;
    }

    public TableRenderer columnLabels(@Nullable List<?> columnLabels) {
        this.columnLabels = copyAndNormalize(columnLabels);
        return this;
    }

    public TableRenderer columnLabels(Object... columnLabels) {
        return columnLabels(Arrays.asList(columnLabels));
    }

    public TableRenderer labels(@Nullable List<?> columnLabels) {
        return columnLabels(columnLabels);
    }

    public TableRenderer labels(Object... columnLabels) {
        return labels(Arrays.asList(columnLabels));
    }

    public TableRenderer rows(List<? extends List<?>> rows) {
        this.rows.clear();
        if(rows == null) return this;
        for(List<?> row : rows)
            this.rows.add(copyAndNormalize(row));
        return this;
    }

    public TableRenderer addRow(List<?> row) {
        rows.add(copyAndNormalize(row));
        return this;
    }

    public TableRenderer addRow(Object... values) {
        return addRow(Arrays.asList(values));
    }

    private List<String> copyAndNormalize(@Nullable List<?> list) {
        if(list == null)
            return List.of();
        List<String> copy = new ArrayList<>();
        int nulls = 0;
        for(Object o : list) {
            String s = Objects.toString(o);
            if(o == null || s == null)
                nulls++;
            else {
                for(int i=0; i<nulls; i++)
                    copy.add("");
                nulls = 0;
                copy.add(s);
            }
        }
        return Utils.view(copy);
    }

    public TableRenderer table(Table<?,?> table) {
        return rows(table.rows().map(Table.Vector::asList))
                .columnLabels(table.columnLabels())
                .rowLabels(table.rowLabels());
    }

    public TableRenderer identicalCellWidths(boolean identicalCellWidths) {
        this.identicalCellWidths = identicalCellWidths;
        if(identicalCellWidths && maxCellWidths.length > 1)
            maxCellWidths = new int[] { Integer.MAX_VALUE };
        return this;
    }

    public TableRenderer identicalCellHeights(boolean identicalCellHeights) {
        this.identicalCellHeights = identicalCellHeights;
        return this;
    }

    public TableRenderer identicalCellSizes(boolean identicalCellWidths, boolean identicalCellHeights) {
        return identicalCellWidths(identicalCellWidths).identicalCellHeights(identicalCellHeights);
    }

    public TableRenderer maxCellWidths(int... maxCellWidths) {
        if(maxCellWidths == null || maxCellWidths.length == 0)
            this.maxCellWidths = new int[] { Integer.MAX_VALUE };
        else {
            for(int i=0; i<maxCellWidths.length; i++)
                Arguments.checkRange(maxCellWidths[i], 1, null);
            this.maxCellWidths = maxCellWidths.clone();
            this.identicalCellWidths &= maxCellWidths.length == 1;
        }
        return this;
    }

    public TableRenderer maxCellWidth(int maxCellWidth) {
        return maxCellWidths(maxCellWidth);
    }

    public TableRenderer maxRowLabelWidth(@Nullable Integer maxRowLabelWidth) {
        if(maxRowLabelWidth != null)
            Arguments.checkRange(maxRowLabelWidth, 1, null);
        this.maxRowLabelWidth = maxRowLabelWidth;
        return this;
    }

    public TableRenderer useUnicodeSymbols(boolean unicodeSymbols) {
        this.unicodeSymbols = unicodeSymbols;
        return this;
    }

    public TableRenderer style(Style style) {
        this.style = Arguments.checkNull(style, "tableStyle");
        return this;
    }

    public TableRenderer horizontalAlignment(Alignment horizontalAlignment) {
        this.horizontalAlignment = horizontalAlignment;
        return this;
    }

    public TableRenderer verticalAlignment(Alignment verticalAlignment) {
        this.verticalAlignment = verticalAlignment;
        return this;
    }

    public TableRenderer alignment(Alignment horizontalAlignment, Alignment verticalAlignment) {
        this.horizontalAlignment = horizontalAlignment;
        this.verticalAlignment = verticalAlignment;
        return this;
    }

    public TableRenderer realignMultilineStrings(boolean realignMultilineStrings) {
        this.realignMultilineStrings = realignMultilineStrings;
        return this;
    }



    public AsciiArt render() {
        int rowOffset = !columnLabels.isEmpty() ? 1 : 0;
        int columnOffset = !rowLabels.isEmpty() ? 1 : 0;

        int rowCount = Math.max(rowLabels.size(), rows.size()) + rowOffset;
        int columnCount = Math.max(columnLabels.size(), rows.stream().mapToInt(List::size).max().orElse(0)) + columnOffset;

        String[][][] cells = new String[rowCount][columnCount][];
        for(int i=0; i<cells.length; i++)
            Arrays.fill(cells[i], new String[0]);

        if(rowCount * columnCount != 0) {
            // Fill in the grid sides with the labels if any
            for(int i=0; i<rowLabels.size(); i++)
                cells[i + rowOffset][0] = LineWrapper.splitLines(rowLabels.get(i), maxWidth(-1));
            for(int i=0; i<columnLabels.size(); i++)
                cells[0][i + columnOffset] = LineWrapper.splitLines(columnLabels.get(i), maxWidth(i));
            if(rowOffset + columnOffset == 2)
                cells[0][0] = LineWrapper.splitLines(title, maxWidth(-1));
        }

        // Fill in the main content
        for(int i=0; i<rows.size(); i++) {
            List<String> row = rows.get(i);
            for(int j=0; j<row.size(); j++)
                cells[i + rowOffset][j + columnOffset] = LineWrapper.splitLines(row.get(j), maxWidth(j));
        }


        // Calculate the minimum height and width of all rows and columns so that they can fit all elements
        int[][][] cellLengths = new int[rowCount][columnCount][];
        int[] rowHeights = new int[rowCount];
        int[] columnWidths = new int[columnCount];
        for(int i=0; i<rowCount; i++) {
            for(int j = 0; j < columnCount; j++) {
                rowHeights[i] = Math.max(rowHeights[i], cells[i][j].length);
                cellLengths[i][j] = new int[cells[i][j].length];
                for(int k = 0; k < cells[i][j].length; k++) {
                    cellLengths[i][j][k] = length(cells[i][j][k]);
                    columnWidths[j] = Math.max(columnWidths[j], cellLengths[i][j][k]);
                }
            }
        }

        // Adjust row / column heights / widths if all should be identical
        int maxRowHeight = Mathf.max(rowHeights);
        if(identicalCellHeights)
            Arrays.fill(rowHeights, maxRowHeight);
        if(identicalCellWidths && columnWidths.length > 1) {
            int start = (rowLabels.isEmpty() || maxWidth(-1) == Integer.MAX_VALUE) ? 0 : 1;
            int maxWidth = columnWidths[start];
            for(int i=start+1; i<columnWidths.length; i++)
                maxWidth = Math.max(maxWidth, columnWidths[i]);
            Arrays.fill(columnWidths, start, columnWidths.length, maxWidth);
        }

        // Add padding around each cell so that its size matches the expected size
        for(int i=0; i<rowCount; i++) {
            for(int j=0; j<columnCount; j++) {
                String[] cell = cells[i][j];
                int[] cellLength = cellLengths[i][j];
                String[] padded = new String[rowHeights[i]];

                int topPadding = 0;
                if(verticalAlignment == Alignment.CENTER)
                    topPadding = (padded.length - cell.length) / 2;
                else if(verticalAlignment == Alignment.BOTTOM)
                    topPadding = padded.length - cell.length;

                for(int k=0; k<topPadding; k++)
                    padded[k] = Utils.blank(columnWidths[j]);

                int cellWidth = Mathf.max(cell, String::length);
                int leftPadding = 0;
                if(horizontalAlignment == Alignment.CENTER)
                    leftPadding = (columnWidths[j] - cellWidth + 1) / 2;
                else if(horizontalAlignment == Alignment.RIGHT)
                    leftPadding = columnWidths[j] - cellWidth;

                for(int k=0; k<cell.length; k++) {
                    if(realignMultilineStrings) {
                        if(horizontalAlignment == Alignment.CENTER)
                            leftPadding = (columnWidths[j] - cellLength[k] + 1) / 2;
                        else if(horizontalAlignment == Alignment.RIGHT)
                            leftPadding = columnWidths[j] - cellLength[k];
                    }
                    padded[k + topPadding] = Utils.blank(leftPadding) + cell[k] + Utils.blank(columnWidths[j] - leftPadding - cellLength[k]);
                }

                for(int k = 0; k < padded.length - topPadding - cell.length; k++)
                    padded[k + topPadding + cell.length] = Utils.blank(columnWidths[j]);

                cells[i][j] = padded;
            }
        }

        Style style = unicodeSymbols ? this.style : this.style.ascii;

        int totalWidth = Mathf.sum(columnWidths)
                         + Math.max(0, columnCount - 1 - columnOffset) * width(style.style[BETWEEN_CELLS][BETWEEN_CELLS])
                         + columnOffset * width(style.style[BELOW_LABEL][BETWEEN_CELLS])
                         + width(style.style[BETWEEN_CELLS][LEFT])
                         + width(style.style[BETWEEN_CELLS][RIGHT]);

        int left = rowLabels.isEmpty() ? LEFT : BEFORE_LABEL;

        List<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder(totalWidth);
        for(int i=0; i<rowCount; i++) {

            line.setLength(0);

            if(i == 0) {
                for(int k = 0; k < style.style[TOP][BETWEEN_CELLS].length; k++)
                    lines.add(buildBar(line, style, columnWidths, columnOffset != 0, columnLabels.isEmpty() ? TOP : ABOVE_LABEL, k));
            }

            for(int k=0; k<rowHeights[i]; k++) {
                int _i = i, _k = k;
                lines.add(buildLine(line, style, j -> cells[_i][j][_k], columnCount, i == rowOffset-1 ? IN_LABEL : IN_CELL, left, 0));
            }

            if(i == rowCount - 1) {
                for(int k=0; k<style.style[BOTTOM][BETWEEN_CELLS].length; k++)
                    lines.add(buildBar(line, style, columnWidths, columnOffset != 0, BOTTOM, k));
            }
            else if(i == 0 && !columnLabels.isEmpty()) {
                for(int k=0; k<style.style[BELOW_LABEL][BETWEEN_CELLS].length; k++)
                    lines.add(buildBar(line, style, columnWidths, columnOffset != 0, BELOW_LABEL, k));
                if(this.style == Style.BLANK && maxRowHeight > 1)
                    lines.add(Utils.blank(totalWidth));
            }
            else if(this.style != Style.BLANK || maxRowHeight > 1)
                for(int k=0; k<style.style[BETWEEN_CELLS][BETWEEN_CELLS].length; k++)
                    lines.add(buildBar(line, style, columnWidths, columnOffset != 0, BETWEEN_CELLS, k));
        }

        return new AsciiArt(lines.toArray(String[]::new));
    }

    private String buildBar(StringBuilder line, Style style, int[] columnWidths, boolean hasLabel, int rowType, int k) {
        return buildLine(line, style, j -> Utils.repeat(style.style[rowType][j == 0 && hasLabel ? IN_LABEL : IN_CELL][k], columnWidths[j]), columnWidths.length, rowType, hasLabel ? BEFORE_LABEL : LEFT, k);
    }

    private String buildLine(StringBuilder line, Style style, IntFunction<String> contentGetter, int columnCount, int rowType, int left, int k) {

        line.setLength(0);

        for(int j=0; j<columnCount; j++) {
            if(j == 0)
                line.append(style.style[rowType][left][k]);

            line.append(contentGetter.apply(j));

            if(j == columnCount - 1)
                line.append(style.style[rowType][RIGHT][k]);
            else if(j == 0 && !rowLabels.isEmpty())
                line.append(style.style[rowType][AFTER_LABEL][k]);
            else line.append(style.style[rowType][BETWEEN_CELLS][k]);
        }
        return line.toString();
    }

    private int maxWidth(int column) {
        if(column < 0)
            return maxRowLabelWidth != null ? maxRowLabelWidth : maxCellWidths[0];
        return maxCellWidths[Math.min(column, maxCellWidths.length - 1)];
    }

    private static final Pattern ANSI_ESCAPE_SEQUENCE_PAT = Pattern.compile("\u001b\\[\\d+(;\\d+)*m");
    private static int length(String str) {
        return ANSI_ESCAPE_SEQUENCE_PAT.matcher(str).replaceAll("").length();
    }

    private static int width(String[] lines) {
        return lines.length == 0 ? 0 : lines[0].length();
    }

    public static final class Style {

        private static final int COUNT = BOTTOM + 1;

        public static final Style BLANK = new Style(new String[][] {
                { "", "", "", "", "", "", "" },
                { "", "", "", "", "", "", "" },
                { "", "", " ", "   ", " ", "  ", "" },
                { "", "", "═", "   ", "═", "  ", "" },
                { "", "", " ", "   ", " ", "  ", "" },
                { "", "", " ", "   ", " ", "  ", "" },
                { "", "", "", "", "", "", "" }
            }, new Style(new String[][] {
                { "", "", "", "", "", "", "" },
                { "", "", "", "", "", "", "" },
                { "", "", " ", "   ", " ", "  ", "" },
                { "", "", "=", "   ", "=", "  ", "" },
                { "", "", " ", "   ", " ", "  ", "" },
                { "", "", " ", "   ", " ", "  ", "" },
                { "", "", "", "", "", "", "" }
        }, null));
        public static final Style COMPACT_BLANK = new Style(new String[][] {
                { "", "", "", "", "", "", "" },
                { "", "", "", "", "", "", "" },
                { "", "", " ", "   ", " ", "  ", "" },
                { "", "", "═", "   ", "═", "  ", "" },
                { "", "", " ", "   ", " ", "  ", "" },
                { "", "", "", "", "", "", "" },
                { "", "", "", "", "", "", "" }
        }, new Style(new String[][] {
                { "", "", "", "", "", "", "" },
                { "", "", "", "", "", "", "" },
                { "", "", " ", "   ", " ", "  ", "" },
                { "", "", "=", "   ", "=", "  ", "" },
                { "", "", " ", "   ", " ", "  ", "" },
                { "", "", " ", "   ", " ", "  ", "" },
                { "", "", "", "", "", "", "" }
        }, null));
        public static final Style BOLD_BLANK = new Style(new String[][] {
                { "", "", "", "", "", "", "" },
                { "", "", "", "", "", "", "" },
                { "", "", " ", "   ", " ", "  ", "" },
                { "", "", "", "", "", "", "" },
                { "", "", " ", "   ", " ", "  ", "" },
                { "", "", "", "", "", "", "" },
                { "", "", "", "", "", "", "" }
        }, new Style(new String[][] {
                { "", "", "", "", "", "", "" },
                { "", "", "", "", "", "", "" },
                { "", "", " ", "   ", " ", "  ", "" },
                { "", "", "", "", "", "", "" },
                { "", "", " ", "   ", " ", "  ", "" },
                { "", "", " ", "   ", " ", "  ", "" },
                { "", "", "", "", "", "", "" }
        }, null)).withColoredLabels(Attribute.BOLD());
        public static final Style LINES = new Style(new String[][] {
                { "┌─", "┌─", "─", "─┬─", "─", "─┬─", "─┐" },
                { "┌─", "┌─", "─", "─┬─", "─", "─┬─", "─┐" },
                { "│ ", "│ ", " ", " │ ", " ", " │ ", " │" },
                { "├─", "├─", "─", "─┼─", "─", "─┼─", "─┤" },
                { "│ ", "│ ", " ", " │ ", " ", " │ ", " │" },
                { "├─", "├─", "─", "─┼─", "─", "─┼─", "─┤" },
                { "└─", "└─", "─", "─┴─", "─", "─┴─", "─┘" }
        }, new Style(new String[][] {
                { "+-", "+-", "-", "-+-", "-", "-+-", "-+" },
                { "+-", "+-", "-", "-+-", "-", "-+-", "-+" },
                { "| ", "| ", " ", " | ", " ", " | ", " |" },
                { "+-", "+-", "-", "-+-", "-", "-+-", "-+" },
                { "| ", "| ", " ", " | ", " ", " | ", " |" },
                { "+-", "+-", "-", "-+-", "-", "-+-", "-+" },
                { "+-", "+-", "-", "-+-", "-", "-+-", "-+" }
        }, null));
        public static final Style LINES_NO_BORDER = new Style(new String[][] {
                { "", "", "", "", "", "", "" },
                { "", "", "─", "─┬─", "─", "─┬─", "" },
                { "", "", " ", " │ ", " ", " │ ", "" },
                { "", "", "─", "─┼─", "─", "─┼─", "" },
                { "", "", " ", " │ ", " ", " │ ", "" },
                { "", "", "─", "─┼─", "─", "─┼─", "" },
                { "", "", "", "", "", "", "" }
        }, new Style(new String[][] {
                { "", "", "", "", "", "", "" },
                { "", "", "-", "-+-", "-", "-+-", "" },
                { "", "", " ", " | ", " ", " | ", "" },
                { "", "", "-", "-+-", "-", "-+-", "" },
                { "", "", " ", " | ", " ", " | ", "" },
                { "", "", "-", "-+-", "-", "-+-", "" },
                { "", "", "", "", "", "", "" }
        }, null));
        public static final Style DOUBLE_LINES_AFTER_LABELS = new Style(new String[][] {
                { "┌─", "┌─", "─", "─╥─", "─", "─┬─", "─┐" },
                { "┌─", "┌─", "─", "─╥─", "─", "─┬─", "─┐" },
                { "│ ", "│ ", " ", " ║ ", " ", " │ ", " │" },
                { "╞═", "╞═", "═", "═╬═", "═", "═╪═", "═╡" },
                { "│ ", "│ ", " ", " ║ ", " ", " │ ", " │" },
                { "├─", "├─", "─", "─╫─", "─", "─┼─", "─┤" },
                { "└─", "└─", "─", "─╨─", "─", "─┴─", "─┘" }
        }, new Style(new String[][] {
                { "+-", "+-", "-", "-++-", "-", "-+-", "-+" },
                { "+-", "+-", "-", "-++-", "-", "-+-", "-+" },
                { "| ", "| ", " ", " || ", " ", " | ", " |" },
                { "+=", "+=", "=", "=++=", "=", "=+=", "=+" },
                { "| ", "| ", " ", " || ", " ", " | ", " |" },
                { "+-", "+-", "-", "-++-", "-", "-+-", "-+" },
                { "+-", "+-", "-", "-++-", "-", "-+-", "-+" }
        }, null));
        public static final Style DOUBLE_LINES_AFTER_LABELS_NO_BORDER = new Style(new String[][] {
                { "", "", "", "", "", "", "" },
                { "", "", "─", "─╥─", "─", "─┬─", "" },
                { "", "", " ", " ║ ", " ", " │ ", "" },
                { "", "", "═", "═╬═", "═", "═╪═", "" },
                { "", "", " ", " ║ ", " ", " │ ", "" },
                { "", "", "─", "─╫─", "─", "─┼─", "" },
                { "", "", "", "", "", "", "" }
        }, new Style(new String[][] {
                { "", "", "", "", "", "", "" },
                { "", "", "-", "-++-", "-", "-+-", "" },
                { "", "", " ", " || ", " ", " | ", "" },
                { "", "", "=", "=++=", "=", "=+=", "" },
                { "", "", " ", " || ", " ", " | ", "" },
                { "", "", "-", "-++-", "-", "-+-", "" },
                { "", "", "", "", "", "", "" }
        }, null));
        public static final Style DOUBLE_LINES = new Style(new String[][] {
                { "╔═", "╔═", "═", "═╦═", "═", "═╦═", "═╗" },
                { "╔═", "╔═", "═", "═╦═", "═", "═╦═", "═╗" },
                { "║ ", "║ ", " ", " ║ ", " ", " ║ ", " ║" },
                { "╠═", "╠═", "═", "═╬═", "═", "═╬═", "═╣" },
                { "║ ", "║ ", " ", " ║ ", " ", " ║ ", " ║" },
                { "╠═", "╠═", "═", "═╬═", "═", "═╬═", "═╣" },
                { "╚═", "╚═", "═", "═╩═", "═", "═╩═", "═╝" }
        }, new Style(new String[][] {
                { "++=", "++=", "=", "=++=", "=", "=++=", "=++" },
                { "++=", "++=", "=", "=++=", "=", "=++=", "=++" },
                { "|| ", "|| ", " ", " || ", " ", " || ", " ||" },
                { "++=", "++=", "=", "=++=", "=", "=++=", "=++" },
                { "|| ", "|| ", " ", " || ", " ", " || ", " ||" },
                { "++=", "++=", "=", "=++=", "=", "=++=", "=++" },
                { "++=", "++=", "=", "=++=", "=", "=++=", "=++" }
        }, null));
        public static final Style DOUBLE_LINES_NO_BORDER = new Style(new String[][] {
                { "", "", "", "", "", "", "" },
                { "", "", "═", "═╦═", "═", "═╦═", "" },
                { "", "", " ", " ║ ", " ", " ║ ", "" },
                { "", "", "═", "═╬═", "═", "═╬═", "" },
                { "", "", " ", " ║ ", " ", " ║ ", "" },
                { "", "", "═", "═╬═", "═", "═╬═", "" },
                { "", "", "", "", "", "", "" }
        }, new Style(new String[][] {
                { "", "", "", "", "", "", "" },
                { "", "", "=", "=++=", "=", "=++=", "" },
                { "", "", " ", " || ", " ", " || ", "" },
                { "", "", "=", "=++=", "=", "=++=", "" },
                { "", "", " ", " || ", " ", " || ", "" },
                { "", "", "=", "=++=", "=", "=++=", "" },
                { "", "", "", "", "", "", "" }
        }, null));


        private final String[][][] style = new String[COUNT][COUNT][];
        private final Style ascii;

        public Style(String[][] style, @Nullable Style ascii) {
            if(Arguments.checkNull(style, "style").length != COUNT)
                throw new IllegalArgumentException("style.length != "+COUNT);
            for(int i=0; i<style.length; i++) {
                if(Arguments.deepCheckNull(style[i], "style["+i+"]").length != COUNT)
                    throw new IllegalArgumentException("style["+i+"].length != "+COUNT);

                for(int j=0; j<style[i].length; j++)
                    this.style[i][j] = style[i][j].lines().toArray(String[]::new);

                int height = Mathf.max(this.style[i], ls -> ls.length);
                for(int j=0; j<style[i].length; j++) {
                    if(this.style[i][j].length == height) continue;
                    String[] extended = new String[height];
                    System.arraycopy(this.style[i][j], 0, extended, 0, this.style[i][j].length);
                    String filler = this.style[i][j].length == 0 ? "" : Utils.blank(length(extended[0]));
                    Arrays.fill(extended, this.style[i][j].length, extended.length, filler);
                    this.style[i][j] = extended;
                }
            }
//            for(int j=0; j<style[0].length; j++)
//                for(int i=1; i<style.length; i++)
//                    if(style[i][j].length() != style[0][j].length())
//                        throw new IllegalArgumentException("Widths of column "+i+" are not equal in all rows");

            this.ascii = ascii != null ? ascii : this;
        }

        private Style(String[][][] style, @Nullable Style ascii) {
            for(int i=0; i<COUNT; i++) for(int j=0; j<COUNT; j++)
                this.style[i][j] = style[i][j].clone();
            this.ascii = ascii != null ? ascii : this;
        }

        public Style withColoredLabels(Attribute... attributes) {
            if(attributes.length == 0) return this;

            String formatted = Ansi.colorize("_ABC_", attributes);
            String beginFormat = formatted.substring(0, formatted.indexOf("_ABC_"));
            String endFormat = formatted.substring(formatted.indexOf("_ABC_") + 5);

            String[][][] style = new String[COUNT][COUNT][];
            for(int i=0; i<COUNT; i++) for(int j=0; j<COUNT; j++)
                style[i][j] = this.style[i][j].clone();

            addColors(style[IN_LABEL][LEFT], "", beginFormat);
            addColors(style[IN_LABEL][BEFORE_LABEL], "", beginFormat);
            addColors(style[IN_LABEL][AFTER_LABEL], endFormat, beginFormat);
            addColors(style[IN_LABEL][BETWEEN_CELLS], endFormat, beginFormat);
            addColors(style[IN_LABEL][RIGHT], endFormat, "");
            addColors(style[IN_CELL][BEFORE_LABEL], "", beginFormat);
            addColors(style[IN_CELL][AFTER_LABEL], endFormat, "");

            return new Style(style, ascii == this ? null : ascii.withColoredLabels(attributes));
        }

        private static void addColors(String[] lines, String prefix, String postfix) {
            for(int i=0; i<lines.length; i++)
                lines[i] = prefix + lines[i] + postfix;
        }
    }

    public static void main(String[] args) {
        TableRenderer table = new TableRenderer()
                .title("Title")
                .rowLabels("Row 1", "Row 2", "Row 3")
                .columnLabels("Label1", "Label2")
                .addRow("Val 1", "Val 2")
                .addRow("A very long cell content which is way too long for a single line and should be wrapped multiple times...", "Row 2.2")
                .addRow("Val 3", "A very long cell content which is way too long for a single line and should be wrapped multiple times...")
                .maxCellWidths(15)
                .maxRowLabelWidth(10)
                .identicalCellWidths(true)
                .alignment(Alignment.RIGHT, Alignment.CENTER);
        ;
        for(Style style : new Style[] { Style.BLANK, Style.COMPACT_BLANK, Style.BOLD_BLANK, Style.LINES, Style.LINES_NO_BORDER, Style.DOUBLE_LINES_AFTER_LABELS, Style.DOUBLE_LINES_AFTER_LABELS_NO_BORDER, Style.DOUBLE_LINES, Style.DOUBLE_LINES_NO_BORDER }) {
            System.out.println(table.useUnicodeSymbols(true).style(style.withColoredLabels(Attribute.BOLD())) + "\n\n");
            System.out.println(table.useUnicodeSymbols(false).style(style.withColoredLabels(Attribute.BOLD())) + "\n\n");
        }
    }
}
