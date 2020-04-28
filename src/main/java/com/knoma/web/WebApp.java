package com.knoma.web;

import com.knoma.web.config.WebConfig;
import com.knoma.web.resource.PersonResource;
import io.dropwizard.Application;
import io.dropwizard.health.conf.HealthConfiguration;
import io.dropwizard.health.core.HealthCheckBundle;
import io.dropwizard.jdbi3.JdbiFactory;
import io.dropwizard.jersey.jackson.JsonProcessingExceptionMapper;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.jdbi.v3.core.Jdbi;

import java.io.IOException;

public class WebApp extends Application<WebConfig> {

    public static void main(String[] args) throws Exception {
        new WebApp().run(args);
    }

    @Override
    public void run(WebConfig config, Environment env) throws IOException {

        final JdbiFactory factory = new JdbiFactory();
        final Jdbi jdbi = factory.build(env, config.getDataSourceFactory(), "postgresql");


        env.jersey().register(new PersonResource(jdbi));
        env.healthChecks();

        env.jersey().register(new JsonProcessingExceptionMapper(true));

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