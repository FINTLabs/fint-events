package no.fint.events.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Component
public class FintEventsProps {

    public static final String QUEUE_ENDPOINT_ENABLED = "fint.events.queue-endpoint-enabled";

    @Autowired
    private Environment environment;

    @Getter
    @Value("${fint.events.orgIds:}")
    private String[] orgIds;

    @Getter
    @Value("${fint.events.env:local}")
    private String env;

    @Getter
    @Value("${fint.events.component:default}")
    private String component;

    @Getter
    @Value("${fint.events.default-downstream-queue:downstream_{component}_{env}_{orgId}}")
    private String defaultDownstreamQueue;

    @Getter
    @Value("${fint.events.default-upstream-queue:upstream_{component}_{env}_{orgId}}")
    private String defaultUpstreamQueue;

    @Getter
    @Value("${fint.events.test-mode:false}")
    private String testMode;

    @Value("${" + QUEUE_ENDPOINT_ENABLED + ":false}")
    private String queueEndpointEnabled;

    @Getter
    @Value("${fint.events.healthcheck.timeout-in-seconds:120}")
    private int healthCheckTimeout;

    @Value("${fint.events.redisson.config-file:}")
    private String redissonConfigFile;

    @Getter
    private Config redissonConfig;

    @PostConstruct
    public void init() throws IOException {
        log.info("Started with env:{}, component:{}", env, component);

        if (Boolean.valueOf(testMode)) {
            log.info("Test-mode enabled, loading default redisson config");
            redissonConfig = loadDefaultRedissonConfig();
        } else {
            redissonConfig = loadRedissonConfig();
        }

        if (Boolean.valueOf(queueEndpointEnabled)) {
            log.info("Queue endpoint enabled, initializing FintEventsController");
        } else {
            log.info("Queue endpoint disabled, will not load FintEventsController");
        }
    }

    private Config loadRedissonConfig() throws IOException {
        InputStream inputStream;
        if (StringUtils.isEmpty(redissonConfigFile)) {
            String[] profiles = environment.getActiveProfiles();
            inputStream = loadClasspathRedissonFile(profiles);
        } else {
            inputStream = loadConfiguredRedissonFile();
        }

        if (inputStream == null) {
            return loadDefaultRedissonConfig();
        } else {
            return Config.fromYAML(inputStream);
        }
    }

    private InputStream loadClasspathRedissonFile(String[] profiles) {
        for (String profile : profiles) {
            String redissonFileName = String.format("/redisson-%s.yml", profile);
            InputStream inputStream = FintEventsProps.class.getResourceAsStream(redissonFileName);
            if (inputStream != null) {
                log.info("Loading Redisson config from {}", redissonFileName);
                return inputStream;
            }
        }

        InputStream inputStream = FintEventsProps.class.getResourceAsStream("/redisson.yml");
        if (inputStream != null) {
            log.info("Loading Redisson config from /redisson.yml");
        }
        return inputStream;
    }

    private InputStream loadConfiguredRedissonFile() {
        if (!StringUtils.isEmpty(redissonConfigFile)) {
            try {
                File file = new File(redissonConfigFile);
                FileInputStream inputStream = FileUtils.openInputStream(file);
                log.info("Loading Redisson config from configured file {}", redissonConfigFile);
                return inputStream;
            } catch (IOException e) {
                throw new IllegalStateException(String.format("Unable to open configured redisson config file %s, %s", redissonConfigFile, e.getMessage()));
            }
        }
        return null;
    }


    private Config loadDefaultRedissonConfig() {
        log.info("No redisson.yml file found, using default config");
        Config config = new Config();
        config.useSingleServer().setAddress("127.0.0.1:6379");
        return config;
    }
}
