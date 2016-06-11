package xyz.exemplator.exemplator.data;

import com.github.benmanes.caffeine.cache.stats.ConcurrentStatsCounter;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class SnippetFetcher {

    private static String GITHUB_API_URL = "https://api.github.com/search/code?q=";

    public void fetch(String url) {
        url = GITHUB_API_URL + "addClass+in:file+language:js";

        CloseableHttpAsyncClient httpclient = HttpAsyncClients.createDefault();
        try {
            // Start the client
            httpclient.start();

            // Execute request
            final HttpGet request1 = new HttpGet(url);
            Future<HttpResponse> future = httpclient.execute(request1, null);
            // and wait until a response is received
            HttpResponse response1 = future.get();
            System.out.println(request1.getRequestLine() + "->" + response1.getStatusLine());

            // One most likely would want to use a callback for operation result
            final CountDownLatch latch1 = new CountDownLatch(1);
            final HttpGet request2 = new HttpGet(url);
            httpclient.execute(request2, new FutureCallback<HttpResponse>() {

                public void completed(final HttpResponse response2) {
                    latch1.countDown();
                    System.out.println(request2.getRequestLine() + "->" + response2.getStatusLine());
                }

                public void failed(final Exception ex) {
                    latch1.countDown();
                    System.out.println(request2.getRequestLine() + "->" + ex);
                }

                public void cancelled() {
                    latch1.countDown();
                    System.out.println(request2.getRequestLine() + " cancelled");
                }

            });
            latch1.await();
        } catch (InterruptedException | ExecutionException e) {

        }
    }
}
