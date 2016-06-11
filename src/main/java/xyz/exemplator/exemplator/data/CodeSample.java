package xyz.exemplator.exemplator.data;

import org.json.simple.JSONObject;

import java.io.InputStream;

public class CodeSample implements Comparable<CodeSample> {
    private String url;
    private int stars;
    private InputStream codeInputStream;
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
        return url;
    }

    public InputStream getCodeInputStream() {
        return codeInputStream;
    }

    public int getStars() {
        return stars;
    }

    public void setUrl(String url) {
        this.url = url;
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

    @Override
    public int compareTo(CodeSample o) {
        return o.getStars() - stars;
    }
}
