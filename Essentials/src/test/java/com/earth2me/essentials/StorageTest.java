package com.earth2me.essentials;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockbukkit.mockbukkit.world.WorldMock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class StorageTest {
    private Essentials ess;
    private ServerMock server;
    private World world;

    @BeforeEach
    public void setUp() {
        this.server = TestWorlds.mockServer();
        world = TestWorlds.addWorld(server, "testWorld");
        Essentials.TESTING = true;
        ess = MockBukkit.load(Essentials.class);
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    public void testJailReloadAfterWorldIdentityChanges() throws Exception {
        final Jails jails = new Jails(ess);
        final NamespacedKey key = world.getKey();
        jails.startTransaction();
        jails.setJail("migration", new Location(world, 12.5, 64, -8, 90, 15));
        jails.stopTransaction(true);

        server.removeWorld((WorldMock) world);
        final World replacement = TestWorlds.addWorld(server, "renamed", key);
        jails.reloadConfig();

        assertEquals(new Location(replacement, 12.5, 64, -8, 90, 15), jails.getJail("migration"));
    }

    @Test
    public void testWorldCommandSelectors() {
        assertSame(world, ess.getWorld("minecraft:testworld"));
        assertSame(world, ess.getWorld("testWorld"));
        assertSame(world, ess.getWorld("1"));
    }

    @Test
    public void testOldUserdata() {
        final ExecuteTimer ext = new ExecuteTimer();
        ext.start();
        final PlayerMock base1 = server.addPlayer("testPlayer1");
        ext.mark("fake user created");
        final UserData user = ess.getUser(base1);
        ext.mark("load empty user");
        for (int j = 0; j < 1; j++) {
            user.setHome("home", new Location(world, j, j, j));
        }
        ext.mark("change home 1 times");
        user.save();
        ext.mark("write user");
        user.save();
        ext.mark("write user (cached)");
        user.reloadConfig();
        ext.mark("reloaded file");
        user.reloadConfig();
        ext.mark("reloaded file (cached)");
        System.out.println(ext.end());
    }
}
