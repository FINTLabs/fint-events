package no.fint.events.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
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
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class RedissonConfig {
    @Autowired
    private Environment environment;

    @Getter
    private String redissonJsonConfig;

    @Value("${fint.events.redisson.addresses:redis://127.0.0.1:6379}")
    private String addresses[];

    @Value("${fint.events.redisson.mode:SINGLE}")
    private String mode;

    @Value("${fint.events.redisson.retry-attempts:100}")
    private int retryAttempts;

    @Value("${fint.events.redisson.retry-interval:5000}")
    private int retryInterval;

    @Value("${fint.events.redisson.reconnection-timeout:10000}")
    private int reconnectionTimeout;

    @Value("${fint.events.redisson.timeout:10000}")
    private int timeout;

    @Value("${fint.events.redisson.dns-monitoring:false}")
    private String dnsMonitoring;

    @Value("${fint.events.redisson.use-linux-native-epoll:false}")
    private String useLinuxNativeEpoll;

    @PostConstruct
    public void init() throws JsonProcessingException {
        RedissonMode redissonMode = RedissonMode.valueOf(this.mode);
        Map<String, Object> config = new HashMap<>();
        if (addresses.length == 1) {
            config.put(redissonMode.getAddressField(), addresses[0]);
        } else {
            config.put(redissonMode.getAddressField(), addresses);
        }

        config.put("retryAttempts", retryAttempts);
        config.put("retryInterval", retryInterval);
        config.put("reconnectionTimeout", reconnectionTimeout);
        config.put("timeout", timeout);
        if (redissonMode == RedissonMode.SINGLE) {
            config.put("dnsMonitoring", Boolean.valueOf(dnsMonitoring));
        }

        ObjectMapper objectMapper = new ObjectMapper();
        redissonJsonConfig = objectMapper.writeValueAsString(ImmutableMap.of(redissonMode.getModeRoot(), config, "useLinuxNativeEpoll", Boolean.valueOf(useLinuxNativeEpoll)));
    }

    public Config getConfig() {
        String[] profiles = environment.getActiveProfiles();
        InputStream inputStream = loadClasspathRedissonFile(profiles);
        try {
            if (inputStream == null) {
                log.info("No redisson.yml file found, using default config");
                return loadSystemPropertiesConfig();
            } else {
                return Config.fromYAML(inputStream);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to load redisson json config, " + e.getMessage());
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

    public Config getDefaultConfig() {
        try {
            log.info("Loading default redisson config");
            return loadSystemPropertiesConfig();
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to load default redisson config, " + e.getMessage());
        }
    }

    private Config loadSystemPropertiesConfig() throws IOException {
        return Config.fromJSON(redissonJsonConfig);
    }

}
