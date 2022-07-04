package org.jcp.api;


import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

import io.vertx.junit5.VertxTestContext;
import lombok.extern.slf4j.Slf4j;
import org.jcp.api.RestApiVerticle;
import org.jcp.vertx.VertxComponent;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest(properties = "springBootApp.workOffline=true")
public abstract class AbstractRestApiTest {

    @Autowired
    VertxComponent vertxComponent;

    @BeforeEach
    void deploy_verticle(final Vertx vertx, final VertxTestContext testContext) {
        vertx.deployVerticle(vertxComponent, testContext.succeeding(id -> testContext.completeNow()));
        vertx.deployVerticle(new RestApiVerticleTest.WebClientListener());
        vertx.deployVerticle(new RestApiVerticleTest.WebClientListener());
        vertx.deployVerticle(new RestApiVerticleTest.WebClientListener());
    }

}
