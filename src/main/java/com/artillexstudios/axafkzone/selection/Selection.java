package com.artillexstudios.axafkzone.selection;

import com.artillexstudios.axapi.collections.ThreadSafeList;
import com.artillexstudios.axapi.selection.Cuboid;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Selection {
    private Location position1;
    private Location position2;
    private final ThreadSafeList<Location> borders = new ThreadSafeList<>();

    public Location getPosition1() {
        return position1;
    }

    public void setPosition1(@NotNull Location position1) {
        this.position1 = position1;
        updateVisualizer();
    }

    public Location getPosition2() {
        return position2;
    }

    public void setPosition2(@NotNull Location position2) {
        this.position2 = position2;
        updateVisualizer();
    }

    public void show(@NotNull Player player) {
        if (borders.isEmpty()) return;
        borders.forEach(location -> {
            Location l2 = location.clone().add(0.5, 0.5, 0.5);
            player.spawnParticle(Particle.WAX_ON, l2, 1, 0, 0, 0, 0);
        });
    }

    private void updateVisualizer() {
        if (position1 == null || position2 == null || !Objects.equals(position1.getWorld(), position2.getWorld())) return;

        Cuboid cube = new Cuboid(position1.getWorld(), position1.getBlockX(), position2.getBlockX(), position1.getBlockZ(), position2.getBlockZ(), position1.getBlockY(), position2.getBlockY());

        borders.clear();
        for (double x = cube.getMinX(); x <= cube.getMaxX(); x += 0.25) {
            for (double y = cube.getMinY(); y <= cube.getMaxY(); y += 0.25) {
                for (double z = cube.getMinZ(); z <= cube.getMaxZ(); z += 0.25) {
                    int components = 0;
                    if (x == cube.getMinX() || x == cube.getMaxX()) components++;
                    if (y == cube.getMinY() || y == cube.getMaxY()) components++;
                    if (z == cube.getMinZ() || z == cube.getMaxZ()) components++;
                    if (components >= 2) {
                        borders.add(new Location(cube.getWorld(), x, y, z));
                    }
                }
            }
        }
    }
}
