package xyz.exemplator.exemplator.data;

import com.sun.org.apache.bcel.internal.classfile.Code;
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
import java.util.stream.Collectors;

public class CodeSearch implements ICodeSearch {
    private static String SEARCHCODE_API_URL = "https://searchcode.com/api/codesearch_I/?q=";
    private static String RAW_CODE_URL = "https://searchcode.com/api/result/";

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public List<CodeSample> fetch(List<String> searchTerms, int page) throws HttpException {
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
        List<CodeSample> codeSamples = new ArrayList<>();
        codeSamplesJson.entrySet().stream()
                .forEach(entry -> {
                    CodeSample codeSample = entry.getValue();
//                    String rawResponse = httpRequest.getRequest(RAW_CODE_URL + entry.getKey().get("id"));
                    String rawResponse = httpRequest.getRequest(entry.getValue().getUrl());
                    if (rawResponse != null) {
                        codeSample.setCodeSnippet(rawResponse);
                        InputStream stream = new ByteArrayInputStream(rawResponse.getBytes(StandardCharsets.UTF_8));
                        codeSample.setCodeInputStream(stream);
                        codeSamples.add(codeSample);
                    }
                });
        Collections.sort(codeSamples);
        return codeSamples;
    }

    private Map<JSONObject, CodeSample> parseJson(String jsonString) {
        JSONParser parser = new JSONParser();
        try {
            Object jsonObj = parser.parse(jsonString);
            JSONObject jsonObject = (JSONObject) jsonObj;

            JavaCodeRater javaCodeRater = new JavaCodeRater();

            Map<JSONObject, CodeSample> codeSampleMap = new HashMap<>();
            JSONArray codeSamples = (JSONArray) jsonObject.get("results");
            codeSamples.stream().forEach(codeSample ->
                    codeSampleMap.put((JSONObject) codeSample, javaCodeRater.rateJavaCodeSample((JSONObject) codeSample)));

            return codeSampleMap;
        } catch (ParseException e) {
            logger.error("Unable to parse json", e);
        }

        return null;
    }

    /**
     * github = 2, java = 23
     */
    private String createQuery(List<String> searchTerms, int page, int vcs, int language) {
        if (searchTerms != null && !searchTerms.isEmpty()) {
            String searchString = searchTerms.stream()
                    .collect(Collectors.joining("+"));
            return SEARCHCODE_API_URL + searchString + "&p=" + page + "&src=" + vcs + "&lan=" + language;
        }

        return null;
    }
}
