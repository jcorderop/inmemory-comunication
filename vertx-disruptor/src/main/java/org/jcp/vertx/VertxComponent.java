package org.jcp.vertx;

import lombok.AllArgsConstructor;
import org.jcp.api.OrderRouter;
import org.jcp.api.RestApiVerticle;
import io.vertx.core.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Slf4j
//@AllArgsConstructor
@Component
public class VertxComponent extends AbstractVerticle {

    @Autowired
    private RestApiVerticle restApiVerticle;
    //@Autowired
    //private RestApiVerticle restApiVerticle2;

    @Override
    public void start(Promise<Void> startPromise) {
        vertx.deployVerticle(restApiVerticle)
                //.onComplete(event -> vertx.deployVerticle(restApiVerticle2))
                .onComplete(event -> startPromise.complete()).onFailure(startPromise::fail)
                .onSuccess(id -> {
                    log.info("DeployRestApiVerticle {} with id {}", RestApiVerticle.class.getName(), id);
                    startPromise.complete();
                });
    }

    private int processors() {
        final int numOfProcessors = Math.max(1, Runtime.getRuntime().availableProcessors()/2);
        //numOfProcessors = 1;
        log.info("Number of processors available: {}", numOfProcessors);
        log.info("Number parallel process to start: {}", numOfProcessors);
        return numOfProcessors;
    }
}
