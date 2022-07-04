package org.jcp.api;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Slf4j
//@AllArgsConstructor
@Configuration
public class RestApiVerticle extends AbstractVerticle {
    public static final int REST_API_PORT = 8070;

    @Autowired
    private OrderRouter orderRouter;

    @Override
    public void start(final Promise<Void> startPromise)  {
        startServer(startPromise);//.exceptionHandler(event -> event.getCause());
        startPromise.complete();
    }

    private HttpServer startServer(final Promise<Void> startPromise) {
        log.info("Preparing to start http server...");
        return startHttpServer(startPromise, attachtRoutes());
    }

    private Router attachtRoutes() {
        final Router restApi = Router.router(vertx);
        restApi.route()
                .handler(BodyHandler.create()
                        //.setBodyLimit(1024)
                        //.setHandleFileUploads(true)
                )
                .failureHandler(RestApiVerticle::routeErrorHandler);

        orderRouter.attach(restApi);
        return restApi;
    }

    private static void routeErrorHandler(final RoutingContext routingContext) {
        if (routingContext.response().ended()) {
            //the user stop the request
            return;
        }
        log.error("Router Error:  {}", routingContext.response().getStatusMessage());
        routingContext.response()
                .setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
                .end(new JsonObject().put("message", "something went wrong...").toBuffer());
    }

    private HttpServer startHttpServer(final Promise<Void> startPromise, final Router restApi) {
        return vertx.createHttpServer()
                .requestHandler(restApi)
                .exceptionHandler(error -> {
                    log.error("HTTP error: ", error);
                })
                .listen(REST_API_PORT, http -> {
                    if (http.succeeded()) {
                        log.info("HTTP server started on port {}", REST_API_PORT);
                    } else {
                        log.error("Error trying to initiate the web server, {} ", http.cause().getMessage());
                        startPromise.fail(http.cause());
                    }
                });
    }
}
