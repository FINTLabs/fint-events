package no.fint.events.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Getter
@Component
public class FintEventsProps {

    @Autowired
    private Environment environment;

    @Value("${fint.events.default-downstream-queue:%s.downstream}")
    private String defaultDownstreamQueue;

    @Value("${fint.events.default-upstream-queue:%s.upstream}")
    private String defaultUpstreamQueue;

    @Value("${fint.events.test-mode:false}")
    private String testMode;

    @Value("${fint.events.queue-endpoint-enabled:false}")
    private String queueEndpointEnabled;

    private Config redissonConfig;

    @PostConstruct
    public void init() throws IOException {
        String[] profiles = environment.getActiveProfiles();
        InputStream inputStream = getConfigInputStream(profiles);
        if (inputStream == null) {
            redissonConfig = loadDefaultConfig();
        } else {
            redissonConfig = Config.fromYAML(inputStream);
        }
    }

    private InputStream getConfigInputStream(String[] profiles) {
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

    private Config loadDefaultConfig() {
        log.info("No redisson.yml file found, using default config");
        Config config = new Config();
        config.useSingleServer().setAddress("127.0.0.1:6379");
        return config;
    }

}
