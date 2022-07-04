package org.jcp.api;

import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
//@AllArgsConstructor
@Component
public class OrdersHandler implements Handler<RoutingContext> {

    @Autowired
    private OrderService orderService;

    @Override
    public void handle(RoutingContext routingContext) {
        final Long orderId = Long.valueOf(routingContext.pathParam("orderId"));
        log.info("New orderId: {}", orderId);
        orderService.processNewOrder(orderId);
        sucessResponseHandler(routingContext, new JsonObject().put("status", "accepted"));
    }

    private void sucessResponseHandler(RoutingContext routingContext, JsonObject response) {
        log.info("Path {} response with {}", routingContext.normalizedPath(), response.encode());
        routingContext.response()
                .putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
                .putHeader("my-header","my-value")
                .setStatusCode(HttpResponseStatus.OK.code())
                .end(response.toBuffer());
    }
}
