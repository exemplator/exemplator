package xyz.exemplator.exemplator.parser;

import com.github.javaparser.Position;

/**
 * @author LeanderK
 * @version 1.0
 */
public class Selection {
    private final Position start;
    private final Position end;

    public Selection(Position start, Position end) {
        this.start = start;
        this.end = end;
    }

    public Position getStart() {
        return start;
    }

    public Position getEnd() {
        return end;
    }
}
