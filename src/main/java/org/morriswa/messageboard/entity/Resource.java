package org.morriswa.messageboard.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Entity @Table(name = "post_resource")
@NoArgsConstructor @Builder @Getter
@AllArgsConstructor
public class Resource {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID resourceId;
    private UUID resourceId1;
    private UUID resourceId2;
    private UUID resourceId3;
    private UUID resourceId4;
    private UUID resourceId5;
    private UUID resourceId6;
    private UUID resourceId7;
    private UUID resourceId8;
    private UUID resourceId9;

    public List<UUID> getList() {
        var response = new ArrayList<UUID>();
        for (UUID resource : Arrays.asList(
                resourceId,
                resourceId1,
                resourceId2,
                resourceId3,
                resourceId4,
                resourceId5,
                resourceId6,
                resourceId7,
                resourceId8,
                resourceId9)) {
            if (resource != null) response.add(resource);
        }
        return response;
    }

    public void setList(List<UUID> resources) throws NoSuchFieldException, IllegalAccessException {
        if (resources.size()>10) throw new RuntimeException("should never have > 10 vals");

        Class<Resource> resourceClass = Resource.class;

        for (int i = 0;i < 10; i++) {
            var field = i==0?resourceClass.getDeclaredField("resourceId")
                    :resourceClass.getDeclaredField("resourceId"+i);
            field.setAccessible(true);
            field.set(this, null);
        }

        for (int i = 0;i < resources.size(); i++) {

            var field = i==0?resourceClass.getDeclaredField("resourceId")
                    :resourceClass.getDeclaredField("resourceId"+i);
            field.setAccessible(true);
            field.set(this, resources.get(i));
        }
    }
}
