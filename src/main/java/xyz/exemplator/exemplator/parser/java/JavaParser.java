package xyz.exemplator.exemplator.parser.java;

import com.github.javaparser.ParseException;
import com.github.javaparser.TokenMgrError;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.QualifiedNameExpr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.exemplator.exemplator.parser.Parser;
import xyz.exemplator.exemplator.parser.Selection;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author LeanderK
 * @version 1.0
 */
public class JavaParser implements Parser {
    private static final Logger logger = LoggerFactory.getLogger(JavaParser.class);
    private final CompilationUnit cu;

    public JavaParser(CompilationUnit cu) {
        this.cu = cu;
    }

    public static Optional<Parser> of(InputStream inputStream) {
        CompilationUnit cu;
        try {
            // parse the file
            cu = com.github.javaparser.JavaParser.parse(inputStream);
        } catch (ParseException | TokenMgrError e) {
            logger.error("Unable to parse input", e);
            return Optional.empty();
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                logger.error("Unable to close input", e);
            }
        }
        return Optional.of(new JavaParser(cu));
    }

    @Override
    public List<Selection> getMatches(Command command) {
        Predicate<ImportDeclaration> isMentioned = importDeclaration -> {
            if (importDeclaration.isAsterisk()) {
                if (command.getPackageName().isPresent()) {
                    return command.getPackageName().get().startsWith(importDeclaration.getName().toString());
                } else {
                    return true;
                }
            } else {
                if (command.getPackageName().isPresent()) {
                    return importDeclaration.getName().toString().equals(command.getPackageName().get() + "." + command.getClassName().get());
                } else if (command.getClassName().isPresent()) {
                    return importDeclaration.getName().toString().endsWith(command.getClassName().get());
                } else {
                    return false;
                }
            }
        };

        boolean imported = cu.getImports().stream()
                .filter(importDeclaration -> !importDeclaration.isStatic())
                .filter(isMentioned)
                .findAny()
                .isPresent();

        boolean staticImported = cu.getImports().stream()
                .filter(ImportDeclaration::isStatic)
                .filter(isMentioned)
                .findAny()
                .isPresent();

        return cu.getTypes().stream()
                .map(type -> new UsageFinder(cu, type, command, imported, staticImported, new ArrayList<>()))
                .map(UsageFinder::checkTypeForUsages)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }
}
