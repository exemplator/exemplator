package xyz.exemplator.exemplator.parser.java;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.TypeDeclaration;

import java.util.Optional;
import java.util.function.BiPredicate;

/**
 * @author LeanderK
 * @version 1.0
 */
public class Command {
    private final String className;
    private final String packageName;
    private final String methodName;

    public Command(String className, String packageName, String methodName) {
        if (className != null) {
            this.className = className.trim();
        } else {
            this.className = null;
        }
        if (packageName != null) {
            this.packageName = packageName.trim();
        } else {
            this.packageName = null;
        }
        if (methodName != null) {
                this.methodName = methodName.trim();
        } else {
            this.methodName = null;
        }
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
