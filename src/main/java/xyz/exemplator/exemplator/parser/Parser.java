package xyz.exemplator.exemplator.parser;

import xyz.exemplator.exemplator.parser.java.Command;

import java.util.List;

/**
 * @author LeanderK
 * @version 1.0
 */
public interface Parser {
    List<Selection> getMatches(Command command);
}
