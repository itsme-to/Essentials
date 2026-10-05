package com.earth2me.essentials;

import com.earth2me.essentials.config.entities.LazyLocation;
import com.earth2me.essentials.config.serializers.LocationTypeSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.World;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public class LazyLocationTest {
    private MockedStatic<Bukkit> bukkit;
    private Server server;
    private World world;

    @BeforeEach
    public void setUp() {
        server = mock(Server.class);
        world = mock(World.class);
        when(world.getUID()).thenReturn(UUID.randomUUID());
        when(world.getName()).thenReturn("renamed_nether");
        when(world.getKey()).thenReturn(NamespacedKey.minecraft("the_nether"));
        when(server.getWorlds()).thenReturn(Collections.singletonList(world));
        bukkit = mockStatic(Bukkit.class);
        bukkit.when(Bukkit::getServer).thenReturn(server);
    }

    @AfterEach
    public void tearDown() {
        bukkit.close();
    }

    @Test
    public void testSavedKeyRecoversChangedUuidAndName() throws SerializationException {
        final ConfigurationNode node = BasicConfigurationNode.root();
        node.node("world").set(UUID.randomUUID().toString());
        node.node("world-name").set("world_nether");
        node.node("world-key").set("minecraft:the_nether");
        node.node("x").set(12.5);
        node.node("y").set(-32.0);
        node.node("z").set(-7.25);
        node.node("yaw").set(90.0f);
        node.node("pitch").set(-15.0f);
        final LocationTypeSerializer serializer = new LocationTypeSerializer();
        final LazyLocation saved = serializer.deserialize(LazyLocation.class, node);

        assertEquals(new Location(world, 12.5, -32, -7.25, 90, -15), saved.location());
        serializer.serialize(LazyLocation.class, saved, node);
        assertEquals(world.getUID().toString(), node.node("world").getString());
        assertEquals("renamed_nether", node.node("world-name").getString());
        assertEquals("minecraft:the_nether", node.node("world-key").getString());
    }

    @Test
    public void testUuidTakesPrecedenceOverKey() {
        final World original = mock(World.class);
        final UUID id = UUID.randomUUID();
        when(original.getUID()).thenReturn(id);
        when(original.getName()).thenReturn("original");
        bukkit.when(() -> Bukkit.getWorld(id)).thenReturn(original);
        final LazyLocation saved = new LazyLocation(id.toString(), "old", "minecraft:the_nether", 0, 64, 0, 0, 0);

        assertSame(original, saved.location().getWorld());
    }

    @Test
    public void testLegacyNameAndUuidFallback() {
        when(server.getWorld("world_nether")).thenReturn(world);
        assertSame(world, new LazyLocation("world_nether", "", 0, 64, 0, 0, 0).location().getWorld());
        assertSame(world, new LazyLocation(UUID.randomUUID().toString(), "world_nether", 0, 64, 0, 0, 0).location().getWorld());
    }

    @Test
    public void testNamespacedKeyInExistingWorldField() throws SerializationException {
        final ConfigurationNode node = BasicConfigurationNode.root();
        node.node("world").set("minecraft:the_nether");
        final LazyLocation saved = new LocationTypeSerializer().deserialize(LazyLocation.class, node);

        assertSame(world, saved.location().getWorld());
    }

    @Test
    public void testUnloadedWorldCanResolveLater() {
        final String id = UUID.randomUUID().toString();
        final LazyLocation saved = new LazyLocation(id, "old", "minecraft:the_nether", 0, 64, 0, 0, 0);
        when(server.getWorlds()).thenReturn(Collections.emptyList());
        assertNull(saved.location());
        assertEquals(id, saved.world());
        assertEquals("minecraft:the_nether", saved.worldKey());

        when(server.getWorlds()).thenReturn(Collections.singletonList(world));
        assertSame(world, saved.location().getWorld());
    }

    @Test
    public void testNewLocationSavesKeyAlongsideLegacyFields() throws SerializationException {
        final ConfigurationNode node = BasicConfigurationNode.root();
        final LocationTypeSerializer serializer = new LocationTypeSerializer();
        serializer.serialize(LazyLocation.class, LazyLocation.fromLocation(new Location(world, 1, 2, 3)), node);

        assertEquals(world.getUID().toString(), node.node("world").getString());
        assertEquals(world.getName(), node.node("world-name").getString());
        assertEquals("minecraft:the_nether", node.node("world-key").getString());
        assertEquals(new Location(world, 1, 2, 3), serializer.deserialize(LazyLocation.class, node).location());
    }

    @Test
    public void testMissingWorldDoesNotSelectAnotherDimension() {
        assertNull(new LazyLocation("minecraft:the_end", "missing", 0, 64, 0, 0, 0).location());
    }
}
