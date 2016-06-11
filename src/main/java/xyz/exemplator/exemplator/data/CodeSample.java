package xyz.exemplator.exemplator.data;

import org.json.simple.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class CodeSample implements Comparable<CodeSample> {
    private String rawUrl;
    private String userUrl;
    private int stars = 0;
    private InputStream codeInputStream;
    private String codeSnippet;

    public String getCodeSnippet() {
        return codeSnippet;
    }

    public void setCodeSnippet(String codeSnippet) {
        this.codeSnippet = codeSnippet;
    }

    private String userName;
    private String repository;

    public CodeSample(int stars) {
        this.stars = stars;
    }

    public CodeSample(String userName, String repository) {
        this.userName = userName;
        this.repository = repository;
    }

    public String getUrl() {
        return rawUrl;
    }

    public InputStream getCodeInputStream() {
        return codeInputStream;
    }

    public int getStars() {
        return stars;
    }

    public void setUrl(String url) {
        this.rawUrl = url;
    }

    public void setStars(int stars) {
        this.stars = stars;
    }

    public void setCodeInputStream(InputStream codeInputStream) {
        this.codeInputStream = codeInputStream;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public String getRawUrl() {
        return rawUrl;
    }

    public void setRawUrl(String rawUrl) {
        this.rawUrl = rawUrl;
    }

    public String getUserUrl() {
        return userUrl;
    }

    public void setUserUrl(String userUrl) {
        this.userUrl = userUrl;
    }

    public CodeSample fetchRawCode() {
        if (rawUrl != null) {
            HTTPRequest httpRequest = new HTTPRequest();
            String rawResponse = httpRequest.getRequest(rawUrl);
            if (rawResponse != null) {
                setCodeSnippet(rawResponse);
                InputStream stream = new ByteArrayInputStream(rawResponse.getBytes(StandardCharsets.UTF_8));
                setCodeInputStream(stream);
                return this;
            }
        }

        return null;
    }

    @Override
    public int compareTo(CodeSample o) {
        return o.getStars() - stars;
    }
}
