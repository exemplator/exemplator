package xyz.exemplator.exemplator;

import io.netty.util.concurrent.PromiseCombiner;
import ratpack.exec.Blocking;
import ratpack.exec.Promise;
import ratpack.jackson.Jackson;
import ratpack.server.RatpackServer;
import ratpack.server.ServerConfig;
import xyz.exemplator.exemplator.data.CodeSample;
import xyz.exemplator.exemplator.data.ICodeSearch;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.sun.javafx.tools.resource.DeployResource.Type.data;
import static java.util.stream.Collectors.toList;
import static ratpack.jackson.Jackson.fromJson;

/**
 * @author LeanderK
 * @version 1.0
 */
public class Router {
    private final int port;
    private final ICodeSearch codeSearch;
    private ExecutorService executorService = Executors.newCachedThreadPool();

    public Router(int port, ICodeSearch codeSearch) {
        this.port = port;
        this.codeSearch = codeSearch;
    }

    public void init() throws Exception {
        RatpackServer.start(server -> server
                .serverConfig(ServerConfig.embedded().port(port))
                .handlers(chain ->
                        chain.all(ctx -> {
                            ctx.getResponse().getHeaders().add("access-control-allow-origin", "*");
                            ctx.getResponse().getHeaders().add("access-control-allow-methods", "GET,PUT,POST,PATCH,DELETE,OPTIONS");
                            ctx.getResponse().getHeaders().add("access-control-allow-credentials", "true");
                            ctx.getResponse().getHeaders().add("access-control-allow-headers", "Authorization,Content-Type");
                            ctx.getResponse().getHeaders().add("access-control-expose-headers", "Link,Location");
                            ctx.getResponse().getHeaders().add("access-control-max-age", "86400");
                            ctx.next();
                        })
                        .post("search", ctx -> {
                            if (!ctx.getRequest().getContentType().isJson()) {
                                ctx.getResponse().status(500);
                                ctx.render("Expected Content-Type: application/json");
                                return;
                            }
                            ctx.render(ctx.parse(fromJson(Request.class))
                                    .flatMap(request -> {
                                        if (!request.getToken().isPresent()
                                                && !request.getClassName().isPresent() && !request.getMethodName().isPresent()) {
                                            throw new IllegalArgumentException("token, methodName or classname must be present");
                                        }
                                        List<String> searchTerms = Stream.of(request.getClassName(), request.getMethodName(), request.getPackageName(), request.getToken())
                                                .filter(Optional::isPresent)
                                                .map(Optional::get)
                                                .collect(toList());
                                        return Blocking.get(() -> codeSearch.fetch(searchTerms, request.getPage()));
                                    })
                                    .map(codeSamples -> codeSamples.stream()
                                            .map(sample -> CompletableFuture.supplyAsync(() -> codeSearch.rawFetch(sample), executorService)
                                                    .thenApply(result -> {
                                                        Response.Position position = new Response.Position(1, 1);
                                                        List<Response.Position> positions = new ArrayList<>();
                                                        positions.add(position);
                                                        return new Response.Occurrence(result.getRawUrl(), result.getUserUrl(), result.getCodeSnippet(), positions);
                                                    })
                                            )
                                            .collect(Collectors.toList())
                                    )
                                    .map(futures ->
                                            CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]))
                                                    .thenApply(v -> futures.stream()
                                                            .map(CompletableFuture::join)
                                                            .collect(toList())
                                                    )
                                    )
                                    .flatMap((ratpack.func.Function<CompletableFuture<List<Response.Occurrence>>, Promise<List<Response.Occurrence>>>)future ->
                                            Promise.async(downstream -> downstream.accept(future)))
                                    .map(Response::new)
                                    .map(Jackson::json));
                        })
                )
        );
    }
}
