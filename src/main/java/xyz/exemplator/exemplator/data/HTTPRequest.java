package xyz.exemplator.exemplator.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

class HTTPRequest {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    CompletableFuture<String> getRequest(String url, ExecutorService executorService) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URL obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                    response.append("\n");
                }
                in.close();

                return response.toString();
            } catch (IOException e) {
                // error, not printing to console to be faster, also this happens a lot because
                // git stargazers cannot be fetched on all repos
            }

            return null;
        }, executorService);
    }
}
