package xyz.exemplator.exemplator;

import com.github.javaparser.Position;
import ratpack.exec.Blocking;
import ratpack.exec.Promise;
import ratpack.jackson.Jackson;
import ratpack.server.RatpackServer;
import ratpack.server.ServerConfig;
import xyz.exemplator.exemplator.data.CodeSample;
import xyz.exemplator.exemplator.data.ICodeSearch;
import xyz.exemplator.exemplator.parser.Parsers;
import xyz.exemplator.exemplator.parser.java.Command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
                                        Command command = new Command(request.getClassName().orElse(null), request.getPackageName().orElse(null), request.getMethodName().orElse(null));
                                        return Blocking.get(() -> codeSearch.fetch(searchTerms, request.getPage(), executorService))
                                                .map(futures -> {
                                                    CompletableFuture<Optional<CodeSample>>[] parsedFutures = (CompletableFuture<Optional<CodeSample>>[]) futures.stream()
                                                            .map(future -> future.thenApply(opt ->
                                                                    opt.flatMap(sample ->
                                                                            Parsers.from("JAVA", sample.getCodeInputStream())
                                                                                    .map(parser -> parser.getMatches(command))
                                                                                    .map(matches -> {
                                                                                        sample.setSelections(matches);
                                                                                        return sample;
                                                                                    })
                                                                                    .filter(codeSample -> !codeSample.getSelections().isEmpty())

                                                                    )
                                                            ))
                                                            .toArray(size -> new CompletableFuture[futures.size()]);

                                                    return CompletableFuture.allOf(parsedFutures)
                                                            .thenApply(v -> Arrays.stream(parsedFutures)
                                                                    .map(CompletableFuture::join)
                                                                    .collect(toList())
                                                            );
                                                })
                                                .map(future -> future.thenApply(list -> list.stream()
                                                        .filter(Optional::isPresent)
                                                        .map(Optional::get)
                                                        .collect(Collectors.toList()))
                                                )
                                                .flatMap((ratpack.func.Function<CompletableFuture<List<CodeSample>>, Promise<List<CodeSample>>>)future ->
                                                        Promise.async(downstream -> downstream.accept(future)))
                                                .map(list -> list.stream()
                                                        .map(this::createOccurence)
                                                        .collect(Collectors.toList()))
                                                .map(Response::new)
                                                .map(Jackson::json);
                                    }));
                        })
                )
        );
    }

    private Response.Occurrence createOccurence(CodeSample codeSample) {
        List<Response.Selection> selections = codeSample.getSelections().stream()
                .map(selection -> {
                    Response.Position start = new Response.Position(selection.getStart().getLine(), selection.getStart().getColumn());
                    Response.Position end = new Response.Position(selection.getEnd().getLine(), selection.getEnd().getColumn());
                    return new Response.Selection(start, end);
                })
                .collect(Collectors.toList());

        return new Response.Occurrence(codeSample.getRawUrl(), codeSample.getUserUrl(), codeSample.getCode(), selections);
    }
}
