package com.abnamro.assignment.base;

import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.support.hierarchical.ParallelExecutionConfiguration;
import org.junit.platform.engine.support.hierarchical.ParallelExecutionConfigurationStrategy;

public class ParallelExecutionStrategy implements ParallelExecutionConfigurationStrategy {
    @Override
    public ParallelExecutionConfiguration createConfiguration(ConfigurationParameters configurationParameters) {
        return new ParallelExecutionConfiguration() {
            @Override
            public int getParallelism() {
                return TestConfig.getConfiguration().getInt("threads", 1);
            }

            @Override
            public int getMinimumRunnable() {
                return 0;
            }

            @Override
            public int getMaxPoolSize() {
                return TestConfig.getConfiguration().getInt("threads",1);
            }

            @Override
            public int getCorePoolSize() {
                return TestConfig.getConfiguration().getInt("threads",1);
            }

            @Override
            public int getKeepAliveSeconds() {
                return 30;
            }
        };
    }
}
