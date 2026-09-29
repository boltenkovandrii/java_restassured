package com.abnamro.assignment.base;


import org.apache.commons.configuration2.*;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TestConfig {
    private final CompositeConfiguration config;
    private static final Logger LOGGER = LoggerFactory.getLogger(TestConfig.class);

    private static class Holder {
        static final TestConfig INSTANCE = new TestConfig();
    }

    public static CompositeConfiguration getConfiguration() {
        return Holder.INSTANCE.config;
    }

    private TestConfig() {
        LOGGER.info("Initializing TestConfig and loading configuration parameters");
        config = new CompositeConfiguration();

        // System properties and environment variables have highest priority.
        config.addConfiguration(new SystemConfiguration());
        config.addConfiguration(new EnvironmentConfiguration());

        // General test configuration.
        try {
            Configurations configs = new Configurations();
            config.addConfiguration(
                    configs.properties("src/test/resources/config.properties"));
        } catch (ConfigurationException e) {
            throw new IllegalStateException(
                    "Failed to load test configuration", e);
        }

        // Load local GitLab configuration if available.
        loadGitlabConfiguration();
    }


    private void loadGitlabConfiguration() {
        try {
            Configurations configs = new Configurations();
            config.addConfiguration(
                    configs.properties("src/test/resources/gitlab.properties"));
        } catch (ConfigurationException e) {
            LOGGER.warn(
                    "Failed to load gitlab.properties. This is expected in CI, "
                            + "where GitLab configuration is provided through "
                            + "environment variables.",
                    e);
        }
    }

}