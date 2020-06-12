package com.knoma.web;

import brave.Tracing;
import brave.propagation.StrictScopeDecorator;
import brave.propagation.ThreadLocalCurrentTraceContext;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.knoma.web.config.WebConfig;
import com.knoma.web.dao.PersonDAO;
import com.knoma.web.resource.PersonResource;
import io.dropwizard.Application;
import io.dropwizard.health.conf.HealthConfiguration;
import io.dropwizard.health.core.HealthCheckBundle;
import io.dropwizard.jersey.jackson.JsonProcessingExceptionMapper;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.glassfish.jersey.internal.inject.AbstractBinder;

import javax.inject.Singleton;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WebApp extends Application<WebConfig> {

    private static final Tracing tracing = Tracing.newBuilder()
            .currentTraceContext(ThreadLocalCurrentTraceContext.newBuilder()
                    .addScopeDecorator(StrictScopeDecorator.create())
                    .build())
            .build();
    private Session session;

    public static void main(String[] args) throws Exception {
        new WebApp().run(args);
    }

    @Override
    public void run(WebConfig config, Environment env) throws IOException {

        this.session = config.getCassandraFactory().build(env.metrics(), env.lifecycle(),
                env.healthChecks(), tracing);


        env.jersey().register(new JsonProcessingExceptionMapper(true));

        env.jersey().register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(session).to(Session.class);
                bind(Executors.newCachedThreadPool()).to(ExecutorService.class);
                bind(PersonDAO.class).to(PersonDAO.class).in(Singleton.class);
            }
        });


        env.jersey().register(PersonResource.class);

        env.healthChecks();

    }

    @Override
    public void initialize(Bootstrap<WebConfig> bootstrap) {
        super.initialize(bootstrap);

        bootstrap.addBundle(new HealthCheckBundle<WebConfig>() {
            @Override
            protected HealthConfiguration getHealthConfiguration(final WebConfig configuration) {
                return configuration.getHealthConfiguration();
            }
        });
    }
}