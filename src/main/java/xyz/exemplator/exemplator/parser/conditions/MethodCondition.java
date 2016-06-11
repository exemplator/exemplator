package xyz.exemplator.exemplator.parser.conditions;

/**
 * @author LeanderK
 * @version 1.0
 */
public class MethodCondition implements Condition {
    private final String method;

    public MethodCondition(String method) {
        this.method = method;
    }

    public String getMethod() {
        return method;
    }
}
