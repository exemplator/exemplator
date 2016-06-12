package xyz.exemplator.exemplator.parser.java;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.TypeDeclaration;

import java.util.Optional;
import java.util.function.BiPredicate;

/**
 * @author LeanderK
 * @version 1.0
 */
public class Command<X extends Node> {
    private final Class<X> target;
    private final BiPredicate<TypeDeclaration, Boolean> identifierOnlyValid;
    private final String className;
    private final String packageName;
    private final String methodName;

    public Command(Class<X> target,
                   BiPredicate<TypeDeclaration, Boolean> identifierOnlyValid,
                   String className, String packageName, String methodName) {
        this.target = target;
        this.identifierOnlyValid = identifierOnlyValid;
        this.className = className;
        this.packageName = packageName;
        this.methodName = methodName;
    }

    public Class<X> getTarget() {
        return target;
    }

    public BiPredicate<TypeDeclaration, Boolean> getIdentifierOnlyValid() {
        return identifierOnlyValid;
    }

    public Optional<String> getClassName() {
        return Optional.ofNullable(className);
    }

    public Optional<String> getPackageName() {
        return Optional.ofNullable(packageName);
    }

    public Optional<String> getMethodName() {
        return Optional.ofNullable(methodName);
    }
}
