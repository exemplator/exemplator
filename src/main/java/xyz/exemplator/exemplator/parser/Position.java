package xyz.exemplator.exemplator.parser;

/**
 * @author LeanderK
 * @version 1.0
 */
public class Position {
    private final int line;
    private final int column;

    public Position(int line, int column) {
        this.line = line;
        this.column = column;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }
}
