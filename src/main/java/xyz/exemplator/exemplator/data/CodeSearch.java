package xyz.exemplator.exemplator.data;

import org.apache.http.HttpException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

public class CodeSearch implements ICodeSearch {
    private static String SEARCHCODE_API_URL = "https://searchcode.com/api/codesearch_I/?q=";
    private static String LANG_JAVA = "java";
    private static String GITHUB_REPO_URL = "https://api.github.com/search/repositories?q=";
    private static String GITHUB_RAW_URL = "https://raw.githubusercontent.com/";
    private static String GITHUB_URL = "https://github.com/";

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public List<CompletableFuture<Optional<CodeSample>>> fetch(List<String> searchTerms, int page,
                                                               ExecutorService executorService) throws HttpException {
        HTTPRequest httpRequest = new HTTPRequest();
        String url = createQuery(searchTerms, page, 2, 23);
        if (url == null) {
            throw new HttpException("Url is null");
        }

        String response = httpRequest.getRequest(url);
        Map<JSONObject, CodeSample> codeSamplesJson;
        if (response != null) {
            codeSamplesJson = parseJson(response);
        } else {
            throw new HttpException("Unable to query url: " + url);
        }

        // Get raw code from github
        Set<CodeSample> codeSamples = new HashSet<>();
        codeSamplesJson.entrySet().stream()
                .forEach(entry -> codeSamples.add(entry.getValue()));

        return codeSamples.stream().map(codeSample -> {
            CompletableFuture<Optional<CodeSample>> futureCode = CompletableFuture.supplyAsync(() -> fetchRawCode(codeSample), executorService);
            //CompletableFuture<Optional<CodeSample>> futureGit = CompletableFuture.supplyAsync(() -> fetchGithubRating(codeSample), executorService);
            //return CompletableFuture.allOf(futureCode, futureGit).thenApply(v -> futureCode.join().flatMap(ignore -> futureGit.join()));
            return CompletableFuture.allOf(futureCode).thenApply(v -> futureCode.join());
        }).collect(Collectors.toList());
    }

    private Optional<CodeSample> fetchRawCode(CodeSample codeSample) {
        if (codeSample.getRawUrl() != null) {
            HTTPRequest httpRequest = new HTTPRequest();
            String rawResponse = httpRequest.getRequest(codeSample.getUrl());
            if (rawResponse != null) {
                rawResponse = rawResponse.replace("    ", "  ");
                rawResponse = rawResponse.trim();
                codeSample.setCode(rawResponse);
                InputStream stream = new ByteArrayInputStream(rawResponse.getBytes(StandardCharsets.UTF_8));
                codeSample.setCodeInputStream(stream);
                return Optional.of(codeSample);
            }
        }

        return Optional.empty();
    }

    private Optional<CodeSample> fetchGithubRating(CodeSample codeSample) {
        HTTPRequest httpRequest = new HTTPRequest();
        String response = httpRequest.getRequest(GITHUB_REPO_URL + codeSample.getRepository() + "+user:" + codeSample.getUserName());
        JSONParser parser = new JSONParser();

        if (response == null) {
            // Not supported by github
            codeSample.setStars(-1);
            return Optional.of(codeSample);
        }

        try {
            Object jsonObj = parser.parse(response);
            JSONObject jsonObject = (JSONObject) jsonObj;
            JSONArray items = (JSONArray) jsonObject.get("items");
            Optional<Integer> starCountOptional = items.stream()
                    .filter(item -> {
                        JSONObject obj = (JSONObject) item;
                        String name = (String) obj.get("name");
                        return name.toLowerCase().equals(codeSample.getRepository().toLowerCase());
                    })
                    .findFirst()
                    .map(item -> {
                        JSONObject obj = (JSONObject) item;
                        return (int) (long) obj.get("stargazers_count");
                    });

            if (starCountOptional.isPresent()) {
                codeSample.setStars(starCountOptional.get());
                return Optional.of(codeSample);
            }
        } catch (ParseException e) {
            logger.error("Unable to parse json", e);
        }

        // Not supported by github
        codeSample.setStars(-1);
        return Optional.of(codeSample);
    }

    private Map<JSONObject, CodeSample> parseJson(String jsonString) {
        JSONParser parser = new JSONParser();
        try {
            Object jsonObj = parser.parse(jsonString);
            JSONObject jsonObject = (JSONObject) jsonObj;

            Map<JSONObject, CodeSample> codeSampleMap = new HashMap<>();
            JSONArray codeSamples = (JSONArray) jsonObject.get("results");
            codeSamples.stream().forEach(codeSample ->
                    codeSampleMap.put((JSONObject) codeSample, initCodeSample((JSONObject) codeSample)));

            return codeSampleMap;
        } catch (ParseException e) {
            logger.error("Unable to parse json", e);
        }

        return null;
    }

    private CodeSample initCodeSample(JSONObject codeSample)  {
        String language = (String) codeSample.get("language");

        // Make sure type is set to java
        if (language == null || !language.toLowerCase().equals(LANG_JAVA)) {
            return null;
        }

        String filename = (String) codeSample.get("filename");

        // Make sure filename ends in .java
        if (filename != null) {
            String[] nameParts = filename.split("\\.");
            if (!nameParts[nameParts.length - 1].toLowerCase().equals(LANG_JAVA)) {
                return null;
            }
        } else {
            return null;
        }

        String gitRepo = (String) codeSample.get("repo");
        String location = (String) codeSample.get("location");
        String[] gitData = gitRepo.substring(0, gitRepo.length() - 4).split("/");

        String user = gitData[3];
        String repo = gitData[4];

        String rawURL = GITHUB_RAW_URL + user + "/" + repo + "/master" + location + "/" + filename;
        String fileURL = GITHUB_URL + user + "/" + repo + "/blob/master" + location + "/" + filename;

        CodeSample codeSampleObj = new CodeSample(user, repo);

        codeSampleObj.setUserUrl(gitRepo);
        codeSampleObj.setRawUrl(rawURL);
        codeSampleObj.setFileUrl(fileURL);
        return codeSampleObj;
    }

    /**
     * github = 2, java = 23
     */
    private String createQuery(List<String> searchTerms, int page, int vcs, int language) {
        if (searchTerms != null && !searchTerms.isEmpty()) {
            String searchString = searchTerms.stream()
                    .map(String::trim)
                    .collect(Collectors.joining("+"));
            return SEARCHCODE_API_URL + searchString + "&p=" + page + "&src=" + vcs + "&lan=" + language;
        }

        return null;
    }
}
