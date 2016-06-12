package xyz.exemplator.exemplator;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

/**
 * @author LeanderK
 * @version 1.0
 */
public class Request {
    private final String packageName;
    private final String className;
    private final String methodName;
    private final String token;
    private final int page;

    public Request(@JsonProperty("package")String packageName,
                   @JsonProperty("class")String className,
                   @JsonProperty("method")String methodName,
                   @JsonProperty("token")String token,
                   @JsonProperty("page")int page) {
        if (packageName != null && packageName.isEmpty()) {
            this.packageName = null;
        } else {
            this.packageName = packageName;
        }

        if (className != null && className.isEmpty()) {
            this.className = null;
        } else {
            this.className = className;
        }

        if (methodName != null && methodName.isEmpty()) {
            this.methodName = null;
        } else {
            this.methodName = methodName;
        }

        if (token != null && token.isEmpty()) {
            this.token = null;
        } else {
            this.token = token;
        }
        int actualPage = 0;
        if (page != -1 ) {
            actualPage = page;
        }
        this.page = actualPage;
    }

    public Optional<String> getClassName() {
        return Optional.ofNullable(className);
    }

    public Optional<String> getMethodName() {
        return Optional.ofNullable(methodName);
    }

    public Optional<String> getPackageName() {
        return Optional.ofNullable(packageName);
    }

    public Optional<String> getToken() {
        return Optional.ofNullable(token);
    }

    public int getPage() {
        return page;
    }
}
