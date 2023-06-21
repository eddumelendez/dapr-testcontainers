package com.salaboy.dapr.javaapp;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

public class DaprContainer extends GenericContainer<DaprContainer> {

    private static final DockerImageName DAPRD_IMAGE_NAME = DockerImageName.parse("daprio/daprd");

    private static final DockerImageName DAPR_IMAGE_NAME = DockerImageName.parse("daprio/dapr");

    private static final int DAPR_SIDECARD_PORT = 50001;

    private static final int DAPR_PLACEMENT_PORT = 50006;

    private static final String placementCommand = "./placement -port " + DAPR_PLACEMENT_PORT;

    private static final String sidecarCommand = "./daprd -app-id %s -placement-host-address %s --dapr-listen-addresses=0.0.0.0 -components-path %s";

    private boolean enablePlacement;

    private String appId;

    private String componentsPath = "/components";

    private String placementHostAddress;

    public DaprContainer(String image) {
        this(DockerImageName.parse(image));
    }

    public DaprContainer(DockerImageName image) {
        super(image);
        image.assertCompatibleWith(DAPRD_IMAGE_NAME, DAPR_IMAGE_NAME);
    }

    @Override
    protected void configure() {
        if (this.enablePlacement) {
            withExposedPorts(DAPR_PLACEMENT_PORT);
            withCommand(placementCommand);
        } else {
            withExposedPorts(DAPR_SIDECARD_PORT);
            withCommand(String.format(sidecarCommand, this.appId, this.placementHostAddress, this.componentsPath));
        }
    }

    public DaprContainer enablePlacement() {
        this.enablePlacement = true;
        return this;
    }

    public DaprContainer withAppId(String appId) {
        this.appId = appId;
        return this;
    }

    public DaprContainer withComponentsPath(String componentsPath) {
        this.componentsPath = componentsPath;
        return this;
    }

    public DaprContainer withPlacementHostAddress(String placementHostAddress) {
        this.placementHostAddress = placementHostAddress;
        return this;
    }

}
