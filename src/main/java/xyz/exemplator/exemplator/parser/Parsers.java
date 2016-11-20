package xyz.exemplator.exemplator.parser;

import io.netty.util.internal.chmv8.ConcurrentHashMapV8;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.exemplator.exemplator.data.CodeSample;
import xyz.exemplator.exemplator.parser.java.Command;
import xyz.exemplator.exemplator.parser.java.JavaParser;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author LeanderK
 * @version 1.0
 */
public class Parsers {
    private static Logger logger = LoggerFactory.getLogger(Parsers.class);

    public static Optional<Parser> from(String language, CodeSample sample) {
        switch (language) {
            case "JAVA": return JavaParser.of(sample);
            default: logger.info("no parser for language: {}", language);
                return Optional.empty();
        }
    }
}
