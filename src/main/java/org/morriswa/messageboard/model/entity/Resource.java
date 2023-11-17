package org.morriswa.messageboard.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Getter @AllArgsConstructor
public class Resource {
    private UUID id;
    private List<UUID> resources;

    public List<UUID> getList() {
        return resources;
    }

    public void setList(List<UUID> resources) {
        this.resources = resources;
    }

    public Resource() {
        this.id = UUID.randomUUID();
        this.resources = new ArrayList<>(10);
    }

    public void add(UUID uuid) {
        this.resources.add(uuid);
    }
}
