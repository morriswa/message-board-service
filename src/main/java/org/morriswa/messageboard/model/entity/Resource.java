package org.morriswa.messageboard.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Getter @AllArgsConstructor
public class Resource {
    private UUID id;
    @Setter private List<UUID> resources;

    public Resource() {
        this.id = UUID.randomUUID();
        this.resources = new ArrayList<>(10);
    }

    public void add(UUID newImageTag) {
        this.resources.add(newImageTag);
    }
}
