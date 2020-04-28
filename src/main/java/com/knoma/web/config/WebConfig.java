package com.knoma.web.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.health.conf.HealthConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;


public class WebConfig extends Configuration {

    @NotEmpty
    private String version;

    @Valid
    @NotNull
    private DataSourceFactory database = new DataSourceFactory();
    @Valid
    @NotNull
    @JsonProperty("health")
    private HealthConfiguration healthConfiguration = new HealthConfiguration();

    @JsonProperty("database")
    public DataSourceFactory getDataSourceFactory() {
        return database;
    }

    @JsonProperty("database")
    public void setDataSourceFactory(DataSourceFactory factory) {
        this.database = factory;
    }

    public HealthConfiguration getHealthConfiguration() {
        return healthConfiguration;
    }

    public void setHealthConfiguration(final HealthConfiguration healthConfiguration) {
        this.healthConfiguration = healthConfiguration;
    }

    @JsonProperty
    public String getVersion() {
        return version;
    }

    @JsonProperty
    public void setVersion(String version) {
        this.version = version;
    }

}
