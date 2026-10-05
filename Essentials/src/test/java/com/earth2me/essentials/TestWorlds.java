package com.earth2me.essentials;

import org.bukkit.NamespacedKey;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.world.WorldMock;

import java.util.Locale;

/**
 * Supplies world keys missing from the current MockBukkit WorldMock implementation.
 */
public final class TestWorlds {
    private TestWorlds() {
    }

    public static ServerMock mockServer() {
        final ServerMock server = MockBukkit.mock();
        addWorld(server, "world");
        return server;
    }

    public static WorldMock addWorld(final ServerMock server, final String name) {
        return addWorld(server, name, NamespacedKey.minecraft(name.toLowerCase(Locale.ENGLISH)));
    }

    public static WorldMock addWorld(final ServerMock server, final String name, final NamespacedKey key) {
        final WorldMock world = new WorldMock() {
            @Override
            public NamespacedKey getKey() {
                return key;
            }
        };
        world.setName(name);
        server.addWorld(world);
        return world;
    }
}
