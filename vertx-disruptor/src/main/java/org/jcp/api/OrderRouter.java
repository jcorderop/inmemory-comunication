package org.jcp.api;

import io.vertx.ext.web.Router;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

//@AllArgsConstructor
@Configuration
public class OrderRouter {

    @Autowired
    private OrdersHandler ordersHandler;

    public void attach(final Router restApi) {
        restApi.get("/order/:orderId").handler(ordersHandler);
    }

}
