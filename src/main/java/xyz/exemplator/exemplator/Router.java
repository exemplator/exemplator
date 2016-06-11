package xyz.exemplator.exemplator;

import ratpack.server.RatpackServer;
import ratpack.server.ServerConfig;

/**
 * @author LeanderK
 * @version 1.0
 */
public class Router {
    private final int port;

    public Router(int port) {
        this.port = port;
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
                        })
                        .get("index", ctx -> ctx.render("Hello"))
                )
        );
    }
}
