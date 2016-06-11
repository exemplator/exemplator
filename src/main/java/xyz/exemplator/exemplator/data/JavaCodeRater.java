package xyz.exemplator.exemplator.data;

import org.apache.http.protocol.HTTP;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by root on 11.06.16.
 */
class JavaCodeRater {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private static String LANG_JAVA = "java";
    private static String GITHUB_REPO_URL = "https://api.github.com/search/repositories?q=";
    private static String GITHUB_RAW_URL = "https://raw.githubusercontent.com/";

    CodeSample rateJavaCodeSample(JSONObject codeSample) {
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
        Long id = (Long) codeSample.get("id");
        String[] gitData = gitRepo.substring(0, gitRepo.length() - 4).split("/");

        String user = gitData[3];
        String repo = gitData[4];

        String rawURL = GITHUB_RAW_URL + user + "/" + repo + "/master" + location + "/" + filename;

        //CodeSample codeSampleObj = getGitRepoStars(user, repo);
        CodeSample codeSampleObj = new CodeSample(user, repo);

        codeSampleObj.setUrl(gitRepo);
        codeSampleObj.setRawUrl(rawURL);
        return codeSampleObj;
    }
}
