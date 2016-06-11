package xyz.exemplator.exemplator.parser.java;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.TypeDeclaration;
import org.jooq.lambda.function.Function3;

import java.util.Optional;
import java.util.function.BiPredicate;

/**
 * @author LeanderK
 * @version 1.0
 */
public class Command<X extends Node> {
    private final Class<X> target;
    private final BiPredicate<TypeDeclaration, Boolean> identifierOnlyValid;
    private final Function3<X, Boolean, Boolean, Boolean> checkToken;
    private final String className;
    private final String packageName;

    public Command(Class<X> target,
                   BiPredicate<TypeDeclaration, Boolean> identifierOnlyValid,
                   Function3<X, Boolean, Boolean, Boolean> checkToken, String className, String packageName) {
        this.target = target;
        this.identifierOnlyValid = identifierOnlyValid;
        this.checkToken = checkToken;
        this.className = className;
        this.packageName = packageName;
    }

    public Class<X> getTarget() {
        return target;
    }

    public BiPredicate<TypeDeclaration, Boolean> getIdentifierOnlyValid() {
        return identifierOnlyValid;
    }

    public Function3<X, Boolean, Boolean, Boolean> getCheckToken() {
        return checkToken;
    }

    public Optional<String> getClassName() {
        return Optional.ofNullable(className);
    }

    public Optional<String> getPackageName() {
        return Optional.ofNullable(packageName);
    }
}
