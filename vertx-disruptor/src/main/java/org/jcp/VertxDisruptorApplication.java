package org.jcp;

import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import org.jcp.vertx.VertxComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@Slf4j
@SpringBootApplication
public class VertxDisruptorApplication {

    @Autowired
    private VertxComponent vertxComponent;

    @Value("${springBootApp.workOffline:false}")
    private boolean workOffline = false;

    public static void main(String[] args) {
        SpringApplication.run(VertxDisruptorApplication.class, args);
    }

    @PostConstruct
    public void createVertxContext () {
        log.info("workOffline: {}",workOffline);
        if (!workOffline) {
            var vertx = Vertx.vertx();
            vertx.exceptionHandler(error -> {
                log.error("Internal exception, {}, {}", error.getMessage(), error.getCause());
            });

            vertx.deployVerticle(vertxComponent)
                    .onFailure(error -> log.error(">>> Failed to be deployed... {}", error.getMessage()))
                    .onSuccess(event -> log.info("Main Start Deployed {}!", VertxComponent.class.getName()));
        }
    }
}
