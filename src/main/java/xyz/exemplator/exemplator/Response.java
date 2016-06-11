package xyz.exemplator.exemplator;

import java.util.List;

import static com.sun.tools.doclint.Entity.ge;

/**
 * @author LeanderK
 * @version 1.0
 */
public class Response {
    List<Occurrence> occurrences;

    public Response(List<Occurrence> occurrences) {
        this.occurrences = occurrences;
    }

    public List<Occurrence> getOccurrences() {
        return occurrences;
    }

    static class Occurrence {
        private String rawUrl;
        private String userUrl;
        private String code;
        private List<Position> positions;

        public Occurrence(String rawUrl, String userUrl, String code, List<Position> positions) {
            this.rawUrl = rawUrl;
            this.userUrl = userUrl;
            this.code = code;
            this.positions = positions;
        }

        public List<Position> getPositions() {
            return positions;
        }

        public String getCode() {
            return code;
        }

        public String getRawUrl() {
            return rawUrl;
        }

        public String getUserUrl() {
            return userUrl;
        }
    }

    static class Position {
        private int line;
        private int column;

        public Position(int line, int column) {
            this.line = line;
            this.column = column;
        }

        public int getColumn() {
            return column;
        }

        public int getLine() {
            return line;
        }
    }
}
