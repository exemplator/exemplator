package xyz.exemplator.exemplator.data;

import xyz.exemplator.exemplator.parser.Selection;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CodeSample implements Comparable<CodeSample> {
    private String rawUrl;
    private String userUrl;
    private String fileUrl;
    private int stars = 0;
    private InputStream codeInputStream;
    private String codeSnippet;
    private List<Selection> selections = new ArrayList();

    public String getCode() {
        return codeSnippet;
    }

    public void setCode(String codeSnippet) {
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

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getUserUrl() {
        return userUrl;
    }

    public void setUserUrl(String userUrl) {
        this.userUrl = userUrl;
    }

    public void setSelections(List<Selection> selections) {
        this.selections = selections;
    }

    public List<Selection> getSelections() {
        return selections;
    }

    public Map<Selection, String> getSurroundings() {
        String[] split = codeSnippet.split("\\n");

        return selections.stream()
                .collect(Collectors.toMap(Function.identity(), selection -> {
                    int start = (selection.getStart().getLine() - 1) - 10;
                    int end = (selection.getEnd().getLine() - 1) + 10;

                    int realStart = Math.min(Math.max(start, 0), split.length - 1);

                    StringBuilder builder = new StringBuilder();
                    for (int i = realStart; i <= end && i < split.length; i++) {
                        builder.append(split[i]);
                        builder.append("\n");
                    }
                    return builder.toString();
                }));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CodeSample that = (CodeSample) o;

        return rawUrl != null ? rawUrl.equals(that.rawUrl) : that.rawUrl == null;

    }

    @Override
    public int hashCode() {
        return rawUrl != null ? rawUrl.hashCode() : 0;
    }

    @Override
    public int compareTo(CodeSample o) {
        return o.getStars() - stars;
    }
}
