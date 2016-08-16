package xyz.exemplator.exemplator.parser.java;

/**
 * @author LeanderK
 * @version 1.0
 */
public class LeftQualifier {
    private final boolean isSameType;
    private final String name;

    public LeftQualifier(boolean isSameType, String name) {
        this.isSameType = isSameType;
        this.name = name;
    }

    public boolean isSameType() {
        return isSameType;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LeftQualifier)) return false;

        LeftQualifier that = (LeftQualifier) o;

        return name.equals(that.name);

    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
