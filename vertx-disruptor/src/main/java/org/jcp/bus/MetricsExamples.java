package org.jcp.bus;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.dropwizard.DropwizardMetricsOptions;
import io.vertx.ext.dropwizard.Match;
import io.vertx.ext.dropwizard.MatchType;
import io.vertx.ext.dropwizard.MetricsService;

import java.util.Set;

public class MetricsExamples {
//https://www.programcreek.com/java-api-examples/?code=vert-x3%2Fvertx-dropwizard-metrics%2Fvertx-dropwizard-metrics-master%2Fsrc%2Fmain%2Fjava%2Fexamples%2FMetricsExamples.java
    public void setup() {
        Vertx vertx = Vertx.vertx(new VertxOptions().setMetricsOptions(
                new DropwizardMetricsOptions().setEnabled(true)
        ));
    }

    public void setupJMX() {
        Vertx vertx = Vertx.vertx(new VertxOptions().setMetricsOptions(
                new DropwizardMetricsOptions().setJmxEnabled(true)
        ));
    }

    public void setupJMXWithDomain() {
        Vertx vertx = Vertx.vertx(new VertxOptions().setMetricsOptions(
                new DropwizardMetricsOptions().
                        setJmxEnabled(true).
                        setJmxDomain("mydomain")
        ));
    }

    public void setupMonitoredHandlers() {
        Vertx vertx = Vertx.vertx(new VertxOptions().setMetricsOptions(
                new DropwizardMetricsOptions().
                        setEnabled(true).
                        addMonitoredEventBusHandler(
                                new Match().setValue("some-address")).
                        addMonitoredEventBusHandler(
                                new Match().setValue("business-.*").setType(MatchType.REGEX))
        ));
    }

    public void setupMonitoredUris() {
        Vertx vertx = Vertx.vertx(new VertxOptions().setMetricsOptions(
                new DropwizardMetricsOptions().
                        setEnabled(true).
                        addMonitoredHttpServerUri(
                                new Match().setValue("/")).
                        addMonitoredHttpServerUri(
                                new Match().setValue("/foo/.*").setType(MatchType.REGEX))
        ));
    }

    public void setupMonitoredUrisWithAliases() {
        Vertx vertx = Vertx.vertx(new VertxOptions().setMetricsOptions(
                new DropwizardMetricsOptions().
                        setEnabled(true).
                        addMonitoredHttpServerUri(new Match().setValue("/users/.*").setAlias("users").setType(MatchType.REGEX))
        ));
    }

    public void setupMonitoredEndpoints() {
        Vertx vertx = Vertx.vertx(new VertxOptions().setMetricsOptions(
                new DropwizardMetricsOptions().
                        setEnabled(true).
                        addMonitoredHttpClientEndpoint(
                                new Match().setValue("some-host:80")).
                        addMonitoredHttpClientEndpoint(
                                new Match().setValue("another-host:.*").setType(MatchType.REGEX))
        ));
    }

    public void naming1(Vertx vertx, MetricsService metricsService) {
        JsonObject metrics = metricsService.getMetricsSnapshot(vertx);
        metrics.getJsonObject("vertx.eventbus.handlers");
    }

    public void naming2(Vertx vertx, MetricsService metricsService) {
        EventBus eventBus = vertx.eventBus();
        JsonObject metrics = metricsService.getMetricsSnapshot(eventBus);
        metrics.getJsonObject("handlers");
    }

    public void naming3(Vertx vertx, MetricsService metricsService) {
        Set<String> metricsNames = metricsService.metricsNames();
        for (String metricsName : metricsNames) {
            System.out.println("Known metrics name " + metricsName);
        }
    }

    public void baseName() {
        DropwizardMetricsOptions metricsOptions =
                new DropwizardMetricsOptions().setBaseName("foo");
    }

    public void example1(Vertx vertx) {
        MetricsService metricsService = MetricsService.create(vertx);
        JsonObject metrics = metricsService.getMetricsSnapshot(vertx);
        System.out.println(metrics);
    }

    public void example2(Vertx vertx) {
        MetricsService metricsService = MetricsService.create(vertx);
        HttpServer server = vertx.createHttpServer();
        // set up server
        JsonObject metrics = metricsService.getMetricsSnapshot(server);
    }

    public void example3(Vertx vertx) {
        MetricsService metricsService = MetricsService.create(vertx);
        JsonObject metrics = metricsService.getMetricsSnapshot("vertx.eventbus.message");
    }

    public void getRegistry() {
        VertxOptions options = new VertxOptions().setMetricsOptions(
                new DropwizardMetricsOptions().setEnabled(true).setRegistryName("my-registry")
        );
        Vertx vertx = Vertx.vertx(options);
        // Get the registry
        MetricRegistry registry = SharedMetricRegistries.getOrCreate("my-registry");
        // Do whatever you need with the registry
    }

    public void example4() {
        Vertx vertx = Vertx.vertx(new VertxOptions().setMetricsOptions(
                new DropwizardMetricsOptions()
                        .setEnabled(true)
                        .setJmxEnabled(true)
                        .setJmxDomain("vertx-metrics")));
    }
}
