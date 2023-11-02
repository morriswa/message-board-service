package org.morriswa.messageboard.entity;

import org.junit.jupiter.api.Test;
import org.morriswa.messageboard.MessageboardTest;
import org.morriswa.messageboard.model.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ResourceMethodsTest extends MessageboardTest {
    @Test
    void testResourceMethods() throws NoSuchFieldException, IllegalAccessException {
        List<UUID> list = new ArrayList<UUID>(){{
            for (int i = 0; i < 10; i++) add(UUID.randomUUID());
        }};

        var resource = new Resource();
        resource.setList(list);

        int i = 0;
        for (UUID uuid: resource.getList()) {

            assertEquals(uuid, list.get(i));
            i++;
        }


    }

    @Test
    void testResourceMethodOneUUID() throws NoSuchFieldException, IllegalAccessException {
        List<UUID> list = new ArrayList<UUID>(){{
            add(UUID.randomUUID());
        }};

        var resource = new Resource();
        resource.setList(list);

        int i = 0;
        for (UUID uuid: resource.getList()) {
            assertEquals(uuid, list.get(i));
            i++;
        }
    }

    @Test
    void testResourceMethodFiveUUID() throws NoSuchFieldException, IllegalAccessException {
        List<UUID> list = new ArrayList<UUID>(){{
            for (int i = 0; i < 5; i++) add(UUID.randomUUID());
        }};

        var resource = new Resource();
        resource.setList(list);

        int i = 0;
        for (UUID uuid: resource.getList()) {
            assertEquals(uuid, list.get(i));
            i++;
        }
    }
}
