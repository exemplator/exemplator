package xyz.exemplator.exemplator.data;

import xyz.exemplator.exemplator.parser.Selection;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class CodeSample implements Comparable<CodeSample> {
    private String rawUrl;
    private String userUrl;
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

    public List<String> splitCode() {
        String[] array = codeSnippet.split("\\n");

        return selections.stream().map(selection -> {
            String result = "";
            int startRow = selection.getStart().getLine();
            int endRow = selection.getEnd().getLine();
            for (int i = 0; i < array.length; i++) {
                if (i >= startRow - 10 && i <= endRow + 10) {
                    result += array[i] + "\n";
                }
            }

            return result;
        }).collect(Collectors.toList());
    }

    @Override
    public int compareTo(CodeSample o) {
        return o.getStars() - stars;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CodeSample) {
            CodeSample codeSample = (CodeSample) obj;

            List<String> codeSnippets2 = codeSample.splitCode();
            return splitCode().stream()
                    .filter(codeSnippets2::contains)
                    .collect(Collectors.toList()).size() == 0;
        }

        return false;
    }
}
