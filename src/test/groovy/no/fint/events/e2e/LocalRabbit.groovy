package no.fint.events.e2e

import com.spotify.docker.client.DefaultDockerClient
import com.spotify.docker.client.DockerClient
import com.spotify.docker.client.messages.ContainerConfig
import com.spotify.docker.client.messages.HostConfig
import com.spotify.docker.client.messages.PortBinding
import groovy.transform.PackageScope

class LocalRabbit {
    private final String IMAGE_RABBITMQ = 'rabbitmq:3-management'

    private DockerClient docker
    private String containerId

    @PackageScope
    void start() {
        if (docker == null) {
            println 'Starting local rabbitmq'

            docker = DefaultDockerClient.fromEnv().build()
            docker.pull(IMAGE_RABBITMQ)

            def portBindings = [
                    '5672/tcp' : [PortBinding.of('', '5672')],
                    '15672/tcp': [PortBinding.of('', '15672')]
            ]

            def hostConfig = HostConfig.builder().portBindings(portBindings).build()
            def containerConfig = ContainerConfig.builder().image(IMAGE_RABBITMQ).hostConfig(hostConfig).build()
            def container = docker.createContainer(containerConfig)

            containerId = container.id()
            docker.startContainer(containerId)

            Thread.sleep(7000)
            println 'Rabbitmq docker container started'
        }
    }

    @PackageScope
    void stop() {
        def containerInfo = docker.inspectContainer(containerId)
        if (containerInfo != null && containerInfo.state().running()) {
            docker.stopContainer(containerId, 5)
            docker.removeContainer(containerId)
            docker.close()
        }
    }

}
