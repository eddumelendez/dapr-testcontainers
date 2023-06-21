package com.salaboy.dapr.javaapp;

import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.utility.MountableFile;

import java.util.List;

public interface CommonContainers {

    Network daprNetwork = getNetwork();

    static Network getNetwork() {
        Network defaultDaprNetwork = new Network() {
            @Override
            public String getId() {
                return "dapr";
            }

            @Override
            public void close() {

            }

            @Override
            public Statement apply(Statement base, Description description) {
                return null;
            }
        };

        List<com.github.dockerjava.api.model.Network> networks = DockerClientFactory.instance().client().listNetworksCmd().withNameFilter("dapr").exec();
        if (networks.isEmpty()) {
            Network.builder()
                    .createNetworkCmdModifier(cmd -> cmd.withName("dapr"))
                    .build().getId();
            return defaultDaprNetwork;
        } else {
            return defaultDaprNetwork;
        }
    }

    GenericContainer<?> redis = new GenericContainer<>("redis:alpine")
            .withExposedPorts(6379) // for wait
            .withNetwork(daprNetwork)
            .withNetworkAliases("redis")
            .withReuse(true);

    DaprContainer daprSidecar = new DaprContainer("daprio/daprd:edge")
            .withAppId("write-app")
            .withPlacementHostAddress("placement:50006")
            .withCopyFileToContainer(
                    MountableFile.forClasspathResource("components"),
                    "/components/")
            .withNetwork(daprNetwork)
            .withReuse(true);

    DaprContainer daprPlacement = new DaprContainer("daprio/dapr")
            .enablePlacement()
            .withNetwork(daprNetwork)
            .withNetworkAliases("placement")
            .withReuse(true);

    @DynamicPropertySource
    static void daprProperties(DynamicPropertyRegistry registry) {
        System.setProperty("dapr.grpc.port", daprSidecar.getMappedPort(50001).toString());
    }

}
