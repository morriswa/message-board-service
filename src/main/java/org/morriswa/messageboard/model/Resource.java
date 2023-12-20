package org.morriswa.messageboard.model;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Resource {
    private final UUID id;
    private List<UUID> resources;

    public Resource() {
        this.id = UUID.randomUUID();
        this.resources = new ArrayList<>(10);
    }

    public Resource(UUID id, List<UUID> resources) {
        this.id = id; this.resources = resources;
    }

    public void add(UUID newImageTag) { this.resources.add(newImageTag); }

    public UUID getId() { return this.id; }

    public List<UUID> getResources() { return this.resources; }
}
