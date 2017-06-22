package no.fint.events.testmode;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.*;
import lombok.extern.slf4j.Slf4j;
import no.fint.events.config.FintEventsProps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class DockerRedis {
    private static final String IMAGE_REDIS = "redis:3.0-alpine";

    private DockerClient docker = null;
    private String containerId;

    @Autowired
    private FintEventsProps props;

    @PostConstruct
    public void startRedisContainer() throws DockerCertificateException, DockerException, InterruptedException {
        if (StringUtils.isEmpty(props.getCiName()) && Boolean.valueOf(props.getTestMode()) && docker == null) {
            log.info("Test mode enabled, starting docker redis");
            docker = DefaultDockerClient.fromEnv().build();
            docker.pull(IMAGE_REDIS);

            Map<String, List<PortBinding>> portBindings = ImmutableMap.of(
                    "6379/tcp", Lists.newArrayList(PortBinding.of("", "6379"))
            );

            HostConfig hostConfig = HostConfig.builder().portBindings(portBindings).build();
            ContainerConfig containerConfig = ContainerConfig.builder()
                    .image(IMAGE_REDIS)
                    .hostConfig(hostConfig)
                    .build();
            ContainerCreation container = docker.createContainer(containerConfig);
            containerId = container.id();
            docker.startContainer(containerId);

            log.info("Redis docker container started");
        }
    }

    @PreDestroy
    public void stopRedisContainer() throws DockerException, InterruptedException {
        log.info("Stopping Redis docker container");
        ContainerInfo containerInfo = docker.inspectContainer(containerId);
        if (containerInfo != null && containerInfo.state().running()) {
            docker.stopContainer(containerId, 5);
            docker.removeContainer(containerId);
            docker.close();
        }
    }
}
