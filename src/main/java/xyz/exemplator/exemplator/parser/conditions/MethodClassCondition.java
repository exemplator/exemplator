package xyz.exemplator.exemplator.parser.conditions;

/**
 * @author LeanderK
 * @version 1.0
 */
public class MethodClassCondition implements Condition {
    private final String fullClassName;
    private final String methodName;

    public MethodClassCondition(String fullClassName, String methodName) {
        this.fullClassName = fullClassName;
        this.methodName = methodName;
    }

    public String getFullClassName() {
        return fullClassName;
    }

    public String getMethodName() {
        return methodName;
    }
}
