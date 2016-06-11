package xyz.exemplator.exemplator.parser.conditions;

/**
 * @author LeanderK
 * @version 1.0
 */
public class ClassCondition implements Condition {
    private final String fullClassName;
    private final Boolean usage;

    public ClassCondition(String fullClassName, Boolean usage) {
        this.fullClassName = fullClassName;
        this.usage = usage;
    }

    public String getFullClassName() {
        return fullClassName;
    }

    public Boolean getUsage() {
        return usage;
    }
}
