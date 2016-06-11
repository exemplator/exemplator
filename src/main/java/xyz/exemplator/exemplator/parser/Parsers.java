package xyz.exemplator.exemplator.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.exemplator.exemplator.parser.java.JavaParser;

import java.io.InputStream;
import java.util.Optional;

/**
 * @author LeanderK
 * @version 1.0
 */
public class Parsers {
    private static Logger logger = LoggerFactory.getLogger(Parsers.class);

    public static Optional<Parser> from(String language, InputStream inputStream) {
        switch (language) {
            case "JAVA": return JavaParser.of(inputStream);
            default: logger.info("no parser for language: {}", language);
                return Optional.empty();
        }
    }
}
