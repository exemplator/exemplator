package xyz.exemplator.exemplator;

import java.util.List;

/**
 * @author LeanderK
 * @version 1.0
 */
public class Response {
    List<Occurrence> occurrences;
    int startPage;
    int endPage;

    public Response(List<Occurrence> occurrences, int startPage, int endPage) {
        this.occurrences = occurrences;
        this.startPage = startPage;
        this.endPage = endPage;
    }

    public List<Occurrence> getOccurrences() {
        return occurrences;
    }

    static class Occurrence {
        private String rawUrl;
        private String userUrl;
        private String code;
        private List<Selection> selections;

        public Occurrence(String rawUrl, String userUrl, String code, List<Selection> selections) {
            this.rawUrl = rawUrl;
            this.userUrl = userUrl;
            this.code = code;
            this.selections = selections;
        }

        public List<Selection> getSelections() {
            return selections;
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

    static class Selection {
        private Position start;
        private Position end;

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
