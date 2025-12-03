package com.artillexstudios.axafkzone.selection;

import com.artillexstudios.axafkzone.zones.Zone;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class Region {
    private final Location corner1;
    private final Location corner2;
    private final Location center;
    private World world;
    private final Zone zone;

    public Region(Location corner1, Location corner2, @NotNull Zone zone) {
        this.corner1 = corner1;
        this.corner2 = corner2;
        this.world = corner1.getWorld();
        this.zone = zone;

        this.center = new Location(corner1.getWorld(), (corner1.getBlockX() + corner2.getBlockX()) / 2D, (corner1.getBlockY() + corner2.getBlockY()) / 2D, (corner1.getBlockZ() + corner2.getBlockZ()) / 2D);
    }

    public Set<Player> getPlayersInZone() {
        if (world == null) return Set.of();
        final HashSet<Player> players = new HashSet<>();

        String permission = zone.getSettings().getString("permission");
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.isDead()) continue;
            if (!player.getWorld().equals(world)) continue;
            if (!permission.isBlank() && !player.hasPermission(permission)) continue;

            final Location loc = player.getLocation();

            int x1 = Math.min(corner1.getBlockX(), corner2.getBlockX());
            int y1 = Math.min(corner1.getBlockY(), corner2.getBlockY());
            int z1 = Math.min(corner1.getBlockZ(), corner2.getBlockZ());
            int x2 = Math.max(corner1.getBlockX(), corner2.getBlockX());
            int y2 = Math.max(corner1.getBlockY(), corner2.getBlockY());
            int z2 = Math.max(corner1.getBlockZ(), corner2.getBlockZ());

            if (!(loc.getBlockX() >= x1 && loc.getBlockX() <= x2 && loc.getBlockY() >= y1 && loc.getBlockY() <= y2 && loc.getBlockZ() >= z1 && loc.getBlockZ() <= z2)) continue;

            players.add(player);
        }

        return players;
    }

    @NotNull
    public Location getCorner1() {
        return corner1;
    }

    @NotNull
    public Location getCorner2() {
        return corner2;
    }

    public long getCenterX() {
        return center.getBlockX();
    }

    public long getCenterY() {
        return center.getBlockY();
    }

    public long getCenterZ() {
        return center.getBlockZ();
    }

    @NotNull
    public Location getCenter() {
        return center;
    }

    @Nullable
    public World getWorld() {
        return world;
    }

    public void setWorld(World world) {
        this.world = world;
    }
}
