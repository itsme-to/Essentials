package com.earth2me.essentials.utils;

import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.World;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WorldUtilTest {
    @Test
    public void testResolvesCustomDimensionKey() {
        final Server server = mock(Server.class);
        final World world = mock(World.class);
        when(world.getKey()).thenReturn(new NamespacedKey("custom", "dungeons/ice"));
        when(server.getWorlds()).thenReturn(Collections.singletonList(world));

        assertSame(world, WorldUtil.getWorld(server, "custom:dungeons/ice"));
        assertNull(WorldUtil.getWorld(server, "minecraft:the_nether"));
        assertNull(WorldUtil.getWorld(server, "ice"));
        assertNull(WorldUtil.getWorld(server, ""));
        assertNull(WorldUtil.getWorld(server, null));
    }

    @Test
    public void testExactNameTakesPrecedenceOverKey() {
        final Server server = mock(Server.class);
        final World named = mock(World.class);
        final World keyed = mock(World.class);
        when(server.getWorld("custom:world")).thenReturn(named);
        when(keyed.getKey()).thenReturn(new NamespacedKey("custom", "world"));
        when(server.getWorlds()).thenReturn(Collections.singletonList(keyed));

        assertSame(named, WorldUtil.getWorld(server, "custom:world"));
        assertSame(keyed, WorldUtil.getWorldByKey(server, "custom:world"));
    }

    @Test
    public void testLegacyWorldWithoutKeyApi() {
        final Server server = mock(Server.class);
        final World world = mock(World.class);
        when(world.getKey()).thenThrow(new NoSuchMethodError("getKey"));
        when(server.getWorld("world_nether")).thenReturn(world);
        when(server.getWorlds()).thenReturn(Collections.singletonList(world));

        assertSame(world, WorldUtil.getWorld(server, "world_nether"));
        assertNull(WorldUtil.getKey(world));
        assertNull(WorldUtil.getWorld(server, "minecraft:the_nether"));
    }
}
