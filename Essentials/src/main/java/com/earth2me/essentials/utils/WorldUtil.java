package com.earth2me.essentials.utils;

import org.bukkit.Server;
import org.bukkit.World;

public final class WorldUtil {
    private WorldUtil() {
    }

    /**
     * Resolves a loaded world by its Bukkit name or explicit namespaced key.
     */
    public static World getWorld(final Server server, final String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        final World world = server.getWorld(name);
        return world == null ? getWorldByKey(server, name) : world;
    }

    public static World getWorldByKey(final Server server, final String key) {
        if (key == null || key.indexOf(':') < 0) {
            return null;
        }
        for (final World world : server.getWorlds()) {
            if (key.equals(getKey(world))) {
                return world;
            }
        }
        return null;
    }

    /**
     * Returns null on legacy Bukkit versions without world keys.
     */
    public static String getKey(final World world) {
        try {
            return world.getKey() == null ? null : world.getKey().toString();
        } catch (final NoSuchMethodError | NoClassDefFoundError ignored) {
            return null;
        }
    }
}
